class Logger {
  private isDevelopment = import.meta.env.DEV
  private prefix = '[StarPivot]'

  log(...args: unknown[]) {
    if (this.isDevelopment) {
      console.log(this.prefix, ...args)
    }
  }

  warn(...args: unknown[]) {
    console.warn(this.prefix, ...args)
  }

  error(...args: unknown[]) {
    console.error(this.prefix, ...args)
    if (!this.isDevelopment) {
      this.sendToErrorMonitoring(args)
    }
  }

  info(...args: unknown[]) {
    if (this.isDevelopment) {
      console.info(this.prefix, ...args)
    }
  }

  debug(...args: unknown[]) {
    if (this.isDevelopment) {
      console.debug(this.prefix, ...args)
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  private sendToErrorMonitoring(args: unknown[]) {}
}

export const logger = new Logger()
