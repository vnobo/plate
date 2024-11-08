package com.plate.boot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootApplication
public class BootApplication {
    public static void main(String[] args) {
        SpringApplication.run(BootApplication.class, args);
    }

    @Component
    static class AppRunner implements ApplicationRunner {

        private final DatabaseClient databaseClient;

        AppRunner(DatabaseClient databaseClient) {
            this.databaseClient = databaseClient;
        }

        @Override
        public void run(ApplicationArguments args) {
            String sql = """
                    do $$
                        declare
                            v_code       text;
                            v_phone      text;
                            batch_size   int := 100; -- 每批提交的记录数
                            commit_count int := 0; -- 记录已提交的批次数
                        begin
                            for i in 1..1000000
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
                                    values (v_code, 'user' || i,
                                            '{pbkdf2}7d8a68bc5d507bd19bc153ff10bcdef66f5a5f3d0c1ab2438630e50b5c65894bccc2c7e4404c5afa',
                                            '普通用户' || i, v_phone, 'user' || i || '@qq.com', null, 'U1000', 'U1000');
                    
                                    -- 每插入 batch_size 条记录后提交事务
                                    commit_count := commit_count + 1;
                                    if commit_count % batch_size = 0 then
                                        commit;
                                    end if;
                                end loop;
                    
                            -- 提交剩余的记录
                            commit;
                        end $$;
                    """;
            databaseClient.sql(() -> sql).fetch().rowsUpdated().subscribe();
        }
    }
}