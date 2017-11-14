'''
@author: Dallas Fraser
@email: d6fraser@uwaterloo.ca
'''
from distutils.core import setup

setup(
    name='Tangent',
    version='0.1.0',
    author="Dallas Fraser",
    author_email="tangent",
    packages=['beautifulsoup4'],
    license='Apache License, Version 2.0',
    description="Creates math tuples",
    long_description=open('README.txt').read(),
    install_requires=["beautifulsoup4>=4.6.0"],
)
