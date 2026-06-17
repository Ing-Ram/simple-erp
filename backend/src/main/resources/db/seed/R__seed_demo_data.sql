-- Demo data for local development only (loaded via the db/seed Flyway location, which the prod
-- profile excludes). Repeatable + delete-first so it reapplies cleanly on every fresh start.
-- Dates are anchored around 2026-06-10 so aging buckets, overdue, and cash flow all populate.

-- Clear children before parents to respect foreign keys.
delete from payments;
delete from bill_payments;
delete from invoice_lines;
delete from bill_lines;
delete from invoices;
delete from bills;
delete from customers;
delete from vendors;
-- HR: break the department↔employee cycle before deleting.
delete from building_presence;
delete from leave_requests;
update departments set manager_id = null;
delete from employees;
delete from departments;
-- Sales (children before parents).
delete from sales_order_lines;
delete from sales_orders;
delete from opportunities;
delete from leads;
-- Projects (children before parents).
delete from time_entries;
delete from tasks;
delete from milestones;
delete from projects;

-- Customers (AR) --------------------------------------------------------------
insert into customers (id, name, email, payment_terms_days, active, created_at, updated_at) values
    (1, 'Acme Corp',        'ap@acme.example',    30, true, timestamp '2026-01-05 09:00:00', timestamp '2026-01-05 09:00:00'),
    (2, 'Globex',           'billing@globex.example', 30, true, timestamp '2026-01-05 09:00:00', timestamp '2026-01-05 09:00:00'),
    (3, 'Initech',          'pay@initech.example', 45, true, timestamp '2026-01-05 09:00:00', timestamp '2026-01-05 09:00:00');

-- Vendors (AP) ----------------------------------------------------------------
insert into vendors (id, name, email, payment_terms_days, active, created_at, updated_at) values
    (1, 'Office Supplies Co', 'ar@officesupplies.example', 15, true, timestamp '2026-01-05 09:00:00', timestamp '2026-01-05 09:00:00'),
    (2, 'Cloud Hosting Inc',  'ar@cloudhosting.example',   30, true, timestamp '2026-01-05 09:00:00', timestamp '2026-01-05 09:00:00');

