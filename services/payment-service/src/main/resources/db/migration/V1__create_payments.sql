create table if not exists payments (
    id uuid primary key,
    booking_id uuid not null,
    user_id uuid not null,
    machine_id uuid not null,
    amount numeric(10, 2) not null,
    status varchar(32) not null,
    rejection_reason varchar(255),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index if not exists idx_payments_booking_status on payments (booking_id, status);
