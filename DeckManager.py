import threading

class Card:
	number = 0
	image = 0
	def __init__(self, number, image):
		self.number = number
		self.image = image


class Deck:
	deckList = []
	inPlayList = [0, 0, 0, 0, 0]
	discardList = []
	deckLock = threading.Lock()

	#def draw(self):
	#	n = randint(0, len(deckList)-1)
	#	inPlayList.append(deckList.pop(n))
	#	return inPlayList[len(inPlayList)-1]

	def __init__(self, imageList):
		deckLock.acquire()
		for x in imageList:
			deckList.append(Card(imageList.index(x), x))
		deckLock.release()

    def hash(self):
		deckLock.acquire()
		hashcode = ''
		for x in decklist:
			hashcode = hashcode + str(x.number).zfill(3)
		deckLock.release()
		return hashcode

	def isLocked(self):
		n = deckLock.acquire(False)
		if (n):
			deckLock.release()
		return not n
		
	def draw(self):
		deckLock.acquire()
		n = randint(0, len(deckList)-1)
		drawnCard = deckList.pop(n)
		deckLock.release()
		return drawnCard

	def moveCard(self, n):
		deckLock.acquire()
		x = inPlayList.pop(n)
		if (x != 0):
			discardList.append(x)
		inPlayList.insert(n, draw())
		deckLock.release()

	def returnToDeck(self):
		deckLock.acquire()
		for x in discardList:
			deckList.append(discardList.pop(discardList.index(x)))
		for x in inPlayList:
			deckList.append(inPlayList.pop(inPlayList.index(x)))
		for x in range(0,5):
			inPlayList.append(0)
		deckLock.release()
