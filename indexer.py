import csv
import json
from elasticsearch import Elasticsearch
from itertools import chain
import os
from getpass import getpass

PATH='spotify-podcasts-2020/'
FILE=PATH+'metadata-summarization-testset.tsv'
TRANSCRIPTS=PATH+'podcasts-transcripts-summarization-testset'

AUTH=("elastic",'')
FINGERPRINT=''
CA_CERT=''


if AUTH[1] == "":
    print("Please set your elasticsearch credentials in the script or write it now.")
    password = getpass()
    AUTH = ("elastic", password)
    print("This won't change the script. To automate it, you can set it in the script.")

if CA_CERT == '' or FINGERPRINT == '':
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

es.indices.refresh(index="spotify-podcasts")
