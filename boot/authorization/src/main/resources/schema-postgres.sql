-- Drop tables if they exist (ensures clean setup)
drop table if exists oauth2_client;
drop table if exists oauth2_authorization;
drop table if exists oauth2_authorization_consent;

create table oauth2_client
(
    id                            SERIAL primary key,
    client_id                     VARCHAR(255)  not null,
    client_id_issued_at           TIMESTAMP     not null default current_timestamp,
    client_secret                 VARCHAR(255)           default null,
    client_secret_expires_at      TIMESTAMP              default null,
    client_name                   VARCHAR(255)  not null,
    client_authentication_methods VARCHAR(1000) not null,
    authorization_grant_types     VARCHAR(1000) not null,
    redirect_uris                 VARCHAR(1000)          default null,
    post_logout_redirect_uris     VARCHAR(1000)          default null,
    scopes                        VARCHAR(1000) not null,
    client_settings               VARCHAR(2000) not null,
    token_settings                VARCHAR(2000) not null
);

create table oauth2_authorization
(
    id                            SERIAL primary key,
    registered_client_id          VARCHAR(255) not null,
    principal_name                VARCHAR(255) not null,
    authorization_grant_type      VARCHAR(255) not null,
    authorized_scopes             VARCHAR(1000) default null,
    attributes                    VARCHAR(4000) default null,
    state                         VARCHAR(500)  default null,
    authorization_code_value      VARCHAR(4000) default null,
    authorization_code_issued_at  TIMESTAMP     default null,
    authorization_code_expires_at TIMESTAMP     default null,
    authorization_code_metadata   VARCHAR(2000) default null,
    access_token_value            VARCHAR(4000) default null,
    access_token_issued_at        TIMESTAMP     default null,
    access_token_expires_at       TIMESTAMP     default null,
    access_token_metadata         VARCHAR(2000) default null,
    access_token_type             VARCHAR(255)  default null,
    access_token_scopes           VARCHAR(1000) default null,
    refresh_token_value           VARCHAR(4000) default null,
    refresh_token_issued_at       TIMESTAMP     default null,
    refresh_token_expires_at      TIMESTAMP     default null,
    refresh_token_metadata        VARCHAR(2000) default null,
    oidc_id_token_value           VARCHAR(4000) default null,
    oidc_id_token_issued_at       TIMESTAMP     default null,
    oidc_id_token_expires_at      TIMESTAMP     default null,
    oidc_id_token_metadata        VARCHAR(2000) default null,
    oidc_id_token_claims          VARCHAR(2000) default null,
    user_code_value               VARCHAR(4000) default null,
    user_code_issued_at           TIMESTAMP     default null,
    user_code_expires_at          TIMESTAMP     default null,
    user_code_metadata            VARCHAR(2000) default null,
    device_code_value             VARCHAR(4000) default null,
    device_code_issued_at         TIMESTAMP     default null,
    device_code_expires_at        TIMESTAMP     default null,
    device_code_metadata          VARCHAR(2000) default null
);

create table oauth2_authorization_consent
(
    id                   SERIAL primary key,
    registered_client_id VARCHAR(255)  not null,
    principal_name       VARCHAR(255)  not null,
    authorities          VARCHAR(1000) not null,
    unique (registered_client_id, principal_name)
);