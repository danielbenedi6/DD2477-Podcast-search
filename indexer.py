import csv
import json
from elasticsearch import Elasticsearch
from itertools import chain
import os
from getpass import getpass
import xml.etree.ElementTree as ET
from datetime import datetime

PATH='spotify-podcasts-2020/'
FILE=PATH+'metadata-summarization-testset.tsv'
TRANSCRIPTS=PATH+'podcasts-transcripts-summarization-testset'
RSS=PATH+'show-rss-summarization-testset'

AUTH=("elastic",'4a8d55e799c357eb')
FINGERPRINT='63fc9699288e16b67200a15ed474b8794b5ddab8'
CA_CERT=''

if AUTH[1] == "":
    print("Please set your elasticsearch credentials in the script or write it now.")
    password = getpass()
    AUTH = ("elastic", password)
    print("This won't change the script. To automate it, you can set it in the script.")

if CA_CERT == '' and FINGERPRINT == '':
    print("The SSL fingerprint or the CA certificate is missing.")
    print("Write 1 for writing the fingerprint or 2 for writing the path to the CA certificate.")
    choice = input()
    if choice == '1':
        print("Please enter the SSL fingerprint of the CA certificate:")
        FINGERPRINT = input()
    elif choice == '2':
        print("Please enter the path to the CA certificate:")
        CA_CERT = input()
    else:
        print("Wrong input. Exiting.")
        exit()
    print("This won't change the script. To automate it, you can set it in the script.")

if CA_CERT != '':
    es = Elasticsearch("https://localhost:9200",
                        basic_auth=AUTH,
                        ca_certs=CA_CERT
                        )
else:
    es = Elasticsearch("https://localhost:9200",
                        basic_auth=AUTH,
                        ssl_assert_fingerprint=FINGERPRINT
                        )

"""
print("Parsing metadata file...")
data = list(chain.from_iterable([[{"index": {"_index":"spotify-podcasts"}}, row] for row in csv.DictReader(open(FILE, newline=''),delimiter='\t')]))

print("Uploading metadata...")
res = es.bulk(operations=data,index='spotify-podcasts')
es.indices.refresh(index="spotify-podcasts")

print("Parsing and uploading transcripts...")
listOfFiles = list()
for (dirpath, dirnames, filenames) in os.walk(TRANSCRIPTS):
    listOfFiles += [os.path.join(dirpath, file) for file in filenames]

for file in listOfFiles:
    res = es.search(
            index='spotify-podcasts', 
            query = {
                "match": {
                    "episode_filename_prefix" : file.split('/')[-1].split('.')[0]
                }
            }
        )

    _id = res["hits"]["hits"][0]["_id"]
    transcript = json.load(open(file))
    text = ""
    words = []
    for chunk in transcript["results"]:
        data = chunk["alternatives"][0]
        if "transcript" in data and "words" in data:
            text += data["transcript"]
            words += data["words"]
    
    res = es.update(
            index = 'spotify-podcasts',
            id = _id,
            doc = {
                "transcript" : text,
                "words" : words,
                "raw" : transcript
            }
        )

"""

es.indices.refresh(index="spotify-podcasts")

print("Parsing and uploading rss...")
listOfFiles = list()
for (dirpath, dirnames, filenames) in os.walk(RSS):
    listOfFiles += [os.path.join(dirpath, file) for file in filenames]
ns = {"itunes":"http://www.itunes.com/dtds/podcast-1.0.dtd"}
for file in listOfFiles:
    try:
        data = ET.parse(file)
    except:
        print("Error parsing file: " + file)
        continue
    root = data.getroot()
    for episode in data.find('channel').findall('item'):
        title = episode.find('title').text

        res = es.search(
            index='spotify-podcasts',
            query={
                "match": {
                    "episode_name": title
                }
            }
        )

        if res["hits"]["total"]["value"] == 0 or title not in res["hits"]["hits"][0]["_source"]["episode_name"]:
            continue
        _id = res["hits"]["hits"][0]["_id"]

        try:
            pubDate = datetime.strptime(episode.find('pubDate').text, '%a, %d %b %Y %H:%M:%S %Z')
        except:
            try:
                pubDate = datetime.strptime(episode.find('pubDate').text, '%a, %d %b %Y %H:%M:%S %z')
            except:
                pubDate = episode.find('pubDate').text

        try:
            image = episode.find('itunes:image', ns).attrib['href']
        except:
            try:
                image = root.find('channel').find('itunes:image', ns).attrib['href']
            except:
                print("No image for " + file)
                image = ""
        try:
            enclosure = episode.find('enclosure').attrib['url']
        except:
            print("No enclosure for " + file)
            enclosure = ""

        res = es.update(
            index = 'spotify-podcasts',
            id = _id,
            doc = {
                "pubDate" : pubDate,
                "image" : image,
                "enclosure" : enclosure
            }
        )