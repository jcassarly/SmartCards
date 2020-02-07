import ijson
import io
import json
import requests
import argparse

def __download_card_image_greedy(cardname):
    found_card = False
    with open('scryfall-default-cards.json', 'r') as inp:
        for prefix, event, value in ijson.parse(inp):
            if prefix.endswith('.name') and value == cardname:
                found_card = True

            if found_card and prefix.endswith(".image_uris.border_crop"):
                print('Found {}'.format(cardname))

                return __download_image(value)

    print("Failed to find {}".format(cardname))
    return False

def __download_card_image(cardname):
    data = []
    with open('scryfall-default-cards.json', 'r') as inp:
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

def __load_image(image_path):
    image_content = bytearray()
    with open(image_path, 'rb') as inp:
        while True:
            image_byte = inp.read(1)

            if not image_byte:
                image_content.append(image_byte)
            else:
                break

    return io.BytesIO(image_content) if image_content != [] else None

def image_cli():
    parser = argparse.ArgumentParser("Pull down a card image and convert it to the proper format and put it in the image processor")

    card_choices = ['card', 'cardname', 'mtg','c']
    url_choices = ['url', 'u']
    file_choices = ['file', 'filepath', 'f']
    choices = card_choices + url_choices + file_choices

    parser.add_argument("source", choices=choices, help="Choose from card, url, or file as the source to download from")
    parser.add_argument("source_arg", type=str, help="The data for the source type (Magic: The Gathering card name for card, image url for url, filepath for file)")

    args = parser.parse_args()

    image_array = None
    if args.source in card_choices:
        print("Finding Magic Card")
        image_array = __download_card_image_greedy(args.source_arg)
        print(image_array)
    elif args.source in url_choices:
        print("Downloading URL")
        image_array = __download_image(args.source_arg)
        print(image_array)
    elif args.source in file_choices:
        print("Reading Image file")
        image_array = args.source_arg
    else:
        print("Error: did not receive a valid choice")


    return image_array

if __name__ == '__main__':
    args = cli_download("img.jpg")
