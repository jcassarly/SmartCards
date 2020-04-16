
import bluetooth
import threading
import sys
import json


shota_phone = "4C:DD:31:C9:92:05"
HOST_MAC = "B4:69:21:BE:FA:A9"
UUID = "b5c65192-1d67-471f-8147-0d0e8904efaa"
FILE_NAME = "decklist.json"
DECK_DIR = "./deck/"
ENCODING = "utf-8"

# states
STATE_WAIT_RESP = 0
STATE_SEND_FILE = 1
STATE_RECV_FILE = 2

# commands
CMD_HEARTBEAT = 0
CMD_LOCKED = 1
CMD_UNLOCKED = 2
CMD_DECK_START = 3
CMD_DECK_END = 4
CMD_ACK = 5
CMD_SEND_START = 6
CMD_SEND_DATA = 7
CMD_SEND_END = 8

# message structure
INDEX_CMD = 0
INDEX_REV_NUM = 4
INDEX_FILE_SIZE = 4
INDEX_FILE_NAME = 8
INDEX_FILE_DATA = 4

# constants
BUFFER_SIZE = 251

# global placeholder lock states
LOCKED = 1
UNLOCKED = 0

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

def getFileTransferList():
    # revision_number = -1
    file_names = [FILE_NAME]
    with open(DECK_DIR + FILE_NAME) as phil:
        """ old implementation for a text file"""
        # for line in phil:
        #     if "revnumber" in line:
        #         num_list = [int(word) for word in line.split() if word.isdigit()][0] # maybe need to add error checking for if there's more than 1 num?
        #     elif "]" not in line and '[' not in line:
        #         image_names.append(line)

        """ new implementation for json """
        json_dict = json.load(phil)
        # revision_number = json_dict['rev_number']
        file_names = file_names + json_dict['deckList'] + json_dict['inPlayList'] + json_dict['discardList']

    return file_names

def getFileBytes(file_path):
    data = None
    with open(file_path, 'rb') as phil:
        data = phil.read()
    return data

def writeFileBytes(file_path, data):
    with open(file_path, 'wb') as phil:
        phil.write(data)

def bytesToInt(data, index):
    return int.from_bytes(data[index:index+4], byteorder="big")

def intToBytes(integer):
    return integer.to_bytes(4, byteorder="big")

