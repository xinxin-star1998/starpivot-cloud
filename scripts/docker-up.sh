#!/usr/bin/env bash
# StarPivot 基础设施启动脚本（Nacos / MySQL / Redis / RabbitMQ / Zipkin）
# 用法: ./scripts/docker-up.sh [--down]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

COMPOSE_FILES=(-f docker-compose.yml)
DOWN=""

for arg in "$@"; do
  case "${arg}" in
    --down) DOWN="1" ;;
    *) echo "Unknown argument: ${arg}"; exit 1 ;;
  esac
done

if [[ -n "${DOWN}" ]]; then
  echo "Stopping StarPivot infrastructure..."
  docker compose "${COMPOSE_FILES[@]}" down
  exit 0
fi

echo "Starting infrastructure..."
docker compose "${COMPOSE_FILES[@]}" up -d
echo ""
echo "Next: ./nacos/import-config.sh"
echo "Then: mvn spring-boot:run -pl <module>"
echo ""
echo "Done."
