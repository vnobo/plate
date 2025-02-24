do
$do$
    declare
        v_code      varchar(32);
        v_phone     varchar(32);
        batch_size  INTEGER := 10000;
        batch_count INTEGER := 0;
    begin
        for i in 1..100000
            loop
                -- 生成用户代码
                if i < 1000 then
                    v_code := 'U' || lpad(i::text, 4, '0');
                else
                    v_code := 'U' || (i + 3)::text;
                end if;

                -- 生成手机号码
                v_phone := '1708911826' || lpad(i::text, 2, '0');

                -- 插入用户数据
                insert into se_users(code, username, password, name, phone, email, bio, creator, updater)
                values (gen_random_uuid(), 'user' || i,
                        '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
                        '普通用户' || i, v_phone, 'user' || i || '@qq.com', null,
                        'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'f47ac10b-58cc-4372-a567-0e02b2c3d479');
                batch_count := batch_count + 1;
                if (batch_count % batch_size = 0) then
                    commit;
                end if;
            end loop;
        commit;
    end;
$do$;