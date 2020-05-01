# SmartCards

An embedded system solution to simulate physical play cards digitally using ePaper displays

You can see a video demo of it [here](https://www.youtube.com/watch?v=mQfrvQA3OFU)

## Description

ePaper displays offer an almost identical physical experience to a traditional playing card, with several advantages you can only get with a software controlled system. SmartCards will address all of the above concerns except for cost. As of 2020, ePaper displays are still costly so unless you were to use these cards exclusively, it would be quite the investment. But as hardware becomes more accessible, this could soon be far more affordable, convenient, and environmentally friendly option than traditional print cards.

To play a game with SmartCards, the user needs an Android Device, A raspberry pi dock, and enough eInk displays for the max number of concurrently used cards for the game you are trying to play (i.e.Texas Holdem with 2 players would require at least 9 cards). To create the deck that will be used, the user must find images of every card needed, and upload them to the app. Then when the game starts, players begin with all the displays being unused, and when they normally would draw from the deck, they take an unused card and place it into the dock. This will ‘draw’ a card from the deck and display the card face onto the display. Now the cards can be used just the same as a normal print playing card. If cards need to be discarded they can just be placed to the side in a graveyard/discard pile as normal. Now if all of the unused displays have had cards drawn onto them, the user can take any of those discarded cards, and stick them into the dock to ‘draw’ a new card. The system will keep track of which cards have been drawn and discarded and the user can view these lists from the app. Additionally, the app contains options for flashing chosen cards, shuffling, and moving cards to and from the discard and the deck. This design we believe can be used to emulate almost every playing card game.

## Pinout

The following table shows the PinOut on the Raspberry Pi for the SmartCards Dock

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

## Test Cases

See the [test plan](./test_plan.md) for more information
