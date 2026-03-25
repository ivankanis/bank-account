# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Backend:**
```bash
mvn verify                          # build + all tests
mvn test -Dtest=AccountTest         # run a single test class
mvn test -Dtest=AccountTest#deposit # run a single test method
```

**Frontend:**
```bash
cd frontend
npm install
npm start                           # dev server on http://localhost:4200
npm test -- --watch=false --browsers=ChromeHeadless
```

**Run full stack:**
```bash
docker compose up
```

## Architecture

Hexagonal architecture with three layers, all under `fr.kanis.bankaccount`:

- **`domain/`** — no Spring/JPA dependencies. Contains the `Account` aggregate, `Money` value object, `Transaction` record, and port interfaces (`port/in/` for use case input ports, `port/out/` for repository output ports).
- **`application/`** — use case implementations (`service/`) and command objects (`command/`). One service class per use case.
- **`infrastructure/`** — all framework code: REST controllers + DTOs (`web/`), JPA entities + adapters (`persistence/`), Spring bean wiring (`config/BeanConfig`).

Dependencies only point inward: infrastructure → application → domain.

**Wiring:** `BeanConfig` manually instantiates all services and injects repositories. There is no `@Service`/`@Component` scanning in the domain or application layers.

**Persistence:** Repository adapters (`AccountRepositoryAdapter`, `TransactionRepositoryAdapter`) convert between domain objects and JPA entities. The inner `JpaAccountRepository` / `JpaTransactionRepository` interfaces are package-private.

**Tests:**
- `domain/` — pure unit tests, no Spring context
- `application/` — service tests with Mockito mocks
- `infrastructure/` — `@WebMvcTest` for controllers, `@DataJpaTest` for persistence adapters (H2 in-memory)
- `e2e/` — `@SpringBootTest` with `TestRestTemplate` against a full Spring context (H2)
