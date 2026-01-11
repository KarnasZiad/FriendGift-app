# FriendGift

Application **responsive** (mobile + desktop) pour gérer des **idées de cadeaux par ami**, avec **authentification** et **isolation des données par utilisateur**.

Stack imposée : **Quarkus (Java)** + **React**.

## Fonctionnalités

- Authentification (login) et accès protégé.
- Liste de ses amis (isolation par utilisateur).
- Consultation des idées de cadeaux d’un ami.
- Ajout d’une idée de cadeau.

Fonctionnalités supplémentaires (bonus) :

- Inscription (création de compte).
- CRUD amis (ajouter / modifier / supprimer).
- UI/UX améliorée (menu ⋮, suggestions d’inspiration, responsive).
- Tests backend (Quarkus + RestAssured).

## Prérequis

- Java 17+
- Maven
- Node.js 18+

## Démarrage rapide

Ouvrir 2 terminaux à la racine du repo.

### 1) Backend (Quarkus)

Au 1er lancement, l’app génère automatiquement des clés JWT locales dans `backend/keys/` (non versionnées).

Si besoin, vous pouvez aussi générer manuellement les clés (une seule fois) :

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\generate-jwt-keys.ps1
```

```powershell
cd backend
mvn quarkus:dev
```

- API : http://localhost:8080

### 2) Frontend (React)

```powershell
cd frontend
npm install
npm run dev
```

- App : http://localhost:5173

## Comptes

Comptes de démo :

- `omar / password`
- `alice / password`

Vous pouvez aussi créer un compte depuis l’écran d’authentification (onglet **Créer un compte**).

## Tests

Backend :

```powershell
mvn -f backend/pom.xml test
```

Frontend (vérifier la compilation) :

```powershell
cd frontend
npm run build
```

## API (résumé)

### Auth

- `POST /api/auth/login` → retourne un JWT
- `POST /api/auth/register` → crée un compte et retourne un JWT

### Amis

- `GET /api/friends`
- `POST /api/friends`
- `PUT /api/friends/{friendId}`
- `DELETE /api/friends/{friendId}`

### Idées cadeaux

- `GET /api/friends/{friendId}/ideas`
- `POST /api/friends/{friendId}/ideas`

## Notes techniques / limites

- Le backend utilise une base **H2** (Hibernate ORM **Panache**).
  - En dev : base **fichier** (`backend/data/friendgift.mv.db`) → les données persistent au redémarrage.
  - En test : base **en mémoire** → schéma recréé à chaque exécution.
  - Réinitialiser les données en dev : arrêter le backend puis supprimer `backend/data/friendgift.mv.db`.
- Authentification via **JWT (RSA)** : après connexion, l’utilisateur ne peut consulter que **ses propres** amis et idées.
- Clés JWT : stockées localement dans `backend/keys/` (non commitées) → le repo ne contient aucun secret.
- Mots de passe : stockés en clair (app démo) → à remplacer par un hash (BCrypt/Argon2) en production.
- Le frontend en dev tourne sur `5173` et appelle l’API Quarkus sur `8080` (CORS autorisé en dev).

### Vérifier que la DB persiste (optionnel)

Un script automatisé démarre Quarkus, crée un ami, redémarre Quarkus et vérifie que l’ami est toujours présent :

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\db-smoke-test.ps1
```

