import os
import threading

from bluetooth_conn import BluetoothConn, QueryCode, RecvFileCode, ErrorCode
import DeckManager

TEMP_IMAGE_DIR = os.path.join(os.sep, 'home', 'pi', 'eDeck', 'temp_deck')

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

        if self.deck_lock.locked() and self.app_lock and query_code == QueryCode.UNLOCK:
            print("Unlocking")
            self.deck_lock.release()
            self.app_lock = False
            self.connection.send_ack()

        elif self.deck_lock.locked():
            print("Busy")
            next_state = SyncState.BUSY_ERROR

        elif query_code == QueryCode.UNLOCK and not self.app_lock:
            print("We already Unlocked")
            # the deck is unlocked already, but this error is not really an issue
            self.connection.send_ack()

        elif query_code == QueryCode.UNLOCK or self.app_lock:
            print("I have no idea how we got here")
            # this should be impossible because app lock can
            # never be true without the deck lock being locked
            next_state = SyncState.UNKNOWN_ERROR

        elif query_code == QueryCode.LOCK:
            print("Locking")
            self.deck_lock.acquire()
            self.app_lock = True
            self.connection.send_ack()

        elif query_code == QueryCode.JSON:
            print("Sending JSON")
            self.connection.send_file(DeckManager.DECK_LIST)

        elif query_code == QueryCode.IMAGE:
            print("sending images")
            self.deck_lock.acquire()
            for card_path in self.deck:
                self.connection.send_file(card_path)
            self.deck_lock.release()

        elif query_code == QueryCode.OVERRIDE:
            print("Overriding")
            self.deck_lock.acquire()

            self.connection.send_ack()

            exit_code = self.connection.recv_file(DeckManager.DECK_LIST)

            temp_deck = DeckManager.load_deck(DeckManager.DECK_LIST)

            # TODO: handle erroneous exit code
            if exit_code != RecvFileCode.OK:
                self.deck_lock.release()
                return SyncState.RECEIVE_ERROR

            # TODO: verify the in play lists match and handle error
            if temp_deck.inPlayList != self.deck.inPlayList:
                self.deck_lock.release()
                return SyncState.MISMATCH_ERROR

            self.connection.send_ack()

            for card_path in temp_deck:
                # try to receive the file up to 3 times
                for tries in range(0, 3):
                    exit_code = self.connection.recv_file(card_path)

                    # TODO: handle erroneous exit code
                    if exit_code == RecvFileCode.OK:
                        break

                    self.send_receive_error()

                # if the above for loop exits normally, then we failed 3 times to receive the file
                if exit_code != RecvFileCode.OK:
                    self.deck.toFile(DeckManager.DECK_LIST)  # revert to before the JSON
                    # TODO: consider reverting the images back to before override started
                    #       this would require temp storage
                    self.deck_lock.release()
                    return SyncState.UNKNOWN_ERROR
                # otherwise the above loop received the file, so ACK it
                else:
                    self.connection.send_ack()

            # update the deck with the JSON file received since everything was received correctly
            self.deck.fromFile(DeckManager.DECK_LIST)

            self.deck_lock.release()

            print("State after Override: {}".format(next_state))

        return next_state

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

