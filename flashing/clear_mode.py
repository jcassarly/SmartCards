import pigpio
import time
from led_status import LEDStatus

class ClearMode():

    CLEAR_MODE_OUTPUT = 26 # gpio 26, pin 37
    CLEAR_MODE_INPUT = 13 # gpio 13, pin 33

    DEBOUNCE_DELAY = 0.05

    def __init__(self, led_status):
        self.led_status = led_status
        self.pi = pigpio.pi()
        self.pi.set_mode(self.CLEAR_MODE_OUTPUT, pigpio.OUTPUT)
        self.pi.set_pull_up_down(self.CLEAR_MODE_INPUT, pigpio.PUD_DOWN)

        self.pi.write(self.CLEAR_MODE_OUTPUT, True)

        self.__clear_mode = True
        self.__toggle_mode()

    def is_in_clear_mode(self):
        return self.__clear_mode # probably gonna need to make this object create a thread that monitors a button change

    def __toggle_mode(self):
        self.__clear_mode = not self.__clear_mode
        self.led_status.update_clear_mode_status(self.__clear_mode)

        time.sleep(self.DEBOUNCE_DELAY)

    def __read_mode(self):
        return self.pi.read(self.CLEAR_MODE_INPUT)

    def report_input_changes(self):
        previous = self.__read_mode()
        self.__clear_mode = previous
        while True:
            current = self.__read_mode()
            if not previous and current:
                self.__toggle_mode()
            else:
                pass # just leave mode as is because the button has not changed state

            previous = current

if __name__ == "__main__":
    led_status = LEDStatus()
    test = ClearMode(led_status)
    test.report_input_changes()
