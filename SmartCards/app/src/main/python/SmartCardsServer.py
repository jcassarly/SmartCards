
import bluetooth
import threading
import sys
import json


shota_phone = "4C:DD:31:C9:92:05"
HOST_MAC = "B4:69:21:BE:FA:A9"
UUID = "b5c65192-1d67-471f-8147-0d0e8904efaa"
FILE_PATH = "decklist.json"
IMAGE_DIR = "./images/"
ENCODING = "utf-8"

# states
SEND_HEARTBEAT = 0
COMPARE_REVISIONS = 1

# commands
CMD_HEARTBEAT = 0
CMD_LOCKED = 1
CMD_UNLOCKED = 2

# constants
DEFAULT_BUF_SIZE = 1024
IMG_BUF_SIZE = 100000

class Placeholder():
    def __init__(self):
        self.locked = True
        self.rev_num = 0
        self.deck = [num for num in range(5)]

    def getLockState(self):
        return self.locked

    def getRevision(self):
        return self.rev_num

    def getDecklist(self):
        return self.deck

    def updateSharedMemory(self, data):
        new_deck = []
        for index in range(0, len(data), 4):
            new_deck.append(int.from_bytes(data[index:index+4], byteorder="big"))
        print("\n\nnew deck: {}\n".format(new_deck))

global_placeholder = Placeholder()

def printDebugInfo(action, state, command, payload):
    print("{0} in state: {1}\n    Command: {2}\n    Payload: {3}".format(action, state, command, payload))

def readFile():
    revision_number = -1
    image_names = []
    with open(FILE_PATH) as phil:
        """ old implementation for a text file"""
        # for line in phil:
        #     if "revnumber" in line:
        #         num_list = [int(word) for word in line.split() if word.isdigit()][0] # maybe need to add error checking for if there's more than 1 num?
        #     elif "]" not in line and '[' not in line:
        #         image_names.append(line)

        """ new implementation for json """
        json_dict = json.load(phil)
        revision_number = json_dict['rev_number']
        image_names = json_dict['deckList'] + json_dict['inPlayList'] + json_dict['discardList']

    return revision_number, image_names

def checkDeckAck(client_sock):
    ack = client_sock.recv(DEFAULT_BUF_SIZE)
    return True #TODO add acutal error checking

def sendDeck(client_sock):
    # deck_manager.toFile()
    revision_number, image_names = readFile()
    command = CMD_UNLOCKED.to_bytes(4, byteorder="big")
    byte_rev_num = revision_number.to_bytes(4, byteorder="big")
    init_packet = command + byte_rev_num
    print("Sending deck start: {}".format(init_packet)) # TODO: REMOVE AFTER DEBUGGING
    client_sock.send(init_packet)

    # wait for response
    checkDeckAck(client_sock)

    payload = b''
    # send contents of deck file
    with open(FILE_PATH, 'rb') as phil:
        payload = init_packet + phil.read()
        print("Sending File {}".format(payload[0:8])) # TODO: REMOVE AFTER DEBUGGING
        client_sock.send(payload)

    # wait for acknowledgement
    checkDeckAck(client_sock)
    
    # send images
    for image_name in image_names:
        with open(IMAGE_DIR + image_name, 'rb') as image:
            payload = init_packet + image.read()
            print("Sending Image {}".format(image_name)) # TODO: REMOVE AFTER DEBUGGING
            print("    Payload: {}".format(payload[0:8]))
            client_sock.send(payload)
            checkDeckAck(client_sock)

    # send indication we're done
    print("Sending finish packet: {}".format(init_packet))
    client_sock.send(init_packet)
    return COMPARE_REVISIONS

def receiveDeck(client_sock):
    # receive the data for the file
    print("Waiting to receive deck")
    data = client_sock.recv(IMG_BUF_SIZE)
    command = CMD_UNLOCKED.to_bytes(4, byteorder="big")
    byte_rev_num = global_placeholder.getRevision().to_bytes(4, byteorder="big")
    response = command + byte_rev_num

    # write the contents of the deck as a file
    with open(FILE_PATH, 'wb') as phil:
        # data = client_sock.recv(IMG_BUF_SIZE)
        inc_cmd = int.from_bytes(data[0:4], byteorder="big") # I should make a method for getting command
        inc_rev_num = int.from_bytes(data[4:8], byteorder="big") # I should make a method for getting revision number
        print("Receiving file. Cmd: {}, Rev: {}".format(inc_cmd, inc_rev_num))
        if inc_cmd == CMD_UNLOCKED:
            phil.write(data[8:])
            client_sock.send(response)
        else:
            print("Error! Incorrect command when expecting new deck from app.\nCommand: {}".format(inc_cmd))

    # Need to run fromFile() here? Also need to set the new revision number?
    
    # receive the images of the deck
    revision_number, image_names = readFile()
    
    inc_rev_num = -1
    for image_name in image_names:
        data = client_sock.recv(IMG_BUF_SIZE)
        inc_cmd = int.from_bytes(data[0:4], byteorder="big") # I should make a method for getting command
        inc_rev_num = int.from_bytes(data[4:8], byteorder="big") # I should make a method for getting revision number
        print("Receiving Image: {}. Bytes: {}, Cmd: {}, Rev: {}".format(image_name, len(data), inc_cmd, inc_rev_num))
        with open(IMAGE_DIR + image_name, 'wb') as image:
            # Do I need error checking here?
            image.write(data[8:])
            client_sock.send(response)

    # final packet should just be command and revision number, need error checking?
    print("finished receiving.")
    data = client_sock.recv(DEFAULT_BUF_SIZE)
    return SEND_HEARTBEAT, inc_rev_num

