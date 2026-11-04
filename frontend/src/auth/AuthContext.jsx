import { useEffect, useState } from 'react';
import * as authApi from '../api/auth';
import { AuthCtx } from './context';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(() => Boolean(localStorage.getItem('token')));

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;
    let active = true;
    authApi.getMe()
      .then(user => { if (active) setUser(user); })
      .catch(() => localStorage.removeItem('token'))
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
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
