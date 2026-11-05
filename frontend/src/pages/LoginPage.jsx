import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/context';

export default function LoginPage() {
  const { doLogin } = useAuth();
  const nav = useNavigate();
  const loc = useLocation();
  const from = loc.state?.from?.pathname || '/';

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [serverErr, setServerErr] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(ev) {
    ev.preventDefault();
    setServerErr('');
    if (!username || !password) {
      setServerErr('Vui lòng nhập đủ tài khoản và mật khẩu.');
      return;
    }
    setSubmitting(true);
    try {
      await doLogin(username, password);
      nav(from, { replace: true });
    } catch (err) {
      setServerErr(err.message || 'Đăng nhập thất bại.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-wrap">
      <form className="card login-card" onSubmit={onSubmit}>
        <h1>Đăng nhập</h1>
        <label>Email
          <input type="email" autoComplete="username" value={username} onChange={e => setUsername(e.target.value)} />
        </label>
        <label>Mật khẩu
          <input type="password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} />
        </label>
        {serverErr && <p className="err">{serverErr}</p>}
        <button className="btn btn--primary" disabled={submitting}>
          {submitting ? 'Đang vào...' : 'Đăng nhập'}
        </button>
      </form>
    </div>
  );
}
