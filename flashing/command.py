class CommandStructure:
    def __init__(self, command, data):
        self.command = command
        self.data = data

class Command:
    PANEL_SETTING = CommandStructure(0x00, [0x1B])
    POWER_SETTING = CommandStructure(0x01, [0x03, 0x00, 0x2B, 0x2B]) # TODO: only datasheet as the last byte (0x09)
    POWER_ON = CommandStructure(0x04, [])
    BOOSTER_SOFT_START = CommandStructure(0x06, [0x17, 0x17, 0x17])
    DATA_STOP = CommandStructure(0x11, [])
    REFRESH = CommandStructure(0x12, [])
    PLL_CONTROL = CommandStructure(0x30, [0x3a])
    VCOM_DATA_INTERVAL = CommandStructure(0x50, [0x07])
    RESOLUTION = CommandStructure(0x61, [0x01, 0x90, 0x01, 0x2c])
    VCM_DC = CommandStructure(0x82, [0x12])

def transmission_command(data):
    return CommandStructure(0x13, data)
