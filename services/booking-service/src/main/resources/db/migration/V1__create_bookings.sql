create table if not exists bookings (
    id uuid primary key,
    user_id uuid not null,
    machine_id uuid not null,
    status varchar(32) not null,
    expires_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
