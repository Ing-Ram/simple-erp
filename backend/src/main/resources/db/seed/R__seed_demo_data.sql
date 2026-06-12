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

-- Advance identity sequences past the seeded ids so app-created rows don't collide.
alter table customers     alter column id restart with 100;
alter table vendors       alter column id restart with 100;
alter table invoices      alter column id restart with 100;
alter table invoice_lines alter column id restart with 100;
alter table payments      alter column id restart with 100;
alter table bills         alter column id restart with 100;
alter table bill_lines    alter column id restart with 100;
alter table bill_payments alter column id restart with 100;
