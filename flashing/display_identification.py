import pigpio

class DisplayIdentification:
    AND1_PIN = 17 # gpio 17, pin 11
    AND2_PIN = 27 # gpio 27, pin 13
    AND3_PIN = 22 # gpio 22, pin 15
    AND_OUT_PIN = 18 # gpio 18, pin 12

    MIN_ID = 1
    MAX_ID = 6
    ID_RANGE = range(MIN_ID, MAX_ID + 1)

    NO_DISPLAY = 0

    def __init__(self):
        self.pi = pigpio.pi()
        self.__setup_and_gate_pins()

    def __setup_and_gate_pins(self):
        self.pi.set_mode(self.AND1_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND2_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND3_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND_OUT_PIN, pigpio.INPUT)

        self.pi.write(self.AND1_PIN, 0)
        self.pi.write(self.AND2_PIN, 0)
        self.pi.write(self.AND3_PIN, 0)

        self.pi.set_pull_up_down(self.AND_OUT_PIN, pigpio.PUD_DOWN)

    def ping_display(self, display_id):
        self.pi.write(self.AND1_PIN, (display_id & 0x01) > 0)
        self.pi.write(self.AND2_PIN, (display_id & 0x02) > 0)
        self.pi.write(self.AND3_PIN, (display_id & 0x04) > 0)

        return self.pi.read(self.AND_OUT_PIN) == 1

    def find_display(self):
        for display_id in self.ID_RANGE:
            if self.__ping_display(display_id):
                print("Found display {}".format(display_id))

                return display_id

        print("Could not find a valid ID [1-6] - not flashing")
        return self.NO_DISPLAY
