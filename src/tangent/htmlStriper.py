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


def strip_tags(html):
    """Returns a string stripped of all html tags
    """
    words = re.sub(r'<.*?>', ' ', html)
    words = words.replace("  ", " ")
    return BeautifulSoup(words).text



if __name__ == "__main__":
    unittest.main()
