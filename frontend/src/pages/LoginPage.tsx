import { useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ApiError, login, register, setToken } from '../api';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const redirectTo = useMemo(() => {
    const state = location.state as { from?: string } | null;
    return state?.from ?? '/friends';
  }, [location.state]);

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  return (
    <div className="container">
      <div className="shell authShell">
        <header className="header">
          <div className="brand">
            <span>FriendGift</span>
            <span className="badge">{mode === 'login' ? 'Connexion' : 'Inscription'}</span>
          </div>
          <span className="badge badgeSuccess">Démo</span>
        </header>

        <section className="card">
          <div className="cardInner">
            <h1 className="h1">Bienvenue</h1>
            <p className="p">
              {mode === 'login'
                ? 'Connecte-toi pour retrouver tes amis et noter tes idées de cadeaux.'
                : 'Crée ton compte pour accéder à ton espace privé.'}
            </p>

            <div style={{ height: 12 }} />

            {error ? <div className="error">{error}</div> : null}

            <div style={{ height: 12 }} />

            <form
              className="row"
              onSubmit={async (e) => {
                e.preventDefault();
                setError(null);

                const trimmedUser = username.trim();
                if (!trimmedUser) {
                  setError("Le nom d’utilisateur est requis.");
                  return;
                }

                if (!password) {
                  setError('Le mot de passe est requis.');
                  return;
                }

                if (mode === 'register') {
                  if (password.length < 6) {
                    setError('Mot de passe trop court (min 6 caractères).');
                    return;
                  }
                  if (password !== confirmPassword) {
                    setError('Les mots de passe ne correspondent pas.');
                    return;
                  }
                }

                setLoading(true);
                try {
                  const result =
                    mode === 'login'
                      ? await login(trimmedUser, password)
                      : await register(trimmedUser, password);
                  setToken(result.token);
                  navigate(redirectTo, { replace: true });
                } catch (e) {
                  if (e instanceof ApiError && e.status === 409) {
                    setError('Ce nom d’utilisateur est déjà utilisé.');
                  } else if (mode === 'login') {
                    setError('Identifiants invalides ou service indisponible.');
                  } else {
                    setError("Inscription impossible. Réessaie avec un autre identifiant.");
                  }
                } finally {
                  setLoading(false);
                }
              }}
            >
              <div className="tabs" role="tablist" aria-label="Authentification">
                <button
                  className={`tab ${mode === 'login' ? 'tabActive' : ''}`}
                  type="button"
                  role="tab"
                  aria-selected={mode === 'login'}
                  disabled={loading}
                  onClick={() => {
                    setMode('login');
                    setConfirmPassword('');
                    setError(null);
                  }}
                >
                  J’ai déjà un compte
                </button>
                <button
                  className={`tab ${mode === 'register' ? 'tabActive' : ''}`}
                  type="button"
                  role="tab"
                  aria-selected={mode === 'register'}
                  disabled={loading}
                  onClick={() => {
                    setMode('register');
                    setConfirmPassword('');
                    setError(null);
                  }}
                >
                  Créer un compte
                </button>
              </div>

              <label className="row">
                <span className="itemMeta">Nom d’utilisateur</span>
                <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
              </label>

              <label className="row">
                <span className="itemMeta">Mot de passe</span>
                <input
                  className="input"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                />
              </label>

              {mode === 'register' ? (
                <label className="row">
                  <span className="itemMeta">Confirmer le mot de passe</span>
                  <input
                    className="input"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    autoComplete="new-password"
                  />
                  <div className="itemMeta">Minimum 6 caractères.</div>
                </label>
              ) : null}

              <button className="button buttonPrimary" type="submit" disabled={loading}>
                {loading ? (mode === 'login' ? 'Connexion…' : 'Inscription…') : mode === 'login' ? 'Se connecter' : 'Créer mon compte'}
              </button>

              <div className="demoBox">
                <div className="itemMeta">Comptes de démo (clique pour remplir) :</div>
                <div style={{ height: 8 }} />
                <div className="chips">
                  <button
                    type="button"
                    className="chip"
                    disabled={loading}
                    onClick={() => {
                      setMode('login');
                      setUsername('omar');
                      setPassword('password');
                      setConfirmPassword('');
                      setError(null);
                    }}
                  >
                    omar
                  </button>
                  <button
                    type="button"
                    className="chip"
                    disabled={loading}
                    onClick={() => {
                      setMode('login');
                      setUsername('alice');
                      setPassword('password');
                      setConfirmPassword('');
                      setError(null);
                    }}
                  >
                    alice
                  </button>
                  <span className="kbd">password</span>
                </div>
              </div>
            </form>
          </div>
        </section>
      </div>
    </div>
  );
}
