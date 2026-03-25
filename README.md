# Bank Account Kata

Application de gestion de compte bancaire full-stack, construite comme un kata d'architecture hexagonale / DDD.

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3.2.3, Spring Data JPA |
| Base de données | PostgreSQL 16 (prod), H2 (tests) |
| Migrations | Flyway |
| Frontend | Angular 21, Nginx |
| Déploiement | Docker, Docker Compose |

## Lancer l'application

```bash
docker compose up
```

| Service | URL |
|---|---|
| Frontend | http://localhost:80 |
| Backend | http://localhost:8080 |

## Construction locale

**Backend :**
```bash
mvn verify
```

**Frontend :**
```bash
cd frontend
npm install
npm start     # serveur de dev sur http://localhost:4200
npm test      # tests Karma/Jasmine
```

## API

Chemin de base : `/api/v1/accounts`

| Méthode | Chemin | Description |
|---|---|---|
| `POST` | `/` | Créer un compte |
| `GET` | `/` | Lister les comptes |
| `GET` | `/{id}` | Obtenir un compte |
| `POST` | `/{id}/deposits` | Déposer de l'argent |
| `POST` | `/{id}/withdrawals` | Retirer de l'argent |
| `GET` | `/{id}/transactions` | Historique des transactions |

**Créer un compte** — `POST /api/v1/accounts`
```json
{ "ownerName": "Alice", "initialAmount": 100.00, "currencyCode": "EUR" }
```

**Dépôt / Retrait** — `POST /api/v1/accounts/{id}/deposits`
```json
{ "amount": 50.00, "currencyCode": "EUR" }
```

**Réponses d'erreur :**

| Statut | `errorCode` | Cause |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Corps de requête invalide |
| 404 | `ACCOUNT_NOT_FOUND` | ID de compte inconnu |
| 422 | `INSUFFICIENT_FUNDS` | Le retrait dépasse le solde |

## Architecture

Architecture hexagonale (Ports & Adaptateurs) :

```
domain/          → Agrégat Account, value object Money, interfaces de ports
application/     → Implémentations des cas d'usage (une classe par cas d'usage)
infrastructure/  → Contrôleurs web, adaptateurs JPA, migrations Flyway
```

Le domaine ne dépend ni de Spring ni de JPA — toutes les dépendances pointent vers l'intérieur.
