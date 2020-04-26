# SmartCards

# Pinout

|Pin Number|Pi Pin Function|External Connection|
|:--:|:--|:--|
|1|3.3V Power|NC|
|2|5V Power|ID Circuit VCC|
|3|I2C1 SDA - GPIO2|NC|
|4|5V Power|NC|
|5|I2C1 SCL - GPIO3|NC|
|6|GND|Display GND|
|7|GPIO 4|Display Data/Command Pin|
|8|UART0 TX|NC|
|9|GND|ID Circuit GND|
|10|UART0 RX|NC|
|11|GPIO 17|ID AND Gate Input 1|
|12|GPIO 18|ID AND Gate Output|
|13|GPIO 27|ID AND Gate Input 2|
|14|GND|NC|
|15|GPIO 22|ID AND Gate Input 3|
|16|GPIO 23|Running LED Status|
|17|3.3V Power|NC|
|18|GPIO 24|Flash Status LED|
|19|SPI0 MOSI - GPIO 10|Display SPI MOSI|
|20|GND|NC|
|21|SPI0 MISO - GPIO 9|NC|
|22|GPIO 25|Clear Mode Status LED|
|23|SPI0 SCLK - GPIO 11|Display SPI SCLK|
|24|SPI0 CS0 - GPIO8|Display SPI _CS|
|25|GND|NC|
|26|SPI0 CS1 - GPIO7|NC|
|27|Reserved|NC|
|28|Reserved|NC|
|29|GPIO 5|Display _Reset|
|30|GND|NC|
|31|GPIO 6|Display Busy/Ready|
|32|GPIO 12|Deck Empty Status LED|
|33|GPIO 13|Clear Mode Button/Switch Input|
|34|GND|NC|
|35|GPIO 19|Display Power/VCC|
|36|GPIO 16|NC|
|37|GPIO 26|Clear Mode Button Output/VCC|
|38|GPIO 20|Flash Error Status|
|39|GND|LED Ground|
|40|GPIO 21|NC|

# App Enviroment
Make sure to create `PYTHON` enviroment variable with the value that is the path to your python.exe. Must be at least V3.8.

- AbstractDeckManager superclass
    - protected void toFile() // same in both
    - void loadDeckName(TextView deckName)
    - abstract void saveDeck()
    - abstract void loadDeck(Context context)
    - abstract void getPrimaryDeck()
    - abstract void getCard(int index) // gets the card from the primary deck
    - abstract void swap(int fromPosition, int toPosition) // swaps in the primary deck
    - abstract void size()


- EditDeckManager extends AbstractDeckManager
    - EditDeckManager getInstance(Context context)
    - void clearDeckFromMemory(Context context)
    - void setIsDeckInMemory(boolean bool)
    - static int getNextID(Context context)
    - static void resetIDs(Context context)
    - void saveDeckName(TextView deckName)
    - void addCard(PlayingCard card)
    - void remove(PlayingCard card)

- GameDeckManager extends AbstractDeckManager
    - GameDeckManager getInstance(Context context)
    - void setPrimaryDeck(subDeck sd)
    - void restartGame()
    - void shuffleDeck()
    - void shuffleInDiscard()
    - void deckToDiscard(int deckIndex) // to top of discard
    - void discardToTopOfDeck(int indexInDiscard)
    - void discardToDeckRandom(int indexInDiscard)

# Gettin the images baybeee

1. need to open the image location on the app to let the user click the the create link
    - nick
2. need a text box for the shared image link to put it onto the app
    - nick
3. need to write the awk script to parse out the url and download it from the shared link
    - jared
    - theres a content variable and "w1355-h1805-no" needs to go on the end of the final url
4. need to make a json object that gets saved and sent from the app to the pi to sync the deck
    - should map filename to shared link url
5. the json object should have the unsaved images written to it and then sent to the pi when the user finishes editing the deck
6. when the pi receives the json, those images need to be downloaded into the deck folder

- python function to write unsaved image shared link urls to a json mapped to their stored location filename in internal memory
    - the filename should not have the full path - just need it to save the image on the pi
    - app code to call this python function on save
    - jared

- app code to send this json object to the pi via bluetooth (new query) and then save it on the pi once received in the state machine
    - shota
