# SimpleERP

A modular-monolith ERP with four modules — **Finance (AP/AR)**, **HR**, **Sales**, and
**Projects** — each with its own dashboard and management UI. Built per the `erp-builder`
conventions: a Java 25 + Spring Boot backend (package-by-module, cross-module calls through
service interfaces only) and a React + TypeScript frontend where every page is assembled from one
shared component kit.

## Status

All four modules are built end to end — backend domain + flows, a single summary dashboard
endpoint each, and full list/create/lifecycle management in the UI.

| Module | What it does |
|---|---|
| **Finance** (AP/AR) | AR invoices & AP bills with lifecycles, partial payments (check capture with masked account numbers), derived overdue, AR/AP aging, customers & vendors. |
| **HR** | Employees (hire/terminate), departments, the leave workflow (submit → approve/reject/cancel), an emergency **roll-call** (check-in/out + end-of-day auto-checkout), headcount/hiring/turnover dashboard. |
| **Sales** | Leads → qualify → opportunities (pipeline stages, win/lose) → orders (fulfil → **invoice, handed off to Finance**), salesperson attribution, weighted-pipeline dashboard. |
| **Projects** | Projects, tasks, milestones, time tracking; **budget-vs-actual** where spend = hours × the assignee's HR hourly cost (salary ÷ 2080); utilization & budget-health dashboard. |

Every entity uses derived-not-stored truth where it matters (overdue, on-leave, budget health,
turnover) and exhaustive status switches so a new state is a compile error.

## Cross-module seams

Modules never touch each other's tables — only published service interfaces:

- **Sales → Finance**: winning an opportunity creates the order; invoicing a fulfilled order calls
  `InvoiceService.createFromOrder(...)`, which raises the AR invoice and links it back.
- **Sales → Finance**: qualifying a lead creates the customer via `CustomerService`.
- **Sales / Projects → HR**: order owners and task assignees resolve through `EmployeeService`;
  Projects derives hourly cost from salary there (salary never leaves HR).

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 25, Spring Boot 3.5, Maven, virtual threads (no reactive stack) |
| Persistence | Flyway migrations; H2 in PostgreSQL mode (dev/test), PostgreSQL (prod) |
| API | REST + JSON under `/api/v1/{module}/...` |
| Frontend | React 18, TypeScript, Vite, TanStack Query, Recharts, Tailwind |

## Layout

```
simple-erp/
├── backend/   src/main/java/com/simpleerp/
│   ├── shared/     Money, AuditableEntity, ApiError, exceptions, error handler
│   ├── finance/    AR (Invoice) + AP (Bill) + parties + dashboard
│   ├── hr/         employees, departments, leave, building presence, dashboard
│   ├── sales/      leads, opportunities, orders, rep attribution, dashboard
│   └── projects/   projects, tasks, milestones, time entries, dashboard
│   └── src/main/resources/db/
│       ├── migration/   V1..V9 schema (prod + dev)
│       └── seed/        R__seed_demo_data.sql (dev only — excluded in prod)
├── frontend/  src/
│   ├── lib/          api client, query keys, formatters, design tokens
│   ├── components/   DashboardCard, StatCard, DataTable, StatusBadge, Modal, …
│   └── modules/      finance/ hr/ sales/ projects/ — each a dashboard + pages
└── docs/      hr.md (module intent)
```

## Run it

The app is two processes — the Spring backend on `:8080` and the Vite dev server on `:5173`
(which proxies `/api` to the backend).

**Backend** — defaults to in-memory H2 in PostgreSQL mode; Flyway owns the schema and a dev-only
seed populates every dashboard on first run:

```bash
cd backend
mvn spring-boot:run        # http://localhost:8080
mvn test                   # 50 tests: unit + web-layer + per-module H2 smoke tests
```

**Frontend**:

```bash
cd frontend
npm install
npm run dev                # http://localhost:5173
npm run build              # type-check + production build
```

For **PostgreSQL**, run the backend with `SPRING_PROFILES_ACTIVE=prod` and set `DATABASE_URL` /
`DATABASE_USER` / `DATABASE_PASSWORD`. The demo seed is dev-only — production starts empty.

> The in-memory H2 database resets on every backend restart, re-applying the demo seed; nothing
> persists between runs unless you use the `prod` profile with PostgreSQL.

## Dashboards

One read-only summary endpoint per module, each rendered into the same fixed grid:

```
GET /api/v1/finance/dashboard     GET /api/v1/sales/dashboard
GET /api/v1/hr/dashboard          GET /api/v1/projects/dashboard
```

## Conventions

Enforced by the companion `../erp-builder-tests` suite (no Lombok, DTOs are records, Money is
never `double`, derived overdue, exhaustive status switches, Flyway naming, TanStack Query with
loading/error/empty states, shared formatters, design tokens):

```bash
cd ../erp-builder-tests
python run_checks.py ../simple-erp --eval-id 0   # finance backend scaffold
python run_checks.py ../simple-erp --eval-id 1   # hr dashboard end-to-end
python run_checks.py ../simple-erp --eval-id 2   # sales win → invoice flow
```
