export interface HeadingItem {
  id: string
  text: string
  level: 2 | 3
  children: HeadingItem[]
}

export function slugify(text: string): string {
  return text
    .trim()
    .toLowerCase()
    .replace(/[（(](.*?)[）)]/g, '$1')
    .replace(/[^\w一-鿿぀-ゟ゠-ヿ가-힯-]/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
}

export function parseHeadings(markdown: string): HeadingItem[] {
  const headingRegex = /^(#{2,3})\s+(.+)$/gm
  const result: HeadingItem[] = []
  const stack: HeadingItem[] = []

  let match: RegExpExecArray | null
  while ((match = headingRegex.exec(markdown)) !== null) {
    const level = match[1].length as 2 | 3
    const text = match[2].trim().replace(/[\\*_`[\]()#>~|-]/g, '').replace(/\{.*?\}/g, '')
    const id = slugify(text) || 'heading'

    const item: HeadingItem = { id, text, level, children: [] }

    if (level === 2) {
      result.push(item)
      stack.length = 0
      stack.push(item)
    } else if (level === 3 && stack.length > 0) {
      stack[stack.length - 1].children.push(item)
    }
  }

  return result
}
