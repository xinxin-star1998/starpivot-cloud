import DOMPurify from 'dompurify'
import { marked } from 'marked'
import {
  formatCodeBlockContent,
  rebuildMarkdownSegments,
  segmentMarkdownByFences
} from '@/utils/ai/format-code-block'

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

function isTableLine(line: string): boolean {
  const trimmed = line.trim()
  return trimmed.startsWith('|') && trimmed.endsWith('|') && trimmed.length > 2
}

function countTableColumns(row: string): number {
  const trimmed = row.trim()
  const cells = trimmed.split('|').map((cell) => cell.trim())
  if (cells.length && cells[0] === '') {
    cells.shift()
  }
  if (cells.length && cells[cells.length - 1] === '') {
    cells.pop()
  }
  return cells.length
}

function isTableSeparatorRow(row: string): boolean {
  if (!isTableLine(row)) {
    return false
  }
  const cells = row
    .trim()
    .split('|')
    .map((cell) => cell.trim())
    .filter((cell) => cell.length > 0)
  return cells.length > 0 && cells.every((cell) => /^:?-{3,}:?$/.test(cell))
}

function isEmptyTableRow(row: string): boolean {
  if (!isTableLine(row)) {
    return false
  }
  const cells = row
    .trim()
    .split('|')
    .map((cell) => cell.trim())
    .filter((cell) => cell.length > 0)
  return cells.length === 0
}

function buildTableSeparator(columnCount: number): string {
  const cells = Array.from({ length: columnCount }, () => '------')
  return `| ${cells.join(' | ')} |`
}

/** 修复缺失/损坏的 GFM 表格分隔行，并移除空管道行 */
function normalizeMarkdownTables(text: string): string {
  const lines = text.split('\n')
  const out: string[] = []
  let index = 0

  while (index < lines.length) {
    const line = lines[index]
    if (!isTableLine(line) || isEmptyTableRow(line)) {
      if (!isEmptyTableRow(line)) {
        out.push(line)
      }
      index++
      continue
    }

    const block: string[] = []
    while (index < lines.length) {
      const current = lines[index]
      if (isEmptyTableRow(current)) {
        index++
        continue
      }
      if (!isTableLine(current)) {
        break
      }
      block.push(current.trim())
      index++
    }

    if (block.length === 0) {
      continue
    }

    const header = block[0]
    const columnCount = countTableColumns(header)
    if (columnCount === 0) {
      out.push(...block)
      continue
    }

    out.push(header)

    let dataStart = 1
    if (block.length > 1 && isTableSeparatorRow(block[1])) {
      out.push(buildTableSeparator(columnCount))
      dataStart = 2
    } else {
      out.push(buildTableSeparator(columnCount))
    }

    for (let rowIndex = dataStart; rowIndex < block.length; rowIndex++) {
      if (!isTableSeparatorRow(block[rowIndex])) {
        out.push(block[rowIndex])
      }
    }
  }

  return out.join('\n')
}

function normalizeLinePreservingTables(line: string): string {
  if (isTableLine(line)) {
    return line
  }

  return line
    .replace(/([^\n-])(-{3,})(?![-\n])/g, '$1\n\n$2\n\n')
    .replace(/([。！？])([^。！？\n#\-\s`])/g, '$1\n\n$2')
}

/** 仅处理围栏外的 Markdown 文本，避免破坏代码块 */
function normalizeMarkdownText(content: string): string {
  let text = content
  if (!text) {
    return text
  }

  text = text.replace(/\((JavaScript|Python|YAML|SQL|JSON|TypeScript)\)/gi, '\n')
  text = text.replace(/([^\n#])(#{1,6})(?=\S)/g, '$1\n\n$2')
  text = text.replace(/^(#{1,6})([^\s#\n])/gm, '$1 $2')
  text = text.replace(/(#{1,6})([\d])/g, '$1 $2')
  text = text.replace(/(#{1,6}\s*\d+\.)(?=\S)/g, '$1 ')
  text = text.replace(/([^\n\d])(\d+)\.\s+/g, '$1\n$2. ')
  text = text.replace(/(\*\*[^*\n]{1,24}\*\*：)(?=\S)/g, '$1\n\n')

  text = text
    .split('\n')
    .map((line) => normalizeLinePreservingTables(line))
    .join('\n')

  const pseudoFence = new RegExp(`(?<![\`\\w])(${CODE_LANGUAGES})(#|//|--)(?=[^\\n])`, 'gi')
  text = text.replace(pseudoFence, (_, lang: string, prefix: string) => {
    return `\n\n\`\`\`${lang.toLowerCase()}\n${prefix}`
  })

  text = text.replace(/([^\n])```/g, '$1\n\n```')
  text = text.replace(/([^\n|])\n(\|)/g, '$1\n\n$2')
  text = normalizeMarkdownTables(text)
  return text
}

function processMarkdownContent(content: string): string {
  const text = normalizeLineEndings(content)
  if (!text) {
    return text
  }

  const segments = segmentMarkdownByFences(text)
  const hasCodeFence = segments.some((segment) => segment.kind === 'code')

  if (!hasCodeFence) {
    return normalizeMarkdownText(text)
  }

  const processed = segments.map((segment) => {
    if (segment.kind === 'text') {
      return { ...segment, value: normalizeMarkdownText(segment.value) }
    }
    return {
      ...segment,
      value: formatCodeBlockContent(segment.value, segment.lang, segment.closed)
    }
  })

  return rebuildMarkdownSegments(processed)
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

function prepareStreamingMarkdown(content: string, streaming: boolean): string {
  if (!streaming) {
    return processMarkdownContent(content)
  }

  let prepared = normalizeLineEndings(content)
  if (!prepared) {
    return prepared
  }

  const fenceCount = (prepared.match(/```/g) || []).length
  if (fenceCount % 2 === 1) {
    prepared += '\n```'
  }
  return balanceCodeFences(prepared)
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

export function stripChatCopyContent(content: string): string {
  return content
    .replace(/\n\n---\n\*已停止生成\*\s*$/g, '')
    .replace(/\n\n---\n\*生成超时，请重试\*\s*$/g, '')
    .replace(/^\*已停止生成\*\s*$/g, '')
    .replace(/^\*生成超时，请重试\*\s*$/g, '')
    .trim()
}
