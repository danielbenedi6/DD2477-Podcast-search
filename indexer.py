import csv
import json
from elasticsearch import Elasticsearch
from itertools import chain
import os

PATH='podcasts-no-audio-13GB/spotify-podcasts-2020/'
FILE=PATH+'metadata-summarization-testset.tsv'
TRANSCRIPTS=PATH+'podcasts-transcripts-summarization-testset'

AUTH=("elastic",'')
FINGERPRINT=''

es = Elasticsearch("https://localhost:9200", 
                    basic_auth=AUTH,
                    ssl_assert_fingerprint=FINGERPRINT
                    )

"""
print("Parsing file...")
data = list(chain.from_iterable([[{"index": {"_index":"spotify-podcasts"}}, row] for row in csv.DictReader(open(FILE, newline=''),delimiter='\t')]))

print("Uploading...")
try:
    res = es.bulk(operations=data,index='spotify-podcasts')
except:
    print(data)

"""
es.indices.refresh(index="spotify-podcasts")

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
