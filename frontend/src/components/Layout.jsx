import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { user, doLogout } = useAuth();

  return (
    <div className="app">
      <header className="topbar">
        <Link to="/" className="brand">TechByte Service Desk</Link>
        <nav className="nav">
          <Link to="/">Dashboard</Link>
          <Link to="/tickets">Tickets</Link>
        </nav>
        <div className="userbox">
          <span>{user?.fullName || user?.username}</span>
          <button className="btn btn--ghost" onClick={doLogout}>Đăng xuất</button>
        </div>
      </header>
      <main className="main"><Outlet /></main>
    </div>
  );
}