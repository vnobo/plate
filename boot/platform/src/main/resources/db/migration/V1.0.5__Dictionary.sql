drop table if exists se_dictionaries;
-- Create data dictionary table
create table if not exists se_dictionaries
(
    id          BIGSERIAL primary key,
    code        uuid         not null unique default uuidv7(),
    version     int          not null        default 0,
    pcode       uuid         not null        default '00000000-0000-0000-0000-000000000000',
    tenant_code uuid         not null        default '00000000-0000-0000-0000-000000000000',
    dict_type   varchar(128) not null,
    dict_key    varchar(256) not null,
    dict_value  text         not null,
    dict_label  varchar(512) not null,
    description text,
    sort_no     int                          default 0,
    enabled     boolean      not null        default true,
    extend      jsonb,
    created_by  uuid         not null        default '00000000-0000-0000-0000-000000000000',
    updated_by  uuid         not null        default '00000000-0000-0000-0000-000000000000',
    created_at  TIMESTAMPTZ  not null        default current_timestamp,
    updated_at  TIMESTAMPTZ  not null        default current_timestamp,
    text_search tsvector generated always as (
        setweight(to_tsvector('chinese', code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', tenant_code::text), 'A') || ' ' ||
        setweight(to_tsvector('chinese', dict_type), 'A') || ' ' ||
        setweight(to_tsvector('chinese', dict_key), 'A') || ' ' ||
        setweight(to_tsvector('chinese', dict_label), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(dict_value, '')), 'B') || ' ' ||
        setweight(to_tsvector('chinese', coalesce(description, '')), 'C')
        ) stored,
    constraint se_dictionaries_tenant_type_key_ux unique (tenant_code, dict_type, dict_key)
);

create index se_dictionaries_ttk_idx on se_dictionaries (tenant_code, dict_type, dict_key);
create index se_dictionaries_type_idx on se_dictionaries (dict_type);
create index se_dictionaries_extend_gin_idx on se_dictionaries using gin (extend);
create index se_dictionaries_text_search_gin_idx on se_dictionaries using gin (text_search);

comment on table se_dictionaries is '数据字典表';
comment on column se_dictionaries.dict_type is '字典类型';
comment on column se_dictionaries.dict_key is '字典键';
comment on column se_dictionaries.dict_value is '字典值';
comment on column se_dictionaries.dict_label is '字典标签';
comment on column se_dictionaries.description is '字典描述';
comment on column se_dictionaries.sort_no is '排序号';
comment on column se_dictionaries.enabled is '是否启用';
