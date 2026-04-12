import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  accessToken: string | null
  refreshToken: string | null
  username: string | null
  roleName: string | null
  hasHydrated: boolean
  setTokens: (access: string, refresh: string) => void
  setUserInfo: (username: string, roleName: string) => void
  setHasHydrated: (value: boolean) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      username: null,
      roleName: null,
      hasHydrated: false,
      setTokens: (access, refresh) => set({ accessToken: access, refreshToken: refresh }),
      setUserInfo: (username, roleName) => set({ username, roleName }),
      setHasHydrated: (value) => set({ hasHydrated: value }),
      logout: () => set({ accessToken: null, refreshToken: null, username: null, roleName: null }),
    }),
    {
      name: 'pharma-auth',
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true)
      },
    }
  )
)
