import { reactive } from 'vue'

export interface ConfirmOptions {
  title?: string
  message: string
}

interface ConfirmState {
  visible: boolean
  title: string
  message: string
  resolve: ((value: boolean) => void) | null
}

const state = reactive<ConfirmState>({
  visible: false,
  title: '确认操作',
  message: '',
  resolve: null,
})

export function useConfirm() {
  function showConfirm(raw: ConfirmOptions | string): Promise<boolean> {
    const options: ConfirmOptions = typeof raw === 'string' ? { message: raw } : raw

    state.title = options.title ?? '确认操作'
    state.message = options.message
    state.visible = true

    return new Promise<boolean>((resolve) => {
      state.resolve = resolve
    })
  }

  return { showConfirm }
}

/** 供 ConfirmDialog 绑定 */
export { state as _confirmState }
