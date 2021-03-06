import os
import threading
import json

from bluetooth_conn import BluetoothConn, QueryCode, RecvFileCode, ErrorCode
import DeckManager
import image_loader

TEMP_DECK_LIST = os.path.join(os.sep, 'home', 'pi', 'eDeck', 'temp_deck.json')

class SyncState():
    """Enum class for states of DeckSynchronizer"""
    WAITING_FOR_QUERY = 0
    WAITING_FOR_CONNECTION = 1
    BUSY_ERROR = 2
    RECEIVE_ERROR = 3
    MISMATCH_ERROR = 4
    UNKNOWN_ERROR = 5 # this error should never happen.  should probably restart everything if we get this naturally


class DeckSynchronizer():

    def __init__(self, deck, deck_lock):
        self.deck = deck
        self.deck_lock = deck_lock
        self.connection = BluetoothConn()
        self.state = SyncState.WAITING_FOR_CONNECTION

        # flag to indicate whether the app locked the deck
        self.app_lock = False

        self.STATE_SWITCH = {
            SyncState.WAITING_FOR_QUERY: self.wait_for_query,
            SyncState.WAITING_FOR_CONNECTION: self.wait_for_connection,
            SyncState.BUSY_ERROR: self.send_busy_error,
            SyncState.RECEIVE_ERROR: self.send_receive_error,
            SyncState.MISMATCH_ERROR: self.send_mismatch_error,
            SyncState.UNKNOWN_ERROR: self.send_unknown_error,
        }

    def run_state_machine(self):
        while True:
            try:
                if not self.connection.is_connected():
                    self.state = SyncState.WAITING_FOR_CONNECTION

                state_func = self.STATE_SWITCH.get(self.state, self.send_unknown_error)

                self.state = state_func()

            except OSError:
                # the bluetooth connection was likely broken, so make a new one
                self.state = SyncState.WAITING_FOR_CONNECTION

    def wait_for_query(self):
        query_code = self.connection.recv_query()

        next_state = SyncState.WAITING_FOR_QUERY

        if self.deck_lock.locked() and self.app_lock and query_code == QueryCode.UNLOCK: # 111
            self.deck_lock.release()
            self.app_lock = False
            self.connection.send_ack()

        elif self.deck_lock.locked() and not self.app_lock: # 101 100
            next_state = SyncState.BUSY_ERROR

        elif not self.deck_lock.locked() and not self.app_lock and query_code == QueryCode.UNLOCK: # 001
            # the deck is unlocked already, but this error is not really an issue
            self.connection.send_ack()

        elif not self.deck_lock.locked() and query_code == QueryCode.UNLOCK: # 010 011
            print("I have no idea how we got here")
            # this should be impossible because app lock can
            # never be true without the deck lock being locked
            next_state = SyncState.UNKNOWN_ERROR
# 000 110
        elif query_code == QueryCode.LOCK:
            self.__acquire_lock_with_app_bypass()
            self.app_lock = True
            self.connection.send_ack()

        elif query_code == QueryCode.JSON:
            # probably dont need to lock to do this
            self.connection.send_file(DeckManager.DECK_LIST)

        # this is not used, but leaving the functionality here because why not
        elif query_code == QueryCode.IMAGE:
            self.deck_lock.acquire()
            for card_path in self.deck:
                self.connection.send_file(card_path)
            self.deck_lock.release()

        elif query_code == QueryCode.OVERRIDE:
            next_state = self.override_files()

        elif query_code == QueryCode.IMAGE_TRANSFER:
            next_state = self.get_images()

        return next_state

    def __acquire_lock_with_app_bypass(self):
        if not self.app_lock and self.deck_lock.locked():
            print("not sure how we got here (Acquire)")

        if not self.app_lock:
            self.deck_lock.acquire()

    def __release_lock_with_app_bypass(self):
        if not self.app_lock and not self.deck_lock.locked():
            print("not sure how we got here (Release)")

        if not self.app_lock:
            self.deck_lock.release()

    def override_files(self):
        self.__acquire_lock_with_app_bypass()

        self.connection.send_ack()

        print("Receiving JSON")
        exit_code = self.connection.recv_file(TEMP_DECK_LIST)

        temp_deck = DeckManager.load_deck(TEMP_DECK_LIST)

        # handle erroneous exit code
        if exit_code != RecvFileCode.OK:
            print("Recv Error: {}".format(exit_code))
            self.deck_lock.release()
            return SyncState.RECEIVE_ERROR

        # verify the in play lists match and handle error
        #if temp_deck.inPlayList != self.deck.inPlayList:
        #    print("Mismatch Error")
        #    self.deck_lock.release()
        #    return SyncState.MISMATCH_ERROR

        self.connection.send_ack()

        print("Done Overriding")
        # update the deck with the JSON file received since everything was received correctly
        self.deck.from_file(TEMP_DECK_LIST)
        self.deck.to_file(DeckManager.DECK_LIST)

        self.__release_lock_with_app_bypass()

        return SyncState.WAITING_FOR_QUERY

    def get_images(self):
        self.__acquire_lock_with_app_bypass()
        self.connection.send_ack()

        print("Receiving Image Transfer JSON")
        recv_file_code = self.connection.recv_file(DeckManager.IMAGE_TRANSFER_LIST)

        if recv_file_code != RecvFileCode.OK:
            print("Recv Error: {}".format(recv_file_code))
            self.connection.send_err(ErrorCode.RECEIVE)
            self.deck_lock.release()
            return SyncState.RECEIVE_ERROR

        self.connection.send_ack()

        image_dict = {}
        with open(DeckManager.IMAGE_TRANSFER_LIST, 'r') as image_file:
            image_dict = json.load(image_file)

        print("Downloading Images")
        for url in image_dict.keys():
            image_name = image_dict[url]
            print("Downloading {} from {}".format(image_name, url))
            image_path = DeckManager.add_path(image_name)
            image_loader.download_photo(image_path, url)
        print("Done Downloading Images")

        self.__release_lock_with_app_bypass()

        return SyncState.WAITING_FOR_QUERY

    def wait_for_connection(self):
        self.connection.wait_for_connection()

        return SyncState.WAITING_FOR_QUERY

    def send_busy_error(self):
        self.connection.send_err(code=ErrorCode.BUSY)

        return SyncState.WAITING_FOR_QUERY

    def send_receive_error(self):
        self.connection.send_err(code=ErrorCode.RECEIVE)

        return SyncState.WAITING_FOR_QUERY

    def send_mismatch_error(self):
        self.connection.send_err(code=ErrorCode.MISMATCH)

        return SyncState.WAITING_FOR_QUERY

    def send_unknown_error(self):
        self.connection.send_err(code=ErrorCode.UNKNOWN)

        # reset the connection because things went really wrong
        self.state = SyncState.WAITING_FOR_CONNECTION
        self.connection.kill_connection()
        return self.state

if __name__ == "__main__":
    deck = DeckManager.load_deck(DeckManager.DECK_LIST)
    deck_lock = threading.Lock()

    # spawn the bluetooth update process with deck
    synchronizer = DeckSynchronizer(deck, deck_lock)
    synchronizer.run_state_machine()

