package edu_bot.db_class.consumer;

import edu_bot.db_class.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class UserTypeMergePostgresConsumer implements Consumer<User>
{
    private final JdbcTemplate jdbcTemplate;

    public UserTypeMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(User user)
    {
        jdbcTemplate.update("INSERT INTO \"User\" (\"chatId\", \"chatName\", \"groupId\") VALUES (?, ?, ?) ON CONFLICT " +
                        "(\"chatId\") DO UPDATE SET \"chatName\" = ?, \"groupId\" = ?", user.getChatId(), user.getChatName(),
                user.getGroupId(), user.getChatName(), user.getGroupId());
    }
}
