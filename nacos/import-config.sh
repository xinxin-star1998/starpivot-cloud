#!/usr/bin/env bash
# 将 nacos/config 目录下的配置发布到 Nacos 配置中心
set -euo pipefail

NACOS_SERVER="${NACOS_SERVER:-127.0.0.1:8848}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="${SCRIPT_DIR}/config"

echo "Publishing configs to Nacos ${NACOS_SERVER} (group=${NACOS_GROUP}, namespace=${NACOS_NAMESPACE:-public})"

for file in "${CONFIG_DIR}"/*.yaml; do
  data_id="$(basename "${file}")"
  echo "  -> ${data_id}"

  curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/cs/configs" \
    -u "${NACOS_USERNAME}:${NACOS_PASSWORD}" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@${file}" \
    | grep -q "true" && echo "     OK" || echo "     FAILED"
done

echo "Done."
