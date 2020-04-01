import pigpio
import time
import argparse

from command import Command, transmission_command
from conversion import DisplayConversion
from display_identification import DisplayIdentification
import image_loader
from led_status import LEDStatus

class ImageFlasher:
    """Class to handle flashing images to the ePaper Display."""
    # spi flags
    # 21 20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0
    #  b  b  b  b  b  b  R  T  n  n  n  n  W  A u2 u1 u0 p2 p1 p0  m  m
    #  0  0  0  0  0  0  1  0  0  0  0  0  0  0  0  0  0  0  0  0  0  1

    # m is the mode which is 0
    # p is 0 since CS is active low
    # u is 0 since using GPIO reserved for SPI
    # A is 0 since using auxillary SPI
    # W is 0 since device is not 3-wire
    # n is 0 since it is ignored to to W=0
    # T is 0 since the pi is little endian by default
    # R is 0 since the everything is little endian
    # b is 0 since using 8-bit words

    # SPI setup options
    SPI_FLAGS   = 0x00000000
    SPI_BAUD    = 2000000
    SPI_CHANNEL = 0  # CE is on pin 8

    DATA_COMMAND_PIN = 4 # gpio 4, pin 7
    RESET_PIN = 5 # gpio 5, pin 29
    READY_PIN = 6 # gpio 6, pin 31

    RESET_DELAY = 0.1 # sec

    READY_WAIT_TIMEOUT = 1 # sec

    def __init__(self, led_status):
        self.led_status = led_status

        self.pi = pigpio.pi()
        self.spi_handle = self.pi.spi_open(self.SPI_CHANNEL, self.SPI_BAUD, self.SPI_FLAGS)

        self.__setup_non_spi_pins()

        self.__reset_display()

        self.__power_up()

    def __setup_non_spi_pins(self):
        """Perform setup (modes and pull downs) for all the non-spi pins"""
        # Set IO modes
        self.pi.set_mode(self.DATA_COMMAND_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.RESET_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.READY_PIN, pigpio.INPUT)

        # put a pull down on the ready
        self.pi.set_pull_up_down(self.READY_PIN, pigpio.PUD_UP)

    def __reset_display(self):
        """Toggles the reset pin to reset the display."""
        self.pi.write(self.RESET_PIN, 0)
        time.sleep(self.RESET_DELAY)
        self.pi.write(self.RESET_PIN, 1)
        time.sleep(self.RESET_DELAY)

    def __power_up(self):
        """Performs the startup sequence as specified by the datasheet."""
        self.__write_command(Command.BOOSTER_SOFT_START)
        self.__write_command(Command.POWER_SETTING)
        self.__write_command(Command.POWER_ON)

        self.__wait_until_ready()

        self.__write_command(Command.PANEL_SETTING)

        self.__write_command(Command.PLL_CONTROL)
        self.__write_command(Command.RESOLUTION)
        self.__write_command(Command.VCM_DC)
        self.__write_command(Command.VCOM_DATA_INTERVAL)

    def __is_ready(self):
        """Verifies whether the display is ready (checks the ready pin).

        :returns: true if the ready pin is active LOW, false otherwise

        """
        return self.pi.read(self.READY_PIN) == 0

    def __wait_until_ready(self, use_timeout=True):
        """Waits until the display is ready, blinking the status LED during this period.

        :param bool use_timeout:
            True if the READY_WAIT_TIMEOUT should be used, otherwise
            the pi will wait indefinitely for the ready pin to go low
        :returns:
            whether True if the ready pin went low, false if the blinking timed out

        """
        timeout = self.READY_WAIT_TIMEOUT if use_timeout else None
        return self.led_status.blink_until(self.__is_ready, timeout=timeout)

    def __put_in_command_mode(self):
        """Puts the display into command mode such that info sent will be interpreted as commands."""
        self.pi.write(self.DATA_COMMAND_PIN, False)

    def __put_in_data_mode(self):
        """Puts the display into data mode such that info sent will be interpreted as data."""
        self.pi.write(self.DATA_COMMAND_PIN, True)

    def __write_command(self, command_structure):
        """Writes a command structure object to the display.

        :param CommandStructure command_structure:
            A CommandStructure object where to send the command and then the data to the display

        """
        # write the command to the display
        self.__put_in_command_mode()
        self.pi.spi_write(self.spi_handle, [command_structure.command])

        # write any data to the display
        self.__put_in_data_mode()
        self.pi.spi_write(self.spi_handle, command_structure.data)

    def transmit_data(self, data):
        """Transmits image data to the display and refreshes the display.

        :param bytearray data: an array of bytes to display on the ePaper display

        """
        self.__write_command(transmission_command(data))
        self.__write_command(Command.REFRESH)

        self.__wait_until_ready(use_timeout=False)

        self.led_status.update_flash_status(True)

if __name__ == '__main__':
    start_time = time.time()

    image_array = image_loader.image_cli()

    print("Download Elapsed: {}".format(time.time() - start_time))

    if image_array or image_array is None:
        start_time = time.time()

        led_status = LEDStatus()
        image = DisplayConversion(image_array)

        print("Conversion Elapsed: {}".format(time.time() - start_time))

        start_time = time.time()

        flasher = ImageFlasher(led_status)
        flasher.transmit_data(image.epaper_array)
        # TODO: integrate the display ID with the deck management

        elapsed = time.time() - start_time
        print("Flash Elapsed: {}".format(elapsed))

        if elapsed < 1:
            led_status.blink_for_time(5)

        led_status.turn_status_leds_off()
