import numpy as np

# class TestClass():
#     def __init__(self, num):
#         self.num = num

def getLove():
    return "Python: I love you {} much.\nnumpy version: {}".format(
        np.random.randint(10, size=5),
        np.__version__)