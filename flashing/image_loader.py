import ijson
import io
import re
import json
import time
import requests
import argparse
from PIL import Image

"""
This file was pretty much just for early testing and playing around with
the scryfall database to flash magic cards, so I'm not going to comment this
as it is not necessary int he final version.  It is not used in flash_loop
"""

CARD_DATABASE = 'scryfall-default-cards.json'

def __download_card_image_greedy(cardname):
    found_card = False
    with open(CARD_DATABASE, 'r') as inp:
        for prefix, event, value in ijson.parse(inp):
            if prefix.endswith('.name') and value == cardname:
                found_card = True

            if found_card and prefix.endswith(".image_uris.border_crop"):
                print('Found {}'.format(cardname))

                return __download_image(value)

    print("Failed to find {}".format(cardname))
    return None

def __download_card_image(cardname):
    data = []
    with open(CARD_DATABASE, 'r') as inp:
        data = json.load(inp)

    for item in data:
        if item['name'] == cardname:
            print('Found {}'.format(cardname))

            return __download_image(item['image_uris']['border_crop'])

    print("Failed to find {}".format(cardname))
    return None

def __download_image(image_url):
    response = requests.get(image_url)

    valid_response = response is not None and response.content is not None

    return io.BytesIO(response.content) if valid_response else None

def download_photo(destination_path, photos_url):
    """
    destination path is the path to save the image
    photos_url is the url of the file to download
    """
    response = requests.get(photos_url)

    match = re.search("content=\"(https://lh3.googleusercontent.com[^\"]*)=w600-h315-p-k\"", response.text)
    image_url = "{}{}".format(match.group(1), "=w1355-h1805-no")

    time.sleep(0.5)

    response = requests.get(image_url)

    with open(destination_path, "wb") as out:
        out.write(response.content)

def image_cli():
    parser = argparse.ArgumentParser("Pull down a card image and convert it to the proper format and put it in the image processor")

    card_choices = ['card', 'cardname', 'mtg','c']
    url_choices = ['url', 'u']
    file_choices = ['file', 'filepath', 'f']
    blank_choices = ['blank', 'b']
    choices = card_choices + url_choices + file_choices + blank_choices

    parser.add_argument("source", choices=choices, help="Choose from card, url, or file as the source to download from")
    parser.add_argument("source_arg", type=str, help="The data for the source type (Magic: The Gathering card name for card, image url for url, filepath for file)")

    args = parser.parse_args()

    image_array = None
    if args.source in card_choices:
        print("Finding Magic Card")
        image_array = __download_card_image_greedy(args.source_arg)
    elif args.source in url_choices:
        print("Downloading URL")
        image_array = __download_image(args.source_arg)
    elif args.source in file_choices:
        print("Reading Image file")
        image_array = args.source_arg
    elif args.source in blank_choices:
        print("Clearing Card")
        image_array = None  # None yields a blank image
    else:
        print("Error: did not receive a valid choice")


    return image_array

if __name__ == '__main__':
    #args = image_cli()
    start = time.time()
    download_photo("photo.jpg","https://photos.app.goo.gl/TjHt5YB4ZsdMkNnu6")

    print("Elapsed = {}".format(time.time() - start))
