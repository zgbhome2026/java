#!/usr/bin/env bash
set -euo pipefail
mvn clean package -DskipTests
echo
echo "Built: target/shopping.jar"
