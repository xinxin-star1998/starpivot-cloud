const CLIKE_KEYWORDS = [
  'synchronized',
  'implements',
  'instanceof',
  'abstract',
  'continue',
  'protected',
  'interface',
  'volatile',
  'transient',
  'namespace',
  'constexpr',
  'function',
  'private',
  'public',
  'static',
  'return',
  'switch',
  'throws',
  'throw',
  'while',
  'break',
  'catch',
  'class',
  'const',
  'boolean',
  'await',
  'async',
  'false',
  'final',
  'float',
  'double',
  'short',
  'super',
  'this',
  'true',
  'byte',
  'case',
  'char',
  'else',
  'enum',
  'void',
  'long',
  'null',
  'for',
  'try',
  'new',
  'int',
  'let',
  'var',
  'def',
  'if',
  'do',
  'go',
  'fn'
]

const CLIKE_LANGS = new Set([
  '',
  'java',
  'javascript',
  'js',
  'typescript',
  'ts',
  'tsx',
  'jsx',
  'c',
  'cpp',
  'csharp',
  'cs',
  'go',
  'rust',
  'kotlin',
  'swift',
  'scala',
  'php',
  'python',
  'py'
])

const INDENT_UNIT = '  '

const MASHED_CODE_PATTERN =
  /publicstatic|publicclass|privatestatic|protectedstatic|void[a-z]|for\(int|int\[\][a-z]|boolean[a-z]|return[a-z]|newint|newlong|booleanswapped/i

interface ScanState {
  inString: '"' | "'" | null
  stringEscape: boolean
  inLineComment: boolean
  inBlockComment: boolean
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/** 检测代码是否被挤成连写（缺空格） */
export function shouldFormatCode(code: string): boolean {
  const trimmed = code.trim()
  if (!trimmed || trimmed.length < 12) {
    return false
  }

  const compact = trimmed.replace(/\s+/g, '')
  if (MASHED_CODE_PATTERN.test(compact)) {
    return true
  }

  const spaceCount = (trimmed.match(/\s/g) || []).length
  const ratio = spaceCount / trimmed.length
  const hasBraces = /[{}();]/.test(trimmed)
  return hasBraces && ratio < 0.035
}

/** 检测是否需要自动换行/缩进（单行过长、行均宽过大，或多行但语句仍挤在一行） */
export function shouldLayoutCode(code: string): boolean {
  const trimmed = code.trim()
  if (!trimmed || trimmed.length < 28 || !/[{};]/.test(trimmed)) {
    return false
  }

  const lines = trimmed.split('\n').filter((line) => line.trim())
  if (lines.some((line) => line.length > 56 && /[{};]/.test(line))) {
    return true
  }

  if (lines.length <= 1) {
    return trimmed.length > 48
  }

  const averageWidth = trimmed.length / lines.length
  return averageWidth > 72
}

function splitGluedKeywords(code: string): string {
  const keywords = [...CLIKE_KEYWORDS].sort((a, b) => b.length - a.length)
  let result = code

  for (const keyword of keywords) {
    const pattern = new RegExp(`(${escapeRegExp(keyword)})(?=[A-Za-z_])`, 'gi')
    result = result.replace(pattern, '$1 ')
  }

  return result
}

function spaceAroundDelimiters(code: string): string {
  let result = code

  result = result.replace(/\b(for|if|while|switch|catch|else)\(/gi, '$1 (')
  result = result.replace(/\{(?=[A-Za-z_])/g, '{ ')
  result = result.replace(/([^\s{}();,[\]])\{/g, '$1 {')
  result = result.replace(/\}([^\s;,)\]}])/g, '} $1')
  result = result.replace(/;(?=[A-Za-z_])/g, '; ')
  result = result.replace(/,(?=[A-Za-z_0-9"'([])/g, ', ')
  result = result.replace(/(\])(?=[A-Za-z_])/g, '$1 ')
  result = result.replace(/([A-Za-z_0-9\])])(?=\{)/g, '$1 ')

  return result
}

function spaceAroundOperators(code: string): string {
  let result = code

  const compoundOps = ['<<=', '>>=', '===', '!==', '<=', '>=', '==', '!=', '&&', '||', '+=', '-=', '*=', '/=']
  for (const op of compoundOps) {
    const pattern = new RegExp(`([^\\s])${escapeRegExp(op)}([^\\s])`, 'g')
    result = result.replace(pattern, `$1 ${op} $2`)
    const startPattern = new RegExp(`^${escapeRegExp(op)}([^\\s])`, 'g')
    result = result.replace(startPattern, `${op} $1`)
    const endPattern = new RegExp(`([^\\s])${escapeRegExp(op)}$`, 'g')
    result = result.replace(endPattern, `$1 ${op}`)
  }

  result = result.replace(/([^<>\s])<([^=>\s])/g, '$1 < $2')
  result = result.replace(/([^<>\s])>([^=\s])/g, '$1 > $2')
  result = result.replace(/([A-Za-z_0-9\])])=(?=[A-Za-z_0-9"'([])/g, '$1 = ')
  result = result.replace(/=\s+(?=[0-9])/g, '= ')

  return result
}

function tidySpacing(code: string): string {
  return code
    .replace(/\(\s+/g, '(')
    .replace(/\s+\)/g, ')')
    .replace(/\[\s+/g, '[')
    .replace(/\s+\]/g, ']')
}

function collapseExtraSpaces(code: string): string {
  return code
    .split('\n')
    .map((line) => line.replace(/ {2,}/g, ' ').trimEnd())
    .join('\n')
}

function createScanState(): ScanState {
  return {
    inString: null,
    stringEscape: false,
    inLineComment: false,
    inBlockComment: false
  }
}

function skipWhitespace(source: string, index: number): number {
  let i = index
  while (i < source.length && /\s/.test(source[i])) {
    i++
  }
  return i
}

function readFollowBraceClause(source: string, start: number): { text: string; nextIndex: number; openBrace: boolean } {
  let text = ''
  let i = start
  let parenDepth = 0
  let openBrace = false

  while (i < source.length) {
    const ch = source[i]

    if (ch === '(') {
      parenDepth++
      text += ch
      i++
      continue
    }

    if (ch === ')') {
      parenDepth = Math.max(0, parenDepth - 1)
      text += ch
      i++
      continue
    }

    if (ch === '{' && parenDepth === 0) {
      text += ' {'
      openBrace = true
      i++
      break
    }

    text += ch
    i++
  }

  return { text: text.trimEnd(), nextIndex: i, openBrace }
}

function consumeToken(source: string, index: number, state: ScanState): { text: string; nextIndex: number } {
  let text = ''
  let i = index

  while (i < source.length) {
    const ch = source[i]
    const next = source[i + 1] ?? ''

    if (state.inLineComment) {
      text += ch
      if (ch === '\n') {
        state.inLineComment = false
      }
      i++
      continue
    }

    if (state.inBlockComment) {
      text += ch
      if (ch === '*' && next === '/') {
        text += '/'
        i += 2
        state.inBlockComment = false
        continue
      }
      i++
      continue
    }

    if (state.inString) {
      text += ch
      if (state.stringEscape) {
        state.stringEscape = false
      } else if (ch === '\\') {
        state.stringEscape = true
      } else if (ch === state.inString) {
        state.inString = null
      }
      i++
      continue
    }

    if (ch === '/' && next === '/') {
      state.inLineComment = true
      text += ch
      i++
      continue
    }

    if (ch === '/' && next === '*') {
      state.inBlockComment = true
      text += ch
      i++
      continue
    }

    if (ch === '"' || ch === "'") {
      state.inString = ch
      text += ch
      i++
      continue
    }

    if (ch === '(' || ch === ')' || ch === '{' || ch === '}' || ch === ';') {
      break
    }

    text += ch
    i++
  }

  return { text, nextIndex: i }
}

/** 按花括号/分号自动换行并缩进（2 空格） */
export function layoutCLikeCode(code: string): string {
  const flattened = code
    .replace(/\r\n/g, '\n')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .join(' ')

  if (!flattened) {
    return code
  }

  const lines: string[] = []
  let currentLine = ''
  let indent = 0
  let parenDepth = 0
  const state = createScanState()

  const pushLine = (content: string, level = indent) => {
    const trimmed = content.trim()
    if (trimmed) {
      lines.push(INDENT_UNIT.repeat(Math.max(0, level)) + trimmed)
    }
    currentLine = ''
  }

  let i = 0
  while (i < flattened.length) {
    i = skipWhitespace(flattened, i)
    if (i >= flattened.length) {
      break
    }

    const ch = flattened[i]

    if (ch === '(') {
      parenDepth++
      currentLine += '('
      i++
      continue
    }

    if (ch === ')') {
      parenDepth = Math.max(0, parenDepth - 1)
      currentLine += ')'
      i++
      continue
    }

    if (ch === ';') {
      currentLine += ';'
      if (parenDepth === 0) {
        pushLine(currentLine)
      } else {
        currentLine += ' '
      }
      i++
      continue
    }

    if (ch === '{') {
      const header = `${currentLine.trimEnd()} {`.trim()
      pushLine(header, indent)
      indent++
      currentLine = ''
      i++
      continue
    }

    if (ch === '}') {
      if (currentLine.trim()) {
        pushLine(currentLine)
      }

      indent = Math.max(0, indent - 1)
      let clause = '}'
      i++

      i = skipWhitespace(flattened, i)
      const tail = flattened.slice(i)
      const keywordMatch = tail.match(/^(else|catch|finally|while)\b/)
      if (keywordMatch) {
        const keyword = keywordMatch[1]
        i += keyword.length
        const follow = readFollowBraceClause(flattened, i)
        clause += ` ${keyword}${follow.text ? ` ${follow.text}` : ''}`
        i = follow.nextIndex
        if (follow.openBrace) {
          lines.push(INDENT_UNIT.repeat(indent) + clause)
          indent++
          currentLine = ''
          continue
        }
      }

      lines.push(INDENT_UNIT.repeat(indent) + clause)
      currentLine = ''
      continue
    }

    const token = consumeToken(flattened, i, state)
    currentLine += token.text
    i = token.nextIndex
  }

  if (currentLine.trim()) {
    pushLine(currentLine)
  }

  return lines.join('\n')
}

/** 将连写的 C 系语言代码补全空格并排版 */
export function formatCLikeCode(code: string, options?: { layout?: boolean }): string {
  const needsSpace = shouldFormatCode(code)
  const needsLayout =
    options?.layout !== false &&
    (shouldLayoutCode(code) || (needsSpace && /{/.test(code)))

  if (!needsSpace && !needsLayout) {
    return code
  }

  let formatted = code
  if (needsSpace) {
    formatted = splitGluedKeywords(formatted)
    formatted = spaceAroundDelimiters(formatted)
    formatted = spaceAroundOperators(formatted)
    formatted = tidySpacing(formatted)
    formatted = collapseExtraSpaces(formatted)
  }

  if (needsLayout) {
    formatted = layoutCLikeCode(formatted)
  }

  return formatted
}

export function formatCodeBlockContent(code: string, lang = '', closed = true): string {
  const language = lang.trim().toLowerCase()
  if (!CLIKE_LANGS.has(language)) {
    return code
  }

  return formatCLikeCode(code, { layout: closed })
}

export type MarkdownSegment =
  | { kind: 'text'; value: string }
  | { kind: 'code'; lang: string; value: string; closed: boolean }

/** 按围栏切分 Markdown，未闭合的流式代码块也会识别 */
export function segmentMarkdownByFences(content: string): MarkdownSegment[] {
  const source = content.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  const segments: MarkdownSegment[] = []
  const fencePattern = /```([\w-]*)\n/g
  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = fencePattern.exec(source)) !== null) {
    if (match.index > lastIndex) {
      segments.push({ kind: 'text', value: source.slice(lastIndex, match.index) })
    }

    const lang = match[1] || ''
    const codeStart = match.index + match[0].length
    const closeIndex = source.indexOf('\n```', codeStart)

    if (closeIndex === -1) {
      segments.push({ kind: 'code', lang, value: source.slice(codeStart), closed: false })
      lastIndex = source.length
      break
    }

    segments.push({ kind: 'code', lang, value: source.slice(codeStart, closeIndex), closed: true })
    lastIndex = closeIndex + 4
    fencePattern.lastIndex = lastIndex
  }

  if (lastIndex < source.length) {
    segments.push({ kind: 'text', value: source.slice(lastIndex) })
  }

  return segments.length ? segments : [{ kind: 'text', value: source }]
}

export function rebuildMarkdownSegments(segments: MarkdownSegment[]): string {
  return segments
    .map((segment) => {
      if (segment.kind === 'text') {
        return segment.value
      }
      const langPart = segment.lang ? segment.lang : ''
      const closing = segment.closed ? '\n```' : ''
      return `\`\`\`${langPart}\n${segment.value}${closing}`
    })
    .join('')
}
