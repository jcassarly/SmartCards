class CommandStructure:
    """Class to represent a Command for the ePaper Display"""
    def __init__(self, command, data):
        self.command = command
        self.data = data

class Command:
    """Definitions for specific command on  the ePaper Display"""
    PANEL_SETTING = CommandStructure(0x00, [0x1B])
    POWER_SETTING = CommandStructure(0x01, [0x03, 0x00, 0x2B, 0x2B])
    POWER_ON = CommandStructure(0x04, [])
    BOOSTER_SOFT_START = CommandStructure(0x06, [0x17, 0x17, 0x17])
    DATA_STOP = CommandStructure(0x11, [])
    REFRESH = CommandStructure(0x12, [])
    PLL_CONTROL = CommandStructure(0x30, [0x3a])
    VCOM_DATA_INTERVAL = CommandStructure(0x50, [0x07])
    RESOLUTION = CommandStructure(0x61, [0x01, 0x90, 0x01, 0x2c])
    VCM_DC = CommandStructure(0x82, [0x12])

TRANSMIT_COMMAND_OP_CODE = 0x13

def transmission_command(data):
    """Generates a command for transmitting data to the ePaper display using data

    :param bytearray data: a bytearray with the data to transmit to the display

    """
    return CommandStructure(TRANSMIT_COMMAND_OP_CODE, data)
