/*
 SKU 销售属性迁移 — 旧谷粒商城 → 新属性体系

 映射关系：
   attr_id 9  「颜色」         → 66 「机身颜色」
   attr_id 12 「版本」(存储)    → 68 「机身存储(ROM)」
   attr_id 12 「版本」(8GB+256GB) → 67 「运行内存(RAM)」+ 68 「机身存储(ROM)」

 前置条件：已执行 pms_attr.sql 重建属性表（含 66~69）

 用法：
   mysql -u root -p star_pivot_mall < pms_sku_sale_attr_value_migrate.sql

 说明：脚本按 attr_id=9/12 匹配，可重复执行（已迁移行不再命中）。
*/

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 0. 清理属性值首尾空格（如历史数据中的「128GB 」）
-- ---------------------------------------------------------------------------
UPDATE `pms_sku_sale_attr_value`
SET `attr_value` = TRIM(`attr_value`)
WHERE `attr_value` IS NOT NULL
  AND `attr_value` <> TRIM(`attr_value`);

-- ---------------------------------------------------------------------------
-- 1. 颜色 → 机身颜色
-- ---------------------------------------------------------------------------
UPDATE `pms_sku_sale_attr_value`
SET `attr_id`   = 66,
    `attr_name` = '机身颜色',
    `attr_sort` = COALESCE(`attr_sort`, 0)
WHERE `attr_id` = 9;

-- ---------------------------------------------------------------------------
-- 2. 版本(8GB+256GB 组合) → 先补 ROM 行，再把原行改为 RAM
--    例：8GB+256GB → 运行内存 8GB + 机身存储 256GB
-- ---------------------------------------------------------------------------
INSERT INTO `pms_sku_sale_attr_value` (`sku_id`, `attr_id`, `attr_name`, `attr_value`, `attr_sort`)
SELECT
  t.`sku_id`,
  68,
  '机身存储(ROM)',
  TRIM(SUBSTRING_INDEX(REPLACE(TRIM(t.`attr_value`), ' ', ''), '+', -1)),
  COALESCE(t.`attr_sort`, 2)
FROM `pms_sku_sale_attr_value` t
WHERE t.`attr_id` = 12
  AND TRIM(REPLACE(t.`attr_value`, ' ', '')) REGEXP '^[0-9]+GB\\+[0-9]+GB$'
  AND NOT EXISTS (
    SELECT 1
    FROM `pms_sku_sale_attr_value` x
    WHERE x.`sku_id` = t.`sku_id`
      AND x.`attr_id` = 68
      AND x.`attr_value` = TRIM(SUBSTRING_INDEX(REPLACE(TRIM(t.`attr_value`), ' ', ''), '+', -1))
  );

UPDATE `pms_sku_sale_attr_value`
SET `attr_id`   = 67,
    `attr_name` = '运行内存(RAM)',
    `attr_value` = TRIM(SUBSTRING_INDEX(REPLACE(TRIM(`attr_value`), ' ', ''), '+', 1)),
    `attr_sort` = COALESCE(`attr_sort`, 1)
WHERE `attr_id` = 12
  AND TRIM(REPLACE(`attr_value`, ' ', '')) REGEXP '^[0-9]+GB\\+[0-9]+GB$';

-- ---------------------------------------------------------------------------
-- 3. 版本(纯存储) → 机身存储(ROM)
--    例：128GB / 256GB / 64GB（iPhone 11 等）
-- ---------------------------------------------------------------------------
UPDATE `pms_sku_sale_attr_value`
SET `attr_id`   = 68,
    `attr_name` = '机身存储(ROM)',
    `attr_value` = TRIM(`attr_value`),
    `attr_sort` = COALESCE(`attr_sort`, 1)
WHERE `attr_id` = 12;

-- ---------------------------------------------------------------------------
-- 4. 校验（执行后应无 attr_id 9 / 12）
-- ---------------------------------------------------------------------------
SELECT 'remaining legacy attr rows (expect 0)' AS check_item, COUNT(*) AS cnt
FROM `pms_sku_sale_attr_value`
WHERE `attr_id` IN (9, 12);

SELECT `attr_id`, `attr_name`, COUNT(*) AS cnt
FROM `pms_sku_sale_attr_value`
GROUP BY `attr_id`, `attr_name`
ORDER BY `attr_id`;
