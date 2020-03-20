import time
import argparse

from command import Command
from conversion import DisplayConversion
from display_identification import DisplayIdentification
import image_loader
from led_status import LEDStatus
from flash import ImageFlasher

DECK_FOLDER = "deck"

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

    identifier = DisplayIdentification()

    current_display_id = DisplayIdentification.NO_DISPLAY # start off with thinking there was no display inserted

    while True:
        new_display_id = identifier.find_display()

        if current_display_id == DisplayIdentification.NO_DISPLAY \
           and new_display_id != DisplayIdentification.NO_DISPLAY:
            # acquire the lock on the deck

            # If in clear mode - probably need a new class that checks a button/switch
                # move card on display to discard
                # flash a blank card to the display
            # else if deck is empty
                # indicate empty status
            # else
                # move card on the display to discard
                # "draw" a card (moving the next card into play)
                # flash the new card

            # release the lock on the deck

            pass
