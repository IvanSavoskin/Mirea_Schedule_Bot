package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Group;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class GroupMergeH2Consumer implements Consumer<Group>
{
    private final JdbcTemplate jdbcTemplate;

    public GroupMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Group group)
    {
        jdbcTemplate.update("MERGE INTO \"Group\" (\"id\", \"groupName\", \"fileName\") KEY(\"id\") " +
                "VALUES (?, ?, ?)", group.getId(), group.getGroupName(), group.getFileName());
    }
}
