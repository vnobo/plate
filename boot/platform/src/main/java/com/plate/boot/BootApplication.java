package com.plate.boot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

/**
 * Entry point for the Spring Boot application.
 * <p>
 * This class serves as the primary class to launch the Spring Boot application.
 * It is annotated with `@SpringBootApplication`, which is a convenience annotation
 * that includes `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.
 * These annotations together configure the application context, enable autoconfiguration
 * of Spring features, and scan for Spring components in the package where this class is located
 * and its sub-packages.
 * <p>
 * The `main` method initiates the application's run process by calling
 * `SpringApplication.run(BootApplication.class, args)`, where `args` are command-line arguments
 * passed to the application, if any.
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
                            batch_size   int := 1000; -- 每批提交的记录数
                            commit_count int := 0; -- 记录已提交的批次数
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