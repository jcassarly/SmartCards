import pigpio
import time

class LEDStatus:
    RUNNING_LED_PIN = 23 # gpio 23, pin 16
    FLASH_STATUS_PIN = 24 # gpio 24, pin 18
    CLEAR_MODE_STATUS_PIN = 25 # gpio 25, pin 22
    DECK_EMPTY_STATUS_PIN = 12 # gpio 12, pin 32
    FLASH_ERROR_STATUS_PIN = 20 # gpio 20, pin 38

    BLINK_INTERVAL = 0.25 # sec

    def __init__(self):
        self.pi = pigpio.pi()
        self.__setup_status_pins()

    def __setup_status_pins(self):
        """Perform mode setup and defaults for status pins"""
        self.pi.set_mode(self.RUNNING_LED_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.FLASH_STATUS_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.CLEAR_MODE_STATUS_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.DECK_EMPTY_STATUS_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.FLASH_ERROR_STATUS_PIN, pigpio.OUTPUT)

        self.pi.write(self.RUNNING_LED_PIN, 1)
        self.update_flash_status(False)

    def blink_until(self, wait_function, timeout=None):
        """Blink the FLASH_STATUS_PIN until a timeout or wait_function returns true

        Blinks over BLINK_INTERVAL and constantly checks timeout if it is not none.
        if timeout is None, then blink_until will continue blinking until wait_function
        returns true

        When the function exits, the FLASH_STATUS_PIN is brought LOW

        :param function wait_function:
            a function that accepts no arguments and returns a boolean that is
            used to check whether the blinking should continue (True means
            stop binking, False means continue)
        :param float timeout:
            a positive time value that if the total time blinking surpasses,
            blink_until exits.
        :returns:
            True if blink_until exited because wait_function returned true, False
            if blink_until exited because it timed out

        """
        timer = time.time()
        next_end_cycle = timer + self.BLINK_INTERVAL
        blink_status = True

        while not wait_function():
            # check if there was a timeout
            if (timeout is not None and (time.time() - timer > timeout)):
                # turn off the status pin and exit False
                self.update_flash_status(False)
                return False

            # Check if the status pin needs to be toggled (a blink cycle has passed)
            if time.time() >= next_end_cycle:
                self.update_flash_status(blink_status)
                next_end_cycle = next_end_cycle + self.BLINK_INTERVAL
                blink_status = not blink_status

        # wait_function became true, so turn off the status pin and exit True
        self.update_flash_status(False)
        return True

    def blink_for_time(self, blink_time):
        """Blinks the FLASH_STATUS_PIN for blink_time

        :param float blink_time:
            a positive time value that if the total time blinking surpasses,
            blink_until exits.

        """
        self.blink_until(lambda: False, timeout=blink_time)

    def __update_status(self, pin, new_status):
        """Updates the status of `pin` to the HIGH or LOW value specified by new_status

        This function is mostly just here to make status update changes easier if the way
        in which that happens in the future should change to something more complicated
        than just writing a pin since this function currently just does what pigpio's
        write function does

        :param int pin: the GPIO pin number of the pin to update the status of
        :param bool new_status: True to set the pin to HIGH, False to set it to LOW

        """
        self.pi.write(pin, new_status)

    def update_flash_status(self, new_status):
        """Updates the FLASH_STATUS_PIN to the new_status value

        :param bool new_status: True to set the pin to HIGH, False to set it to LOW

        """
        self.__update_status(self.FLASH_STATUS_PIN, new_status)

    def update_clear_mode_status(self, new_status):
        """Updates the CLEAR_MODE_STATUS_PIN to the new_status value

        :param bool new_status: True to set the pin to HIGH, False to set it to LOW

        """
        self.__update_status(self.CLEAR_MODE_STATUS_PIN, new_status)

    def update_deck_empty_status(self, new_status):
        """Updates the DECK_EMPTY_STATUS_PIN to the new_status value

        :param bool new_status: True to set the pin to HIGH, False to set it to LOW

        """
        self.__update_status(self.DECK_EMPTY_STATUS_PIN, new_status)

    def update_flash_error_status(self, new_status):
        """Updates the FLASH_ERROR_STATUS_PIN to the new_status value

        :param bool new_status: True to set the pin to HIGH, False to set it to LOW

        """
        self.__update_status(self.FLASH_ERROR_STATUS_PIN, new_status)

    def turn_status_leds_off(self):
        """Writes a low value to all of the pins managed by LED_STATUS"""
        self.__update_status(self.RUNNING_LED_PIN, False)
        self.update_flash_status(False)
        self.update_clear_mode_status(False)
        self.update_deck_empty_status(False)
