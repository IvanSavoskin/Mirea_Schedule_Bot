package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Group;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class GroupMergePostgresConsumer implements Consumer<Group>
{
    private final JdbcTemplate jdbcTemplate;

    public GroupMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Group group)
    {
        jdbcTemplate.update("INSERT INTO \"Group\" (\"id\", \"groupName\", \"fileName\") " +
                        "VALUES (?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET \"groupName\" = ?, \"fileName\" = ?",
                group.getId(), group.getGroupName(), group.getFileName(), group.getGroupName(), group.getFileName());
    }
}
