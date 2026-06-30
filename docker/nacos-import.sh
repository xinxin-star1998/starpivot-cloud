#!/bin/sh
# 等待 Nacos 就绪后将 nacos/config/*.yaml 发布到配置中心（Docker 首次启动用）
set -eu

NACOS_SERVER="${NACOS_SERVER:-nacos:8848}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
CONFIG_DIR="${CONFIG_DIR:-/config}"
BASE_URL="http://${NACOS_SERVER}/nacos/v1/cs/configs"

echo "Waiting for Nacos at ${NACOS_SERVER}..."
until curl -sf "http://${NACOS_SERVER}/nacos/v1/console/health/readiness" >/dev/null 2>&1; do
  sleep 3
done
echo "Nacos is ready."

echo "Publishing configs (group=${NACOS_GROUP}, namespace=${NACOS_NAMESPACE:-public})"
for file in "${CONFIG_DIR}"/*.yaml; do
  [ -f "$file" ] || continue
  data_id="$(basename "$file")"
  echo "  -> ${data_id}"
  if curl -sf -X POST "${BASE_URL}" \
    -u "${NACOS_USERNAME}:${NACOS_PASSWORD}" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@${file}" | grep -q "true"; then
    echo "     OK"
  else
    echo "     FAILED"
    exit 1
  fi
done

echo "Nacos config import done."
