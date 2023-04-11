#! /bin/sh
# Export env vars
export $(grep -v '^#' .env | xargs)

# Start grafana agent via local config file
grafana-agent -config.expand-env -config.file ./grafana-agent.yaml