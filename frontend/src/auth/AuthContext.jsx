import { createContext, useContext, useEffect, useState } from 'react';
import * as authApi from '../api/auth';

const AuthCtx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) { setLoading(false); return; }
    authApi.getMe()
      .then(setUser)
      .catch(() => localStorage.removeItem('token'))
      .finally(() => setLoading(false));
  }, []);

  async function doLogin(username, password) {
    const u = await authApi.login(username, password);
    setUser(u);
    return u;
  }

  async function doLogout() {
    await authApi.logout();
    setUser(null);
    window.location.assign('/login');
  }

  return (
    <AuthCtx.Provider value={{ user, loading, doLogin, doLogout }}>
      {children}
    </AuthCtx.Provider>
  );
}

export const useAuth = () => useContext(AuthCtx);