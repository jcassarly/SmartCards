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

