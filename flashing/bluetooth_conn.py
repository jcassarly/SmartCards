JSON = 1
IMAGE = 2
OVERRIDE = 3

RECV_FILE_OK = 1
RECV_FILE_PARTIAL = 2
RECV_FILE_ERR = 3

class QueryCode():
    JSON = 1
    IMAGE = 2
    OVERRIDE = 3
    LOCK = 4
    UNLOCK = 5

class RecvFileCode():
    OK = 1
    PARTIAL = 2
    ERR = 3

class ErrorCode():
    BUSY = 2
    RECEIVE = 3
    MISMATCH = 4
    UNKNOWN = 5 # this error should never happen.  should probably restart everything if we get this naturally

class BluetoothConn():

    def __init__(self):
        self.queries = [
            QueryCode.IMAGE,
            QueryCode.OVERRIDE,
            QueryCode.UNLOCK,
            QueryCode.UNLOCK,
            QueryCode.LOCK,
            QueryCode.JSON,
            QueryCode.OVERRIDE,
            QueryCode.IMAGE,
            QueryCode.LOCK,
            QueryCode.UNLOCK,
            QueryCode.JSON,
            QueryCode.OVERRIDE,
        ]

    def wait_for_connection(self):
        pass

    def is_connected(self):
        return True

    def kill_connection(self):
        pass

    def send_ack(self):
        """Sends Acknowledgement Packet"""
        pass#print("Sending ACK")

    def send_err(self, code=0):
        """Send error code to the app"""
        pass#print("Sending Error {}".format(code))

    def send_file(self, file_path):
        """Sends the given file to the app"""
        print("Sending {}".format(file_path))

    def recv_query(self):
        """Returns type of query (JSON, IMAGE, OVERRIDE)"""
        return QueryCode.UNLOCK #self.queries.pop() if self.queries != [] else None

    def recv_file(self, file_path):
        """Returns status (RECV_FILE_X)"""
        print("Receiving {}".format(file_path))
        return RecvFileCode.OK
