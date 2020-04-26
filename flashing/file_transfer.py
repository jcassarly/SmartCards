import json

from DeckManager import add_path, remove_path

class FileTransferDict():
    def __init__(self):
        self.dict = {}

    def add(self, image_url, image_path):
        self.dict.update({image_url: image_path})

    def __iter__(self):
        for image_url in self.dict:
            yield image_url, self.dict[image_url]

    def save_file_transfer(self, file_path):
        with open(file_path, 'w') as output:

            for image_url in self.dict:
                self.dict[image_url] = remove_path(self.dict[image_url])

            output.write(json.dumps(self.dict))

    def load_file_transfer(self, file_path):
        with open(file_path, 'r') as input_file:
            data = json.load(input_file)

            self.dict = data

            for image_url in self.dict:
                self.dict[image_url] = add_path(self.dict[image_url])

