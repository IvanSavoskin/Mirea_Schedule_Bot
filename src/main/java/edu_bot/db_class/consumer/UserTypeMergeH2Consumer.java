package edu_bot.db_class.consumer;

import edu_bot.db_class.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class UserTypeMergeH2Consumer implements Consumer<User>
{
    private final JdbcTemplate jdbcTemplate;

    public UserTypeMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(User user)
    {
        jdbcTemplate.update("MERGE INTO \"User\" (\"chatId\", \"chatName\", \"groupId\") KEY(\"chatId\") " +
                "VALUES (?, ?, ?)",  user.getChatId(), user.getChatName(), user.getGroupId());
    }
}
