#!/bin/bash

# Start the container using the start script
./start.sh

echo ""
read -p "Would you like to open the logs now? (y/n): " choice

case "$choice" in 
  y|Y ) 
    echo "Opening logs (press Ctrl+C to exit logs)..."
    docker logs -f huihui-server
    ;;
  * ) 
    echo "Skipping logs. Server is running in the background."
    ;;
esac
