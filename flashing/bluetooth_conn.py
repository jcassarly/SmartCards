import bluetooth
import base64
import select
# from queue import Queue
from functools import reduce

class QueryCode():
    JSON = 1
    IMAGE = 2
    OVERRIDE = 3
    LOCK = 4
    UNLOCK = 5
    IMAGE_TRANSFER = 6

class RecvFileCode():
    OK = 1
    ERR = 2
    BEGIN = 3
    END = 4

class ErrorCode():
    BUSY = 2
    RECEIVE = 3
    MISMATCH = 4
    UNKNOWN = 5

def bytes_to_int(data, index):
        return int.from_bytes(data[index:index+4], byteorder="big")

def ints_to_bytes(int_list):
    byte_list = list(map(lambda x: x.to_bytes(4, byteorder="big") , int_list))
    return reduce(lambda x,y: x + y, byte_list, b'')


class BluetoothConn():

    shota_phone = "4C:DD:31:C9:92:05"
    HOST_MAC = "B4:69:21:BE:FA:A9"
    UUID = "b5c65192-1d67-471f-8147-0d0e8904efaa"
    FILE_NAME = "decklist.json"
    DECK_DIR = "./deck/"
    ENCODING = "utf-8"

    BUFFER_SIZE = 50000

    MSG_QUERY = 1
    MSG_RECV_FILE = 2
    MSG_ERROR = 3
    MSG_ACK = b'\xbe\xef\xca\xfe'

    INDEX_TYPE = 0
    INDEX_CODE = 4

    RECV_SIZE = 0
    RECV_DATA = 4

    def server_setup(self):
        self.server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
        self.server_sock.bind(("",bluetooth.PORT_ANY))
        self.server_sock.listen(1)

        bluetooth.advertise_service(
            self.server_sock,
            "SmartCards Bluetooth Server",
            BluetoothConn.UUID,
            service_classes=[BluetoothConn.UUID, bluetooth.SERIAL_PORT_CLASS],
            profiles=[bluetooth.SERIAL_PORT_PROFILE])

        port = self.server_sock.getsockname()
        print("\nlistening on port {}".format(port))

    def __init__(self):
        self.server_sock = None
        self.conn_sock = None
        # self.query_queue = Queue()
        # self.file_queue = Queue()
        self.moon = None
        self.server_setup()

    def kill_connection(self):
        self.conn_sock.close()
        self.conn_sock = None
        pass

    def is_connected(self):
        return self.conn_sock != None

    def wait_for_connection(self):
        if self.is_connected():
            self.kill_connection()

        print("Accepting new connections")
        self.conn_sock, address = self.server_sock.accept()
        print("Accepted connection from {}".format(address))

        # put code for making thread query and file code queues here in the future?

    def send(self, data):
        if self.is_connected():
            size = ints_to_bytes([len(data)])
            self.conn_sock.send(size + data)

    def select_recv(self):
        receiving, _, _ = select.select([self.conn_sock], [],[], 10)
        if len(receiving) == 0:
            self.conn_sock.close()
            self.conn_sock = None
            return None
        else:
            return receiving[0].recv(BluetoothConn.BUFFER_SIZE)

    def recv(self):
        data = None
        return_code = RecvFileCode.ERR
        if self.is_connected():
            data = self.select_recv()
            if data is not None:
                size = bytes_to_int(data, BluetoothConn.RECV_SIZE)
                print("Receiving {} bytes, Packet size: {}".format(len(data), size))
                data = data[BluetoothConn.RECV_DATA : ]
                while len(data) < size:
                    recv_data = self.select_recv()
                    if data is None or recv_data is None:
                        break
                    data += recv_data
                    print("Received {} bytes".format(len(data)), end="\r", flush=True)
                if len(data) == size:
                    return_code = RecvFileCode.OK
                print("\nDone receiving")

        return data, return_code

    def send_ack(self):
        print("sending ack")
        self.send(BluetoothConn.MSG_ACK)

    def send_err(self, code=ErrorCode.UNKNOWN):
        # send an error code to the app?
        self.send(ints_to_bytes([BluetoothConn.MSG_ERROR, code]))

    def send_file(self, file_path):
        data = None
        with open(file_path, 'rb') as phil:
            data = phil.read()
        self.moon = data
        # TODO: I may not need to send the name of the file being sent
        # name_bytes = bytearray(file_name.encode('utf-8'))
        # name_size = len(name_bytes)
        int_bytes = ints_to_bytes([BluetoothConn.MSG_RECV_FILE, RecvFileCode.BEGIN])
        print("Send start send")
        self.send(int_bytes)
        print("Size of file: {}".format(len(data)))
        self.send(data)
        print("Sent File")

    def recv_query(self):
        # returns type of query, an integer
        data, code = self.recv()
        if data is not None and bytes_to_int(data, BluetoothConn.INDEX_TYPE) == BluetoothConn.MSG_QUERY:
            code = bytes_to_int(data, BluetoothConn.INDEX_CODE)
        else:
            code = -1 # temp err handling
        return code

    def recv_file(self, file_path):
        data, code = self.recv()

        if code == RecvFileCode.OK:
            with open(file_path, 'wb') as phil:
                phil.write(data)

        return code

if __name__=="__main__":
    server = BluetoothConn()
    server.wait_for_connection()

    while server.is_connected():
        server.recv()
        server.send_ack()
