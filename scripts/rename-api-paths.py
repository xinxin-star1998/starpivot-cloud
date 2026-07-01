#!/usr/bin/env python3
"""Rename list/delete API paths to {biz}PageList and remove{Biz} per controller."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

BASE_ENTITY: dict[str, str] = {
    "/sys/user": "user",
    "/sys/role": "role",
    "/sys/post": "post",
    "/sys/dept": "dept",
    "/sys/menu": "menu",
    "/sys/operlog": "operLog",
    "/sys/logininfor": "logininfor",
    "/sys/dict/type": "dictType",
    "/sys/dict/data": "dictData",
    "/config": "config",
    "/notice": "notice",
    "/avatar": "avatar",
    "/mall/product": "product",
    "/mall/sku": "sku",
    "/mall/category": "category",
    "/mall/attr": "attr",
    "/mall/group": "attrGroup",
    "/mall/brand": "brand",
    "/mall/comment": "comment",
    "/mall/coupon": "coupon",
    "/mall/coupon-history": "couponHistory",
    "/mall/member-price": "memberPrice",
    "/mall/spu-bounds": "spuBounds",
    "/mall/sku-ladder": "skuLadder",
    "/mall/sku-full-reduction": "skuFullReduction",
    "/mall/home-adv": "homeAdv",
    "/mall/subject": "subject",
    "/mall/seckill-session": "seckillSession",
    "/mall/seckill-promotion": "seckillPromotion",
    "/mall/member": "member",
    "/mall/member-level": "memberLevel",
    "/mall/member-statistics": "memberStatistics",
    "/mall/address": "address",
    "/mall/wareinfo": "wareInfo",
    "/mall/ware-sku": "wareSku",
    "/mall/ware-task": "wareTask",
    "/mall/purchase": "purchase",
    "/mall/order": "order",
    "/mall/order-return": "orderReturn",
    "/mall/payment": "payment",
    "/mall/refund": "refund",
    "/portal/order": "portalOrder",
    "/portal/cart": "cart",
    "/approval/template": "template",
    "/approval/instance": "approvalInstance",
    "/approval/task": "approvalTask",
    "/file": "file",
    "/tool/gen": "genTable",
    "/monitor/job": "job",
}

SUBPATH_RENAMES: dict[str, list[tuple[str, str]]] = {
    "/mall/member-growth": [
        ("/integration/list", "/integrationPageList"),
        ("/growth/list", "/growthPageList"),
    ],
    "/approval/instance": [
        ("/mine/list", "/mineInstancePageList"),
    ],
    "/approval/task": [
        ("/todo/list", "/todoTaskPageList"),
        ("/done/list", "/doneTaskPageList"),
    ],
    "/approval/template": [
        ("/bind/list", "/templateBindPageList"),
        ("/bind/remove", "/removeTemplateBind"),
    ],
    "/mall/purchase": [
        ("/unreceive/list", "/unreceivePurchasePageList"),
        ("/detail/list", "/purchaseDetailPageList"),
        ("/detail/remove", "/removePurchaseDetail"),
    ],
    "/tool/gen": [
        ("/db/list", "/genDbTablePageList"),
    ],
    "/monitor/job": [
        ("/log/list", "/jobLogPageList"),
    ],
    "/file": [
        ("/recycle/list", "/recycleFilePageList"),
    ],
}

REQUEST_MAPPING_RE = re.compile(r'@RequestMapping\("([^"]+)"\)')
POST_MAPPING_RE = re.compile(r'@PostMapping\("([^"]+)"\)')
DELETE_MAPPING_RE = re.compile(r'@DeleteMapping\("([^"]+)"\)')
POST_MAPPING_MULTI_RE = re.compile(r'@PostMapping\(\{([^}]+)\}\)')
DELETE_MAPPING_MULTI_RE = re.compile(r'@DeleteMapping\(\{([^}]+)\}\)')


def pascal(camel: str) -> str:
    return camel[0].upper() + camel[1:] if camel else camel


def infer_entity(base: str) -> str:
    if base in BASE_ENTITY:
        return BASE_ENTITY[base]
    seg = base.rstrip("/").split("/")[-1]
    if "-" in seg:
        parts = seg.split("-")
        return parts[0] + "".join(p[:1].upper() + p[1:] for p in parts[1:])
    return seg


def url_replacements() -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    for base, entity in {**BASE_ENTITY, **{b: infer_entity(b) for b in SUBPATH_RENAMES}}.items():
        if base not in BASE_ENTITY and base not in SUBPATH_RENAMES:
            continue
    for base, entity in BASE_ENTITY.items():
        page = f"/{entity}PageList"
        remove = f"/remove{pascal(entity)}"
        for old in ("/list", "/pageList"):
            reps.append((f"{base}{old}", f"{base}{page}"))
        for old in ("/delete", "/remove", "/removeNotice"):
            reps.append((f"{base}{old}", f"{base}{remove}"))
    for base, mappings in SUBPATH_RENAMES.items():
        for old, new in mappings:
            reps.append((f"{base}{old}", f"{base}{new}"))
    reps.append(("/mall/member-level/list", "/mall/member-level/memberLevelPageList"))
    return sorted(set(reps), key=lambda x: len(x[0]), reverse=True)


def transform_controller(text: str) -> tuple[str, list[tuple[str, str]]]:
    m = REQUEST_MAPPING_RE.search(text)
    if not m:
        return text, []
    base = m.group(1)
    entity = infer_entity(base)
    page = f"/{entity}PageList"
    remove = f"/remove{pascal(entity)}"
    local_reps: list[tuple[str, str]] = []

    def sub_post(match: re.Match[str]) -> str:
        inner = match.group(1)
        if "list" in inner or "pageList" in inner:
            local_reps.append((f"{base}/list", f"{base}{page}"))
            local_reps.append((f"{base}/pageList", f"{base}{page}"))
            return f'@PostMapping("{page}")'
        return match.group(0)

    def sub_delete(match: re.Match[str]) -> str:
        inner = match.group(1)
        if "delete" in inner or "remove" in inner:
            local_reps.append((f"{base}/delete", f"{base}{remove}"))
            local_reps.append((f"{base}/remove", f"{base}{remove}"))
            local_reps.append((f"{base}/removeNotice", f"{base}{remove}"))
            return f'@DeleteMapping("{remove}")'
        return match.group(0)

    text = POST_MAPPING_MULTI_RE.sub(sub_post, text)
    text = DELETE_MAPPING_MULTI_RE.sub(sub_delete, text)

    for old, new in [
        ('@PostMapping("/pageList")', f'@PostMapping("{page}")'),
        ('@PostMapping("/list")', f'@PostMapping("{page}")'),
        ('@DeleteMapping("/delete")', f'@DeleteMapping("{remove}")'),
        ('@DeleteMapping("/remove")', f'@DeleteMapping("{remove}")'),
        ('@DeleteMapping("/removeNotice")', f'@DeleteMapping("{remove}")'),
    ]:
        if old in text:
            if 'PostMapping' in old:
                suffix_old = "/pageList" if "pageList" in old else "/list"
                local_reps.append((f"{base}{suffix_old}", f"{base}{page}"))
            else:
                for s in ("/delete", "/remove", "/removeNotice"):
                    if s in old:
                        local_reps.append((f"{base}{s}", f"{base}{remove}"))
            text = text.replace(old, new)

    for old_suffix, new_suffix in SUBPATH_RENAMES.get(base, []):
        post_old = f'@PostMapping("{old_suffix}")'
        post_new = f'@PostMapping("{new_suffix}")'
        del_old = f'@DeleteMapping("{old_suffix}")'
        del_new = f'@DeleteMapping("{new_suffix}")'
        if post_old in text:
            local_reps.append((f"{base}{old_suffix}", f"{base}{new_suffix}"))
            text = text.replace(post_old, post_new)
        if del_old in text:
            local_reps.append((f"{base}{old_suffix}", f"{base}{new_suffix}"))
            text = text.replace(del_old, del_new)

    # member-level: GET /list -> keep; POST /list already handled
    if base == "/mall/member-level":
        local_reps.append(("/mall/member-level/list", "/mall/member-level/memberLevelPageList"))

    return text, local_reps


def main() -> None:
    url_reps = url_replacements()
    java_roots = [
        ROOT / "starpivot-system",
        ROOT / "starpivot-mall",
        ROOT / "starpivot-approval",
        ROOT / "starpivot-file",
        ROOT / "starpivot-generator",
        ROOT / "starpivot-monitor",
    ]
    changed_java = 0
    for root in java_roots:
        for path in root.rglob("*Controller.java"):
            text = path.read_text(encoding="utf-8")
            new_text, _ = transform_controller(text)
            if new_text != text:
                path.write_text(new_text, encoding="utf-8")
                changed_java += 1
                print(f"java: {path.relative_to(ROOT)}")

    ui_api = ROOT / "star-pivot-ui" / "src" / "api"
    changed_ts = 0
    if ui_api.exists():
        for path in ui_api.rglob("*.ts"):
            text = path.read_text(encoding="utf-8")
            original = text
            for old, new in url_reps:
                text = text.replace(f"/api{old}", f"/api{new}")
            if text != original:
                path.write_text(text, encoding="utf-8")
                changed_ts += 1
                print(f"ts: {path.relative_to(ROOT)}")

    print(f"Done. Java: {changed_java}, TS: {changed_ts}")


if __name__ == "__main__":
    main()
