# DD2477-Podcast-search
Group project for DD2477. We will develop a podcast searcher using elasticsearch

# Running the enviroment

## Setting up Elasticsearch and Kibana

There is a script for Linux for running Elasticsearch with Kibana for the first time, it should be run with superuser permissions.
```
 sudo ./setup.sh
```
This script will set up everything as in a real environment with SSL security, 3 replications of Elasticsearch and 1 interface with Kibana. It is needed to run with sudo because it will change the system variable `vm.max_map_count` because it is needed to have at least 262144KiB. The user and password will be prompted in console as well as the URLs for accessing the services. Also, it will be saved in a file called `.env`.
All the other times, it is enough if running with:
```
sudo sysctl -w vm.max_map_count=262144
docker-compose up -d
```

## Indexing

For indexing, python3 is needed:
```
python3 indexing.py
```
It will ask for username and password in Elasticsearch as well as the fingerprint or the certificate of the CA. The fingerprint was showed in the previous step.

## Running the search engine

It is explained in detail in the [README in the subfolder podcasts-searcher](podcast-searcher/README.md)
