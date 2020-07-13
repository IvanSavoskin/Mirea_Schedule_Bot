package edu_bot.main_class.config_class;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;


@PropertySources({
        @PropertySource("classpath:config/bot/application.properties"),
        @PropertySource(value = "file:application.properties", ignoreResourceNotFound = true)
})
@ComponentScan("edu_bot")
@Configuration
public class AppConfig
{
    @Value("${jdbc.datasource.h2.user:sa}")
    private String user;

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Profile("h2")
    public DataSource h2DataSource()
    {
        SimpleDriverDataSource dataSource = new  SimpleDriverDataSource();

        /** Блок получения строки подключения для базы данных H2 */
        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUsername(user);
        dataSource.setPassword("");
        dataSource.setUrl("jdbc:h2:file:./H2/H2_DateBase;init=runscript from './H2/schema.sql';IFEXISTS=false;DB_CLOSE_ON_EXIT=FALSE");

        return dataSource;
    }
}
