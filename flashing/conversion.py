from PIL import Image

class DisplayConversion:

    WHITE_PIXEL = 0x00

    def __bmp_to_epaper(self):
        """Converts self.image object into an array in ePaper Display format

        :returns: an array of bytes that can be flashed to an ePaper Display

        """
        output_arr = []

        # bmp_to_epaper.exe source was used as a reference for this conversion
        # iterate over the image to pull out each byte worth of data from the bmp
        # (8 pixels in the bmp = 1 byte of epaper data from the bmp)
        for row in range(0, 300):
            for col in range(0, 400, 8):
                output_arr.append(self.__next_epaper_byte(row, col))

        return output_arr

    def __next_epaper_byte(self, row, start_col):
        """Extracts the next 8 pixels from the image and packs them into a byte.

        :param int row: the row in self.image to start the extraction at
        :param int start_col: the column in self.image to start the extraction from
        :returns: the next 8 pixels packed into a byte

        """
        next_byte = 0
        # iterate over the next 8 pixels to extract create a byte for epaper data
        for bit_number in range(0, 8):
            # if the pixel is white, write a 1 to the current bit
            if self.image.getpixel((bit_number + start_col, row)) == self.WHITE_PIXEL:
                next_byte = next_byte | (1 << (7 - bit_number))

        return next_byte

    def __convert_image(self, input_image_path):
        """Converts the image at the given path to a correctly oriented black and white Image object.

        The converted image is in self.image

        :param str input_image_path: the path to the image to convert

        """
        self.image = Image.open(input_image_path)

        # rotate the image such that it appears best on the display
        rotation = 270 if self.image.width <= self.image.height else 180
        self.image = self.image.rotate(rotation, expand=True)

        # reverse the image for endianness of the ePaper format
        self.image = self.image.transpose(Image.FLIP_TOP_BOTTOM)

        # change the image size to fit exactly on the display
        self.image = self.image.resize((400, 300))

        # monochrome the image
        self.image = self.image.convert('1')

    def __init__(self, input_image_path):
        """Initializes the DisplayConversion and puts the converted array into epaper_array

        :param str input_image_path: the path to the image to convert

        """
        self.__convert_image(input_image_path)

        self.epaper_array = self.__bmp_to_epaper()


