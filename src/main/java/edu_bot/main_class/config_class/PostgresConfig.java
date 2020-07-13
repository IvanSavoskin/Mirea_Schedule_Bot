package edu_bot.main_class.config_class;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("postgres")
public class PostgresConfig
{

    @Value("${jdbc.datasource.postgres.host:localhost}")
    private String host;

    @Value("${jdbc.datasource.postgres.port:5432}")
    private int port;

    @Value("${jdbc.datasource.postgres.ssl:false}")
    private boolean isSsl;

    @Value("${jdbc.datasource.postgres.user:postgres}")
    private String user;

    @Value("${jdbc.datasource.postgres.password:postgres}")
    private String password;

    @Value("${jdbc.datasource.postgres.database:mirea_bot}")
    private String database;

    @Bean
    @Profile("postgres")
    public DataSource postgresDataSource()
    {
        final PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(host);
        dataSource.setPortNumber(port);
        dataSource.setSsl(isSsl);
        if (!user.isEmpty()) {
            dataSource.setUser(user);
        }
        if (!password.isEmpty()) {
            dataSource.setPassword(password);
        }
        if (!database.isEmpty()) {
            dataSource.setDatabaseName(database);
        }
        return dataSource;
    }
}

