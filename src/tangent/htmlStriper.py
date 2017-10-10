'''
Name: Dallas Fraser
Email: d6fraser@uwaterloo.ca
Date: 2017-07-27
Project: Tangent GT
Purpose: Used to strip html for parsing documents
'''
import unittest
import re
from bs4 import BeautifulSoup
from html.parser import HTMLParser


class MLStripper(HTMLParser):
    def __init__(self):
        super(HTMLParser, self).__init__()
        self.reset()
        self.strict = False
        self.convert_charrefs = True
        self.fed = []

    def handle_data(self, d):
        self.fed.append(d)

    def get_data(self):
        return ' '.join(self.fed)


def strip_tags(html):
    """Returns a string stripped of all html tags
    """
    parser = HTMLParser()
    html = parser.unescape(html)
    s = MLStripper()
    s.feed(html)
    return s.get_data()


if __name__ == "__main__":
    unittest.main()
