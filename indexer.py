import csv
from elasticsearch import Elasticsearch
from itertools import chain

FILE='podcasts-no-audio-13GB/spotify-podcasts-2020/metadata-summarization-testset.tsv'
AUTH=("elastic",'')
FINGERPRINT=''

es = Elasticsearch("https://localhost:9200", 
                    basic_auth=AUTH,
                    ssl_assert_fingerprint=FINGERPRINT
                    )


print("Parsing file...")
data = list(chain.from_iterable([[{"index": {"_index":"spotify-podcasts"}}, row] for row in csv.DictReader(open(FILE, newline=''),delimiter='\t')]))

print("Uploading...")
try:
    res = es.bulk(operations=data,index='spotify-podcasts')
except:
    print(data)


