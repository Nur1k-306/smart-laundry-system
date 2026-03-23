create table if not exists machines (
    id uuid primary key,
    name varchar(64) not null unique,
    location varchar(255) not null,
    business_status varchar(32) not null,
    technical_status varchar(32) not null,
    active_booking_id uuid null,
    active_user_id uuid null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);
