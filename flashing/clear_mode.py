import pigpio
import time
from led_status import LEDStatus

class ClearMode():
    '''Class to handle monitoring the Clear Mode button and update the LED with the status'''

    CLEAR_MODE_OUTPUT = 26 # gpio 26, pin 37
    CLEAR_MODE_INPUT = 13 # gpio 13, pin 33

    DEBOUNCE_DELAY = 0.05

    def __init__(self, led_status):
        self.led_status = led_status

        # Set up the input pin
        self.pi = pigpio.pi()
        self.pi.set_pull_up_down(self.CLEAR_MODE_INPUT, pigpio.PUD_DOWN)

        # set up the output pin that just stays high to set the button correctly
        self.pi.set_mode(self.CLEAR_MODE_OUTPUT, pigpio.OUTPUT)
        self.pi.write(self.CLEAR_MODE_OUTPUT, True)

        # Need to set clear mode to false and have that shown on the LED
        self.__clear_mode = True
        self.__toggle_mode()

    def is_in_clear_mode(self):
        '''Checks whether the Pi is in Clear Mode

        :returns: true if the clear mode has been set by the input button, false otherwise

        '''
        return self.__clear_mode # probably gonna need to make this object create a thread that monitors a button change

    def __toggle_mode(self):
        '''Toggles the clear mode status and waits for the button to debounce

        Note that this toggle is updated on the status LED

        '''
        self.__clear_mode = not self.__clear_mode
        self.led_status.update_clear_mode_status(self.__clear_mode)

        time.sleep(self.DEBOUNCE_DELAY)

    def __read_mode(self):
        '''Reads the input pin and returns the value

        :returns: true if the input pin is high, false otherwise

        '''
        return self.pi.read(self.CLEAR_MODE_INPUT)

    def report_input_changes(self):
        '''Loop that monitors the input pin for rising edges and toggles clear mode on these edges'''
        previous = self.__read_mode()
        self.__clear_mode = previous

        # begin the monitoring process
        while True:
            current = self.__read_mode()

            # if rising edge
            if not previous and current:
                self.__toggle_mode()
            else:
                pass # just leave mode as is because the button has not changed state

            previous = current

if __name__ == "__main__":
    led_status = LEDStatus()
    test = ClearMode(led_status)
    test.report_input_changes()
