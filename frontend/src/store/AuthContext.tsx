import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { UserInfo, RoleCode } from '@/types'
import { getCurrentUser, clearAuthStorage } from '@/api'

interface AuthContextValue {
  user: UserInfo | null
  setUser: (user: UserInfo | null) => void
  logout: () => void
  hasRole: (roles: RoleCode[]) => boolean
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(getCurrentUser())

  useEffect(() => {
    const u = getCurrentUser()
    if (u) setUser(u)
  }, [])

  const logout = () => {
    clearAuthStorage()
    setUser(null)
  }

  const hasRole = (roles: RoleCode[]) => {
    if (!user) return false
    return roles.includes(user.role)
  }

  return (
    <AuthContext.Provider value={{ user, setUser, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = (): AuthContextValue => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