def runStateMachineSend(state, client_sock):
    command = b'\x00'
    payload = b'\x00'
    if state == SEND_HEARTBEAT:
        command = CMD_HEARTBEAT.to_bytes(4, byteorder="big")
        payload = global_placeholder.getRevision().to_bytes(4, byteorder="big")
        # printDebugInfo("Sending", state, command, payload)
        client_sock.send(command + payload)
    elif state == COMPARE_REVISIONS:
        if global_placeholder.getLockState():
            command = CMD_LOCKED.to_bytes(4, byteorder="big")
            payload = global_placeholder.getRevision().to_bytes(4, byteorder="big")
            printDebugInfo("Sending", state, command, payload)
            client_sock.send(command + payload)
        else:
            # command = CMD_UNLOCKED.to_bytes(4, byteorder="big")
            # deck.toFile() needs to be run at some point
            # payload = bytes(global_placeholder.getDecklist())
            sendDeck(client_sock)
            # printDebugInfo("Sending", state, command, payload)
            # state will exit to SEND_HEARTBEAT after receiving new decklist

    # client_sock.send(command + payload)
    return state

def runStateMachineRecv(state, client_sock):
    if state == SEND_HEARTBEAT:
        # TODO: error check command
        data = client_sock.recv(DEFAULT_BUF_SIZE)
        # command = int.from_bytes(data[0:4], byteorder="big")
        new_rev = int.from_bytes(data[4:8], byteorder="big") # get revision number
        # printDebugInfo("Receiving", state, command, new_rev)
        if new_rev != global_placeholder.getRevision():
            state = COMPARE_REVISIONS
    elif state == COMPARE_REVISIONS:
        # printDebugInfo("Receiving", state, command, new_rev)
        # TODO: Error check command
        if global_placeholder.getLockState():
            data = client_sock.recv(DEFAULT_BUF_SIZE)
            new_rev = int.from_bytes(data[4:8], byteorder="big") # get revision number
            state = SEND_HEARTBEAT
            # TODO: error check that the returned revision number is correct
        else:
            # global_placeholder.rev_num = new_rev
            state, new_rev = receiveDeck(client_sock)
        

    return state, new_rev


def serverSetup():
    server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
    server_sock.bind(("",bluetooth.PORT_ANY))
    server_sock.listen(1)

    bluetooth.advertise_service(
        server_sock,
        "SmartCards Bluetooth Server",
        UUID,
        service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS],
        profiles=[bluetooth.SERIAL_PORT_PROFILE])

    port = server_sock.getsockname()
    print("\nlistening on port {}".format(port))

    return server_sock

def heartbeat():
    server_sock = serverSetup()

    input_thread = threading.Thread(target=debug, args=((server_sock, )))
    input_thread.start()
    
    # Accepting = True
    while True:
        try:
            print("Accepting new connections")
            client_sock,address = server_sock.accept()
            print("Accepted connection from {}".format(address))

            Connected = True
            state = SEND_HEARTBEAT
            while Connected:
                try:
                    state = runStateMachineSend(state, client_sock)
                    state, new_rev = runStateMachineRecv(state, client_sock)
                    rev_num = new_rev
                    # data = client_sock.recv(DEFAULT_BUF_SIZE)
                    # print("received [{}]".format(data))
                except OSError:
                    Connected = False
                    print("Connection with {} interrupted.".format(address))
                
            client_sock.close()

        except OSError:
            print('Server socket errored or closed')
            break

    print("Server shutting down")
    server_sock.close()
    sys.exit(0)


def debug(server_sock):
    while True:
        in_val = input("\nCommands:\n  'quit'\n  'lock #': lock or unlock\n  deck &: unimplemented\n  'status': status\n  '#': change revision number\n>")
        try:
            args = in_val.split(" ")
            cmd = args[0]
            print(cmd)
            if cmd == 'quit':
                print('quitting')
                server_sock.close()
                break
            elif cmd == "lock":
                global_placeholder.locked = int(args[1])
                print("Set locked to {}".format(global_placeholder.locked))
            elif cmd == "deck":
                pass # TODO: Parse the deck
            elif cmd == "status":
                print("Lock {}, Rev Num: {}, Deck: {}".format(global_placeholder.getLockState(), global_placeholder.getRevision(), global_placeholder.getDecklist()))
            else:
                global_placeholder.rev_num = int(cmd)
        except:
            print('Invalid Command: {}'.format(in_val))

    sys.exit(0)


if __name__=='__main__':
    heartbeat()
