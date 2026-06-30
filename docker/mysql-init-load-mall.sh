#!/bin/bash
# 将商城 SQL 导入 star_pivot_mall 库（避免 .sql 默认写入 MYSQL_DATABASE=star_pivot）
set -e
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" star_pivot_mall < /mall-data/star_pivot_mall.sql
