from PIL import Image

class DisplayConversion:

    WHITE_PIXEL = 0x00  # value of white pixel in image epaper data

    DISPLAY_WIDTH_PIXELS = 300
    DISPLAY_LENGTH_PIXELS = 400

    BITS_IN_BYTE = 8
    MAX_BIT_SHIFT = BITS_IN_BYTE - 1

    ROTATE_PORTRAIT_IMAGE = 270
    ROTATE_LANDSCAPE_IMAGE = 180

    BLACK_AND_WHITE_IMAGE = '1'
    BLANK_PIXEL = 1  # value of white pixel in blank and white PIL image object

    def __bmp_to_epaper(self):
        """Converts self.image object into an array in ePaper Display format

        :returns: an array of bytes that can be flashed to an ePaper Display

        """
        output_arr = []

        # bmp_to_epaper.exe source was used as a reference for this conversion
        # iterate over the image to pull out each byte worth of data from the bmp
        # (8 pixels in the bmp = 1 byte of epaper data from the bmp)
        for row in range(0, self.DISPLAY_WIDTH_PIXELS):
            for col in range(0, self.DISPLAY_LENGTH_PIXELS, self.BITS_IN_BYTE):
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
        for bit_number in range(0, self.BITS_IN_BYTE):
            # if the pixel is white, write a 1 to the current bit
            if self.image.getpixel((bit_number + start_col, row)) == self.WHITE_PIXEL:
                next_byte = next_byte | (1 << (self.MAX_BIT_SHIFT - bit_number))

        return next_byte

    def __convert_image(self, input_image):
        """Converts the given image to a correctly oriented black and white Image object.

        The converted image is placed in self.image

        :param str input_image:
            the path or file object for the image to convert (None for a blank image)

        """
        if input_image is not None:
            self.image = Image.open(input_image)
        else:
            # when the input image is none, flash a blank image
            self.image = Image.new(self.BLACK_AND_WHITE_IMAGE,
                                   (self.DISPLAY_LENGTH_PIXELS, self.DISPLAY_WIDTH_PIXELS),
                                   self.BLANK_PIXEL)

        # rotate the image such that it appears best on the display
        rotation = self.ROTATE_PORTRAIT_IMAGE
        if self.image.width > self.image.height:
            rotation = self.ROTATE_LANDSCAPE_IMAGE

        self.image = self.image.rotate(rotation, expand=True)

        # reverse the image for endianness of the ePaper format
        self.image = self.image.transpose(Image.FLIP_TOP_BOTTOM)

        # change the image size to fit exactly on the display
        self.image = self.image.resize((self.DISPLAY_LENGTH_PIXELS, self.DISPLAY_WIDTH_PIXELS))

        # monochrome the image
        self.image = self.image.convert(self.BLACK_AND_WHITE_IMAGE)

    def __init__(self, input_image):
        """Initializes the DisplayConversion and puts the converted array into epaper_array

        :param str input_image:
            the path or file object for the image to convert (None for a blank image)

        """
        self.__convert_image(input_image)

        self.epaper_array = self.__bmp_to_epaper()


