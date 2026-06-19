export interface PreviewTreeNode {
  id: string
  label: string
  fullPath?: string
  isFile?: boolean
  children?: PreviewTreeNode[]
}

export function buildPreviewPathTree(paths: string[]): PreviewTreeNode[] {
  const roots: PreviewTreeNode[] = []

  for (const filePath of paths) {
    const segments = filePath.split('/').filter(Boolean)
    let level = roots
    let prefix = ''

    segments.forEach((segment, index) => {
      const isFile = index === segments.length - 1
      prefix = prefix ? `${prefix}/${segment}` : segment

      let node = level.find((item) => item.label === segment && Boolean(item.isFile) === isFile)
      if (!node) {
        node = {
          id: isFile ? filePath : prefix,
          label: segment,
          fullPath: isFile ? filePath : undefined,
          isFile,
          children: isFile ? undefined : []
        }
        level.push(node)
      } else if (isFile) {
        node.fullPath = filePath
      }

      if (!isFile && node.children) {
        level = node.children
      }
    })
  }

  return sortTreeNodes(roots)
}

function sortTreeNodes(nodes: PreviewTreeNode[]): PreviewTreeNode[] {
  nodes.sort((a, b) => {
    if (Boolean(a.isFile) !== Boolean(b.isFile)) {
      return a.isFile ? 1 : -1
    }
    return a.label.localeCompare(b.label)
  })
  nodes.forEach((node) => {
    if (node.children?.length) {
      sortTreeNodes(node.children)
    }
  })
  return nodes
}
