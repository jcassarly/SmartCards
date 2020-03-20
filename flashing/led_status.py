import pigpio
import time

class LEDStatus:
    RUNNING_LED_PIN = 23 # gpio 23, pin 16
    FLASH_STATUS_PIN = 24 # gpio 24, pin 18
    CLEAR_MODE_STATUS_PIN = 25 # gpio 25, pin 22
    DECK_EMPTY_STATUS_PIN = 12 # gpio 12, pin 32

    BLINK_INTERVAL = 0.25 # sec

    def __init__(self):
        self.pi = pigpio.pi()
        self.__setup_status_pins()

    def __setup_status_pins(self):
        self.pi.set_mode(self.RUNNING_LED_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.FLASH_STATUS_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.CLEAR_MODE_STATUS_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.DECK_EMPTY_STATUS_PIN, pigpio.OUTPUT)

        self.pi.write(self.RUNNING_LED_PIN, 1)
        self.update_flash_status(False)

    def blink_until(self, wait_function, timeout=None):
        timer = time.time()
        next_end_cycle = timer + self.BLINK_INTERVAL
        blink_status = True
        while not wait_function():
            if (timeout is not None and (time.time() - timer > timeout)):
                print("Timed out")
                self.update_flash_status(False)
                return False

            if time.time() >= next_end_cycle:
                self.update_flash_status(blink_status)
                next_end_cycle = next_end_cycle + self.BLINK_INTERVAL
                blink_status = not blink_status

        self.update_flash_status(False)
        return True

    def blink_for_time(self, blink_time):
        self.blink_until(lambda: False, timeout=blink_time)

    def __update_status(self, pin, new_status):
        self.pi.write(pin, new_status)

    def update_flash_status(self, new_status):
        self.__update_status(self.FLASH_STATUS_PIN, new_status)

    def update_clear_mode_status(self, new_status):
        self.__update_status(self.CLEAR_MODE_STATUS_PIN, new_status)

    def update_deck_empty_status(self, new_status):
        self.__update_status(self.DECK_EMPTY_STATUS_PIN, new_status)

    def turn_status_leds_off(self):
        self.pi.write(self.RUNNING_LED_PIN, False)
        self.update_flash_status(False)
        self.update_clear_mode_status(False)
        self.update_deck_empty_status(False)
