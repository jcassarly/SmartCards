import os
import json
import threading
import random

IMAGE_DIR = os.path.join(os.sep, 'home', 'pi', 'eDeck', 'deck')
DECK_LIST = os.path.join(os.sep, 'home', 'pi', 'eDeck', 'decklist.json')

def load_deck(file_path):
	deck = Deck([])
	deck.fromFile(file_path)
	return deck

# shuffle
# add to end (top of the deck)
# add to index
# remove
	# from exact index
	# remove end (top of the deck) - NOTE: this is just the draw function
# move card around in deck list
# move card from discard to deck list
# move card from deck to discard

class Deck:
	def __init__(self, imageList):
		self.deckList = []
		self.inPlayList = [None, None, None, None, None, None]
		self.discardList = []
		self.update_rev_number()

		for x in imageList:
			self.deckList.append(x)

	def __hash__(self):
		hashcode = hash((tuple(self.deckList), tuple(self.inPlayList), tuple(self.discardList)))
		return hashcode

	def update_rev_number(self):
		self.rev_number = self.__hash__()

	def __remove_path(self, deckList):
		filenames = []
		for card in deckList:
			# TODO: add something to handle the case of the card not having the IMAGE_DIR prefix
			filenames.append(card[len(IMAGE_DIR) + len(os.sep):] if card is not None else None)

		return filenames

	def __add_path(self, deckList):
		paths = []

		for card in deckList:
			paths.append(os.path.join(IMAGE_DIR, card) if card is not None else None)

		return paths

	def toFile(self, file_path):
		'''write the data to a file (specified by file_path) and then
		\\ at the beginning of a name indicates a special case (ie \\None'''
		'''Format:
		{
			'deckList': ['image1', 'image2', 'image3'],
			'inPlayList': [null, null, 'image5', 'image8', null, null],
			'discardList': ['image4', 'image6'],
			'rev_number': 12937981365012
		}
		'''
		self.update_rev_number()

		with open(file_path, 'w') as output:
			output.write(json.dumps({
				'deckList': self.__remove_path(self.deckList),
				'inPlayList': self.__remove_path(self.inPlayList),
				'discardList': self.__remove_path(self.discardList),
				'rev_number': self.rev_number
			}))

	def fromFile(self, file_path):
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
		random.shuffle(self.deckList)

	def add_to_top(self, card):
		self.deckList.append(card)

	def insert(self, index, card):
		self.deckList.insert(index, card)

	def remove_from_index(self, index):
		return self.deckList.pop(index)

	def move_card_in_deck(self, start_index, end_index):
		self.insert(end_index, self.deckList.pop(start_index))

	def move_card_to_discard(self, index):
		self.discardList.append(self.remove_from_index(index))

	def discard_top_of_deck(self):
		self.discardList.append(self.remove_from_top())

	def return_to_deck(self, discard_index, deck_index):
		"""Move card from discard to deck"""
		self.insert(deck_index, self.discardList.pop(discard_index))

	def return_to_top(self, discard_index):
		self.add_to_top(self.discardList.pop(discard_index))

	def remove_from_top(self):
		return self.deckList.pop()

	def discard_from_play(self, display_id):
		if (self.inPlayList[display_id] is not None):
			self.discardList.append(self.inPlayList[display_id])
			self.inPlayList[display_id] = None

	def draw(self, display_id): # move into discard and get a new one into play
		self.discard_from_play(display_id)
		drawnCard = self.remove_from_top()
		self.inPlayList[display_id] = drawnCard
		return drawnCard

	def is_empty(self):
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
