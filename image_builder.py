import os
import subprocess
from PIL import Image, ImageFilter, BmpImagePlugin

import json
import requests

import argparse

def bmp_to_epaper(filename):
    subprocess.check_call(["cmd.exe", "/C", "bmp_to_epaper.exe", "{}.bmp".format(filename), "1", "0", "0"])

    lines = []
    with open('{}.h'.format(filename), 'r') as header:
        lines = header.readlines()


    with open(os.path.join("flashing", "image_array.py"), "w") as output:
        output.write("arr = [\n")
        for line in lines[19::]:
            output.write("    {}".format(line.replace(" ", "").replace("{", "").replace("};", "")))
        output.write("]\n")


def black_and_white(input_image_path,
    output_image_path):
    card = Image.open(input_image_path)
    #card = card.filter(ImageFilter.MinFilter(size=4))
    #card = card.filter(ImageFilter.SHARPEN)
    #card = card.filter(ImageFilter.EDGE_ENHANCE)

    card = card.rotate(270, expand=True)
    card = card.transpose(Image.FLIP_TOP_BOTTOM)
    card = card.resize((400, 300))
    #card = card.resize((300, 400))

    card = card.convert('1')
    card = card.convert('RGB')
    card.save(output_image_path, format="bmp")

    #bmpfile = BmpImagePlugin.BmpImageFile(output_image_path)
    #bmpfile.

def download_card_image(cardname):
    data = []
    with open('scryfall-default-cards.json', 'r') as inp:
        data = json.load(inp)

    for item in data:
        if item['name'] == cardname:
            print('Found {}'.format(cardname))

            response = requests.get(item['image_uris']['border_crop'])

            with open('img.jpg', 'wb') as out:
                out.write(response.content)

            return True

    print("Failed to find {}".format(cardname))
    return False

if __name__ == '__main__':
    parser = argparse.ArgumentParser("Pull down a card image and convert it to the proper format and put it in the image processor")

    parser.add_argument("cardname", help="The name of the card to download")

    args = parser.parse_args()

    #print (args.cardname)

    if download_card_image(args.cardname):

        black_and_white('img.jpg', 'bw_out.bmp')

        bmp_to_epaper('bw_out')


