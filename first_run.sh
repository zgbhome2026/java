#!/bin/bash

echo "Checking for Hugging Face cache directory..."
mkdir -p /data/yanbo/hf_cache

echo "Pulling the latest vLLM Docker image..."
docker pull vllm/vllm-openai:latest

echo "Starting huihui-server container..."
docker run -d --name huihui-server --runtime nvidia --gpus all \
  -p 8000:8000 \
  -v /data/yanbo/hf_cache:/root/.cache/huggingface \
  vllm/vllm-openai:latest \
  --model huihui-ai/Huihui-Qwen3.8-27B-abliterated \
  --host 0.0.0.0 --port 8000 \
  --trust-remote-code \
  --enable-auto-tool-choice \
  --tool-call-parser qwen3_xml

echo ""
read -p "Would you like to open the logs to watch the model download and start up? (y/n): " choice

case "$choice" in 
  y|Y ) 
    echo "Opening logs (press Ctrl+C to exit logs)..."
    docker logs -f huihui-server
    ;;
  * ) 
    echo "Skipping logs. Server is downloading and running in the background."
    ;;
esac
