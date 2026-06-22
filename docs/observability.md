# 可观测性说明

## TraceId 全链路

| 环节 | 行为 |
|------|------|
| 网关 | `TraceIdGatewayFilter` 读取或生成 `X-Trace-Id`，写入下游请求头与响应头 |
| 微服务 | `TraceIdServletFilter`（自动配置）写入 SLF4J MDC，键名 `traceId` |
| Feign | `InternalFeignRequestInterceptor` 从 MDC 透传 `X-Trace-Id` |

启用 Zipkin 后，TraceId 优先与 Micrometer Tracing 的 `traceId` 对齐；B3/W3C 头由框架自动透传至 Feign、Gateway 下游。

日志格式（`common-config.yaml`）：

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{50} - %msg%n
```

排查时在网关响应头或日志中复制 `X-Trace-Id`，在各服务日志中检索同一 ID 即可串联一次请求；或在 Zipkin UI 按 trace 查看调用链。

## Actuator 端点

公共配置默认暴露：

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

网关经白名单 `/**/actuator/**` 可匿名访问（生产建议收紧或加鉴权）。

## Prometheus 指标

依赖 `micrometer-registry-prometheus`（经 `starpivot-common` 传递），各服务需引入 `spring-boot-starter-actuator`。

采集示例（直连某服务）：

```bash
curl http://localhost:8082/actuator/prometheus
```

Grafana 配置 Prometheus 数据源后，可按 `application` 标签（值为 `spring.application.name`）区分服务。HTTP 请求指标默认开启百分位直方图（`http.server.requests`）。

## Zipkin 分布式追踪

依赖 `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave`（经 `starpivot-common` 传递）。

### 启动 Zipkin

```bash
docker compose up -d zipkin
```

控制台：http://localhost:9411

### 配置项（环境变量 / Nacos `common-config.yaml`）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `TRACING_ENABLED` | `true` | 是否启用追踪 |
| `TRACING_SAMPLING_PROBABILITY` | `1.0` | 采样率（0.0–1.0，生产建议调低） |
| `ZIPKIN_ENDPOINT` | `http://127.0.0.1:9411/api/v2/spans` | Span 上报地址 |

本地无 Zipkin 时可设 `TRACING_ENABLED=false` 关闭上报。

## 后续扩展建议

- 统一 JSON 结构化日志（Logstash/ELK）
- Prometheus + Grafana 仪表盘与告警规则
- 生产环境对 `/actuator/prometheus` 做网络隔离或鉴权
