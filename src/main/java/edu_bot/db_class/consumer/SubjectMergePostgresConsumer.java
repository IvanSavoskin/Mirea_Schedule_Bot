package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Subject;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class SubjectMergePostgresConsumer implements Consumer<Subject>
{
    private final JdbcTemplate jdbcTemplate;

    public SubjectMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Subject subject)
    {
        jdbcTemplate.update("INSERT INTO \"Subject\" (\"id\", \"subjectName\", \"teacherId\")  VALUES (?, ?, ?)" +
                        "ON CONFLICT (\"id\") DO UPDATE SET \"subjectName\" = ?, \"teacherId\" = ?", subject.getId(),
                subject.getSubjectName(), subject.getTeacherId(), subject.getSubjectName(), subject.getTeacherId());
    }
}
