import { Link, useNavigate } from 'react-router-dom';
import { setToken } from '../api';

export default function TopBar({ subtitle }: { subtitle?: string }) {
  const navigate = useNavigate();

  return (
    <header className="header">
      <div className="brand">
        <Link to="/friends" aria-label="Accueil">
          <span className="logo" aria-hidden="true">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M12 21s-7-4.6-7-10.5C5 7.1 6.9 5 9.4 5c1.5 0 2.8.8 3.6 2 0.8-1.2 2.1-2 3.6-2C19.1 5 21 7.1 21 10.5 21 16.4 12 21 12 21Z"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinejoin="round"
              />
              <path
                d="M9.2 13.2h5.6"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
              />
            </svg>
          </span>
          <span className="brandName">FriendGift</span>
        </Link>
        <span className="badge badgeAccent">Espace privé</span>
        {subtitle ? <span className="badge">{subtitle}</span> : null}
      </div>

      <button
        className="button buttonDanger"
        type="button"
        onClick={() => {
          setToken(null);
          navigate('/login');
        }}
      >
        Déconnexion
      </button>
    </header>
  );
}