class SmartCardsServer:
    def __init__(self, uuid, deck_file, deck_dir):
        self.uuid = uuid
        self.deck_file = deck_file
        self.deck_dir = deck_dir
        self.server_sock = None
        self.conn_sock = None
        self.state = STATE_WAIT_RESP
        self.send_file_buffer = None
        self.send_file_queue = None
        self.recv_file_buffer = None
        self.recv_file_name = None
        print("init done")

    def serverSetup(self):
        self.server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
        self.server_sock.bind(("",bluetooth.PORT_ANY))
        self.server_sock.listen(1)

        bluetooth.advertise_service(
            self.server_sock,
            "SmartCards Bluetooth Server",
            self.uuid,
            service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS],
            profiles=[bluetooth.SERIAL_PORT_PROFILE])

        port = self.server_sock.getsockname()
        print("\nlistening on port {}".format(port))


    def sendCmdAndRev(self, command):
        cmd_bytes = intToBytes(command)
        rev_bytes = intToBytes(global_placeholder.getRevision())
        self.conn_sock.send(cmd_bytes + rev_bytes)

    def readSendFile(self):
        with open(self.deck_dir + self.send_file_queue, 'rb') as phil:
            self.send_file_buffer = phil.read()

    def parseCmdWaitResp(self, command, data):
        if command == CMD_HEARTBEAT:
            app_rev_num = bytesToInt(data, INDEX_REV_NUM)
            # check if difference in revisions
            if global_placeholder.getRevision() == app_rev_num:
                # no difference, continue heartbeats
                send_command = CMD_HEARTBEAT
            elif global_placeholder.getLockState() == LOCKED:
                # difference but currently flashing, wait until flash done
                send_command = CMD_LOCKED
            else:
                # difference in revisions, indicate need to send deck data
                send_command = CMD_UNLOCKED
            self.sendCmdAndRev(send_command)
        elif command == CMD_LOCKED:
            # app has reverted, continue heartbeats
            self.sendCmdAndRev(CMD_HEARTBEAT)
        elif command == CMD_UNLOCKED:
            # switch to send files state
            self.state = STATE_SEND_FILE
            # send the command to start sending the deck
            self.send_file_queue = getFileTransferList()
            self.send_file_buffer = None
            self.sendCmdAndRev(CMD_DECK_START)

    def parseCmdSendFile(self, command, data):
        # expected response while sending files should always just be an acknowledgement
        if command == CMD_ACK:
            # if there is no file send in progress
            if self.send_file_buffer == None:
                # if there are still files to send, load the next one
                if len(self.send_file_queue) > 0:
                    self.readSendFile()
                else:
                    # There are no more files to send, prepare to receive app's deck
                    self.state = STATE_RECV_FILE
                    # tell the app we are finished sending the deck
                    self.sendCmdAndRev(CMD_DECK_END)
           
            # file is currently being sent
            else:
                # if the file is done sending
                if len(self.send_file_buffer) > 0:
                    self.send_file_buffer == None
                    self.sendCmdAndRev(CMD_SEND_END)
                else:
                    send_buffer = self.send_file_buffer[:BUFFER_SIZE]
                    self.send_file_buffer = self.send_file_buffer[BUFFER_SIZE:]
                    self.conn_sock.send(send_buffer)

    def parseCmdRecvFile(self, command, data):
        response_cmd = CMD_ACK
        if command == CMD_DECK_START:
            self.recv_file_buffer = None
            self.recv_file_name = None
        elif command == CMD_SEND_START:
            # TODO: should I save the number of incoming bytes somewhere?
            self.recv_file_buffer = []
            self.recv_file_name = data[INDEX_FILE_NAME :].decode("utf-8")
        elif command == CMD_SEND_DATA:
            self.recv_file_buffer += data[INDEX_FILE_DATA :]
        elif command == CMD_SEND_END:
            with open(self.deck_dir + self.recv_file_name, 'wb') as phil:
                phil.write(self.recv_file_buffer)
            
            self.recv_file_buffer = None
            self.recv_file_name = None
        elif command == CMD_DECK_END:
            # app has sent all the deck data, return to heartbeat state
            self.state = STATE_WAIT_RESP
            response_cmd = CMD_HEARTBEAT
        
        self.sendCmdAndRev(response_cmd)

    def runStateMachine(self, data):
        command = bytesToInt(data, INDEX_CMD)
        if self.state == STATE_WAIT_RESP:
            self.parseCmdWaitResp(command, data)
        elif self.state == STATE_SEND_FILE:
            self.parseCmdSendFile(command, data)
        elif self.state == STATE_RECV_FILE:
            self.parseCmdRecvFile(command, data)
        else:
            pass # TODO: Error checking

    def heartbeat(self):
        self.serverSetup()

        input_thread = threading.Thread(target=debug, args=((self.server_sock, )))
        input_thread.start()
        
        # Accepting = True
        while True:
            try:
                print("Accepting new connections")
                client_sock,address = self.server_sock.accept()
                print("Accepted connection from {}".format(address))
                self._conn_sock = client_sock

                Connected = True
                while Connected:
                    try:
                        data = self.conn_sock.recv(BUFFER_SIZE)
                        self.runStateMachine()
                        # data = client_sock.recv(DEFAULT_BUF_SIZE)
                        # print("received [{}]".format(data))
                    except OSError:
                        Connected = False
                        print("Connection with {} interrupted.".format(address))
                    
                self._conn_sock.close()

            except OSError:
                print('Server socket errored or closed')
                break

        print("Server shutting down")
        self.server_sock.close()
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
            print('Invalid Command: {}'.format(cmd))

    sys.exit(0)


if __name__=='__main__':
    server = SmartCardsServer(UUID, FILE_NAME, DECK_DIR)
    server.heartbeat()
