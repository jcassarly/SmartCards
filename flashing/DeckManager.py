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

	#def draw(self):
	#	n = randint(0, len(deckList)-1)
	#	inPlayList.append(deckList.pop(n))
	#	return inPlayList[len(inPlayList)-1]

	def __init__(self, imageList):
		for x in imageList:
			self.deckList.append(Card(imageList.index(x), x))

	def draw(self):
		n = randint(0, len(self.deckList)-1)
		return self.deckList.pop(n)

	def moveCard(self, n):
		x = self.inPlayList.pop(n)
		if (x != 0):
			self.discardList.append(x)
		self.inPlayList.insert(n, draw())

	def returnToDeck(self):
		for x in discardList:
			self.deckList.append(self.discardList.pop(self.discardList.index(x)))
		for x in inPlayList:
			self.deckList.append(self.inPlayList.pop(self.inPlayList.index(x)))
		for x in range(0,5):
			self.inPlayList.append(0)

	def is_empty(self):
		return self.deckList == []
