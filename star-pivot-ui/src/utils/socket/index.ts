import {logger} from '../sys'

interface WebSocketOptions {
  url?: string
  messageHandler: (event: MessageEvent) => void
  reconnectInterval?: number // 重连间隔(ms)
  heartbeatInterval?: number // 心跳检测间隔(ms)
  pingInterval?: number // 发送ping间隔(ms)
  reconnectTimeout?: number // 重连超时时间(ms)
  maxReconnectAttempts?: number // 最大重连次数
  connectionTimeout?: number // 连接建立超时时间(ms)
}

export default class WebSocketClient {
  private static instance: WebSocketClient | null = null
  private ws: WebSocket | null = null
  private url: string
  private messageHandler: (event: MessageEvent) => void
  private reconnectInterval: number
  private heartbeatInterval: number
  private pingInterval: number
  private reconnectTimeout: number
  private maxReconnectAttempts: number
  private connectionTimeout: number
  private reconnectAttempts: number = 0 // 当前重连次数

  // 消息队列 - 缓存连接建立前的消息
  private messageQueue: Array<string | ArrayBufferLike | Blob | ArrayBufferView> = []

  // 定时器
  private detectionTimer: ReturnType<typeof setTimeout> | null = null
  private timeoutTimer: ReturnType<typeof setTimeout> | null = null
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private pingTimer: ReturnType<typeof setTimeout> | null = null
  private connectionTimer: ReturnType<typeof setTimeout> | null = null // 连接超时定时器

  // 状态标识
  private isConnected: boolean = false
  private isConnecting: boolean = false // 是否正在连接中
  private stopReconnect: boolean = false

  private constructor(options: WebSocketOptions) {
    this.url = options.url || (process.env.VUE_APP_LOGIN_WEBSOCKET as string)
    this.messageHandler = options.messageHandler
    this.reconnectInterval = options.reconnectInterval || 20 * 1000 // 默认20秒
    this.heartbeatInterval = options.heartbeatInterval || 5 * 1000 // 默认5秒
    this.pingInterval = options.pingInterval || 10 * 1000 // 默认10秒
    this.reconnectTimeout = options.reconnectTimeout || 30 * 1000 // 默认30秒
    this.maxReconnectAttempts = options.maxReconnectAttempts || 10 // 默认最多重连10次
    this.connectionTimeout = options.connectionTimeout || 10 * 1000 // 连接超时10秒
  }

  // 单例模式获取实例
  static getInstance(options: WebSocketOptions): WebSocketClient {
    if (!WebSocketClient.instance) {
      WebSocketClient.instance = new WebSocketClient(options)
    } else {
      // 更新消息处理器
      WebSocketClient.instance.messageHandler = options.messageHandler
      // 如果提供了新的URL，则更新并重新连接
      if (options.url && WebSocketClient.instance.url !== options.url) {
        WebSocketClient.instance.url = options.url
        WebSocketClient.instance.reconnectAttempts = 0
        WebSocketClient.instance.init()
      }
    }
    return WebSocketClient.instance
  }

  // 初始化连接
  init(): void {
    if (this.isConnecting) {
      logger.log('正在建立WebSocket连接中...')
      return
    }

    if (this.ws?.readyState === WebSocket.OPEN) {
      logger.warn('WebSocket连接已存在')
      this.flushMessageQueue()
      return
    }

    try {
      this.isConnecting = true
      this.reconnectAttempts = 0 // 重置重连次数
      this.ws = new WebSocket(this.url)

      this.clearTimer('connectionTimer')
      this.connectionTimer = setTimeout(() => {
        logger.error(`WebSocket连接超时 (${this.connectionTimeout}ms)：${this.url}`)
        this.handleConnectionTimeout()
      }, this.connectionTimeout)

      this.ws.onopen = (event) => this.handleOpen(event)
      this.ws.onmessage = (event) => this.handleMessage(event)
      this.ws.onclose = (event) => this.handleClose(event)
      this.ws.onerror = (event) => this.handleError(event)
    } catch (error) {
      logger.error('WebSocket初始化失败:', error)
      this.isConnecting = false
      this.reconnect()
    }
  }

