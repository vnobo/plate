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

comment on table se_dictionaries is 'Data dictionary table';
comment on column se_dictionaries.dict_type is 'Dictionary type';
comment on column se_dictionaries.dict_key is 'Dictionary key';
comment on column se_dictionaries.dict_value is 'Dictionary value';
comment on column se_dictionaries.dict_label is 'Dictionary label';
comment on column se_dictionaries.description is 'Dictionary description';
comment on column se_dictionaries.sort_no is 'Sort order';
comment on column se_dictionaries.enabled is 'Whether enabled';

-- Insert sample data dictionary entries for testing
-- These examples demonstrate various dictionary types and use cases

-- User Status Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000000', 'USER_STATUS', 'ACTIVE', '1', 'Active', 'User account is active and can login', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'USER_STATUS', 'INACTIVE', '0', 'Inactive', 'User account is inactive and cannot login', 2, true),
    ('00000000-0000-0000-0000-000000000000', 'USER_STATUS', 'LOCKED', '2', 'Locked', 'User account is locked due to security reasons', 3, true),
    ('00000000-0000-0000-0000-000000000000', 'USER_STATUS', 'PENDING', '3', 'Pending', 'User account is pending approval', 4, true);

-- Gender Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000000', 'GENDER', 'MALE', 'M', 'Male', 'Male gender', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'GENDER', 'FEMALE', 'F', 'Female', 'Female gender', 2, true),
    ('00000000-0000-0000-0000-000000000000', 'GENDER', 'OTHER', 'O', 'Other', 'Other gender or prefer not to say', 3, true);

-- Education Level Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000000', 'EDUCATION', 'HIGH_SCHOOL', '1', 'High School', 'High school diploma or equivalent', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'EDUCATION', 'ASSOCIATE', '2', 'Associate Degree', 'Associate degree (2-year college)', 2, true),
    ('00000000-0000-0000-0000-000000000000', 'EDUCATION', 'BACHELOR', '3', 'Bachelor''s Degree', 'Bachelor''s degree (4-year college)', 3, true),
    ('00000000-0000-0000-0000-000000000000', 'EDUCATION', 'MASTER', '4', 'Master''s Degree', 'Master''s degree (graduate)', 4, true),
    ('00000000-0000-0000-0000-000000000000', 'EDUCATION', 'DOCTORATE', '5', 'Doctorate', 'Doctoral degree (PhD, etc.)', 5, true);

-- Task Priority Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000000', 'PRIORITY', 'LOW', '1', 'Low', 'Low priority task', 3, true),
    ('00000000-0000-0000-0000-000000000000', 'PRIORITY', 'MEDIUM', '2', 'Medium', 'Medium priority task', 2, true),
    ('00000000-0000-0000-0000-000000000000', 'PRIORITY', 'HIGH', '3', 'High', 'High priority task', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'PRIORITY', 'URGENT', '4', 'Urgent', 'Urgent priority task requiring immediate attention', 0, true);

-- Document Status Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000000', 'DOC_STATUS', 'DRAFT', 'draft', 'Draft', 'Document is in draft status', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'DOC_STATUS', 'REVIEW', 'review', 'Under Review', 'Document is under review', 2, true),
    ('00000000-0000-0000-0000-000000000000', 'DOC_STATUS', 'APPROVED', 'approved', 'Approved', 'Document has been approved', 3, true),
    ('00000000-0000-0000-0000-000000000000', 'DOC_STATUS', 'PUBLISHED', 'published', 'Published', 'Document has been published', 4, true),
    ('00000000-0000-0000-0000-000000000000', 'DOC_STATUS', 'ARCHIVED', 'archived', 'Archived', 'Document has been archived', 5, true);

-- Language Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES ('00000000-0000-0000-0000-000000000000', 'LANGUAGE', 'ZH_CN', 'zh_CN', 'Chinese', 'Simplified Chinese', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'LANGUAGE', 'EN_US', 'en_US', 'English', 'English (United States)', 2, true),
    ('00000000-0000-0000-0000-000000000000', 'LANGUAGE', 'JA_JP', 'ja_JP', '日本語', 'Japanese', 3, true),
    ('00000000-0000-0000-0000-000000000000', 'LANGUAGE', 'KO_KR', 'ko_KR', '한국어', 'Korean', 4, true);

-- Boolean Yes/No Dictionary
INSERT INTO se_dictionaries (tenant_code, dict_type, dict_key, dict_value, dict_label, description, sort_no, enabled)
VALUES
    ('00000000-0000-0000-0000-000000000000', 'YES_NO', 'YES', '1', 'Yes', 'Affirmative response', 1, true),
    ('00000000-0000-0000-0000-000000000000', 'YES_NO', 'NO', '0', 'No', 'Negative response', 2, true);

