# Bank Account Kata — Plan

## Context

Kata évaluant la qualité de design et de tests autour d'un compte bancaire (dépôt, retrait, historique).
Périmètre choisi : **full** — métier + REST API + PostgreSQL + client Angular + Docker + CI/CD GitHub Actions.
Stack backend : Java 21, Spring Boot 3.x, Maven, JUnit 5, Mockito, Flyway.
Stack frontend : Angular 17+ (standalone components, Signals), TypeScript.

---

## Architecture : DDD + Hexagonale (Ports & Adapters)

```
domain/        ← zéro dépendance framework  [DDD : Agrégats, Value Objects, Domain Events, Exceptions domaine]
application/   ← use cases, commandes       [DDD : Application Services, Commands]
infrastructure/← web (Spring MVC), persistence (JPA), config  [Adapters]
```

La règle de dépendance est stricte : domain ne connaît ni Spring, ni JPA, ni HTTP.

### Patterns DDD appliqués

| Pattern | Classe(s) | Rôle |
|---|---|---|
| **Aggregate Root** | `Account` | Garantit la cohérence du compte et de son historique ; seul point d'entrée pour modifier l'état |
| **Value Object** | `Money`, `AccountId`, `TransactionId` | Immuables, égalité structurelle, invariants au constructeur |
| **Entity** | `Transaction` | Identité propre (`TransactionId`), immuable après création |
| **Domain Exception** | `InsufficientFundsException` | Violation d'invariant métier, exprimée dans le langage du domaine |
| **Factory Method** | `Account.open(...)`, `Account.reconstitute(...)` | Sépare la création (métier) de la reconstitution (persistance) |
| **Repository (port)** | `AccountRepository`, `TransactionRepository` | Abstraction de la persistance — interface dans le domaine, implémentation dans l'infra |
| **Application Service** | `DepositMoneyService`, etc. | Orchestre le domaine, ne contient pas de logique métier |
| **Command** | `DepositMoneyCommand`, etc. | DTO d'intention métier, validé à la frontière |
| **Ubiquitous Language** | Tout le code | Les noms (`deposit`, `withdraw`, `InsufficientFunds`) reflètent le vocabulaire métier, pas technique |

### Bounded Context

Ce kata représente un unique Bounded Context : **BankAccount**.
Le context map est trivial (un seul BC), mais la structure hexagonale prépare à en ajouter d'autres sans couplage.

---

## Structure de fichiers cible

```
bank-account-kata/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .github/workflows/ci.yml
└── src/
    ├── main/java/com/kata/bankaccount/
    │   ├── BankAccountApplication.java
    │   ├── domain/
    │   │   ├── model/
    │   │   │   ├── Account.java                  ← agrégat, toutes les règles métier
    │   │   │   ├── AccountId.java                ← record typé (UUID)
    │   │   │   ├── Money.java                    ← record immuable, invariants au constructeur
    │   │   │   ├── Transaction.java              ← record immuable
    │   │   │   ├── TransactionId.java
    │   │   │   ├── TransactionType.java          ← enum DEPOSIT / WITHDRAWAL
    │   │   │   └── InsufficientFundsException.java
    │   │   └── port/
    │   │       ├── in/  CreateAccountUseCase, DepositMoneyUseCase,
    │   │       │        WithdrawMoneyUseCase, GetTransactionHistoryUseCase
    │   │       └── out/ AccountRepository, TransactionRepository
    │   ├── application/
    │   │   ├── service/  CreateAccountService, DepositMoneyService,
    │   │   │             WithdrawMoneyService, GetTransactionHistoryService
    │   │   └── command/  CreateAccountCommand, DepositMoneyCommand, WithdrawMoneyCommand
    │   └── infrastructure/
    │       ├── web/
    │       │   ├── AccountController.java
    │       │   ├── TransactionController.java
    │       │   ├── GlobalExceptionHandler.java   ← @RestControllerAdvice
    │       │   └── dto/  *Request, *Response, ErrorResponse
    │       ├── persistence/
    │       │   ├── AccountRepositoryAdapter.java
    │       │   ├── TransactionRepositoryAdapter.java
    │       │   ├── JpaAccountRepository.java
    │       │   ├── JpaTransactionRepository.java
    │       │   └── entity/ AccountEntity, TransactionEntity
    │       └── config/BeanConfig.java
    └── main/resources/
        ├── application.yml
        └── db/migration/ V1__create_accounts.sql, V2__create_transactions.sql
```

---

## Domain model clé

### `Money` (record)
- Montant `BigDecimal` arrondi à 2 décimales + `Currency`
- Constructeur rejette montant négatif
- `add`, `subtract`, `isGreaterThanOrEqual` — rejette devises différentes

