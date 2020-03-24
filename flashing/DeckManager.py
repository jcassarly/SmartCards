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
		self.deckLock = threading.Lock()

		for x in imageList:
			self.deckList.append(Card(imageList.index(x), x))

	def __hash__(self):
		hashcode = hash((tuple(self.deckList), tuple(self.inPlayList), tuple(self.discardList), self.deckLock))
		return hashcode

	def isLocked(self):
		return self.deckLock.locked()

	def draw(self):
		return self.deckList.pop()

	def discardCard(self, display_id):
		if (self.inPlayList[display_id] is not None):
			self.discardList.append(self.inPlayList.pop(display_id))

	def moveCard(self, display_id): # move into discard and get a new one into play
		self.discardCard(display_id)
		drawnCard = self.draw()
		self.inPlayList.insert(display_id, drawnCard)
		return drawnCard.image # shouldnt have to do None checks?

	def returnToDeck(self):
		self.deckLock.acquire()
		for x in discardList:
			self.deckList.append(self.discardList.pop(self.discardList.index(x)))
		for x in inPlayList:
			self.deckList.append(self.inPlayList.pop(self.inPlayList.index(x)))
		for x in range(0,6):
			self.inPlayList.append(0)
		self.deckLock.release()

	def is_empty(self):
		return self.deckList == []
