# SimpleERP

A modular-monolith ERP with four modules — **Finance (AP/AR)**, **HR**, **Sales**, and
**Projects** — each with its own dashboard. Built per the `erp-builder` conventions: a Java 25 +
Spring Boot backend (package-by-module, cross-module calls through services only) and a
React + TypeScript frontend where every dashboard is rendered from one shared component kit.

## Status

| Area | State |
|---|---|
| Foundation (shared backend, frontend kit) | ✅ Built |
| **Finance** (AP/AR end-to-end + dashboard) | ✅ Built |
| HR / Sales / Projects | ⬜ Stubbed — routed placeholders, follow the Finance template |

The Finance module is the reference implementation: invoice (AR) and bill (AP) lifecycles with
partial payments, derived overdue, AR/AP aging, and a single `/api/v1/finance/dashboard` summary
endpoint feeding the React dashboard.

## Layout

```
simple-erp/
├── backend/   Java 25, Spring Boot, Maven
│   └── src/main/java/com/simpleerp/
│       ├── shared/    Money, AuditableEntity, ApiError, exceptions, error handler
│       └── finance/   AR (Invoice) + AP (Bill) + dashboard, with dto/
└── frontend/  React 18 + TS + Vite
    └── src/
        ├── lib/          api client, query keys, formatters, design tokens
        ├── components/   DashboardCard, StatCard, DataTable, StatusBadge, …
        └── modules/finance/   Dashboard.tsx, api.ts, types.ts
```

## Run it

**Backend** (defaults to in-memory H2 in PostgreSQL mode; Flyway owns the schema):

```bash
cd backend
mvn spring-boot:run        # serves http://localhost:8080
mvn test                   # unit + web-layer + Flyway smoke test
```

For PostgreSQL: `SPRING_PROFILES_ACTIVE=prod` with `DATABASE_URL` / `DATABASE_USER` /
`DATABASE_PASSWORD`.

**Frontend** (proxies `/api` to the backend):

```bash
cd frontend
npm install
npm run dev                # serves http://localhost:5173
npm run build              # type-check + production build
```

## Conventions

Enforced by the companion `../erp-builder-tests` suite (no Lombok, DTOs are records, Money is
never `double`, overdue is derived, exhaustive status switches, Flyway naming, TanStack Query with
loading/error/empty states, shared formatters, design tokens). Run against this project with:

```bash
cd ../erp-builder-tests
python run_checks.py ../simple-erp --eval-id 0
```
