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

- whenever we go to edit deck or play game, need to check if connected and not move if not connected
    - when we finish and are sending the image files, we should keep trying until its not busy - so wait
    - maybe want to send a toast to say its busy
        - if this works, should be able to also send toasts while uploading/saving images
- whenever we go from landing page to play game - need to override the images on the pi
    - also need to not go to this page if we are not connected via bluetooth
        - if not connected, dont change pages
    - if its busy when we override, jsut dont change pages and send a toast to say that
- whenever we go to edit one of the list on the play game screen,
    - lock the pi
    - we should get the pi json and save that and then load it

    - if we get busy after locking, then just dont go to the edit screen and pop up a toast
- whenever we finish making an edit to the deck (clicking one of the manipulation buttons)
    - we need to save the changes
    - we need to send those down to the pi
    - then unlock the pi

    - no need to error checking - pi could not lock the deck
- when restart game is clicked, just make the changes, save, and override
    - if the pi is locked, then just make a toast and dont do it
- whenever we go from the play game screen to the landing page, need to get the pi's version of the deck
    - keep trying until not busy
    - this should be in the finish method
        - call before super.finish
- need to update the deck numbers whenever the play game screen loads
