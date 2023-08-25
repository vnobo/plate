drop table if exists se_users;
create table se_users
(
    id                  serial8 primary key,
    code        varchar(32) not null unique,
    tenant_code varchar(32) not null default '0',
    username            varchar(64) not null unique,
    password            text        not null,
    disabled            boolean     not null default false,
    account_expired     boolean     not null default false,
    account_locked      boolean     not null default false,
    credentials_expired boolean     not null default false,
    name                varchar(64),
    extend              jsonb,
    creator             varchar(64),
    updater             varchar(64),
    login_time          timestamp            default current_timestamp,
    created_time        timestamp            default current_timestamp,
    updated_time        timestamp            default current_timestamp
);
create index idx_se_users_extend_gin on se_users using gin (extend);
comment on table se_users is '用户表';

drop table if exists se_authorities;
create table se_authorities
(
    id        serial8 primary key,
    user_code varchar(32)  not null,
    authority varchar(256) not null,
    unique (user_code, authority)
);
comment on table se_authorities is '用户权限表';

drop table if exists se_groups;
create table se_groups
(
    id           serial8 primary key,
    code         varchar(32) not null unique,
    pcode        varchar(32) not null default '0',
    tenant_code varchar(32) not null default '0',
    name         varchar(64) not null,
    extend       jsonb,
    created_time timestamp            default current_timestamp,
    updated_time timestamp            default current_timestamp
);
create index idx_se_groups_extend_gin on se_groups using gin (extend);
comment on table se_groups is '角色表';

drop table if exists se_group_authorities;
create table se_group_authorities
(
    id         serial8 primary key,
    group_code varchar(32) not null,
    authority  varchar(64) not null,
    unique (group_code, authority)
);
comment on table se_group_authorities is '角色权限表';

drop table if exists se_group_members;
create table se_group_members
(
    id         serial8 primary key,
    group_code varchar(32) not null,
    user_code varchar(32) not null,
    unique (group_code, user_code)
);
comment on table se_group_members is '角色用户关系表';

drop table if exists se_tenants;
create table se_tenants
(
    id           serial primary key,
    code         varchar(32) not null unique,
    pcode        varchar(32) not null,
    name         varchar(64) not null,
    description  text,
    extend       jsonb,
    creator varchar(64),
    updater varchar(64),
    created_time timestamp default current_timestamp,
    updated_time timestamp default current_timestamp
);
create index idx_se_tenants_extend_gin on se_tenants using gin (extend);
comment on table se_tenants is '租户表';

drop table if exists se_tenant_members;
create table se_tenant_members
(
    id          serial8 primary key,
    tenant_code varchar(32) not null,
    user_code varchar(32) not null,
    enabled     boolean     not null default true,
    unique (tenant_code, user_code)
);
comment on table se_tenant_members is '租户用户关系表';

drop table if exists se_menus;
create table se_menus
(
    id           serial8 primary key,
    code         varchar(32) not null unique,
    pcode        varchar(32) not null default '0',
    tenant_code varchar(32) not null default '0',
    type        varchar(20) not null default 'MENU',
    sort        int         not null default 0,
    authority    varchar(64) not null,
    name         varchar(64) not null,
    path         text,
    extend       jsonb,
    created_time timestamp            default current_timestamp,
    updated_time timestamp            default current_timestamp,
    unique (tenant_code, authority)
);
create index idx_se_menus_extend_gin on se_menus using gin (extend);
comment on table se_menus is '菜单权限表';

drop table if exists se_loggers;
create table se_loggers
(
    id           serial8 primary key,
    code        varchar(32) not null unique,
    tenant_code varchar(32) not null default '0',
    prefix       varchar(32),
    operator     varchar(32),
    status       varchar(32),
    method       varchar(32),
    url          text,
    context      jsonb,
    created_time timestamp            default current_timestamp,
    updated_time timestamp            default current_timestamp
);
create index idx_se_loggers_extend_gin on se_loggers using gin (context);
comment on table se_loggers is '操作日志表';