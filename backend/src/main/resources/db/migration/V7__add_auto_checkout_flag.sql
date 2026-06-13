-- Marks presence records the end-of-day sweep closed automatically (employee never checked out),
-- so the roll-call can distinguish a real check-out from a system-corrected one.
alter table building_presence add column auto_checked_out boolean not null default false;
