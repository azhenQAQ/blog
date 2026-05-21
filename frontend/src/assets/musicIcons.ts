/**
 * 播放器 SVG 图标定义 (viewBox="0 0 24 24")
 * fill=true  → 实心填充图标（prev/play/pause/next）
 * fill=false → 描边轮廓图标（mode/volume/note/playlist）
 */
export interface IconDef {
  viewBox: string
  d: string
  fill: boolean
}

export const musicIcons: Record<string, IconDef> = {
  prev: {
    viewBox: '0 0 24 24',
    d: 'M16 5 L9 12 L16 19 Z',
    fill: true,
  },
  play: {
    viewBox: '0 0 24 24',
    d: 'M7 5 L18 12 L7 19 Z',
    fill: true,
  },
  pause: {
    viewBox: '0 0 24 24',
    d: 'M7 5h3v14H7z M14 5h3v14h-3z',
    fill: true,
  },
  next: {
    viewBox: '0 0 24 24',
    d: 'M8 5 L15 12 L8 19 Z',
    fill: true,
  },
  'mode-list': {
    viewBox: '0 0 24 24',
    d: 'm17 2 4 4-4 4 M3 11v-1a8 8 0 0 1 14-4.5 M7 22l-4-4 4-4 M21 13v1a8 8 0 0 1-14 4.5',
    fill: false,
  },
  'mode-single': {
    viewBox: '0 0 24 24',
    d: 'm17 2 4 4-4 4 M3 11v-1a8 8 0 0 1 14-4.5 M7 22l-4-4 4-4 M21 13v1a8 8 0 0 1-14 4.5 M11 10v4 M10 10h2',
    fill: false,
  },
  'mode-shuffle': {
    viewBox: '0 0 24 24',
    d: 'M3 7h3l5 6 5-6h3 M21 17h-3l-5-6-5 6h-3',
    fill: false,
  },
  'volume-on': {
    viewBox: '0 0 24 24',
    d: 'M11 5L6 9H2v6h4l5 4z M16 9.5a3 3 0 0 1 0 5 M19.5 7.5a6 6 0 0 1 0 9',
    fill: false,
  },
  'volume-mute': {
    viewBox: '0 0 24 24',
    d: 'M11 5L6 9H2v6h4l5 4z M16 9l5 5 M21 9l-5 5',
    fill: false,
  },
  'music-note': {
    viewBox: '0 0 24 24',
    d: 'M9 18V5l12-2v13 M9 18a3 3 0 1 1-6 0 3 3 0 0 1 6 0z M21 16a3 3 0 1 1-6 0 3 3 0 0 1 6 0z',
    fill: false,
  },
  playlist: {
    viewBox: '0 0 24 24',
    d: 'M4 6h16 M4 12h16 M4 18h12',
    fill: false,
  },
}
