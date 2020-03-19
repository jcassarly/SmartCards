
import bluetooth


shota_phone = "4C:DD:31:C9:92:05"
HOST_MAC = "B4:69:21:BE:FA:A9"
UUID = "b5c65192-1d67-471f-8147-0d0e8904efaa"

# states
SEND_HEARTBEAT = 0
COMPARE_REVISIONS = 1

# commands
CMD_HEARTBEAT = 0
CMD_LOCKED = 1
CMD_UNLOCKED = 2

locked = True
def getLockState():
    return locked

rev_num = 0
def getRevision():
    return rev_num

deck = [num for num in range(5)]
def getDecklist():
    return deck

def unpackDecklist(bytes):
    updateSharedMemory(bytes)

def updateSharedMemory(bytes):
    pass


def runStateMachineSend(state, client_sock):
    command = b'\x00'
    payload = b'\x00'
    if state == SEND_HEARTBEAT:
        command = CMD_HEARTBEAT.to_bytes(4, byteorder="little")
        payload = getRevision().to_bytes(4, byteorder="little")
    elif state == COMPARE_REVISIONS:
        if getLockState():
            command = CMD_LOCKED.to_bytes(4, byteorder="little")
            state = SEND_HEARTBEAT
        else:
            command = CMD_UNLOCKED.to_bytes(4, byteorder="little")
            payload = bytes(getDecklist())
            # state will exit to SEND_HEARTBEAT after receiving new decklist

    client_sock.send(command, payload)
    return state

def runStateMachineRecv(state, client_sock):
    data = client_sock.recv(1024)
    command = int.from_bytes(data[0:4], byteorder="little")
    new_rev = int.from_bytes(data[4:8], byteorder="little") # get revision number
    if state == SEND_HEARTBEAT:
        # TODO: error check command
        state = COMPARE_REVISIONS
    elif state == COMPARE_REVISIONS:
        # TODO: Error check command
        if getLockState():
            pass
            # TODO: error check that the returned revision number is correct
        else:
            unpackDecklist(data[8:])
            state = SEND_HEARTBEAT

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
    return server_sock

def heartbeat():
    server_sock = serverSetup()
    
    Accepting = True
    while Accepting:
        client_sock,address = server_sock.accept()
        print("Accepted connection from {}".format(address))

        Connected = True
        state = SEND_HEARTBEAT
        while Connected:
            try:
                state = runStateMachineSend(state, client_sock)
                state, new_rev = runStateMachineRecv(state, client_sock)
                rev_num = new_rev
                # data = client_sock.recv(1024)
                # print("received [{}]".format(data))
            except OSError:
                print("Connection with {} interrupted. Accepting new connections".format(address))
            
            

        client_sock.close()
    server_sock.close()

