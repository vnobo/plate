drop table if exists
    oauth2_authorized_client,
    se_authorities,
    se_group_authorities,
    se_group_members,
    se_tenant_members,
    se_menus,
    se_loggers,
    se_users,
    se_tenants,
    se_groups cascade;

create table if not exists se_menus
(
    id          BIGSERIAL primary key,
    code        uuid        not null unique default gen_random_uuid(),
    version     int         not null        default 0,
    pcode       uuid        not null        default '00000000-0000-0000-0000-000000000000',
    tenant_code uuid        not null        default '00000000-0000-0000-0000-000000000000',
    type        varchar(20) not null        default 'MENU',
    authority   varchar(256) not null unique,
    name        varchar(256) not null,
    path        text,
    sort_no     int                         default 0,
    extend      jsonb,
    created_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at  TIMESTAMPTZ not null        default current_timestamp,
    updated_at  TIMESTAMPTZ not null        default current_timestamp
);
create index se_menus_pttn_idx on se_menus (pcode, tenant_code, type, name);
create index se_menus_extend_gin_idx on se_menus using gin (extend);
comment on table se_menus is '菜单权限表';


create table if not exists oauth2_authorized_client
(
    client_registration_id  varchar(100)                            not null,
    principal_name          varchar(200)                            not null,
    access_token_type       varchar(100)                            not null,
    access_token_value      bytea                                   not null,
    access_token_issued_at  TIMESTAMPTZ                           not null,
    access_token_expires_at TIMESTAMPTZ                           not null,
    access_token_scopes     varchar(1000) default null,
    refresh_token_value     bytea         default null,
    refresh_token_issued_at TIMESTAMPTZ default null,
    created_at              TIMESTAMPTZ default current_timestamp not null,
    primary key (client_registration_id, principal_name)
);

create table if not exists se_users
(
    id                  BIGSERIAL primary key,
    code                uuid        not null unique default gen_random_uuid(),
    version             int         not null        default 0,
    tenant_code         uuid        not null        default '00000000-0000-0000-0000-000000000000',
    username            varchar(256) not null unique,
    password            text         not null,
    disabled            boolean     not null        default false,
    account_expired     boolean     not null        default false,
    account_locked      boolean     not null        default false,
    credentials_expired boolean     not null        default false,
    name                varchar(512),
    email               varchar(512),
    phone               varchar(32),
    avatar              text,
    bio                 text,
    extend              jsonb,
    login_time          TIMESTAMPTZ                 default current_timestamp,
    created_by          uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by          uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at          TIMESTAMPTZ not null        default current_timestamp,
    updated_at          TIMESTAMPTZ not null        default current_timestamp,
    text_search         tsvector generated always as (
        setweight(to_tsvector('chinese', code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', tenant_code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', username), 'A') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(name, '')), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(phone, '')), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(email, '')), 'C')
        ) stored
);
create index se_users_text_full_search_gist_idx on se_users using gin (text_search);
create index se_users_tu_idx on se_users (tenant_code, username);
create index se_users_extend_gin_idx on se_users using gin (extend);
comment on table se_users is '用户表';

create table if not exists se_authorities
(
    id          BIGSERIAL primary key,
    code        uuid        not null unique default gen_random_uuid(),
    version     int         not null        default 0,
    tenant_code uuid        not null        default '00000000-0000-0000-0000-000000000000',
    user_code   uuid         not null,
    authority   varchar(512) not null,
    extend      jsonb,
    created_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at  TIMESTAMPTZ not null        default current_timestamp,
    updated_at  TIMESTAMPTZ not null        default current_timestamp,
    unique (user_code, authority),
    foreign key (user_code) references se_users (code) on delete cascade
);
create index se_authorities_extend_gin_idx on se_authorities using gin (extend);
comment on table se_authorities is '用户权限表';

