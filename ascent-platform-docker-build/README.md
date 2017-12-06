# Centralize Logging for Ascent Platform

Implementation of the [Ascent Logging Design](https://github.com/department-of-veterans-affairs/ascent/wiki/Ascent-Logging:-Search-Visualize-and-Monitor) and [Platform Logging Design](https://github.com/department-of-veterans-affairs/ascent/wiki/Platform-Logging).

## Deployment

### Tool Needed
The following tools will need to be installed before you can successfully deploy and use the Ascent Platform Logging solution.
* [Docker](https://store.docker.com/search?type=edition&offering=community)

### Local Deployment
To deploy our logging stack locally, run the following command in your terminal:

```bash
docker-compose -f docker-compose.logging.yml -f docker-compose.logging.override.yml up --build -d
```

### Swarm Deployment
To deploy our logging stack to a Docker Swarm, run the following command in your terminal:

```bash
docker stack deploy -c docker-compose.logging.yml logging
```
## Usage
The Kibana UI should be available at [http://localhost:5601](http://localhost:5601). The default credentials are:<br/>
_Username:_ elastic<br/>
_Password:_ changeme

## Custom Configuration
The following environment variables can be set when the containers are launched in order to customize their configuration:
* Filebeat
    * __LOGPATH__ - Path to the Docker logs directory in the container. Defaults to "/dockerlogs".
    * __LS_HOST__ - The Logstash service hostname. Defaults to "logstash".
    * __LS_PORT__ - The port of the Logstash service. Defaults to "5044".
    * __VAULT_TOKEN__ - The token to authenticate to vault with. When this is specified, Filebeat will configure itself to use SSL authentication with Logstash. It will pull its SSL information from Vault using the VAULT_ADDR env variable value.
    * __VAULT_ADDR__ - The address of the vault server. Defaults to "https://vault:8200"
* LogStash
    * __ES_HOST__ - Hostname of the ElasticSearch service. Defaults to "elasticsearch"
    * __ES_USER__ - Username to authenticate to ElasticSearch service. Defaults to "elastic".
    * __ES_PASSWORD__ - Password for the ElasticSearch user. Defaults to "changeme".
    * __VAULT_TOKEN__ - The token to authenticate to vault with. When this is specified, Logstash will configure itself to use SSL authentication with Filebeat. It will pull its SSL information from Vault using the VAULT_ADDR env variable value.
    * __VAULT_ADDR__ - The address of the vault server. Defaults to "https://vault:8200"
* ElasticSearch
    * __VAULT_TOKEN__ - The token to authenticate to vault with. When this is specified, ElasticSearch will configure itself to use SSL authentication with Logstash and Kibana. It will pull its SSL information from Vault using the VAULT_ADDR env variable value.
    * __VAULT_ADDR__ - The address of the vault server. Defaults to "https://vault:8200"
* Kibana
    * __VAULT_TOKEN__ - The token to authenticate to vault with. When this is specified, Kibana will configure itself to use SSL authentication with ElasticSearch. It will pull its SSL information from Vault using the VAULT_ADDR env variable value.
    * __VAULT_ADDR__ - The address of the vault server. Defaults to "https://vault:8200"