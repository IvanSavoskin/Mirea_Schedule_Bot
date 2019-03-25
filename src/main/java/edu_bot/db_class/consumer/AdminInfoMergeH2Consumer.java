package edu_bot.db_class.consumer;

import edu_bot.db_class.model.AdminInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class AdminInfoMergeH2Consumer implements Consumer<AdminInfo>
{
    private final JdbcTemplate jdbcTemplate;

    public AdminInfoMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(AdminInfo adminInfo) {
        jdbcTemplate.update("MERGE INTO \"AdminInfo\" (\"chatId\", \"login\", \"password\") " +
                "KEY(\"chatId\") VALUES (?, ?, ?)", adminInfo.getChatId(), adminInfo.getLogin(), adminInfo.getPassword());
    }
}