### `Account` (aggregate root)
```java
// Deux factories :
Account.open(ownerName, initialBalance, now)      // génère un nouvel AccountId
Account.reconstitute(id, ownerName, balance, transactions, createdAt)  // depuis persistance

// Comportements métier :
Transaction deposit(Money amount, Instant now)    // retourne la Transaction produite
Transaction withdraw(Money amount, Instant now)   // lance InsufficientFundsException si solde insuffisant
List<Transaction> transactions()                  // copie défensive
```

### `Transaction` (record immuable)
- Champs : `id`, `type`, `amount`, `balanceAfter`, `occurredAt`
- Factories : `Transaction.deposit(...)`, `Transaction.withdrawal(...)`

### `InsufficientFundsException`
- Exception domaine (pas Spring) — le `GlobalExceptionHandler` la traduit en HTTP 422
- Porte les données contextuelles : `accountId`, `currentBalance`, `requested`

### Règles DDD à respecter impérativement
- **L'agrégat `Account` est la seule voie** pour modifier balance ou transactions — jamais depuis l'extérieur
- **Aucune logique métier dans les services applicatifs** — ils orchestrent (charger, appeler le domaine, persister), le domaine décide
- **Les Value Objects ne sont jamais mutés** — toujours recréés
- **Le repository ne retourne que des agrégats reconstitués** via `Account.reconstitute(...)`, jamais des entités JPA exposées

---

## API REST — Base path `/api/v1`

| Méthode | Chemin | Succès | Erreurs |
|---|---|---|---|
| POST | `/accounts` | 201 | 400 validation |
| POST | `/accounts/{id}/deposits` | 200 | 400, 404, 422 |
| POST | `/accounts/{id}/withdrawals` | 200 | 400, 404, 422 INSUFFICIENT_FUNDS |
| GET | `/accounts/{id}/transactions` | 200 | 404 |

Enveloppe d'erreur : `{ status, errorCode, message, timestamp }`.
Codes : `ACCOUNT_NOT_FOUND`, `INSUFFICIENT_FUNDS`, `CURRENCY_MISMATCH`, `VALIDATION_ERROR`.

---

## Stratégie de tests (5 couches)

| Couche | Outil | Ce qui est testé |
|---|---|---|
| Domain unit | JUnit 5 pur (0 mock) | Invariants Money, Account, Transaction |
| Application unit | JUnit 5 + Mockito | Orchestration use cases, appels aux ports |
| Web slice | `@WebMvcTest` + `@MockBean` | HTTP codes, sérialisation, mappage exceptions |
| Persistence | `@DataJpaTest` H2 | Mapping JPA, `reconstitute` roundtrip |
| E2E | `@SpringBootTest` RANDOM_PORT H2 | Scénarios complets via HTTP réel |

Cas non-passants couverts : retrait > solde, montant négatif, compte inexistant, nom vide.

---

## Persistance

- **PostgreSQL 16** en production, **H2** pour les tests
- Flyway gère les migrations automatiquement au démarrage
- Montants stockés en `NUMERIC(19,2)` avec colonne `currency VARCHAR(3)` (auto-descriptif)
- `TransactionRepository.save(transaction, accountId)` — l'accountId est une préoccupation infrastructure, pas domaine

---

## Docker

`Dockerfile` : multi-stage (builder `eclipse-temurin:21-jdk-alpine` → runtime `eclipse-temurin:21-jre-alpine`)

`docker-compose.yml` :
- `db` : `postgres:16-alpine` + named volume
- `app` : depends_on db, variables d'env datasource, healthcheck `/actuator/health`

---

## CI/CD — GitHub Actions

```yaml
Trigger: push toutes branches + PR vers main

Jobs:
  test:
    - Java 21 (temurin) + cache Maven
    - mvn verify  (unit + integration + e2e, tout sur H2)
    - Upload rapports Surefire

  build-image:
    needs: test
    - docker build -t bank-account-kata:$SHA .
    - Push vers GHCR sur branche main (tag latest + SHA)
```

---

## Ordre d'implémentation conseillé

1. `Money`, `AccountId`, `TransactionId`, `TransactionType` (value objects)
2. `Transaction`, `InsufficientFundsException`
3. `Account` (agrégat) + tests domaine complets
4. Ports in/out + commandes
5. Services application + leurs tests Mockito
6. Migrations Flyway + entités JPA + adapters persistence + `@DataJpaTest`
7. Contrôleurs + DTOs + `GlobalExceptionHandler` + `@WebMvcTest`
8. Test E2E bout-en-bout
9. Dockerfile + docker-compose
10. GitHub Actions CI

---

---

## Client Angular (frontend/)

### Structure

