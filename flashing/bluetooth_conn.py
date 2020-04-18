import bluetooth
import base64
# from queue import Queue
from functools import reduce

class QueryCode():
    JSON = 1
    IMAGE = 2
    OVERRIDE = 3
    LOCK = 4
    UNLOCK = 5

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

    def recv(self):
        if self.is_connected():
            data = self.conn_sock.recv(BluetoothConn.BUFFER_SIZE)
            print("Receiving {} bytes".format(len(data)))
            size = bytes_to_int(data, BluetoothConn.RECV_SIZE)
            data = data[BluetoothConn.RECV_DATA : ]
            print("initial packet size: {}, total: {}".format(len(data), size))
            while len(data) < size:
                data += self.conn_sock.recv(BluetoothConn.BUFFER_SIZE)

            print("Received {} bytes".format(len(data)))
            return data

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
        data = self.recv()
        if bytes_to_int(data, BluetoothConn.INDEX_TYPE) == BluetoothConn.MSG_QUERY:
            code = bytes_to_int(data, BluetoothConn.INDEX_CODE)
        else:
            code = -1 # temp err handling
        return code

    def recv_file(self, file_path):
        # returns status,
        #   1: Received all fine
        #   2: received not a full file, nothing written
        #   3: received an error
        #print("Waiting to receive file: {}".format(file_name))
        # metadata = self.recv()
        # # file_name = bytes_to_int(data, BluetoothConn.INDEX_NAME)
        # file_size = bytes_to_int(metadata, BluetoothConn.INDEX_SIZE)
        # print("Receiving file of size: {}".format(file_size))
        # data = bytearray()
        # # extra_data = bytearray()
        # remaining_data = file_size
        # while remaining_data > 0:
        #     remaining_data = remaining_data - len(data)
        #     # recv_data = self.recv()
        #     # data += recv_data[:remaining_data]
        #     data += self.recv()

        data = self.recv()
        #print("data is equal: {}".format(data == self.moon))
        #print("Writing file: {}. Data amount: {}".format(file_name, len(data)))
        with open(file_path, 'wb') as phil:
            phil.write(data)

        return RecvFileCode.OK

if __name__=="__main__":
    server = BluetoothConn()
    server.wait_for_connection()
    # server.send_ack()
    # server.send_err(ErrorCode.BUSY)
    # server.send_file('./deck/', 'test.json')
    # server.recv_file('./deck/', 'test2.json')
    # server.send_file('./deck/', 'decklist.json')
    # server.recv_file('./deck/', 'decklist2.json')
    # server.send_file('./deck/', 'moon.png')
    # server.recv_file('./deck/', 'moon2.png')
    print("Received: {}".format(server.recv_query()))
    # server.send_ack()
    server.send_err(ErrorCode.BUSY)
