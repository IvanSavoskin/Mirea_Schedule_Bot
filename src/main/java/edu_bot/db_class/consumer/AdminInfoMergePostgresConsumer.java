package edu_bot.db_class.consumer;

import edu_bot.db_class.model.AdminInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class AdminInfoMergePostgresConsumer implements Consumer<AdminInfo>
{
    private final JdbcTemplate jdbcTemplate;

    public AdminInfoMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(AdminInfo adminInfo) {
        jdbcTemplate.update("INSERT INTO \"AdminInfo\" (\"chatId\", \"login\", \"password\") VALUES (?, ?, ?) " +
                        "ON CONFLICT (\"chatId\") DO UPDATE SET \"login\" = ?, \"password\" = ?", adminInfo.getChatId(),
                        adminInfo.getLogin(), adminInfo.getPassword());
    }
}
