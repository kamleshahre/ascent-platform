# Starts the entire Ascent platform, including all log aggregation services

docker-compose -f docker-compose.yml \
	-f docker-compose.override.yml \
	-f docker-compose.logging.yml \
	-f docker-compose.logging.override.yml \
    -f docker-compose.vault.yml \
    -f docker-compose.vault.override.yml \
	up --build -d
