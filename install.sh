#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "=== Checking Prerequisites ==="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker not found. Installing Docker..."
    sudo apt-get update
    sudo apt-get install -y curl apt-transport-https ca-certificates gnupg lsb-release
    
    # Add Docker's official GPG key
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    
    # Set up the repository
    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
      
    sudo apt-get update
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    echo "Docker installed successfully."
else
    echo "Docker is already installed."
fi

# Check if NVIDIA Container Toolkit is recommended/needed for GPUs
if ! docker info 2>/dev/null | grep -i "nvidia" > /dev/null; then
    echo "Note: Ensure the NVIDIA Container Toolkit is installed so the container can access your GPU."
fi

echo "=== Downloading Repository Files ==="

REPO_DIR="abliterated-qwen-server"

if [ -d "$REPO_DIR" ]; then
    echo "Directory $REPO_DIR already exists. Pulling latest updates..."
    cd "$REPO_DIR"
    git pull
else
    echo "Cloning repository from GitHub..."
    git clone https://github.com/yanbo12338/abliterated-qwen-server.git
    cd "$REPO_DIR"
fi

echo "=== Making Scripts Executable ==="
chmod +x *.sh

echo "=== Running Deployment ==="
./deploy.sh