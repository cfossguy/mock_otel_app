#! /bin/sh
# Export env vars
export $(grep -v '^#' .env | xargs)

# download the binary
curl -O -L "https://github.com/grafana/agent/releases/download/v0.32.1/${GRAFANA_AGENT_BINARY_RELEASE}"

# extract the binary
unzip "${GRAFANA_AGENT_BINARY_RELEASE}"

# move it to shell executable location
sudo mv "${GRAFANA_AGENT_BINARY_RELEASE%.*}" "${GRAFANA_AGENT_BIN_BINARY}"

# make sure it is executable
sudo chmod a+x "${GRAFANA_AGENT_BIN_BINARY%.*}"