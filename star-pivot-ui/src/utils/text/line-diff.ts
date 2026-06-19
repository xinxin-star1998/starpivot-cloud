export type DiffLineType = 'equal' | 'insert' | 'delete'

export interface DiffLine {
  type: DiffLineType
  oldLineNum?: number
  newLineNum?: number
  content: string
}

/** 按行对比两段文本，返回 unified diff 行列表 */
export function diffLines(oldText: string, newText: string): DiffLine[] {
  const oldLines = splitLines(oldText)
  const newLines = splitLines(newText)
  const lcs = buildLcsTable(oldLines, newLines)
  const ops = backtrack(oldLines, newLines, lcs)
  const result: DiffLine[] = []
  let oldNum = 1
  let newNum = 1

  for (const op of ops) {
    if (op === 'equal') {
      result.push({
        type: 'equal',
        oldLineNum: oldNum,
        newLineNum: newNum,
        content: oldLines[oldNum - 1] ?? ''
      })
      oldNum++
      newNum++
    } else if (op === 'delete') {
      result.push({
        type: 'delete',
        oldLineNum: oldNum,
        content: oldLines[oldNum - 1] ?? ''
      })
      oldNum++
    } else {
      result.push({
        type: 'insert',
        newLineNum: newNum,
        content: newLines[newNum - 1] ?? ''
      })
      newNum++
    }
  }
  return result
}

function splitLines(text: string): string[] {
  if (!text) return []
  return text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n')
}

function buildLcsTable(a: string[], b: string[]): number[][] {
  const m = a.length
  const n = b.length
  const dp: number[][] = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0))
  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (a[i - 1] === b[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1] + 1
      } else {
        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1])
      }
    }
  }
  return dp
}

function backtrack(a: string[], b: string[], dp: number[][]): Array<'equal' | 'insert' | 'delete'> {
  const ops: Array<'equal' | 'insert' | 'delete'> = []
  let i = a.length
  let j = b.length
  while (i > 0 || j > 0) {
    if (i > 0 && j > 0 && a[i - 1] === b[j - 1]) {
      ops.push('equal')
      i--
      j--
    } else if (j > 0 && (i === 0 || dp[i][j - 1] >= dp[i - 1][j])) {
      ops.push('insert')
      j--
    } else {
      ops.push('delete')
      i--
    }
  }
  ops.reverse()
  return ops
}
