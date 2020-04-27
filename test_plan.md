# Flashing Loop

|Test Name|Test Description|Result|
|:--:|:--|:--:|
|Empty Deck|Tests that the Pi can correctly start up from an empty deck file|Pass|
|Single Card Deck|Tests that the Pi can correctly start up with a decklist that only has one card in it and flash that card to the first display inserted|Pass|
|Multi-Card Deck|Tests that the Pi can flash multiple cards in a pre-existing deck that it loads in|Pass|
|Disconnected Display|Tests that the flash error turns on after a display is detected from its ID circuit but the physical ePaper display is never inserted.  This flash error goes away after the display is reinserted correctly and flashed||Pass|
|Clear Mode With Empty Deck|Tests that clear mode will clear displays even when the deck is empty||Pass|
|Clear Mode With Non-Empty Deck|Tests that clear mode only discards cards from play and does not manipulate the deck in any other way|Pass|

# System Tests

|Test Name|Test Description|Result|
|:--:|:--|:--:|
|Create Deck|Verify that the user can add and edit cards in the deck after the bluetooth connection is made.  These changes should be saved to disk memory after completion along with all the images that were added should be uploaded to Imgur and sent to the Pi|Pass|
|Play Game Override|After connecting to bluetooth and having a deck created or stored in memory, the user can open the play game menu and the JSON on the Pi gets overridden with whatever was in the saved JSON file on the App|Pass|
|Bluetooth Connection Checks|When the user opens the main page, if they have not opened a bluetooth connection, they should not be able to go to the edit deck or play game pages. A Toast message error should pop up|Pass|
|Edit Deck List During Game|When the user opens one of the deck lists from the play game page, the Pi should get locked and then whatever the deck list is on the Pi should be read by the app and loaded when displaying. No images should be flashed onto the display until after this lock is undone by leaving the edit deck list screen|Pass|
|Edit Deck List Busy|When the user opens one of the deck lists from the play game pag, if the Pi has locked the deck list because it is actively flashing a card, the response back to the app should indicate that it is busy and the App should display a Toast message and not open the edit deck list screen|Pass|
|Return to Play from Editing|When the user finishes whatever change they make to the deck list screen they are in, that change should be saved (if any) and then override the Pi.  The deck should also be unlocked on the Pi side after this.|Pass
|Restart Game|When the user clicks the restart game button on the play game page, all 3 lists should be shuffled into the deck list and then this should override the Pi.  The deck counts on the play game screen should also update|Pass|
|Restart Game Busy|When the user clicks the restart game button on the paly game page, if the response after shuffling the deck lists together and sending the override command to the pi resturns busy, the App should display a Toast message stating the user should try again because the sync is not complete|Pass|
|Return to Main from Play|When the user returns from the play game page to the main page, the app should get the Pi's decklist to update its own and if the Pi is busy, it will keep trying 5 times every 5 seconds to get this information (this retry behavior should also be seen on the return from edit game to the main page with the file transfer command)|Pass|
|Shuffle Deck|From the deck list editing screen off the play game menu, the user should be able to click shuffle deck and shuffle the contents of the deck list and send that to the Pi.  Upon reopening this menu, they should be able to see that the deck is in a different random order|Pass|
|Add To Top|From the deck list editing screen off the play game menu, the user should be able to select a card and then click Add To Top which will shuffle all the other cards and put the selected card as the next card to draw (On the UI, this will be the last card in the list of cards displayed on the menu). This change should be updated on the Pi|Pass|
|Discard From Deck|From the deck list editing screen off the play game menu, the user should be able to select a card and then click Discard From Deck which will put the selected card on the top of the discard list (again this is actually shown as the last card in the list on the UI). This Change should be updated on the Pi|Pass|
|Return to Deck|From the discard list editing screen off the play game menu, the user should be able to select a card and then click Return To Deck which will return the card to the deck list and then shuffle the deck list.  the change should be updated on the Pi.|Pass|
|Draw Next|From the discard list editing screen off the play game menu, the user should be able to select a card and then click Draw Card Next which will put the card on top of the deck list.  This change should be updated on the Pi|Pass|
|Shuffle Discard into Deck|From the discard list editing screen off the play game menu, the user should be able to click Add All to Deck which will shuffle all of the discard into the deck list. This change should be updated on the Pi|Pass|
|Info Page|When on the main menu, the user should be able to click the Info button at the bottom and display the info page about the app|Pass|

