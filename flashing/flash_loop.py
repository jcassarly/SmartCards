import time
import argparse
import threading

from command import Command
from conversion import DisplayConversion
from display_identification import DisplayIdentification
import image_loader
from led_status import LEDStatus
from flash import ImageFlasher
from clear_mode import ClearMode

DECK_FOLDER = "deck"
REREAD_DELAY = 0.5 # s

def deck_loader():
    # check if the deck folder exists

    # if it does
        # get a list of the filenames in the folder (all images)
        # return a Deck object using the list of filenames
    # else return an empty deck

    return None

def flash_loop():
    deck = deck_loader()

    # spawn the bluetooth update process with deck
    # TODO: tell shota that when the contents of the deck list changes, that needs to be written to non-volatile memory

    led_status = LEDStatus()
    clear_mode_monitor = ClearMode(led_status)
    flasher = ImageFlasher(led_status)

    identifier = DisplayIdentification()

    current_display_id = DisplayIdentification.NO_DISPLAY # start off with thinking there was no display inserted

    clear_mode_thread = threading.Thread(target=clear_mode_monitor.report_input_changes)
    clear_mode_thread.start()

    while True:
        new_display_id = identifier.find_display()

        if current_display_id == DisplayIdentification.NO_DISPLAY \
           and new_display_id != DisplayIdentification.NO_DISPLAY:
            # wait a bit and reread the display to allow all the wires to finish
            # being plugged in
            # TODO: maybe make a real hysteresis to alleviate the issue, but the user taking forever to plug in could still be an issue
            time.sleep(REREAD_DELAY)
            new_display_id = identifier.find_display()

            # acquire the lock on the deck

            print("Found display {}".format(new_display_id))

            if clear_mode_monitor.is_in_clear_mode():
                print("Flashing Blank")
                # TODO: move card on display to discard

                # flash a blank card to the display
                blank = DisplayConversion(None)
                flasher.transmit_data(blank.epaper_array)
            else:
                print("Flashing Image")
                image = DisplayConversion("test.jpg")
                flasher.transmit_data(image.epaper_array)

            # else if deck is empty
                # indicate empty status
            # else
                # move card on the display to discard
                # "draw" a card (moving the next card into play)
                # flash the new card

            # release the lock on the deck

            pass

        current_display_id = new_display_id

if __name__ == "__main__":
    flash_loop()
