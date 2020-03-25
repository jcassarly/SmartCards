import threading
from random import randint

class Card:
	number = 0
	image = 0
	def __init__(self, number, image):
		self.number = number
		self.image = image


class Deck:
	#def draw(self):
	#	n = randint(0, len(deckList)-1)
	#	inPlayList.append(deckList.pop(n))
	#	return inPlayList[len(inPlayList)-1]

	def __init__(self, imageList):
		self.deckList = []
		self.inPlayList = [None, None, None, None, None, None]
		self.discardList = []
		self.rev_number = hash(self)

		for x in imageList:
			self.deckList.append(x)

	def __hash__(self):
		hashcode = hash((tuple(self.deckList), tuple(self.inPlayList), tuple(self.discardList)))
		return hashcode

	def update_rev_number(self):
		self.rev_number = hash(self)

	def toFile(self, file_path):
		'''write the data to a file (specified by file_path) and then'''
		'''Format:
		deckList = [
		images
		]
		inPlayList = [
		images
		]
		discardList = [
		images
		]
		revnumber = <revnumber>
		'''
		pass

	def fromFile(file_path):
		'''filepath is where we read from'''
		pass

	def draw(self):
		return self.deckList.pop()

	def discardCard(self, display_id):
		if (self.inPlayList[display_id] is not None):
			self.discardList.append(self.inPlayList[display_id])
			self.inPlayList[display_id] = None

	def moveCard(self, display_id): # move into discard and get a new one into play
		self.discardCard(display_id)
		drawnCard = self.draw()
		self.inPlayList[display_id] = drawnCard
		return drawnCard

	def returnToDeck(self):
		for x in discardList:
			self.deckList.append(self.discardList.pop(self.discardList.index(x)))
		for x in inPlayList:
			self.deckList.append(self.inPlayList.pop(self.inPlayList.index(x)))
		for x in range(0,6):
			self.inPlayList.append(0)

	def is_empty(self):
		return self.deckList == []
