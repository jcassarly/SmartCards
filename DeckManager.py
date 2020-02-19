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
			deckList.append(Card(imageList.index(x), x))

	def draw(self):
		n = randint(0, len(deckList)-1)
		return deckList.pop(n)

	def moveCard(self, n):
		x = inPlayList.pop(n)
		if (x != 0):
			discardList.append(x)
		inPlayList.insert(n, draw())

	def returnToDeck(self):
		for x in discardList:
			deckList.append(discardList.pop(discardList.index(x)))
		for x in inPlayList:
			deckList.append(inPlayList.pop(inPlayList.index(x)))
		for x in range(0,5):
			inPlayList.append(0)