#!/usr/bin/env bash
# Deploys the Production Simulator (simulator/app) onto the EC2 instance
# recorded in infra/aws/ec2/production-simulator.json, and runs it as a
# systemd service so it survives reboots and SSH disconnects.
#
# This mirrors exactly what was done manually on 2026-11-21 -- kept here so
# the step is reproducible (e.g. after a `systemctl` reset, a replacement
# instance, or redeploying after simulator code changes) rather than only
# existing as one-off shell history.
#
# Usage: ./deploy-simulator.sh <host-public-ip>
# Requires: infra/aws/keys/incidentcommand-simulator-dev.pem present locally.

set -euo pipefail

HOST_IP="${1:?Usage: $0 <host-public-ip>}"
KEY="$(dirname "$0")/../keys/incidentcommand-simulator-dev.pem"
REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
REMOTE="ubuntu@${HOST_IP}"

chmod 400 "$KEY"

echo "==> Copying simulator source"
ssh -i "$KEY" -o StrictHostKeyChecking=accept-new "$REMOTE" "mkdir -p ~/production-simulator"
scp -i "$KEY" -o StrictHostKeyChecking=accept-new -r \
  "$REPO_ROOT/simulator/requirements.txt" \
  "$REPO_ROOT/simulator/app" \
  "$REMOTE:/tmp/"
ssh -i "$KEY" "$REMOTE" "
  rm -rf ~/production-simulator/app && \
  mv /tmp/app /tmp/requirements.txt ~/production-simulator/ && \
  find ~/production-simulator/app -name __pycache__ -type d -exec rm -rf {} + 2>/dev/null; true
"

echo "==> Installing dependencies (venv)"
ssh -i "$KEY" "$REMOTE" '
  set -e
  if ! python3 -m venv --help > /dev/null 2>&1; then
    sudo apt-get update -qq && sudo apt-get install -y -qq python3.12-venv > /dev/null
  fi
  cd ~/production-simulator
  rm -rf .venv
  python3 -m venv .venv
  .venv/bin/pip install --quiet --upgrade pip
  .venv/bin/pip install --quiet -r requirements.txt
'

echo "==> Installing systemd service"
ssh -i "$KEY" "$REMOTE" '
  sudo touch /var/log/production-simulator.log
  sudo chown ubuntu:ubuntu /var/log/production-simulator.log
  sudo tee /etc/systemd/system/production-simulator.service > /dev/null <<EOF
[Unit]
Description=Incident Command AI - Production Simulator
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/production-simulator
ExecStart=/home/ubuntu/production-simulator/.venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8090
Restart=on-failure
RestartSec=5
StandardOutput=append:/var/log/production-simulator.log
StandardError=append:/var/log/production-simulator.log

[Install]
WantedBy=multi-user.target
EOF
  sudo systemctl daemon-reload
  sudo systemctl enable --now production-simulator
'
# Note: StandardOutput/StandardError write to a plain file, not just the
# systemd journal -- the CloudWatch agent (see infra/aws/cloudwatch/
# production-simulator-logging.json) tails this file rather than journald,
# which sidesteps agent-version-specific journald input quirks.

echo "==> Verifying"
sleep 2
curl -sf "http://${HOST_IP}:8090/health" && echo " -- simulator reachable"
