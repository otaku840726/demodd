#!/bin/bash

echo "[STARTUP] Waiting for MySQL..."
cd /workspaces/demodd/docker && docker compose up -d
echo "MySQL ready!"


# sdk default java 24.0.1-tem
# sdk default maven 3.9.9