create table if not exists se_groups
(
    id          BIGSERIAL primary key,
    code        uuid        not null unique default gen_random_uuid(),
    version     int         not null        default 0,
    pcode       uuid        not null        default '00000000-0000-0000-0000-000000000000',
    tenant_code uuid        not null        default '00000000-0000-0000-0000-000000000000',
    name        varchar(512) not null,
    extend      jsonb,
    created_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at  TIMESTAMPTZ not null        default current_timestamp,
    updated_at  TIMESTAMPTZ not null        default current_timestamp,
    text_search tsvector generated always as (
        setweight(to_tsvector('chinese', code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', tenant_code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(name, '')), 'B')
        ) stored
);
create index se_groups_tn_idx on se_groups (tenant_code, name);
create index se_groups_extend_gin_idx on se_groups using gin (extend);
comment on table se_groups is '角色表';

create table if not exists se_group_authorities
(
    id         BIGSERIAL primary key,
    code       uuid         not null unique default gen_random_uuid(),
    version    int          not null        default 0,
    group_code uuid         not null,
    authority  varchar(512) not null,
    extend     jsonb,
    created_by uuid         not null        default '00000000-0000-0000-0000-000000000000',
    updated_by uuid         not null        default '00000000-0000-0000-0000-000000000000',
    created_at TIMESTAMPTZ  not null        default current_timestamp,
    updated_at TIMESTAMPTZ  not null        default current_timestamp,
    unique (group_code, authority),
    foreign key (group_code) references se_groups (code) on delete cascade
);
create index se_group_authorities_extend_gin_idx on se_group_authorities using gin (extend);
comment on table se_group_authorities is '角色权限表';

create table if not exists se_group_members
(
    id         BIGSERIAL primary key,
    code       uuid        not null unique default gen_random_uuid(),
    version    int         not null        default 0,
    group_code uuid        not null,
    user_code  uuid        not null,
    extend     jsonb,
    created_by uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at TIMESTAMPTZ not null        default current_timestamp,
    updated_at TIMESTAMPTZ not null        default current_timestamp,
    unique (group_code, user_code),
    foreign key (group_code) references se_groups (code) on delete cascade,
    foreign key (user_code) references se_users (code) on delete cascade
);
create index se_group_members_extend_gin_idx on se_group_members using gin (extend);
comment on table se_group_members is '角色用户关系表';

create table if not exists se_tenants
(
    id          serial primary key,
    code       uuid        not null unique default gen_random_uuid(),
    version    int         not null        default 0,
    pcode      uuid        not null        default '00000000-0000-0000-0000-000000000000',
    name        varchar(512) not null,
    description text,
    extend      jsonb,
    created_by uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at TIMESTAMPTZ not null        default current_timestamp,
    updated_at TIMESTAMPTZ not null        default current_timestamp,
    text_search tsvector generated always as (
        setweight(to_tsvector('chinese', code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', pcode::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', name), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(description, '')), 'B')
        ) stored
);
create index se_tenants_extend_gin_idx on se_tenants using gin (extend);
comment on table se_tenants is '租户表';

create table if not exists se_tenant_members
(
    id          BIGSERIAL primary key,
    code        uuid        not null unique default gen_random_uuid(),
    version int not null default 0,
    tenant_code uuid        not null        default '00000000-0000-0000-0000-000000000000',
    user_code   uuid        not null,
    enabled     boolean     not null        default true,
    extend      jsonb,
    created_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at  TIMESTAMPTZ not null        default current_timestamp,
    updated_at  TIMESTAMPTZ not null        default current_timestamp,
    unique (tenant_code, user_code),
    foreign key (tenant_code) references se_tenants (code) on delete cascade,
    foreign key (user_code) references se_users (code) on delete cascade
);
create index se_tenant_members_extend_gin_idx on se_tenant_members using gin (extend);
comment on table se_tenant_members is '租户用户关系表';

create table if not exists se_loggers
(
    id          BIGSERIAL primary key,
    code        uuid        not null unique default gen_random_uuid(),
    version int not null default 0,
    tenant_code uuid        not null        default '00000000-0000-0000-0000-000000000000',
    prefix      varchar(64),
    operator    varchar(64),
    status      varchar(64),
    method      varchar(64),
    url         text,
    context     jsonb,
    extend      jsonb,
    created_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    updated_by  uuid        not null        default '00000000-0000-0000-0000-000000000000',
    created_at  TIMESTAMPTZ not null        default current_timestamp,
    updated_at  TIMESTAMPTZ not null        default current_timestamp,
    text_search tsvector generated always as (
        setweight(to_tsvector('chinese', code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', tenant_code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(prefix, '')), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(operator, '')), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(method, '')), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(url, '')), 'C') || ' ' ||
        setweight(jsonb_to_tsvector('chinese', context::jsonb, '[
          "string"
        ]'), 'D')) stored
);
create index se_loggers_context_gin_idx on se_loggers using gin (context);
create index se_loggers_extend_gin_idx on se_loggers using gin (extend);
create index se_loggers_text_search_gin_idx on se_loggers using gin (text_search);
comment on table se_loggers is '操作日志表';