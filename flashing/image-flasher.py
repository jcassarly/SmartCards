import pigpio
import struct
import time
import image_array

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
    # W is 0 since device is not 3-wire
    # n is 0 since it is ignored to to W=0
    # T is 0 since the pi is little endian by default
    # R is 1 since the ADC is big endian
    # b is 0 since using 8-bit words

    # SPI setup options
    SPI_FLAGS   = 0x00008000
    SPI_BAUD    = 2000000
    SPI_CHANNEL = 0  # CE is on pin 8

    DATA_COMMAND_PIN = 4 # gpio 4, pin 7
    RESET_PIN = 5 # gpio 5, pin 29
    READY_PIN = 6 # gpio 6, pin 31

    RESET_DELAY = 0.2 # sec

    READY_WAIT_TIMEOUT = 1 # sec

    def __init__(self):
        self.pi = pigpio.pi()
        self.spi_handle = self.pi.spi_open(self.SPI_CHANNEL, self.SPI_BAUD, self.SPI_FLAGS)

        self.__setup_non_spi_pins()

        self.__reset_display()

        self.__power_up()

    def __setup_non_spi_pins(self):
        # Set IO modes
        self.pi.set_mode(self.DATA_COMMAND_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.RESET_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.READY_PIN, pigpio.INPUT)

        # put a pull down on the ready
        self.pi.set_pull_up_down(self.READY_PIN, pigpio.PUD_UP) # TODO: verify correct pull up/down

    def __reset_display(self):
        print("resetting display")
        self.pi.write(self.RESET_PIN, 0)
        time.sleep(self.RESET_DELAY)
        self.pi.write(self.RESET_PIN, 1)
        time.sleep(self.RESET_DELAY)

    def __power_up(self):
        self.__write_command(Command.POWER_SETTING)
        self.__write_command(Command.BOOSTER_SOFT_START)
        self.__write_command(Command.POWER_ON)

        self.__wait_until_ready()

        self.__write_command(Command.PANEL_SETTING)

        self.__write_command(Command.PLL_CONTROL)
        self.__write_command(Command.RESOLUTION)
        self.__write_command(Command.VCM_DC)
        self.__write_command(Command.VCOM_DATA_INTERVAL)

    def __wait_until_ready(self):
        timer = time.time()
        while (self.pi.read(self.READY_PIN) == 0):
            if (time.time() - timer > self.READY_WAIT_TIMEOUT):
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
        print("writing: {}".format(command_structure.command))

        # write any data to the display
        #for data_byte in command_structure.data:
        self.__put_in_data_mode()
        self.pi.spi_write(self.spi_handle, command_structure.data) # TODO: may need to make CS go high after each byte

    def transmit_data(self, data):
        self.__write_command(transmission_command(data))
        #self.__write_command(Command.DATA_STOP)
        self.__write_command(Command.REFRESH)

flasher = ImageFlasher()

flasher.transmit_data(image_array.arr)
