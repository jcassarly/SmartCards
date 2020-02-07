import ijson
import json
import requests
import argparse

def download_card_image_greedy(cardname, output_path):
    found_card = False
    with open('scryfall-default-cards.json', 'r') as inp:
        for prefix, event, value in ijson.parse(inp):
            if prefix.endswith('.name') and value == cardname:
                found_card = True

            if found_card and prefix.endswith(".image_uris.border_crop"):
                print('Found {}'.format(cardname))

                return download_image(value, output_path)

    print("Failed to find {}".format(cardname))
    return False

def download_card_image(cardname, output_path):
    data = []
    with open('scryfall-default-cards.json', 'r') as inp:
        data = json.load(inp)

    for item in data:
        if item['name'] == cardname:
            print('Found {}'.format(cardname))

            return download_image(item['image_uris']['border_crop'], output_path)

    print("Failed to find {}".format(cardname))
    return False

def download_image(image_url, output_path):
    response = requests.get(image_url)

    with open(output_path, 'wb') as out:
        out.write(response.content)

    return response.content is not None

def cli_download(output_path):
    parser = argparse.ArgumentParser("Pull down a card image and convert it to the proper format and put it in the image processor")

    parser.add_argument("--cardname", nargs=1, type=str, help="The name of the card to download")
    parser.add_argument("--url", nargs=1, type=str, help="link to download an image from")

    args = parser.parse_args()

    downloaded = True
    if args.cardname is not None and args.url is None:
        print("Finding Magic Card")
        downloaded = download_card_image_greedy(args.cardname[0], output_path)
    elif args.cardname is None and args.url is not None:
        print("Downloading URL")
        downloaded = download_image(args.url[0], output_path)
    elif args.cardname is None and args.url is None:
        print("No arguments, so no changes to {}".format(output_path))
    else:
        downloaded = False
        print("Cannot use cardname and url simultaneously")

    return downloaded

if __name__ == '__main__':
    args = cli_download("img.jpg")
