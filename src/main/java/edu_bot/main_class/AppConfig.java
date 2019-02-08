package edu_bot.main_class;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@ComponentScan("edu_bot")
@Configuration
public class AppConfig
{
    /** Получение имени пользователя db H2 */
    public String getH2Username() {
        return Config.H2_USERNAME;
    }

    /** Получение имени пользователя db Amazon */
    public String getAmazonUsername() {
        return Config.AMAZON_USERNAME;
    }

    /** Получение пароля db Amazon */
    public String getAmazonPassword() {
        return Config.AMAZON_PASSWORD;
    }

    /** Получение хоста db Amazon */
    public String getAmazonHost() {
        return Config.AMAZON_HOST;
    }

    /** Получение имени базы данных db Amazon */
    public String getAmazonDataBase() {
        return Config.AMAZON_DATABASE;
    }

    /** Получение порта db Amazon */
    public String getAmazonPort() {
        return Config.AMAZON_PORT;
    }

    @Bean
    public JdbcTemplate jdbcTemplate()
    {
        return new JdbcTemplate(dataSource());
    }

   @Bean
   public DataSource dataSource()
   {
       SimpleDriverDataSource dataSource = new  SimpleDriverDataSource();
       /** Блок получения строки подключения для базы данных Amazon */
       /*dataSource.setDriverClass(org.postgresql.Driver.class);
       String dbName = getAmazonDataBase();
       String userName = getAmazonUsername();
       String password = getAmazonPassword();
       String hostname = getAmazonHost();
       String port = getAmazonPort();
       String jdbcUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
       dataSource.setUrl(jdbcUrl);*/

       /** Блок получения строки подключения для базы данных H2 */
       dataSource.setDriverClass(org.h2.Driver.class);
       dataSource.setUsername(getH2Username());
       dataSource.setPassword("");
       dataSource.setUrl("jdbc:h2:file:./H2/H2_DateBase;init=runscript from './schema.sql';IFEXISTS=false;DB_CLOSE_ON_EXIT=FALSE");

       return dataSource;
   }
}