  private handleConnectionTimeout(): void {
    if (this.ws?.readyState !== WebSocket.OPEN) {
      logger.error('WebSocket连接超时，强制关闭连接')
      this.ws?.close(1000, 'Connection timeout')
      this.isConnecting = false
      this.reconnect()
    }
  }

  // 关闭连接
  close(force?: boolean): void {
    this.clearAllTimers()
    this.stopReconnect = true
    this.isConnecting = false

    if (this.ws) {
      // 1000 表示正常关闭
      this.ws.close(force ? 1001 : 1000, force ? 'Force closed' : 'Normal close')
      this.ws = null
    }

    this.isConnected = false
  }

  send(data: string | ArrayBufferLike | Blob | ArrayBufferView, immediate: boolean = false): void {
    if (immediate && (!this.ws || this.ws.readyState !== WebSocket.OPEN)) {
      logger.error('WebSocket未连接，无法立即发送消息')
      return
    }

    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      logger.log('WebSocket未连接，消息已加入队列等待发送')
      this.messageQueue.push(data)
      if (!this.isConnecting && !this.stopReconnect) {
        this.init()
      }
      return
    }

    try {
      this.ws.send(data)
    } catch (error) {
      logger.error('WebSocket发送消息失败:', error)
      this.messageQueue.push(data)
      this.reconnect()
    }
  }

  private flushMessageQueue(): void {
    if (this.messageQueue.length > 0 && this.ws?.readyState === WebSocket.OPEN) {
      logger.log(`发送队列中的${this.messageQueue.length}条消息`)
      while (this.messageQueue.length > 0) {
        const data = this.messageQueue.shift()
        if (data) {
          try {
            this.ws?.send(data)
          } catch (error) {
            logger.error('发送队列消息失败:', error)
            if (data) this.messageQueue.unshift(data)
            break
          }
        }
      }
    }
  }

  private handleOpen(event: Event): void {
    logger.log('WebSocket连接成功', event)
    this.clearTimer('connectionTimer')
    this.isConnected = true
    this.isConnecting = false
    this.stopReconnect = false
    this.reconnectAttempts = 0
    this.startHeartbeat()
    this.startPing()
    this.flushMessageQueue()
  }

  private handleMessage(event: MessageEvent): void {
    logger.log('收到WebSocket消息:', event)
    this.resetHeartbeat()
    this.messageHandler(event)
  }

  private handleClose(event: CloseEvent): void {
    logger.log(
      `WebSocket断开: 代码=${event.code}, 原因=${event.reason}, 干净关闭=${event.wasClean}`
    )

    // 1000 是正常关闭代码
    const isNormalClose = event.code === 1000

    this.isConnected = false
    this.isConnecting = false
    this.clearAllTimers()

    if (!this.stopReconnect && !isNormalClose) {
      this.reconnect()
    }
  }

  private handleError(event: Event): void {
    logger.error('WebSocket连接错误:')
    logger.error('错误事件:', event)
    logger.error(
      '当前连接状态:',
      this.ws?.readyState ? this.getReadyStateText(this.ws.readyState) : '未初始化'
    )

    this.isConnected = false
    this.isConnecting = false

    if (!this.stopReconnect) {
      this.reconnect()
    }
  }

  private getReadyStateText(state: number): string {
    switch (state) {
      case WebSocket.CONNECTING:
        return 'CONNECTING (0) - 正在连接'
      case WebSocket.OPEN:
        return 'OPEN (1) - 已连接'
      case WebSocket.CLOSING:
        return 'CLOSING (2) - 正在关闭'
      case WebSocket.CLOSED:
        return 'CLOSED (3) - 已关闭'
      default:
        return `未知状态 (${state})`
    }
  }

  private startHeartbeat(): void {
    this.clearTimer('detectionTimer')
    this.clearTimer('timeoutTimer')

    this.detectionTimer = setTimeout(() => {
      this.isConnected = this.ws?.readyState === WebSocket.OPEN

      if (!this.isConnected) {
        logger.warn('WebSocket心跳检测失败，尝试重连')
        this.reconnect()

        this.timeoutTimer = setTimeout(() => {
          logger.warn('WebSocket重连超时')
          this.close()
        }, this.reconnectTimeout)
      }
    }, this.heartbeatInterval)
  }

  // 重置心跳检测
  private resetHeartbeat(): void {
    this.clearTimer('detectionTimer')
    this.clearTimer('timeoutTimer')
    this.startHeartbeat()
  }

  private startPing(): void {
    this.clearTimer('pingTimer')

    this.pingTimer = setInterval(() => {
      if (this.ws?.readyState !== WebSocket.OPEN) {
        logger.warn('WebSocket未连接，停止发送ping')
        this.clearTimer('pingTimer')
        this.reconnect()
        return
      }

      try {
        this.ws.send('ping')
        logger.log('发送ping消息')
      } catch (error) {
        logger.error('发送ping消息失败:', error)
        this.clearTimer('pingTimer')
        this.reconnect()
      }
    }, this.pingInterval)
  }

  private reconnect(): void {
    if (this.stopReconnect || this.isConnecting) {
      return
    }

    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      logger.error(`已达到最大重连次数(${this.maxReconnectAttempts})，停止重连`)
      this.close(true)
      return
    }

    this.reconnectAttempts++
    this.stopReconnect = true
    this.close(true)

    const delay = this.calculateReconnectDelay()
    logger.log(
      `将在${delay / 1000}秒后尝试重新连接（第${this.reconnectAttempts}/${this.maxReconnectAttempts}次）`
    )

    this.clearTimer('reconnectTimer')
    this.reconnectTimer = setTimeout(() => {
      logger.log(`尝试重新连接WebSocket（第${this.reconnectAttempts}次）`)
      this.init()
      this.stopReconnect = false
    }, delay)
  }

  // 计算重连延迟 - 指数退避策略
  private calculateReconnectDelay(): number {
    // 基础延迟 + 随机值，避免多个客户端同时重连
    const jitter = Math.random() * 1000 // 0-1秒的随机延迟
    const baseDelay = Math.min(
      this.reconnectInterval * Math.pow(1.5, this.reconnectAttempts - 1),
      this.reconnectInterval * 5
    )
    return baseDelay + jitter
  }

  // 清除指定定时器
  private clearTimer(
    timerName:
      | 'detectionTimer'
      | 'timeoutTimer'
      | 'reconnectTimer'
      | 'pingTimer'
      | 'connectionTimer'
  ): void {
    if (this[timerName]) {
      clearTimeout(this[timerName] as ReturnType<typeof setTimeout>)
      this[timerName] = null
    }
  }

  // 清除所有定时器
  private clearAllTimers(): void {
    this.clearTimer('detectionTimer')
    this.clearTimer('timeoutTimer')
    this.clearTimer('reconnectTimer')
    this.clearTimer('pingTimer')
    this.clearTimer('connectionTimer')
  }

  // 获取当前连接状态
  get isWebSocketConnected(): boolean {
    return this.isConnected
  }

  // 获取当前连接状态文本
  get connectionStatusText(): string {
    if (this.isConnecting) return '正在连接'
    if (this.isConnected) return '已连接'
    if (this.reconnectAttempts > 0 && !this.stopReconnect)
      return `重连中（${this.reconnectAttempts}/${this.maxReconnectAttempts}）`
    return '已断开'
  }

  // 销毁实例
  static destroyInstance(): void {
    if (WebSocketClient.instance) {
      WebSocketClient.instance.close()
      WebSocketClient.instance = null
    }
  }
}
