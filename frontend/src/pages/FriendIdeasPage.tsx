import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { addIdea, listFriends, listIdeas } from '../api';
import type { FriendDto, GiftIdeaDto } from '../types';
import TopBar from '../components/TopBar';

export default function FriendIdeasPage() {
  const { friendId } = useParams();

  const safeFriendId = friendId ?? '';

  const [friends, setFriends] = useState<FriendDto[] | null>(null);
  const [ideas, setIdeas] = useState<GiftIdeaDto[] | null>(null);
  const [text, setText] = useState('');
  const [ideaQuery, setIdeaQuery] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const inputRef = useRef<HTMLInputElement | null>(null);
  const suggestions = useMemo(() => {
    return [
      'Abonnement salle de sport (1 mois)',
      'Abonnement streaming (1 mois)',
      'Affiche / poster encadré',
      'Album photo personnalisé',
      'Atelier (cuisine, poterie…)',
      'Bande dessinée',
      'Billets de concert',
      'Billets de cinéma (2 places)',
      'Bon pour un massage',
      'Box découverte (thé, café, snacks…)',
      'Livre',
      'Livre audio (abonnement 1 mois)',
      'Mug personnalisé',
      'Montre',
      'Montre connectée',
      'Dîner au restaurant',
      'Dégustation (fromages / chocolat)',
      'Parfum',
      'Carte cadeau',
      'Casque audio',
      'Bougie parfumée',
      'Diffuseur d’huiles essentielles',
      'Sweat / hoodie',
      'T-shirt / pull',
      'Chaussettes fun / chaudes',
      'Écharpe',
      'Jeu de société',
      'Jeu vidéo (carte cadeau)',
      'Lego',
      'Lampe de chevet',
      'Guirlande lumineuse',
      'Sac / tote bag',
      'Sac banane',
      'Portefeuille',
      'Porte-clés personnalisé',
      'Plante d’intérieur',
      'Kit jardinage / plantes aromatiques',
      'Bouteille isotherme',
      'Gourde filtrante',
      'Tapis de sport',
      'Élastiques de fitness',
      'Tapis de yoga',
      'Serviette microfibre (sport/voyage)',
      'Puzzle',
      'Escape game',
      'Week-end surprise',
      'Carnet + stylos (joli set)',
      'Pochette / organiseur de voyage',
      'Chargeur rapide',
      'Batterie externe',
      'Support téléphone voiture',
      'Enceinte Bluetooth',
      'Clavier / souris',
      'Souris ergonomique',
      'Platine vinyle (ou vinyle préféré)',
      'Plante LEGO / déco',
      'Cours de langue (1 mois)',
      'Cours de danse',
      'Trousse de soins (skincare)',
      'Coffret bain / spa maison',
    ];
  }, []);

  const filteredSuggestions = useMemo(() => {
    const q = ideaQuery.trim().toLowerCase();
    if (!q) return suggestions;
    return suggestions.filter((s) => s.toLowerCase().includes(q));
  }, [ideaQuery, suggestions]);

  function applySuggestion(value: string) {
    setText(value);
    inputRef.current?.focus();
  }

  function pickRandomSuggestion() {
    const pool = filteredSuggestions.length > 0 ? filteredSuggestions : suggestions;
    const random = pool[Math.floor(Math.random() * pool.length)];
    if (random) applySuggestion(random);
  }

  const friend = useMemo(() => {
    if (!friends) return null;
    return friends.find((f) => f.id === safeFriendId) ?? null;
  }, [friends, safeFriendId]);

  async function refresh() {
    setError(null);
    const [f, i] = await Promise.all([listFriends(), listIdeas(safeFriendId)]);
    setFriends(f);
    setIdeas(i);
  }

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        await refresh();
      } catch {
        if (!cancelled) setError("Impossible de charger les données.");
      }
    })();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [safeFriendId]);

  return (
    <div className="container">
      <div className="shell">
        <TopBar subtitle={friend ? `Idées pour ${friend.name}` : 'Idées'} />

        <section className="card">
          <div className="cardInner">
            <div className="pageHeader">
              <div>
                <h1 className="h1">Idées de cadeaux</h1>
                <p className="p">
                  {friend ? (
                    <>
                      Ami sélectionné : <strong>{friend.name}</strong>
                    </>
                  ) : (
                    <>Ami introuvable (ou non autorisé).</>
                  )}
                </p>
              </div>

              <div className="pageActions">
                <Link className="button" to="/friends">
                  Retour
                </Link>
                <button className="button" type="button" onClick={() => refresh()} disabled={!safeFriendId}>
                  Rafraîchir
                </button>
              </div>
            </div>

            <div style={{ height: 12 }} />

            {error ? <div className="error">{error}</div> : null}

            <div style={{ height: 12 }} />

            <details className="panelDetails">
              <summary className="panelSummary">
                <span className="panelTitle">Besoin d’inspiration ?</span>
                <span className="panelHint">Suggestions rapides</span>
              </summary>

              <div className="panelBody">
                <div className="panelHeader">
                  <input
                    className="input"
                    value={ideaQuery}
                    onChange={(e) => setIdeaQuery(e.target.value)}
                    placeholder="Rechercher une idée…"
                  />
                  <button className="button buttonSmall" type="button" onClick={pickRandomSuggestion}>
                    Surprise
                  </button>
                </div>

                <div style={{ height: 10 }} />

                <div className="chips">
                  {(ideaQuery.trim() ? filteredSuggestions : filteredSuggestions.slice(0, 8)).map((s) => (
                    <button key={s} type="button" className="chip" onClick={() => applySuggestion(s)}>
                      {s}
                    </button>
                  ))}
                </div>

                {ideaQuery.trim() && filteredSuggestions.length === 0 ? (
                  <div style={{ marginTop: 8 }} className="itemMeta">
                    Aucun résultat. Essaie un mot-clé (ex: “livre”, “sport”, “cours”…).
                  </div>
                ) : null}
              </div>
            </details>

            {!ideas ? (
              <div className="itemMeta">Chargement…</div>
            ) : ideas.length === 0 ? null : (
              <div className="list">
                {ideas.map((idea) => (
                  <div key={idea.id} className="item">
                    <div>
                      <div className="itemTitle">{idea.text}</div>
                      <div className="itemMeta">Ajouté le {new Date(idea.createdAt).toLocaleString()}</div>
                    </div>
                    <span className="badge">Idée</span>
                  </div>
                ))}
              </div>
            )}

            <div style={{ height: 16 }} />

            <div className="card" style={{ background: 'rgba(255,255,255,0.04)' }}>
              <div className="cardInner">
                <h1 className="h1">Ajouter une idée</h1>
                <p className="p">Ex: “Montre connectée”, “Livre de cuisine”, “Billets concert”…</p>

                <div style={{ height: 12 }} />

                <form
                  className="row"
                  onSubmit={async (e) => {
                    e.preventDefault();
                    setError(null);
                    if (!safeFriendId) return;

                    const trimmed = text.trim();
                    if (!trimmed) {
                      setError('Le texte est requis.');
                      return;
                    }

                    setSaving(true);
                    try {
                      await addIdea(safeFriendId, trimmed);
                      setText('');
                      await refresh();
                    } catch {
                      setError("Impossible d'ajouter l'idée.");
                    } finally {
                      setSaving(false);
                    }
                  }}
                >
                  <input
                    ref={inputRef}
                    className="input"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    placeholder="Nouvelle idée…"
                  />
                  <button className="button buttonPrimary" type="submit" disabled={saving || !safeFriendId}>
                    {saving ? 'Ajout…' : 'Ajouter'}
                  </button>
                </form>

                {!friend && friends ? (
                  <div style={{ height: 10 }} className="itemMeta">
                    Cet ami n’est pas dans ta liste. <Link className="link" to="/friends">Revenir</Link>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
