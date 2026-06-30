#!/usr/bin/env bash
# StarPivot Docker 分层启动脚本
# 用法: ./scripts/docker-up.sh [infra|services|ui|full] [--build] [--down]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

MODE="infra"
BUILD=""
DOWN=""

for arg in "$@"; do
  case "${arg}" in
    infra|services|ui|full) MODE="${arg}" ;;
    --build) BUILD="--build" ;;
    --down) DOWN="1" ;;
    *) echo "Unknown argument: ${arg}"; exit 1 ;;
  esac
done

COMPOSE_BASE=(-f docker-compose.yml)
COMPOSE_SERVICES=(-f docker-compose.yml -f docker-compose.services.yml)
COMPOSE_FULL=(-f docker-compose.yml -f docker-compose.services.yml -f docker-compose.ui.yml)

if [[ -n "${DOWN}" ]]; then
  echo "Stopping StarPivot Docker stack..."
  docker compose "${COMPOSE_FULL[@]}" down
  exit 0
fi

case "${MODE}" in
  infra)
    echo "Starting infrastructure only..."
    docker compose "${COMPOSE_BASE[@]}" up -d ${BUILD}
    echo ""
    echo "Next: ./nacos/import-config.sh"
    echo "Then: mvn spring-boot:run -pl <module>"
    ;;
  services)
    echo "Starting infrastructure + microservices..."
    docker compose "${COMPOSE_SERVICES[@]}" up -d ${BUILD}
    echo ""
    echo "API Gateway: http://localhost:8080"
    ;;
  ui|full)
    echo "Starting full stack..."
    docker compose "${COMPOSE_FULL[@]}" up -d --build
    echo ""
    echo "Frontend:    http://localhost:3000"
    echo "API Gateway: http://localhost:8080"
    echo "Default login: admin / 123456"
    ;;
esac

echo ""
echo "Done."
