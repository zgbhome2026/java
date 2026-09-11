#!/bin/bash

echo "Stopping and removing huihui-server container..."
docker stop huihui-server
docker rm huihui-server
echo "Container stopped and removed."
