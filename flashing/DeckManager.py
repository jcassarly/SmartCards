import os
import json
import threading
import random

IMAGE_DIR = os.path.join(os.sep, 'home', 'pi', 'eDeck', 'deck')
DECK_LIST = os.path.join(os.sep, 'home', 'pi', 'eDeck', 'decklist.json')

def load_deck(file_path):
    """Creates a deck loaded from the given file path

    :param str file_path: the path to the file that contains the JSON deck object info
    :returns: the Deck object created after reading this file

    """
    deck = Deck([])
    deck.fromFile(file_path)
    return deck

class Deck:
    def __init__(self, image_paths):
        """Initializes the Deck Manager

        Initial values are a decklist that is a copy of the image_paths, an inPlayList
        that is an array of 6 None values by default (indices 1-5 are directly indexable
        by the display ID they correspond to and the 0 index should be unused), and an
        empty discard list

        All cards in the deck are represented by the absolute filepath to their image
        file (the elements of image_paths).  All images used should be in the top
        level of the IMAGE_DIR directory

        :param arr image_paths:
            an array of strings where each element is the absolute path to the image
            it is representing.  These make up the starting decklist where everything
            is in the deck and no cards are in play or discard

        """
        self.deckList = []
        self.inPlayList = [None, None, None, None, None, None]
        self.discardList = []
        self.update_rev_number()

        for path in image_paths:
            self.deckList.append(path)

    def __hash__(self):
        """Returns a hash value of the 3 lists in this DeckManager

        :returns:
            an integer hash value generated from the deck list, in play list, and
            discard list of this DeckManager

        """
        return hash((tuple(self.deckList), tuple(self.inPlayList), tuple(self.discardList)))

    def update_rev_number(self):
        """Hashes this DeckManager and stores the value into rev_number"""
        self.rev_number = self.__hash__()

    def __remove_path(self, deck_list):
        """Returns a list with all the IMAGE_DIR prefixes removed from the elements of deck_list

        None values are unchanged

        :param arr deck_list:
            an array of filepaths all with the IMAGE_DIR prefix
        :returns:
            an array with the same values as deck_list except with the IMAGE_DIR
            prefix removed

        """
        filenames = []
        for card in deck_list:
            # TODO: add something to handle the case of the card not having the IMAGE_DIR prefix
            # remove the prefix (or leave alone if None)
            filenames.append(card[len(IMAGE_DIR) + len(os.sep):] if card is not None else None)

        return filenames

    def __add_path(self, deck_list):
        """Creates an array with elements in deck_list except with IMAGE_DIR as a prefix for each

        None values are unchanged

        :param arr deck_list:
            an array of file names in the IMAGE_DIR directory that need IMAGE_DIR added as a
            prefix
        :returns:
            an array of file paths that are the values in deck_list with IMAGE_DIR added as a
            prefix
        """
        paths = []

        for card in deck_list:
            # append the IMAGE_DIR prefix (or do nothing if the card is None)
            paths.append(os.path.join(IMAGE_DIR, card) if card is not None else None)

        return paths

    def toFile(self, file_path):
        """Write this DeckManager object to the file specified by file_path

        The format of the JSON written to file_path is like the following example
        (with less white space):

        {
            'deckList': ['image1.jpg', 'image2.jpg', 'image3.jpg'],
            'inPlayList': [null, null, 'image5.jpg', 'image8.jpg', null, null],
            'discardList': ['image4', 'image6'],
            'rev_number': 12937981365012
        }

        the IMAGE_DIR prefix that should be on each filepath in this object is stripped
        when written to the file (for cross platform compatibility since this module
        is used by the Pi and the App)

        :param str file_path:
            the path to the file to write a JSON version of this object to

        """
        self.update_rev_number()

        with open(file_path, 'w') as output:
            output.write(json.dumps({
                'deckList': self.__remove_path(self.deckList),
                'inPlayList': self.__remove_path(self.inPlayList),
                'discardList': self.__remove_path(self.discardList),
                'rev_number': self.rev_number
            }))

    def fromFile(self, file_path):
        """Update this object with the JSON data at file_path

        If the file_path does no exist, the object is reinitialized to an empty deck
        and all other values are defaults

        The format is the same as the toFile format

        The filenames in the JSON file should not have the IMAGE_DIR prefix. Those
        are added back by this method

        :param str file_path: the path to the JSON file with data to update this object

        """
        '''filepath is where we read from'''
        if os.path.exists(file_path):
            with open(file_path, 'r') as input_file:
                deck_data = json.load(input_file)

                self.deckList = self.__add_path(deck_data['deckList'])
                self.inPlayList = self.__add_path(deck_data['inPlayList'])
                self.discardList = self.__add_path(deck_data['discardList'])
                self.rev_number = deck_data['rev_number']
        else:
            self.__init__([])

    def shuffle(self):
        """Shuffle the order of the deck list"""
        random.shuffle(self.deckList)

    def add_to_top(self, card):
        """Add the card (a filepath to the image) to the top of the deck list

        :param str card: the absolute filepath to the card. Should be in IMAGE_DIR

        """
        self.deckList.append(card)

    def insert(self, index, card):
        """Insert a card into the deck before whatever card is at index

        Same as python list's insert method

        :param int index: the location to insert the card
        :param str card: the absolute filepath to the card. Should be in IMAGE_DIR

        """
        self.deckList.insert(index, card)

    def remove_from_index(self, index):
        """Remove the card in the deck at index from the DeckManager

        :param int index: the index of the card to remove
        :returns: the card (filepath) of the card removed

        """
        return self.deckList.pop(index)

    def move_card_in_deck(self, start_index, end_index):
        """Moves the card at start_index in the deck to end_index

        If the end index is past the top of the deck, the card will be moved
        to the top of the deck

        :param int start_index: the index of the original location of the card to move
        :param int end_index: the index of the card to move's destination

        """
        self.insert(end_index, self.deckList.pop(start_index))

    def move_card_to_discard(self, index):
        """Removes the card at the index from the deck and discards it

        :param int index: the index of the card to move to the top of the discard list

        """
        self.discardList.append(self.remove_from_index(index))

    def discard_top_of_deck(self):
        """Removes the top card of the deck and moves it to the top of the discard list"""
        self.discardList.append(self.remove_from_top())

    def return_to_deck(self, discard_index, deck_index):
        """Move card at discard_index of the discard list to deck_index of the deck

        :param int discard_index:
            the index of the card in the discard list to return to the deck
        :param int deck_index:
            the index of the deck to insert the card

        """
        self.insert(deck_index, self.discardList.pop(discard_index))

    def return_to_top(self, discard_index):
        """Returns the card at discard_index of the discard list to the top of the deck

        :param int discard_index: the index of the card to return to the top of the deck

        """
        self.add_to_top(self.discardList.pop(discard_index))

    def remove_from_top(self):
        """Removes the top card of the deck from the DeckManager

        :returns: the card removed

        """
        return self.deckList.pop()

    def discard_from_play(self, display_id):
        """Discards the card on display_id from play

        If a card indexed by display_id exists in play, then it is moved to the
        discard (and set back to None in the in play list).  Otherwise, no change
        it made

        :param int display_id:
            the ID of the display to discard from play (should be integer in
            range 1-5 inclusive)

        """
        if (self.inPlayList[display_id] is not None):
            self.discardList.append(self.inPlayList[display_id])
            self.inPlayList[display_id] = None

    def draw(self, display_id): # move into discard and get a new one into play
        """Draws a card from the deck and puts it into play

        The card on the display specified by display_id is discarded from play
        (if there was a card on that display).  The next card in the deck is then drawn
        and put into play at the index specified by display_id

        :param int display_id:
            the ID of the display to draw the next card into and discard whatever was
            previously in that spot (should be integer in range 1-5 inclusive)

        """
        self.discard_from_play(display_id)
        drawnCard = self.remove_from_top()
        self.inPlayList[display_id] = drawnCard
        return drawnCard

    def is_empty(self):
        """Determines if the deck is empty

        :returns: True if the deck list is an empty list, false otherwise

        """
        return self.deckList == []


if __name__ == '__main__':
    deck = Deck(['deck/test', 'deck/hi', 'deck/yeet'])
    #deck.toFile('testfile')

    print(deck.deckList)
    print(deck.inPlayList)
    print(deck.discardList)
    print(deck.rev_number)

    deck.moveCard(2)
    deck.moveCard(4)

    deck.update_rev_number()
    print(deck.deckList)
    print(deck.inPlayList)
    print(deck.discardList)
    print(deck.rev_number)

    deck.fromFile('testfile')

    print(deck.deckList)
    print(deck.inPlayList)
    print(deck.discardList)
    print(deck.rev_number)
