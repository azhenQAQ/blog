/**
 * 复制文本到剪贴板。优先 Clipboard API（需安全上下文），自动降级到 execCommand。
 */
export async function copyText(text: string): Promise<void> {
  if (navigator.clipboard && globalThis.isSecureContext) {
    await navigator.clipboard.writeText(text)
    return
  }
  // fallback: legacy execCommand 方案，兼容 HTTP 环境
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  document.body.removeChild(textarea)
}
