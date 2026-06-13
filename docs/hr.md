# HR module

## Main goal

HR is the **system of record for the company's people and their time off** — an employee
directory plus leave management. It is deliberately light: **payroll is out of scope** (salary is
stored only as a single figure for reporting and for deriving Projects' hourly cost later).

It exists to do three things, and to be one thing for the rest of the system:

1. **Maintain who works here.** Employees and departments, with employment kept as *history* —
   people are terminated, never hard-deleted, so headcount and turnover math stay honest.
2. **Manage leave end to end.** Requests move through a controlled lifecycle with rules that keep
   the data trustworthy (see below).
3. **Surface the people signals managers act on.** One dashboard answers: active headcount, recent
   hires, pending leave, turnover, who's out, and what's waiting for review.

And structurally:

4. **Own employee identity for the whole ERP.** Other modules look employees up through
   `EmployeeService`; they never reach into HR's tables.

## Scope

In scope:

- **Employees** — name, email, department, position (free-text title), hire date, termination date
  (nullable), salary (a single `Money` figure), and employment status.
- **Departments** — name and an (optional) manager who is an employee.
- **Leave requests** — type (vacation / sick / unpaid / parental), date range, status, reviewer,
  and decision timestamp.
- **The HR dashboard** — a single read-only summary endpoint.

Out of scope (v1): payroll runs, benefits, performance, holiday calendars, sub-day/hourly leave.

## Leave workflow

```
PENDING ──→ APPROVED ──→ CANCELLED   (cancel only while the leave is still in the future)
       └──→ REJECTED
```

Enforced in the service with exhaustive switches, so a new status becomes a compile error:

- End date must not precede start date.
- A request may not overlap the employee's existing **approved** leave (checked on submit and again
  on approve).
- Business-day counts exclude weekends only — holiday calendars are intentionally not modeled in v1.

## Derived, never stored

- **`ON_LEAVE`** is computed from approved leave covering *today*; only `ACTIVE` and `TERMINATED`
  are persisted. There is one copy of the truth, so the status can't drift.
- **Turnover** = terminations in the last 12 months ÷ average headcount (approximated by current
  active headcount in v1), computed at read time.
- Headcount, hires, "who's out", and the pending queue are all aggregated in SQL/JPQL at request
  time — never cached or duplicated.

## Dashboard (`GET /api/v1/hr/dashboard`)

One call returns the whole summary:

- **KPIs** — active headcount · hires in the last 90 days · pending leave requests · turnover
  (trailing 12 months).
- **Headcount by department** — the primary chart.
- **Who's out** — employees on approved leave today and over the next 14 days.
- **Needs attention** — pending leave requests, oldest first, with inline approve / reject (the one
  sanctioned place to mutate from a dashboard, because making approvers leave would break the flow).

## Cross-module contract

HR is the authority for employee identity. Other modules resolve employees through
`EmployeeService.require(id)` — Sales as an order owner, Projects as a task assignee and (later) to
derive an hourly cost from salary. They look up; they never touch HR repositories or entities
directly, which keeps the monolith splittable.
