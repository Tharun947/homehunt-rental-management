package com.homehunt.config;

import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class DatabaseSchemaInitializer {
    private final JdbcTemplate jdbcTemplate;

    @Bean
    ApplicationRunner repairPostgresEnumChecks() {
        return args -> {
            try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
                if (!connection.getMetaData().getDatabaseProductName().toLowerCase().contains("postgresql")) {
                    return;
                }
            }
            jdbcTemplate.execute("alter table if exists properties drop constraint if exists properties_status_check");
            jdbcTemplate.execute("""
                    alter table if exists properties
                    add constraint properties_status_check
                    check (status in ('PENDING', 'APPROVED', 'REJECTED'))
                    """);
        };
    }
}
