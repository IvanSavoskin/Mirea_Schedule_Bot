package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Classroom;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class ClassroomMergeH2Consumer implements Consumer<Classroom>
{
    private final JdbcTemplate jdbcTemplate;

    public ClassroomMergeH2Consumer(JdbcTemplate jdbcTemplate)
        {
        this.jdbcTemplate = jdbcTemplate;
        }

    @Override
    public void accept(Classroom classroom)
    {
        jdbcTemplate.update("MERGE INTO \"Classroom\" (\"id\", \"className\", \"pic\") KEY(\"id\") VALUES (?, ?, ?)",
                classroom.getId(), classroom.getClassName(), classroom.getPic());
    }
}
