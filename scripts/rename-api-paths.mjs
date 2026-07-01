#!/usr/bin/env node
/** Rename list/delete API paths to {biz}PageList and remove{Biz} per controller. */
import { readFileSync, writeFileSync, readdirSync, statSync } from 'fs';
import { join, relative } from 'path';
import { fileURLToPath } from 'url';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const ROOT = join(__dirname, '..');

const BASE_ENTITY = {
  '/sys/user': 'user',
  '/sys/role': 'role',
  '/sys/post': 'post',
  '/sys/dept': 'dept',
  '/sys/menu': 'menu',
  '/sys/operlog': 'operLog',
  '/sys/logininfor': 'logininfor',
  '/sys/dict/type': 'dictType',
  '/sys/dict/data': 'dictData',
  '/config': 'config',
  '/notice': 'notice',
  '/avatar': 'avatar',
  '/mall/product': 'product',
  '/mall/sku': 'sku',
  '/mall/category': 'category',
  '/mall/attr': 'attr',
  '/mall/group': 'attrGroup',
  '/mall/brand': 'brand',
  '/mall/comment': 'comment',
  '/mall/coupon': 'coupon',
  '/mall/coupon-history': 'couponHistory',
  '/mall/member-price': 'memberPrice',
  '/mall/spu-bounds': 'spuBounds',
  '/mall/sku-ladder': 'skuLadder',
  '/mall/sku-full-reduction': 'skuFullReduction',
  '/mall/home-adv': 'homeAdv',
  '/mall/subject': 'subject',
  '/mall/seckill-session': 'seckillSession',
  '/mall/seckill-promotion': 'seckillPromotion',
  '/mall/member': 'member',
  '/mall/member-level': 'memberLevel',
  '/mall/member-statistics': 'memberStatistics',
  '/mall/address': 'address',
  '/mall/wareinfo': 'wareInfo',
  '/mall/ware-sku': 'wareSku',
  '/mall/ware-task': 'wareTask',
  '/mall/purchase': 'purchase',
  '/mall/order': 'order',
  '/mall/order-return': 'orderReturn',
  '/mall/payment': 'payment',
  '/mall/refund': 'refund',
  '/portal/order': 'portalOrder',
  '/portal/cart': 'cart',
  '/approval/template': 'template',
  '/approval/instance': 'approvalInstance',
  '/approval/task': 'approvalTask',
  '/file': 'file',
  '/tool/gen': 'genTable',
  '/monitor/job': 'job',
  '/monitor/cache': 'cache',
};

const SUBPATH_RENAMES = {
  '/mall/member-growth': [
    ['/integration/list', '/integrationPageList'],
    ['/growth/list', '/growthPageList'],
  ],
  '/approval/instance': [['/mine/list', '/mineInstancePageList']],
  '/approval/task': [
    ['/todo/list', '/todoTaskPageList'],
    ['/done/list', '/doneTaskPageList'],
  ],
  '/approval/template': [
    ['/bind/list', '/templateBindPageList'],
    ['/bind/remove', '/removeTemplateBind'],
  ],
  '/mall/purchase': [
    ['/unreceive/list', '/unreceivePurchasePageList'],
    ['/detail/list', '/purchaseDetailPageList'],
    ['/detail/remove', '/removePurchaseDetail'],
  ],
  '/tool/gen': [['/db/list', '/genDbTablePageList']],
  '/monitor/job': [['/log/list', '/jobLogPageList']],
  '/file': [['/recycle/list', '/recycleFilePageList']],
};

const REQUEST_MAPPING_RE = /@RequestMapping\("([^"]+)"\)/;
const POST_MAPPING_MULTI_RE = /@PostMapping\(\{([^}]+)\}\)/g;
const DELETE_MAPPING_MULTI_RE = /@DeleteMapping\(\{([^}]+)\}\)/g;

function pascal(camel) {
  return camel ? camel[0].toUpperCase() + camel.slice(1) : camel;
}

function inferEntity(base) {
  if (BASE_ENTITY[base]) return BASE_ENTITY[base];
  const seg = base.replace(/\/$/, '').split('/').pop();
  if (seg.includes('-')) {
    const parts = seg.split('-');
    return parts[0] + parts.slice(1).map((p) => p[0].toUpperCase() + p.slice(1)).join('');
  }
  return seg;
}

