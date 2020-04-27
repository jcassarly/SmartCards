import io
import re
import json
import time
import requests
import argparse
from PIL import Image

def __download_image(image_url):
    response = requests.get(image_url)

    valid_response = response is not None and response.content is not None

    return io.BytesIO(response.content) if valid_response else None

def download_photo(destination_path, photos_url):
    """
    destination path is the path to save the image
    photos_url is the url of the file to download
    """
    image = __download_image(photos_url)

    if image is not None:
        with open(destination_path, "wb") as out:
            out.write(image.getbuffer())
    else:
        print("Error Downloading {}".format(photos_url))

if __name__ == '__main__':
    start = time.time()
    download_photo("photo.jpg","https://www.complexsql.com/wp-content/uploads/2018/11/null.png")

    print("Elapsed = {}".format(time.time() - start))
