from imgurpython import ImgurClient
import json

def upload_image(image_path):
    client_id = 'ce4a29dd4114691'
    client_secret = '5e083857628ddd410bf287fa72cff65c37802a44'

    client = ImgurClient(client_id, client_secret)

    config = {
        'album': None,
        'name':  'a card',
        'title': 'a card',
        'description': 'a card'
    }

    image = client.upload_from_path(image_path, config=config, anon=True)

    return image['link']
