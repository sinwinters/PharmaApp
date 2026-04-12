import { create } from 'zustand'

interface Toast {
  id: number
  message: string
}

interface ToastState {
  toasts: Toast[]
  push: (message: string) => void
  remove: (id: number) => void
}

export const useToastStore = create<ToastState>((set, get) => ({
  toasts: [],
  push: (message) => {
    const id = Date.now() + Math.floor(Math.random() * 1000)
    set({ toasts: [...get().toasts, { id, message }] })
    window.setTimeout(() => get().remove(id), 2600)
  },
  remove: (id) => set({ toasts: get().toasts.filter((t) => t.id !== id) }),
}))
