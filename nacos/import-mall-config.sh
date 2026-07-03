#!/usr/bin/env bash
# 将商城微服务相关配置发布到 Nacos（common + mq + starpivot-mall*）
set -euo pipefail

NACOS_SERVER="${NACOS_SERVER:-127.0.0.1:8848}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="${SCRIPT_DIR}/config"

MALL_CONFIGS=(
  common-config.yaml
  mq-config.yaml
  starpivot-mall.yaml
  starpivot-mall-member.yaml
  starpivot-mall-product.yaml
  starpivot-mall-ware.yaml
  starpivot-mall-order.yaml
  starpivot-mall-promotion.yaml
)

echo "Importing mall configs to Nacos ${NACOS_SERVER} (group=${NACOS_GROUP}, namespace=${NACOS_NAMESPACE:-public})"
failed=0

for data_id in "${MALL_CONFIGS[@]}"; do
  file="${CONFIG_DIR}/${data_id}"
  if [[ ! -f "${file}" ]]; then
    echo "  SKIP (missing): ${data_id}"
    continue
  fi
  echo "  -> ${data_id}"
  if curl -s -X POST "http://${NACOS_SERVER}/nacos/v1/cs/configs" \
    -u "${NACOS_USERNAME}:${NACOS_PASSWORD}" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "content@${file}" \
    | grep -q "true"; then
    echo "     OK"
  else
    echo "     FAILED"
    failed=$((failed + 1))
  fi
done

if [[ "${failed}" -gt 0 ]]; then
  echo "Finished with ${failed} failure(s)."
  exit 1
fi
echo "All mall configs published."
