import DOMPurify from 'dompurify'
import { marked } from 'marked'

const CODE_LANGUAGES =
  'python|javascript|yaml|sql|json|java|typescript|tsx|jsx|bash|shell|go|rust|vue|html|css|markdown|md'

const SAFE_LINK_PATTERN = /^(https?:|mailto:|#)/i

let sanitizeHookRegistered = false

function ensureSanitizeHooks(): void {
  if (sanitizeHookRegistered) {
    return
  }
  sanitizeHookRegistered = true
  DOMPurify.addHook('afterSanitizeAttributes', (node) => {
    if (node.tagName !== 'A') {
      return
    }
    const href = node.getAttribute('href')
    if (href && !SAFE_LINK_PATTERN.test(href.trim())) {
      node.removeAttribute('href')
    } else if (href) {
      node.setAttribute('rel', 'noopener noreferrer')
      node.setAttribute('target', '_blank')
    }
  })
}

marked.setOptions({
  gfm: true,
  breaks: true
})

function sanitizeHtml(html: string): string {
  ensureSanitizeHooks()
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS: [
      'p',
      'br',
      'strong',
      'em',
      'b',
      'i',
      'u',
      's',
      'del',
      'code',
      'pre',
      'blockquote',
      'ul',
      'ol',
      'li',
      'h1',
      'h2',
      'h3',
      'h4',
      'h5',
      'h6',
      'a',
      'hr',
      'table',
      'thead',
      'tbody',
      'tr',
      'th',
      'td',
      'span',
      'div'
    ],
    ALLOWED_ATTR: ['href', 'title', 'class', 'target', 'rel'],
    ALLOW_DATA_ATTR: false
  })
}

function normalizeLineEndings(content: string): string {
  return content.replace(/\r\n/g, '\n').replace(/\r/g, '\n').trim()
}

/** AI 流式输出常把 Markdown 挤在一行，补全块级换行与伪代码围栏 */
function normalizeAiMarkdown(content: string): string {
  let text = normalizeLineEndings(content)
  if (!text) {
    return text
  }

  // 去掉括号内的语言标注，避免贴在代码前
  text = text.replace(/\((JavaScript|Python|YAML|SQL|JSON|TypeScript)\)/gi, '\n')

  // --- 分隔线
  text = text.replace(/([^\n-])(-{3,})(?![-\n])/g, '$1\n\n$2\n\n')

  // 标题前补换行（###、## 贴在前文后）
  text = text.replace(/([^\n#])(#{1,6})(?=\S)/g, '$1\n\n$2')
  // ###1. → ### 1.
  text = text.replace(/(#{1,6})([\d])/g, '$1 $2')

  // 中文句号后适当断行（须在伪代码围栏之前，避免破坏代码块）
  text = text.replace(/([。！？])([^。！？\n#\-\s`])/g, '$1\n\n$2')

  // 伪代码块：python# / javascript// / sql-- / yaml# 等
  const pseudoFence = new RegExp(`(?<![\`\\w])(${CODE_LANGUAGES})(#|//|--)(?=[^\\n])`, 'gi')
  text = text.replace(pseudoFence, (_, lang: string, prefix: string) => {
    return `\n\n\`\`\`${lang.toLowerCase()}\n${prefix}`
  })

  // 标准围栏前补换行
  text = text.replace(/([^\n])```/g, '$1\n\n```')

  return balanceCodeFences(text)
}

function balanceCodeFences(content: string): string {
  const lines = content.split('\n')
  const out: string[] = []
  let open = false

  for (const line of lines) {
    const trimmed = line.trim()

    if (/^```[\w-]*$/.test(trimmed) || trimmed === '```') {
      if (open && trimmed === '```') {
        open = false
      } else if (!open) {
        open = true
      }
      out.push(line)
      continue
    }

    if (open && /^#{1,6}\s/.test(trimmed)) {
      out.push('```')
      open = false
    }

    out.push(line)
  }

  if (open) {
    out.push('```')
  }

  return out.join('\n')
}

/** 流式输出时补全未闭合的代码块，避免 Markdown 解析抖动 */
function prepareStreamingMarkdown(content: string, streaming: boolean): string {
  let prepared = normalizeAiMarkdown(content)
  if (!streaming || !prepared) {
    return prepared
  }

  const fenceCount = (prepared.match(/```/g) || []).length
  if (fenceCount % 2 === 1) {
    prepared += '\n```'
  }
  return prepared
}

export function renderChatMarkdown(content: string, streaming = false): string {
  if (!content) {
    return ''
  }

  const source = prepareStreamingMarkdown(content, streaming)
  const html = marked.parse(source, { async: false }) as string
  return sanitizeHtml(html)
}

export function isAbortError(error: unknown): boolean {
  if (!error || typeof error !== 'object') {
    return false
  }
  const name = (error as { name?: string }).name
  return name === 'AbortError'
}

/** 复制回答时去掉停止生成等尾部提示 */
export function stripChatCopyContent(content: string): string {
  return content
    .replace(/\n\n---\n\*已停止生成\*\s*$/g, '')
    .replace(/\n\n---\n\*生成超时，请重试\*\s*$/g, '')
    .replace(/^\*已停止生成\*\s*$/g, '')
    .replace(/^\*生成超时，请重试\*\s*$/g, '')
    .trim()
}
