import pigpio

class DisplayIdentification:
    '''Class to handle identifying displays based on their IDing CMOS AND gate logic circuit'''
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
        '''Set up the pins for the CMOS AND Gate Logic circuit for display IDs'''
        # Set the modes on the pins
        self.pi.set_mode(self.AND1_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND2_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND3_PIN, pigpio.OUTPUT)
        self.pi.set_mode(self.AND_OUT_PIN, pigpio.INPUT)

        # tie all the outputs to low by default
        self.pi.write(self.AND1_PIN, 0)
        self.pi.write(self.AND2_PIN, 0)
        self.pi.write(self.AND3_PIN, 0)

        self.pi.set_pull_up_down(self.AND_OUT_PIN, pigpio.PUD_DOWN)

    def ping_display(self, display_id):
        '''Test whether the three least significant bits of display ID yield HIGH on the AND gate output

        :param int display_id: the ID to ping the ID circuit with
        :returns: true if the output is high, false otherwise

        '''
        self.pi.write(self.AND1_PIN, (display_id & 0x01) > 0)
        self.pi.write(self.AND2_PIN, (display_id & 0x02) > 0)
        self.pi.write(self.AND3_PIN, (display_id & 0x04) > 0)

        return self.pi.read(self.AND_OUT_PIN) == 1

    def find_display(self):
        '''Ping each ID in ID_RANGE and return the first one that returns True

        :returns: the ID of the display found after pinging (NO_DISPLAY if all pings returned false)

        '''
        for display_id in self.ID_RANGE:
            if self.ping_display(display_id):
                return display_id

        return self.NO_DISPLAY
