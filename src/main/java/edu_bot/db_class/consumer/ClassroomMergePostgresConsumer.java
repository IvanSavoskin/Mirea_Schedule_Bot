package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Classroom;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class ClassroomMergePostgresConsumer implements Consumer<Classroom>
{
    private final JdbcTemplate jdbcTemplate;

    public ClassroomMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Classroom classroom)
    {
        jdbcTemplate.update("INSERT INTO \"Classroom\" (\"id\", \"className\", \"pic\")  VALUES (?, ?, ?) " +
                        "ON CONFLICT (\"id\") DO UPDATE SET \"className\" = ?, \"pic\" = ?",
                classroom.getId(), classroom.getClassName(), classroom.getPic(), classroom.getClassName(),
                classroom.getPic());
    }
}
