-- Capture routing and check numbers on check payments (nullable; only set when method = CHECK).
alter table payments      add column routing_number varchar(9);
alter table payments      add column check_number   varchar(50);
alter table bill_payments add column routing_number varchar(9);
alter table bill_payments add column check_number   varchar(50);
