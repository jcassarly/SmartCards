# Flashing Loop

notes:
- might want to consider testing an improperly formatted deck
- card with no image file in the deck folder

|Test Name|Test Description|Test Setup|Expected Results|Actual Results|
|:--:|:--|:--|:--|:--:|
|Empty Deck|Tests that the Pi can correctly start up from an empty deck file|1. Place a properly formatted empty decklist into the decklist.json|main loop starts up and turns the Deck Empty LED on and once the card is inserted, nothing happens, additionally, the running LED should be on|
|||2. start the main loop with no displays plugged in|||
|||3. Insert a display|||
|Single Card Deck|Tests that the Pi can correctly start up with a decklist that only has one card in it and flash that card to the first display inserted|1. Create a decklist with one card in the deck (nothing in play nor discard)|Main loop starts up and once the display is inserted flashes the card (blinking the flashing LED and then solid once done).  once the flash finishes, the deck empty LED turns on.||
|||2. Insert a display|||
|Multi-Card Deck|Tests that the Pi can flash multiple cards in a pre-existing deck that it loads in|1. Create a deck list with a full 52 play card deck (shuffled)|Main loop starts up and once the first display is inserted, flashes the next card in the deck (blinking/setting the flashing LED as normal).  Once the first display is removed to insert the second one, the flashing status LED turns off, then blinks again once the second is inserted and begins flashing. The next card is then flashed to the first display when it is reinserted.  The decklist at the end should have the first card in the discard, the second card with the second display, and the third card on the first display.||
|||2. Insert a display and disconnect after flashing|||
|||3. Insert a different display and disconnect after flashing|||
|||4. Insert the original display and disconnect after flashing|||
|Disconnected Display|Tests that the flash error turns on after a display is detected from its ID circuit but the physical ePaper display is never inserted.  This flash error goes away after the display is reinserted correctly and flashed|1. Start up the main loop with the decklist as a result of the multi-card deck test|After the display is inserted the first time (without the ePaper display), the flashing status blinks while attempting to wait for the display to say it is done refreshing.  This should time out and then the flash error LED will turn on.  The card is then disconnected from the pi and then reinserted correctly.  the fourth card is flashed onto the pi and the decklist is updated with only that change.  Any change other than the output of the multi-card deck test and the fourth card being flashed is erroneous||
|||2. Insert the ID circuit of the first display from the multi-card deck test|||
|||3. Reinsert the card correctly into the Pi (ID circuit and ePaper display)|||
|Clear Mode With Empty Deck|Tests that clear mode will clear displays even when the deck is empty|1. Get a display with an image already on it|After the image is inserted and clear mode is activated (The LED should be on), a blank white image should be flashed onto the display||
|||2. Set the deck list to be an empty one and start the main loop|||
|||3. Turn on Clear mode
|||4. Insert the display with an image on it and allow it to flash|||
|Clear Mode With Non-Empty Deck|Tests that clear mode only discards cards from play and does not manipulate the deck in any other way|1. Use the decklist from the resulting decklist from the disconnected display test|After clear mode is turned on, the first display should be inserted into the pi and the image on the display should become blank and the card in the decklist should appear in the discard.  After clear mode is deactivated and the second display is inserted again, the decklist should update accordingly|||
|||2. Start the main loop and turn on clear mode|||
|||3. Insert the first display and allow to flash|||
|||4. Turn off clear mode|||
|||5. Insert the second display|||

# Pi and App

Notes
- might want to write something to test when there is a receive error on the pi and the app needs to resend

|Test Name|Test Description|Test Setup|Expected Results|Actual Results|
|:--:|:--|:--|:--|:--:|
|JSON Query|App can query the JSON decklist on the Pi|1. Put a known decklist onto the Pi and start the main loop|The app can connect to the Pi, send a JSON query and receive the correct response||
|||2. Start the app and connect to the Pi|||
|||3. Send the JSON query from the app to the Pi and wait for the response|||
|Busy JSON Query|Tests that when sending the JSON query to the Pi, if the deck is locked due to flashing a card, the response will be a busy error|1. Start the Pi with any decklist|The received response on the App after sending the JSON query should be a BUSY error message.  The decklist should not be changed other than the change due to the card being flashed.||
|||2. Start the app and connect to the Pi|||
|||3. Insert a card to begin flashing and while that is happening, send a JSON query from the App|||
|Lock/Unlock Query|Tests that the App can lock the deck object on the Pi - inhibiting flashing and other queries until unlocked|1. Start the Pi with a known decklist|Pi should start with no issues. the rest of the expected results are listed with each step||
|||2. Start the App and connect to the Pi|The connection should be made with no issues||
|||3. Send a Lock query to the pi|The Pi should receive the lock and lock the deck and send an ACK||
|||4. After the ACK is received on the app, plug in a card|The card should have no changes to it as the Pi is locked||
|||5. Send a JSON query to the Pi and get the BUSY error message|The Pi should be locked at this point and any requests are met with a busy response||
|||6. Send an Unlock query to the Pi|The Pi should begin flashing the card once this query is received||
|Busy Unlock Query|Tests that if the app sends an unlock query that it cannot unlock the Pi if the deck is locked due to flashing|Same steps as the BUSY JSON query except with an unlock sent instead of a JSON query|Same BUSY error message should be received||
|Override Query|Tests that the App can send a new decklist to the Pi, overriding the preexisting decklist|1. Start the Pi with a known decklist|Pi should start with no issues||
|||2. Start the App with a different decklist than the Pi and connect|The connection should be made with no issues||
|||3. Send the Override command to the Pi|The ACK should be received and all subsequent files should be sent and ACKed||
|||4. Insert a display to flash the next card|The flashed card should be the one sent by the App||
|Override Blocks Flashing|Tests that the Pi cannot flash more cards until the override is done sending images|1. Start the Pi with a known decklist|The Pi should start with no issues||
|||2. Start the App with a different decklist and connect|The connection should be made with no issues||
|||3. Begin the override sequence|All the bluetooth packets should send with no issues||
|||4. While the override sequence is happening, plug in the display to the Pi|The Pi should wait until the override sequence is done sending and then flash the next card in the deck the App sent||
|Override Receive Error|Tests that if the app sends an override and then sends a different query before finishing sending all images, that the Pi responds with a receive error and resets the connection after sending the receive error 3 times|1. |results||

# System Tests

|Test Name|Test Description|Result|
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

