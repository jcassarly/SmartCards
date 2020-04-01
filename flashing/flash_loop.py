import time
import argparse
import threading
import os

from command import Command
from conversion import DisplayConversion
from display_identification import DisplayIdentification
import image_loader
from led_status import LEDStatus
from flash import ImageFlasher
from clear_mode import ClearMode
from DeckManager import Deck

REREAD_DELAY = 0.5 # s

# TODO: need to add something to read the from file and load that in
def deck_loader():
    """Creates a deck from the images listed in the IMAGE_DIR

    Note that this function is probably only useful for setting up the JSON
    file that is used by the to/from files as it will make sure everything is
    properly formatted and not entry errors occur

    """

    cards = []

    # check if the deck folder exists
    if os.path.isdir(DeckManager.IMAGE_DIR):
        # get a list of the filenames in the folder (all images)
        filenames = os.listdir(DeckManager.IMAGE_DIR)

        for cardname in filenames:
            cards.append(os.path.join(DeckManager.IMAGE_DIR, cardname))

    # return the cards in the deck folder or an empty deck if the folder did not exist
    return Deck(cards)

def flash_blank_image(deck, display_id, flasher):
    print("Flashing Blank")
    # move card on display to discard
    deck.discardCard(display_id)

    # flash a blank card to the display
    blank = DisplayConversion(None)
    flasher.transmit_data(blank.epaper_array)

def flash_next_image(deck, display_id, flasher):
    # move card on the display to discard
    # "draw" a card (moving the next card into play)
    image_path = deck.moveCard(display_id)

    # flash the new card
    print("Flashing Image")
    image = DisplayConversion(image_path)
    flasher.transmit_data(image.epaper_array)

def flash_display(display_id, deck, deck_lock, is_in_clear_mode, flasher):
    # acquire the lock on the deck
    deck_lock.acquire()

    print("Found display {}".format(display_id))

    if not is_in_clear_mode and not deck.is_empty():
        flash_next_image(deck, display_id, flasher)

    elif is_in_clear_mode:
        # deck is in clear mode so flash a blank
        flash_blank_image(deck, display_id, flasher)

    else:
        # deck is empty and not in clear mode
        # do nothing because the status led is already set correctly
        pass

    # release the lock on the deck
    deck_lock.release()

def flash_setup():
    led_status = LEDStatus()
    clear_mode_monitor = ClearMode(led_status)
    flasher = ImageFlasher(led_status)

    identifier = DisplayIdentification()

    current_display_id = DisplayIdentification.NO_DISPLAY # start off with thinking there was no display inserted

    # start up the thread to monitor the clear mode button-switch
    clear_mode_thread = threading.Thread(target=clear_mode_monitor.report_input_changes)
    clear_mode_thread.start()

    return led_status, clear_mode_monitor, flasher, identifier, current_display_id

def flash_loop(deck, deck_lock):
    """Flashing Loop for the SmartCards Application

    Waits for displays to be connected to the Pi and then flashes the
    next image on the card to them.  This function should not exit under
    normal circumstances

    :param Deck deck:
        the Deck object that is used to manage what cards to flash to the pi
    :param Lock deck_lock:
        a thread lock to use when accessing / flashing cards from the deck

    """

    led_status, clear_mode_monitor, flasher, identifier, current_display_id = flash_setup()

    while True:
        new_display_id = identifier.find_display()

        led_status.update_deck_empty_status(deck.is_empty())

        # if display id goes from None to something
        if current_display_id == DisplayIdentification.NO_DISPLAY \
           and new_display_id != DisplayIdentification.NO_DISPLAY:
            # wait a bit and reread the display to allow all the wires to finish
            # being plugged in
            # TODO: maybe make a real hysteresis to alleviate the issue, but the user taking forever to plug in could still be an issue
            # TODO: may also want to unbundle the inputs from the output and vcc/gnd
            time.sleep(REREAD_DELAY)
            new_display_id = identifier.find_display()

            if (new_display_id is not DisplayIdentification.NO_DISPLAY):
                flash_display(new_display_id, deck, deck_lock, clear_mode_monitor.is_in_clear_mode(), flasher)
            else:
                print("Display was disconnected too early")

        current_display_id = new_display_id

def main():
    """Main loop for the Pi side of the SmartCards Application"""
    deck = DeckManager.load_deck()
    deck_lock = threading.Lock()

    # spawn the bluetooth update process with deck
    # TODO: finish up the bluetooth spawning
    #bluetooth_thread = threading.Thread(target=None, args=(deck, deck_lock)) # TODO replace None with the bluetooth call
    #bluetooth_thread.start()

    flash_loop(deck, deck_lock)

if __name__ == "__main__":
    main()
