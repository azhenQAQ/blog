/** 歌词行 */
export interface LRCLine {
  time: number
  text: string
}

/**
 * 解析 LRC 文本，返回按时间排序的歌词行数组
 * @param lrcText - LRC 格式文本
 * @returns 按时间排序的歌词行数组
 */
export function parseLRC(lrcText: string): LRCLine[] {
  const lines: LRCLine[] = []
  const timeRegex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]/g

  // 按行处理
  const rawLines = lrcText.split(/\r?\n/)

  for (const rawLine of rawLines) {
    const trimmed = rawLine.trim()
    if (!trimmed) continue

    // 找出所有时间标签
    const times: number[] = []
    let match: RegExpExecArray | null
    let lastIndex = 0

    while ((match = timeRegex.exec(trimmed)) !== null) {
      const minutes = parseInt(match[1], 10)
      const seconds = parseInt(match[2], 10)
      const centiseconds = match[3] ? parseInt(match[3], 10) : 0
      // 处理 [mm:ss] 或 [mm:ss.xx] 或 [mm:ss.xxx]
      const fraction = match[3]?.length === 3 ? centiseconds / 1000 : centiseconds / 100
      const time = minutes * 60 + seconds + fraction
      times.push(time)
      lastIndex = match.index + match[0].length
    }

    if (times.length === 0) continue

    // 提取歌词文本（过滤元数据标签 [ti:], [ar:], [by:], [al:], [offset:] 等）
    const textPart = trimmed.slice(lastIndex).trim()
    if (!textPart) continue
    if (/^[\w-]+:/.test(textPart)) continue

    // 为每个时间标签添加歌词行
    for (const time of times) {
      lines.push({ time, text: textPart })
    }
  }

  // 按时间排序
  lines.sort((a, b) => a.time - b.time)
  return lines
}

/**
 * 给定当前播放时间和歌词行数组，返回当前应该高亮的行索引
 * @param lines - 按时间排序的歌词行数组
 * @param currentTime - 当前播放时间（秒）
 * @returns 当前歌词行索引，无匹配时返回 0
 */
export function getCurrentLineIndex(lines: LRCLine[], currentTime: number): number {
  if (lines.length === 0) return 0

  let left = 0
  let right = lines.length - 1
  let result = 0

  while (left <= right) {
    const mid = Math.floor((left + right) / 2)
    if (lines[mid].time <= currentTime) {
      result = mid
      left = mid + 1
    } else {
      right = mid - 1
    }
  }

  return result
}
