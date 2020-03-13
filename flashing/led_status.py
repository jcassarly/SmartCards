import pigpio
import time

class LEDStatus:
    RUNNING_LED_PIN = 23 # gpio 23, pin 16
    FLASH_STATUS_PIN = 24 # gpio 24, pin 18

    BLINK_INTERVAL = 0.25 # sec

    def __init__(self):
        self.pi = pigpio.pi()
        self.__setup_status_pins()

    def __setup_status_pins(self):
        self.pi.set_mode(self.RUNNING_LED_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.FLASH_STATUS_PIN, pigpio.OUTPUT)

        self.pi.write(self.RUNNING_LED_PIN, 1)
        self.update_flash_status(False)

    def blink_until(self, wait_function, timeout=None):
        timer = time.time()
        next_end_cycle = timer + self.BLINK_INTERVAL
        blink_status = True
        while not wait_function():
            if (timeout is not None and (time.time() - timer > timeout)):
                print("Timed out")
                return False

            if time.time() >= next_end_cycle:
                self.update_flash_status(blink_status)
                next_end_cycle = next_end_cycle + self.BLINK_INTERVAL
                blink_status = not blink_status

        return True

    def blink_for_time(self, blink_time):
        self.blink_until(lambda: False, timeout=blink_time)

    def update_flash_status(self, new_status):
        self.pi.write(self.FLASH_STATUS_PIN, new_status)