-- Invoices (AR) ---------------------------------------------------------------
-- 1: overdue 10 days (D1_30), 2: current, 3: overdue ~87 days (D61_90) + partial, 4: paid, 5: draft.
insert into invoices (id, customer_id, issue_date, due_date, status, sales_order_id, created_at, updated_at) values
    (1, 1, date '2026-05-01', date '2026-05-31', 'SENT',           null, timestamp '2026-05-01 09:00:00', timestamp '2026-05-01 09:00:00'),
    (2, 2, date '2026-06-01', date '2026-07-01', 'SENT',           null, timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (3, 3, date '2026-03-01', date '2026-03-15', 'PARTIALLY_PAID', null, timestamp '2026-03-01 09:00:00', timestamp '2026-05-15 09:00:00'),
    (4, 1, date '2026-06-02', date '2026-06-20', 'PAID',           null, timestamp '2026-06-02 09:00:00', timestamp '2026-06-05 09:00:00'),
    (5, 2, date '2026-06-09', date '2026-07-09', 'DRAFT',          null, timestamp '2026-06-09 09:00:00', timestamp '2026-06-09 09:00:00');

insert into invoice_lines (id, invoice_id, description, quantity, unit_price_amount, unit_price_currency, created_at, updated_at) values
    (1, 1, 'Consulting — May',       1, 5000.0000, 'USD', timestamp '2026-05-01 09:00:00', timestamp '2026-05-01 09:00:00'),
    (2, 2, 'Platform license',       1, 3000.0000, 'USD', timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (3, 3, 'Implementation — Q1',    1, 4000.0000, 'USD', timestamp '2026-03-01 09:00:00', timestamp '2026-03-01 09:00:00'),
    (4, 4, 'Support retainer',       1, 2000.0000, 'USD', timestamp '2026-06-02 09:00:00', timestamp '2026-06-02 09:00:00'),
    (5, 5, 'Draft scope',            1, 1000.0000, 'USD', timestamp '2026-06-09 09:00:00', timestamp '2026-06-09 09:00:00');

-- Payments received — dates within the last 30 days drive "cash in".
insert into payments (id, invoice_id, amount_amount, amount_currency, payment_date, method, created_at, updated_at) values
    (1, 3, 1500.0000, 'USD', date '2026-05-15', 'BANK_TRANSFER', timestamp '2026-05-15 09:00:00', timestamp '2026-05-15 09:00:00'),
    (2, 4, 2000.0000, 'USD', date '2026-06-05', 'CARD',          timestamp '2026-06-05 09:00:00', timestamp '2026-06-05 09:00:00');

-- Bills (AP) ------------------------------------------------------------------
-- 1: due in 5 days (due-soon), 2: current, 3: overdue 21 days (D1_30) + partial, 4: paid.
insert into bills (id, vendor_id, issue_date, due_date, status, created_at, updated_at) values
    (1, 1, date '2026-06-01', date '2026-06-15', 'SENT',           timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (2, 2, date '2026-06-05', date '2026-07-05', 'SENT',           timestamp '2026-06-05 09:00:00', timestamp '2026-06-05 09:00:00'),
    (3, 1, date '2026-05-05', date '2026-05-20', 'PARTIALLY_PAID', timestamp '2026-05-05 09:00:00', timestamp '2026-05-25 09:00:00'),
    (4, 2, date '2026-05-10', date '2026-06-09', 'PAID',           timestamp '2026-05-10 09:00:00', timestamp '2026-06-01 09:00:00');

insert into bill_lines (id, bill_id, description, quantity, unit_price_amount, unit_price_currency, created_at, updated_at) values
    (1, 1, 'Office supplies — June', 1, 1200.0000, 'USD', timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (2, 2, 'Cloud hosting — June',   1,  800.0000, 'USD', timestamp '2026-06-05 09:00:00', timestamp '2026-06-05 09:00:00'),
    (3, 3, 'Office supplies — May',  1, 2000.0000, 'USD', timestamp '2026-05-05 09:00:00', timestamp '2026-05-05 09:00:00'),
    (4, 4, 'Cloud hosting — May',    1,  600.0000, 'USD', timestamp '2026-05-10 09:00:00', timestamp '2026-05-10 09:00:00');

-- Payments made — dates within the last 30 days drive "cash out".
insert into bill_payments (id, bill_id, amount_amount, amount_currency, payment_date, method, created_at, updated_at) values
    (1, 3,  500.0000, 'USD', date '2026-05-25', 'BANK_TRANSFER', timestamp '2026-05-25 09:00:00', timestamp '2026-05-25 09:00:00'),
    (2, 4,  600.0000, 'USD', date '2026-06-01', 'CHECK',         timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00');

-- HR: departments (managers assigned after employees exist) ------------------
insert into departments (id, name, manager_id, created_at, updated_at) values
    (1, 'Engineering', null, timestamp '2026-01-02 09:00:00', timestamp '2026-01-02 09:00:00'),
    (2, 'Sales',       null, timestamp '2026-01-02 09:00:00', timestamp '2026-01-02 09:00:00'),
    (3, 'Operations',  null, timestamp '2026-01-02 09:00:00', timestamp '2026-01-02 09:00:00');

-- Employees: 6 active + 1 terminated (turnover). Carol & Grace are hires in the last 90 days.
insert into employees
    (id, name, email, department_id, position, hire_date, termination_date,
     salary_amount, salary_currency, status, created_at, updated_at) values
    (1, 'Alice Chen',     'alice@simpleerp.example',  1, 'Engineering Manager', date '2024-03-01', null, 145000.0000, 'USD', 'ACTIVE',     timestamp '2024-03-01 09:00:00', timestamp '2024-03-01 09:00:00'),
    (2, 'Bob Martinez',   'bob@simpleerp.example',    1, 'Senior Engineer',     date '2025-11-15', null, 120000.0000, 'USD', 'ACTIVE',     timestamp '2025-11-15 09:00:00', timestamp '2025-11-15 09:00:00'),
    (3, 'Carol Diaz',     'carol@simpleerp.example',  2, 'Account Executive',   date '2026-04-20', null,  98000.0000, 'USD', 'ACTIVE',     timestamp '2026-04-20 09:00:00', timestamp '2026-04-20 09:00:00'),
    (4, 'Dan Wright',     'dan@simpleerp.example',    2, 'Sales Manager',       date '2023-06-01', null, 110000.0000, 'USD', 'ACTIVE',     timestamp '2023-06-01 09:00:00', timestamp '2023-06-01 09:00:00'),
    (5, 'Erin Park',      'erin@simpleerp.example',   3, 'Operations Manager',  date '2022-01-10', null,  88000.0000, 'USD', 'ACTIVE',     timestamp '2022-01-10 09:00:00', timestamp '2022-01-10 09:00:00'),
    (6, 'Frank Lee',      'frank@simpleerp.example',  3, 'Operations Analyst',  date '2021-09-01', date '2026-02-15', 92000.0000, 'USD', 'TERMINATED', timestamp '2021-09-01 09:00:00', timestamp '2026-02-15 09:00:00'),
    (7, 'Grace Kim',      'grace@simpleerp.example',  1, 'Engineer',            date '2026-05-02', null, 105000.0000, 'USD', 'ACTIVE',     timestamp '2026-05-02 09:00:00', timestamp '2026-05-02 09:00:00');

-- Assign department managers now that the employees exist.
update departments set manager_id = 1 where id = 1;
update departments set manager_id = 4 where id = 2;
update departments set manager_id = 5 where id = 3;

-- Leave requests: 2 pending (needs attention), 2 approved in the next 14 days (who's out).
insert into leave_requests
    (id, employee_id, type, start_date, end_date, status, reviewer, decided_at, created_at, updated_at) values
    (1, 2, 'VACATION', date '2026-06-22', date '2026-06-26', 'PENDING',  null, null, timestamp '2026-06-05 09:00:00', timestamp '2026-06-05 09:00:00'),
    (2, 3, 'SICK',     date '2026-06-15', date '2026-06-16', 'PENDING',  null, null, timestamp '2026-06-10 09:00:00', timestamp '2026-06-10 09:00:00'),
    (3, 5, 'VACATION', date '2026-06-10', date '2026-06-16', 'APPROVED', 'HR', timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (4, 4, 'PARENTAL', date '2026-06-20', date '2026-06-27', 'APPROVED', 'HR', timestamp '2026-06-02 09:00:00', timestamp '2026-06-02 09:00:00', timestamp '2026-06-02 09:00:00'),
    (5, 1, 'VACATION', date '2026-03-01', date '2026-03-05', 'APPROVED', 'HR', timestamp '2026-02-20 09:00:00', timestamp '2026-02-20 09:00:00', timestamp '2026-02-20 09:00:00');

-- Building presence today: Alice present, Bob checked out, Grace remote. Erin is on leave;
-- Carol and Dan have no check-in, so the roll-call shows them UNACCOUNTED. Uses current_timestamp
-- so these always count as "today" on a fresh start.
insert into building_presence (id, employee_id, work_mode, check_in_at, check_out_at, created_at, updated_at) values
    (1, 1, 'ON_SITE', current_timestamp, null,              current_timestamp, current_timestamp),
    (2, 2, 'ON_SITE', current_timestamp, current_timestamp, current_timestamp, current_timestamp),
    (3, 7, 'REMOTE',  current_timestamp, null,              current_timestamp, current_timestamp);

-- Sales: leads, opportunities (open + won + lost + stale), orders. Owners are employees 3 & 4;
-- customers are 1 (Acme), 2 (Globex), 3 (Initech). Dates anchored around 2026-06-16.
insert into leads (id, name, company, email, source, status, customer_id, opportunity_id, created_at, updated_at) values
    (1, 'Jane Prospect', 'NewCo',   'jane@newco.example',  'WEBSITE',  'NEW',          null, null, timestamp '2026-06-08 09:00:00', timestamp '2026-06-08 09:00:00'),
    (2, 'Sam Buyer',     'BigCorp', 'sam@bigcorp.example', 'REFERRAL', 'CONTACTED',     null, null, timestamp '2026-06-11 09:00:00', timestamp '2026-06-11 09:00:00'),
    (3, 'Old Lead',      'DeadCo',  'x@deadco.example',    'OUTBOUND', 'DISQUALIFIED',  null, null, timestamp '2026-05-02 09:00:00', timestamp '2026-05-05 09:00:00');

insert into opportunities
    (id, customer_id, owner_employee_id, expected_value_amount, expected_value_currency, probability,
     expected_close_date, stage, previous_stage, closed_date, lost_reason, sales_order_id, created_at, updated_at) values
    (1, 1, 4, 50000.0000, 'USD', 60, date '2026-07-15', 'PROPOSAL',    null,       null,            null,          null, timestamp '2026-05-20 09:00:00', timestamp '2026-06-01 09:00:00'),
    (2, 2, 3, 30000.0000, 'USD', 40, date '2026-06-30', 'QUALIFIED',   null,       null,            null,          null, timestamp '2026-06-02 09:00:00', timestamp '2026-06-02 09:00:00'),
    (3, 3, 4, 80000.0000, 'USD', 80, date '2026-06-10', 'NEGOTIATION', null,       null,            null,          null, timestamp '2026-04-15 09:00:00', timestamp '2026-06-01 09:00:00'),
    (4, 1, 3, 45000.0000, 'USD', 100, date '2026-06-01', 'WON',        null,       date '2026-06-05', null,         1,    timestamp '2026-04-01 09:00:00', timestamp '2026-06-05 09:00:00'),
    (5, 2, 4, 20000.0000, 'USD', 0,  date '2026-05-15', 'LOST',        'PROPOSAL', date '2026-05-20', 'Budget cut', null, timestamp '2026-03-10 09:00:00', timestamp '2026-05-20 09:00:00'),
    (6, 3, 3, 60000.0000, 'USD', 100, date '2026-04-05', 'WON',        null,       date '2026-04-10', null,         2,    timestamp '2026-02-20 09:00:00', timestamp '2026-04-10 09:00:00');

insert into sales_orders (id, customer_id, owner_employee_id, status, order_date, opportunity_id, invoice_id, created_at, updated_at) values
    (1, 1, 3, 'FULFILLED', date '2026-06-05', 4, null, timestamp '2026-06-05 09:00:00', timestamp '2026-06-12 09:00:00'),
    (2, 3, 3, 'OPEN',      date '2026-04-10', 6, null, timestamp '2026-04-10 09:00:00', timestamp '2026-04-10 09:00:00');

insert into sales_order_lines (id, order_id, description, quantity, unit_price_amount, unit_price_currency, created_at, updated_at) values
    (1, 1, 'Won opportunity #4', 1, 45000.0000, 'USD', timestamp '2026-06-05 09:00:00', timestamp '2026-06-05 09:00:00'),
    (2, 2, 'Won opportunity #6', 1, 60000.0000, 'USD', timestamp '2026-04-10 09:00:00', timestamp '2026-04-10 09:00:00');

-- Projects: 3 active (one over budget, one at-risk, one on-track) + 1 completed. Spend is derived
-- from time entries × each assignee's HR hourly cost (salary ÷ 2080), so budgets are demo-sized.
insert into projects
    (id, name, customer_id, manager_employee_id, start_date, target_end_date,
     budget_amount, budget_currency, status, created_at, updated_at) values
    (1, 'Website Redesign', 1,    1, date '2026-04-01', date '2026-08-31', 4000.0000, 'USD', 'ACTIVE',    timestamp '2026-04-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (2, 'Internal Tooling', null, 1, date '2026-05-01', date '2026-06-10', 2000.0000, 'USD', 'ACTIVE',    timestamp '2026-05-01 09:00:00', timestamp '2026-06-01 09:00:00'),
    (3, 'Data Migration',   3,    4, date '2026-05-15', date '2026-09-30', 8000.0000, 'USD', 'ACTIVE',    timestamp '2026-05-15 09:00:00', timestamp '2026-06-01 09:00:00'),
    (4, 'Legacy Sunset',    null, 5, date '2026-03-01', date '2026-05-01', 3000.0000, 'USD', 'COMPLETED', timestamp '2026-03-01 09:00:00', timestamp '2026-05-02 09:00:00');

insert into tasks
    (id, project_id, title, assignee_employee_id, status, due_date, estimate_hours, created_at, updated_at) values
    (1, 1, 'Design mockups', 7, 'IN_PROGRESS', date '2026-06-20', 40.00, timestamp '2026-04-05 09:00:00', timestamp '2026-06-01 09:00:00'),
    (2, 1, 'Frontend build', 2, 'TODO',        date '2026-06-10', 80.00, timestamp '2026-04-05 09:00:00', timestamp '2026-06-01 09:00:00'),
    (3, 2, 'Build CLI',      7, 'IN_PROGRESS', date '2026-06-05', 30.00, timestamp '2026-05-02 09:00:00', timestamp '2026-06-01 09:00:00'),
    (4, 3, 'Migrate schema', 2, 'TODO',        date '2026-07-15', 50.00, timestamp '2026-05-16 09:00:00', timestamp '2026-06-01 09:00:00'),
    (5, 4, 'Decommission',   5, 'DONE',        date '2026-04-20', 20.00, timestamp '2026-03-05 09:00:00', timestamp '2026-04-26 09:00:00');

insert into milestones (id, project_id, name, due_date, completed_at, waived, created_at, updated_at) values
    (1, 1, 'Design sign-off', date '2026-06-25', null,                            false, timestamp '2026-04-05 09:00:00', timestamp '2026-04-05 09:00:00'),
    (2, 1, 'Launch',          date '2026-08-20', null,                            false, timestamp '2026-04-05 09:00:00', timestamp '2026-04-05 09:00:00'),
    (3, 2, 'MVP',             date '2026-06-08', null,                            false, timestamp '2026-05-02 09:00:00', timestamp '2026-05-02 09:00:00'),
    (4, 3, 'Cutover',         date '2026-09-01', null,                            false, timestamp '2026-05-16 09:00:00', timestamp '2026-05-16 09:00:00'),
    (5, 4, 'Shutdown',        date '2026-04-28', timestamp '2026-04-28 09:00:00', false, timestamp '2026-03-05 09:00:00', timestamp '2026-04-28 09:00:00');

-- Time entries drive actual spend. Dates within ~30 days feed utilization; 6/15–6/16 are "this week".
insert into time_entries (id, task_id, employee_id, entry_date, hours, note, created_at, updated_at) values
    (1,  1, 7, date '2026-05-20', 16.00, 'Wireframes',     timestamp '2026-05-20 18:00:00', timestamp '2026-05-20 18:00:00'),
    (2,  1, 7, date '2026-06-15', 16.00, 'High-fidelity',  timestamp '2026-06-15 18:00:00', timestamp '2026-06-15 18:00:00'),
    (3,  2, 2, date '2026-05-22', 16.00, 'Scaffolding',    timestamp '2026-05-22 18:00:00', timestamp '2026-05-22 18:00:00'),
    (4,  2, 2, date '2026-06-16', 16.00, 'Components',     timestamp '2026-06-16 18:00:00', timestamp '2026-06-16 18:00:00'),
    (5,  3, 7, date '2026-05-25', 16.00, 'CLI parser',     timestamp '2026-05-25 18:00:00', timestamp '2026-05-25 18:00:00'),
    (6,  3, 7, date '2026-06-01', 16.00, 'Commands',       timestamp '2026-06-01 18:00:00', timestamp '2026-06-01 18:00:00'),
    (7,  3, 7, date '2026-06-15', 16.00, 'Polish',         timestamp '2026-06-15 18:00:00', timestamp '2026-06-15 18:00:00'),
    (8,  4, 2, date '2026-06-02', 12.00, 'Schema audit',   timestamp '2026-06-02 18:00:00', timestamp '2026-06-02 18:00:00'),
    (9,  4, 2, date '2026-06-10', 12.00, 'ETL draft',      timestamp '2026-06-10 18:00:00', timestamp '2026-06-10 18:00:00'),
    (10, 5, 5, date '2026-04-25', 16.00, 'Final teardown', timestamp '2026-04-25 18:00:00', timestamp '2026-04-25 18:00:00');

-- Advance identity sequences past the seeded ids so app-created rows don't collide.
alter table customers      alter column id restart with 100;
alter table vendors        alter column id restart with 100;
alter table invoices       alter column id restart with 100;
alter table invoice_lines  alter column id restart with 100;
alter table payments       alter column id restart with 100;
alter table bills          alter column id restart with 100;
alter table bill_lines     alter column id restart with 100;
alter table bill_payments  alter column id restart with 100;
alter table departments       alter column id restart with 100;
alter table employees         alter column id restart with 100;
alter table leave_requests    alter column id restart with 100;
alter table building_presence alter column id restart with 100;
alter table leads             alter column id restart with 100;
alter table opportunities     alter column id restart with 100;
alter table sales_orders      alter column id restart with 100;
alter table sales_order_lines alter column id restart with 100;
alter table projects          alter column id restart with 100;
alter table tasks             alter column id restart with 100;
alter table milestones        alter column id restart with 100;
alter table time_entries      alter column id restart with 100;
