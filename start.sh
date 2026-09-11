#!/bin/bash

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

echo "Container started. View logs using: docker logs -f huihui-server"
