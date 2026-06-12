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
delete from leave_requests;
update departments set manager_id = null;
delete from employees;
delete from departments;

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

-- Advance identity sequences past the seeded ids so app-created rows don't collide.
alter table customers      alter column id restart with 100;
alter table vendors        alter column id restart with 100;
alter table invoices       alter column id restart with 100;
alter table invoice_lines  alter column id restart with 100;
alter table payments       alter column id restart with 100;
alter table bills          alter column id restart with 100;
alter table bill_lines     alter column id restart with 100;
alter table bill_payments  alter column id restart with 100;
alter table departments    alter column id restart with 100;
alter table employees      alter column id restart with 100;
alter table leave_requests alter column id restart with 100;
