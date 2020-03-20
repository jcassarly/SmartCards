import ijson
import io
import json
import requests
import argparse
from PIL import Image

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
    args = image_cli()