function urlReplacements() {
  const reps = [];
  for (const [base, entity] of Object.entries(BASE_ENTITY)) {
    const page = `/${entity}PageList`;
    const remove = `/remove${pascal(entity)}`;
    for (const old of ['/list', '/pageList']) {
      reps.push([`${base}${old}`, `${base}${page}`]);
    }
    for (const old of ['/delete', '/remove', '/removeNotice']) {
      reps.push([`${base}${old}`, `${base}${remove}`]);
    }
  }
  for (const [base, mappings] of Object.entries(SUBPATH_RENAMES)) {
    for (const [old, neu] of mappings) {
      reps.push([`${base}${old}`, `${base}${neu}`]);
    }
  }
  reps.push(['/mall/member-level/list', '/mall/member-level/memberLevelPageList']);
  return [...new Map(reps.map((r) => [r[0], r[1]])).entries()].sort((a, b) => b[0].length - a[0].length);
}

function transformController(text) {
  const m = text.match(REQUEST_MAPPING_RE);
  if (!m) return text;
  const base = m[1];
  const entity = inferEntity(base);
  const page = `/${entity}PageList`;
  const remove = `/remove${pascal(entity)}`;

  text = text.replace(POST_MAPPING_MULTI_RE, (match, inner) => {
    if (inner.includes('list') || inner.includes('pageList')) {
      return `@PostMapping("${page}")`;
    }
    return match;
  });

  text = text.replace(DELETE_MAPPING_MULTI_RE, (match, inner) => {
    if (inner.includes('delete') || inner.includes('remove')) {
      return `@DeleteMapping("${remove}")`;
    }
    return match;
  });

  const singles = [
    ['@PostMapping("/pageList")', `@PostMapping("${page}")`],
    ['@PostMapping("/list")', `@PostMapping("${page}")`],
    ['@GetMapping("/list")', `@GetMapping("${page}")`],
    ['@DeleteMapping("/delete")', `@DeleteMapping("${remove}")`],
    ['@DeleteMapping("/remove")', `@DeleteMapping("${remove}")`],
    ['@DeleteMapping("/removeNotice")', `@DeleteMapping("${remove}")`],
  ];
  for (const [old, neu] of singles) {
    text = text.split(old).join(neu);
  }

  for (const [oldSuffix, newSuffix] of SUBPATH_RENAMES[base] ?? []) {
    text = text.split(`@PostMapping("${oldSuffix}")`).join(`@PostMapping("${newSuffix}")`);
    text = text.split(`@DeleteMapping("${oldSuffix}")`).join(`@DeleteMapping("${newSuffix}")`);
  }

  return text;
}

function walk(dir, suffix, out = []) {
  for (const name of readdirSync(dir)) {
    const p = join(dir, name);
    if (statSync(p).isDirectory()) walk(p, suffix, out);
    else if (name.endsWith(suffix)) out.push(p);
  }
  return out;
}

const urlReps = urlReplacements();
const javaRoots = [
  'starpivot-system',
  'starpivot-mall',
  'starpivot-approval',
  'starpivot-file',
  'starpivot-generator',
  'starpivot-monitor',
];

let changedJava = 0;
for (const root of javaRoots) {
  const abs = join(ROOT, root);
  for (const path of walk(abs, 'Controller.java')) {
    const text = readFileSync(path, 'utf8');
    const newText = transformController(text);
    if (newText !== text) {
      writeFileSync(path, newText, 'utf8');
      changedJava++;
      console.log(`java: ${relative(ROOT, path)}`);
    }
  }
}

const uiApi = join(ROOT, 'star-pivot-ui', 'src', 'api');
let changedTs = 0;
if (statSync(uiApi, { throwIfNoEntry: false })) {
  for (const path of walk(uiApi, '.ts')) {
    let text = readFileSync(path, 'utf8');
    const original = text;
    for (const [old, neu] of urlReps) {
      const escaped = old.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
      text = text.replace(new RegExp(`/api${escaped}(?=["'\`])`, 'g'), `/api${neu}`);
    }
    if (text !== original) {
      writeFileSync(path, text, 'utf8');
      changedTs++;
      console.log(`ts: ${relative(ROOT, path)}`);
    }
  }
}

console.log(`Done. Java: ${changedJava}, TS: ${changedTs}`);
