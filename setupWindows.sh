set BLUE='\033[0;36m'
set MAGENTA='\033[0;35m'
set NC='\033[0m'

echo -n "Setting up variables     ... "
FOR /F "tokens=*" %g IN ('openssl rand -base64 16') do (SET PASS1=%g)
FOR /F "tokens=*" %g IN ('openssl rand -base64 16') do (SET PASS2=%g)

echo -e "# Password for the 'elastic' user (at least 6 characters)
ELASTIC_PASSWORD=%PASS1%

# Password for the 'kibana_system' user (at least 6 characters)
KIBANA_PASSWORD=%PASS2%

# Version of Elastic products
STACK_VERSION=8.1.1

# Set the cluster name
CLUSTER_NAME=docker-cluster

# Set to 'basic' or 'trial' to automatically start the 30-day trial
LICENSE=basic

# Port to expose Elasticsearch HTTP API to the host
ES_PORT=9200

# Port to expose Kibana to the host
KIBANA_PORT=5601

# Increase or decrease based on the available host memory (in bytes)
MEM_LIMIT=1073741824

# Project namespace (defaults to the current folder name if not set)
COMPOSE_PROJECT_NAME=project
" > .env

echo -e "%BLUE%done%NC%"
docker-compose up -d

echo -n "Fingerprinting certs    ... "

echo -e "

The Kibana url is: %MAGENTA% http://localhost:5601/login %NC%
	User: %MAGENTA% elastic %NC%
	Password: %MAGENTA% %PASS1% %NC%
The elasticsearch url is: ${MAGENTA} https://localhost:9200 %NC%
	User: %MAGENTA% elastic %NC%
	Password: %MAGENTA% %PASS1% %NC%
To login in the kibana container:
	User: %MAGENTA% kibana_system %NC%
	Password: %MAGENTA% %PASS2% %NC%"
