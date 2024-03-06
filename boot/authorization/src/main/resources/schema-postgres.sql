drop table if exists oauth2_authorized_client;
create table oauth2_authorized_client
(
    client_registration_id  varchar(100)                            not null,
    principal_name          varchar(200)                            not null,
    access_token_type       varchar(100)                            not null,
    access_token_value      bytea                                   not null,
    access_token_issued_at  timestamp                               not null,
    access_token_expires_at timestamp                               not null,
    access_token_scopes     varchar(1000) default null,
    refresh_token_value     bytea         default null,
    refresh_token_issued_at timestamp     default null,
    created_at              timestamp     default current_timestamp not null,
    primary key (client_registration_id, principal_name)
);

drop table if exists se_users;
create table if not exists se_users
(
    id                  serial8 primary key,
    code                varchar(64)  not null unique,
    tenant_code varchar(64) not null default '0',
    username            varchar(256) not null unique,
    password            text         not null,
    disabled            boolean      not null default false,
    account_expired     boolean      not null default false,
    account_locked      boolean      not null default false,
    credentials_expired boolean      not null default false,
    name                varchar(512),
    email               varchar(512),
    phone       varchar(32),
    avatar              text,
    bio         text,
    extend              jsonb,
    creator             varchar(64),
    updater             varchar(64),
    login_time          timestamp             default current_timestamp,
    created_time        timestamp             default current_timestamp,
    updated_time        timestamp             default current_timestamp
);
create index se_users_tu_idx on se_users (tenant_code, username);
create index se_users_extend_gin_idx on se_users using gin (extend);
comment on table se_users is '用户表';

drop table if exists se_authorities;
create table if not exists se_authorities
(
    id           serial8 primary key,
    code         varchar(64)  not null unique,
    user_code    varchar(64)  not null,
    authority    varchar(512) not null,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    unique (user_code, authority)
);
comment on table se_authorities is '用户权限表';

drop table if exists se_groups;
create table if not exists se_groups
(
    id           serial8 primary key,
    code         varchar(64)  not null unique,
    tenant_code varchar(64) not null default '0',
    name         varchar(512) not null,
    extend       jsonb,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp             default current_timestamp,
    updated_time timestamp             default current_timestamp
);
create index se_groups_tn_idx on se_groups (tenant_code, name);
create index se_groups_extend_gin_idx on se_groups using gin (extend);
comment on table se_groups is '角色表';

drop table if exists se_group_authorities;
create table if not exists se_group_authorities
(
    id           serial8 primary key,
    code         varchar(64)  not null unique,
    group_code   varchar(64)  not null,
    authority    varchar(512) not null,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    unique (group_code, authority)
);
comment on table se_group_authorities is '角色权限表';

drop table if exists se_group_members;
create table if not exists se_group_members
(
    id           serial8 primary key,
    code         varchar(64) not null unique,
    group_code   varchar(64) not null,
    user_code    varchar(64) not null,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp,
    unique (group_code, user_code)
);
comment on table se_group_members is '角色用户关系表';

drop table if exists se_tenants;
create table if not exists se_tenants
(
    id           serial primary key,
    code         varchar(64)  not null unique,
    name         varchar(512) not null,
    description  text,
    extend       jsonb,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp
);
create index se_tenants_name_idx on se_tenants (name);
create index se_tenants_extend_gin_idx on se_tenants using gin (extend);
comment on table se_tenants is '租户表';

drop table if exists se_tenant_members;
create table if not exists se_tenant_members
(
    id           serial8 primary key,
    code         varchar(64) not null unique,
    tenant_code  varchar(64) not null,
    user_code    varchar(64) not null,
    enabled      boolean     not null default true,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp            default current_timestamp,
    updated_time timestamp            default current_timestamp,
    unique (tenant_code, user_code)
);
comment on table se_tenant_members is '租户用户关系表';

drop table if exists se_menus;
create table if not exists se_menus
(
    id           serial8 primary key,
    code         varchar(64)  not null unique,
    pcode        varchar(64)  not null default '0',
    tenant_code varchar(64) not null default '0',
    type         varchar(20)  not null default 'MENU',
    authority    varchar(512) not null unique,
    name         varchar(512) not null,
    path         text,
    sort         int                   default 0,
    extend       jsonb,
    creator      varchar(64),
    updater      varchar(64),
    created_time timestamp             default current_timestamp,
    updated_time timestamp             default current_timestamp
);
create index se_menus_pttn_idx on se_menus (pcode, tenant_code, type, name);
create index se_menus_extend_gin_idx on se_menus using gin (extend);
comment on table se_menus is '菜单权限表';

drop table if exists se_loggers;
create table if not exists se_loggers
(
    id           serial8 primary key,
    code         varchar(64) not null unique,
    tenant_code varchar(64) not null default '0',
    prefix       varchar(64),
    operator     varchar(64),
    status       varchar(64),
    method       varchar(64),
    url          text,
    context      jsonb,
    created_time timestamp            default current_timestamp,
    updated_time timestamp            default current_timestamp
);
create index se_loggers_tposm_idx on se_loggers (tenant_code, prefix, operator, status, method);
create index se_loggers_extend_gin_idx on se_loggers using gin (context);
comment on table se_loggers is '操作日志表';