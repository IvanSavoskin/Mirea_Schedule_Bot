package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Subject;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class SubjectMergeH2Consumer implements Consumer<Subject>
{
    private final JdbcTemplate jdbcTemplate;

    public SubjectMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Subject subject)
    {
        jdbcTemplate.update("MERGE INTO \"Subject\" (\"id\", \"subjectName\", \"teacherId\") KEY(\"id\")" +
                " VALUES (?, ?, ?)", subject.getId(), subject.getSubjectName(), subject.getTeacherId());
    }
}
