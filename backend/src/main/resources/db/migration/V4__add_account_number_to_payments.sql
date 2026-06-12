-- Capture the payer's bank account number on check payments, stored MASKED (last 3 digits only);
-- the full number is never persisted. Nullable, set only when method = CHECK.
alter table payments      add column account_number varchar(34);
alter table bill_payments add column account_number varchar(34);