```
frontend/
├── Dockerfile                        ← build Angular puis Nginx
├── nginx.conf                        ← proxy /api → backend, SPA fallback
├── angular.json
├── package.json
└── src/app/
    ├── app.config.ts                 ← bootstrap standalone, provideHttpClient, provideRouter
    ├── app.routes.ts
    ├── core/
    │   ├── models/  account.model.ts, transaction.model.ts
    │   └── services/
    │       ├── account.service.ts    ← HttpClient, retourne des Observables/Signals
    │       └── transaction.service.ts
    └── features/
        ├── accounts/
        │   ├── account-list/         ← liste tous les comptes
        │   └── account-create/       ← formulaire reactive (ownerName, initialAmount, currency)
        └── transactions/
            ├── deposit-withdraw/     ← formulaire dépôt/retrait
            └── transaction-history/  ← tableau des transactions
```

### Routes Angular

```
/                     → redirect vers /accounts
/accounts             → AccountListComponent
/accounts/new         → AccountCreateComponent
/accounts/:id/history → TransactionHistoryComponent (charge dépôt/retrait aussi)
```

### Design frontend

- Composants **standalone** uniquement — pas de NgModules
- State via **Signals** : `signal<Account[]>([])`, `computed(...)`, `effect(...)`
- `AccountService` expose des méthodes retournant `Observable<T>` (HttpClient) + charge le résultat dans des signals via `toSignal`
- Reactive Forms pour tous les formulaires (validation : required, min value)
- Affichage des erreurs métier (ex : "Fonds insuffisants") depuis la réponse API `errorCode`
- Pas de librairie UI externe — CSS minimal propre (pas besoin d'Angular Material pour un kata)

### Tests Angular

- `AccountService` : tests unitaires avec `HttpClientTestingModule`
- `AccountListComponent` : tests avec `TestBed`, mock du service via `provide`
- `AccountCreateComponent` : test soumission formulaire valide + invalide
- `TransactionHistoryComponent` : test affichage liste + cas compte vide

### CORS (backend)

Ajouter `@CrossOrigin` ou une config `CorsRegistry` dans Spring Boot pour autoriser `http://localhost:4200` (dev) et l'origine Nginx en production.

---

## Docker (mis à jour)

### `frontend/Dockerfile`

```
Stage 1 (builder): node:20-alpine
  - npm ci && ng build --configuration=production

Stage 2 (runtime): nginx:alpine
  - COPY dist/ /usr/share/nginx/html
  - COPY nginx.conf /etc/nginx/conf.d/default.conf
```

### `nginx.conf`

```nginx
location /api/ {
    proxy_pass http://app:8080/api/;   # proxy vers le backend
}
location / {
    try_files $uri $uri/ /index.html;  # SPA fallback
}
```

### `docker-compose.yml` (mis à jour)

Services :
- `db` : postgres:16-alpine
- `app` : backend Spring Boot, port 8080
- `frontend` : Nginx + Angular, port 80, depends_on app

---

## CI/CD (mis à jour)

```yaml
Jobs:
  test-backend:
    - Java 21 + Maven cache
    - mvn verify

  test-frontend:
    - Node 20 + npm cache
    - npm ci && ng test --watch=false --browsers=ChromeHeadless

  build-images:
    needs: [test-backend, test-frontend]
    - docker build backend → bank-kata-backend:$SHA
    - docker build frontend → bank-kata-frontend:$SHA
    - Push vers GHCR sur main
```

---

## Ordre d'implémentation conseillé (mis à jour)

1. Value objects domaine : `Money`, `AccountId`, `TransactionId`, `TransactionType`
2. `Transaction`, `InsufficientFundsException`
3. `Account` (agrégat) + tests domaine complets
4. Ports in/out + commandes
5. Services application + tests Mockito
6. Migrations Flyway + entités JPA + adapters + `@DataJpaTest`
7. Contrôleurs REST + DTOs + `GlobalExceptionHandler` + CORS + `@WebMvcTest`
8. Tests E2E Spring Boot (H2)
9. Dockerfile backend + docker-compose de base
10. `ng new frontend --standalone` + models + services + composants + tests Angular
11. Dockerfile frontend + Nginx + docker-compose complet
12. GitHub Actions CI

---

## Vérification

```bash
# Tests backend
mvn verify

# Tests frontend
cd frontend && npm ci && ng test --watch=false --browsers=ChromeHeadless

# Stack complète
docker-compose up --build
# → UI Angular sur http://localhost
# → API sur http://localhost:8080/api/v1

# Smoke test API
curl -X POST http://localhost:8080/api/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"ownerName":"Alice","initialAmount":"100.00","currencyCode":"EUR"}'
```
