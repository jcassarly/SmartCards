import pigpio
import struct
import time
from conversion import DisplayConversion
import argparse
import image_loader

from command import *

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
    # W is 1 since device is not 3-wire
    # n is 1 since it is ignored to to W=0
    # T is 0 since the pi is little endian by default
    # R is 1 since the ADC is big endian TODO: update this comment and maybe change to 0
    # b is 0 since using 8-bit words

    # SPI setup options
    SPI_FLAGS   = 0x00000000
    SPI_BAUD    = 2000000
    SPI_CHANNEL = 0  # CE is on pin 8

    DATA_COMMAND_PIN = 4 # gpio 4, pin 7
    RESET_PIN = 5 # gpio 5, pin 29
    READY_PIN = 6 # gpio 6, pin 31

    AND1_PIN = 17 # gpio 17, pin 11
    AND2_PIN = 27 # gpio 27, pin 13
    AND3_PIN = 22 # gpio 22, pin 15
    AND_OUT_PIN = 18 # gpio 18, pin 12

    RESET_DELAY = 0.1 # sec

    READY_WAIT_TIMEOUT = 1 # sec

    MIN_ID = 1
    MAX_ID = 6
    ID_RANGE = range(MIN_ID, MAX_ID + 1)

    def __init__(self):
        self.pi = pigpio.pi()
        self.spi_handle = self.pi.spi_open(self.SPI_CHANNEL, self.SPI_BAUD, self.SPI_FLAGS)

        self.__setup_and_gate_pins()

        self.__setup_non_spi_pins()

        self.__reset_display()

        self.__power_up()

    def __setup_and_gate_pins(self):
        self.pi.set_mode(self.AND1_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND2_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND3_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND_OUT_PIN, pigpio.INPUT)

        self.pi.write(self.AND1_PIN, 0)
        self.pi.write(self.AND2_PIN, 0)
        self.pi.write(self.AND3_PIN, 0)

        self.pi.set_pull_up_down(self.AND_OUT_PIN, pigpio.PUD_DOWN)

    def __setup_non_spi_pins(self):
        # Set IO modes
        self.pi.set_mode(self.DATA_COMMAND_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.RESET_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.READY_PIN, pigpio.INPUT)

        # put a pull down on the ready
        self.pi.set_pull_up_down(self.READY_PIN, pigpio.PUD_UP)

    def __reset_display(self):
        self.pi.write(self.RESET_PIN, 0)
        time.sleep(self.RESET_DELAY)
        self.pi.write(self.RESET_PIN, 1)
        time.sleep(self.RESET_DELAY)

    def __power_up(self):
        self.__write_command(Command.BOOSTER_SOFT_START)
        self.__write_command(Command.POWER_SETTING)
        self.__write_command(Command.POWER_ON)

        self.__wait_until_ready()

        self.__write_command(Command.PANEL_SETTING)

        self.__write_command(Command.PLL_CONTROL)
        self.__write_command(Command.RESOLUTION)
        self.__write_command(Command.VCM_DC)
        self.__write_command(Command.VCOM_DATA_INTERVAL)

    def __ping_display(self, display_id):
        self.pi.write(self.AND1_PIN, (display_id & 0x01) > 0)
        self.pi.write(self.AND2_PIN, (display_id & 0x02) > 0)
        self.pi.write(self.AND3_PIN, (display_id & 0x04) > 0)

        return self.pi.read(self.AND_OUT_PIN) == 1

    def __wait_until_ready(self, use_timeout=True):
        timer = time.time()
        while (self.pi.read(self.READY_PIN) == 0):
            if (use_timeout and (time.time() - timer > self.READY_WAIT_TIMEOUT)):
                print("Timed out")
                return False

        return True

    def __put_in_command_mode(self):
        self.pi.write(self.DATA_COMMAND_PIN, False)

    def __put_in_data_mode(self):
        self.pi.write(self.DATA_COMMAND_PIN, True)

    def __write_command(self, command_structure):
        # write the command to the display
        self.__put_in_command_mode()
        self.pi.spi_write(self.spi_handle, [command_structure.command])

        # write any data to the display
        self.__put_in_data_mode()
        self.pi.spi_write(self.spi_handle, command_structure.data)

    def transmit_data(self, data):
        for display_id in self.ID_RANGE:
            if self.__ping_display(display_id):
                print("Flashing display {}".format(display_id))

                self.__write_command(transmission_command(data))
                self.__write_command(Command.REFRESH)

                self.__wait_until_ready(use_timeout=False)

                return display_id

        print("Could not find a valid ID [1-6] - not flashing")
        return False

if __name__ == '__main__':
    start_time = time.time()

    image_array = image_loader.image_cli()

    print("Download Elapsed: {}".format(time.time() - start_time))

    if image_array:
        start_time = time.time()

        image = DisplayConversion(image_array)

        print("Conversion Elapsed: {}".format(time.time() - start_time))

        start_time = time.time()

        flasher = ImageFlasher()
        display_id = flasher.transmit_data(image.epaper_array)
        # TODO: integrate the display ID with the deck management

        print("Flash Elapsed: {}".format(time.time() - start_time))
