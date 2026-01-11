import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError, createFriend, deleteFriend, listFriends, updateFriend } from '../api';
import type { FriendDto } from '../types';
import TopBar from '../components/TopBar';

export default function FriendsPage() {
  const [friends, setFriends] = useState<FriendDto[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [newName, setNewName] = useState('');
  const [saving, setSaving] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editingName, setEditingName] = useState('');
  const [menuOpenId, setMenuOpenId] = useState<string | null>(null);

  useEffect(() => {
    if (!menuOpenId) return;

    function onPointerDown(e: MouseEvent | TouchEvent) {
      const target = e.target as HTMLElement | null;
      if (!target) return;
      if (target.closest('.friendMenuWrap')) return;
      setMenuOpenId(null);
    }

    function onKeyDown(e: KeyboardEvent) {
      if (e.key === 'Escape') setMenuOpenId(null);
    }

    document.addEventListener('mousedown', onPointerDown);
    document.addEventListener('touchstart', onPointerDown);
    document.addEventListener('keydown', onKeyDown);

    return () => {
      document.removeEventListener('mousedown', onPointerDown);
      document.removeEventListener('touchstart', onPointerDown);
      document.removeEventListener('keydown', onKeyDown);
    };
  }, [menuOpenId]);

  function initials(name: string) {
    const cleaned = name.trim();
    if (!cleaned) return '?';
    const parts = cleaned.split(/\s+/).filter(Boolean);
    const first = parts[0]?.[0] ?? '';
    const last = parts.length > 1 ? parts[parts.length - 1]?.[0] ?? '' : '';
    const value = (first + last).toUpperCase();
    return value || cleaned.slice(0, 1).toUpperCase();
  }

  const sorted = useMemo(() => {
    if (!friends) return null;
    return [...friends].sort((a, b) => a.name.localeCompare(b.name));
  }, [friends]);

  async function refresh() {
    setError(null);
    const result = await listFriends();
    setFriends(result);
  }

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const result = await listFriends();
        if (!cancelled) setFriends(result);
      } catch (e) {
        if (e instanceof ApiError && (e.status === 401 || e.status === 403)) {
          setError('Session expirée. Merci de te reconnecter.');
        } else {
          setError('Impossible de charger la liste des amis.');
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="container">
      <div className="shell">
        <TopBar subtitle="Mes amis" />

        <section className="card">
          <div className="cardInner">
            <h1 className="h1">Mes amis</h1>
            <p className="p">Consulte leurs idées et ajoute-en de nouvelles.</p>

              <div style={{ height: 12 }} />

              {error ? <div className="error">{error}</div> : null}

              <div style={{ height: 12 }} />

              <form
                className="row"
                onSubmit={async (e) => {
                  e.preventDefault();
                  setError(null);

                  const trimmed = newName.trim();
                  if (!trimmed) {
                    setError("Le nom est requis.");
                    return;
                  }

                  setSaving(true);
                  try {
                    await createFriend(trimmed);
                    setNewName('');
                    await refresh();
                  } catch {
                    setError("Impossible d'ajouter cet ami.");
                  } finally {
                    setSaving(false);
                  }
                }}
              >
                <label className="row">
                  <span className="itemMeta">Ajouter un ami</span>
                  <div className="rowInline">
                    <input
                      className="input"
                      value={newName}
                      onChange={(e) => setNewName(e.target.value)}
                      placeholder="Ex: Nadia"
                    />
                    <button className="button buttonPrimary" type="submit" disabled={saving}>
                      {saving ? 'Ajout…' : 'Ajouter'}
                    </button>
                  </div>
                </label>
              </form>

              <div className="friendsList">
                {!sorted ? (
                  <div className="itemMeta">Chargement…</div>
                ) : sorted.length === 0 ? (
                  <div className="empty">
                    <div className="emptyTitle">Aucun ami pour le moment</div>
                    <div className="emptyText">Connecte-toi avec un compte de démo pour voir des exemples.</div>
                  </div>
                ) : (
                  <div className="list">
                    {sorted.map((f) => (
                      <div key={f.id} className="item">
                        <div className="friendMain">
                          <div className="avatar" aria-hidden="true">
                            {initials(f.name)}
                          </div>
                          <div style={{ minWidth: 0 }}>
                            {editingId === f.id ? (
                              <div className="rowInline">
                                <input
                                  className="input"
                                  value={editingName}
                                  onChange={(e) => setEditingName(e.target.value)}
                                  placeholder="Nom"
                                />
                                <button
                                  className="button buttonPrimary buttonSmall"
                                  type="button"
                                  disabled={saving}
                                  onClick={async () => {
                                    setError(null);
                                    const trimmed = editingName.trim();
                                    if (!trimmed) {
                                      setError('Le nom est requis.');
                                      return;
                                    }

                                    setSaving(true);
                                    try {
                                      await updateFriend(f.id, trimmed);
                                      setEditingId(null);
                                      setEditingName('');
                                      await refresh();
                                    } catch {
                                      setError("Impossible de modifier cet ami.");
                                    } finally {
                                      setSaving(false);
                                    }
                                  }}
                                >
                                  Enregistrer
                                </button>
                                <button
                                  className="button buttonSmall"
                                  type="button"
                                  disabled={saving}
                                  onClick={() => {
                                    setEditingId(null);
                                    setEditingName('');
                                  }}
                                >
                                  Annuler
                                </button>
                              </div>
                            ) : (
                              <>
                                <div className="itemTitle">{f.name}</div>
                                <div className="itemMeta">Voir les idées cadeaux</div>
                              </>
                            )}
                          </div>
                        </div>

                        {editingId !== f.id ? (
                          <div className="itemActions">
                            <Link className="button buttonSmall" to={`/friends/${encodeURIComponent(f.id)}`}>
                              Ouvrir
                            </Link>

                            <div className="friendMenuWrap">
                              <button
                                className="kebabButton"
                                type="button"
                                aria-label={`Menu pour ${f.name}`}
                                aria-haspopup="menu"
                                aria-expanded={menuOpenId === f.id}
                                disabled={saving}
                                onClick={() => setMenuOpenId((cur) => (cur === f.id ? null : f.id))}
                              >
                                ⋮
                              </button>

                              {menuOpenId === f.id ? (
                                <div className="dropdownMenu" role="menu" aria-label={`Actions pour ${f.name}`}>
                                  <button
                                    className="dropdownItem"
                                    type="button"
                                    role="menuitem"
                                    disabled={saving}
                                    onClick={() => {
                                      setMenuOpenId(null);
                                      setEditingId(f.id);
                                      setEditingName(f.name);
                                    }}
                                  >
                                    Modifier
                                  </button>

                                  <button
                                    className="dropdownItem dropdownDanger"
                                    type="button"
                                    role="menuitem"
                                    disabled={saving}
                                    onClick={async () => {
                                      setMenuOpenId(null);
                                      const ok = window.confirm(`Supprimer ${f.name} ?`);
                                      if (!ok) return;

                                      setSaving(true);
                                      setError(null);
                                      try {
                                        await deleteFriend(f.id);
                                        await refresh();
                                      } catch {
                                        setError("Impossible de supprimer cet ami.");
                                      } finally {
                                        setSaving(false);
                                      }
                                    }}
                                  >
                                    Supprimer
                                  </button>
                                </div>
                              ) : null}
                            </div>
                          </div>
                        ) : (
                          <span className="badge">Édition</span>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
        </section>
      </div>
    </div>
  );
}
