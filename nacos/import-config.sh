#!/usr/bin/env bash
# 将 nacos/config 目录下的配置发布到 Nacos 配置中心
# 用法: ./import-config.sh [All|Core|Mall]
set -euo pipefail

PROFILE="${1:-All}"
NACOS_SERVER="${NACOS_SERVER:-127.0.0.1:8848}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="${SCRIPT_DIR}/config"

in_profile() {
  local data_id="$1"
  case "${PROFILE}" in
    Mall) [[ "${data_id}" == "common-config.yaml" || "${data_id}" == "mq-config.yaml" || "${data_id}" == starpivot-mall* ]] ;;
    Core) [[ "${data_id}" != starpivot-mall* ]] ;;
    *) true ;;
  esac
}

echo "Publishing configs to Nacos ${NACOS_SERVER} (profile=${PROFILE}, group=${NACOS_GROUP}, namespace=${NACOS_NAMESPACE:-public})"

for file in "${CONFIG_DIR}"/*.yaml; do
  data_id="$(basename "${file}")"
  if ! in_profile "${data_id}"; then
    continue
  fi
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
