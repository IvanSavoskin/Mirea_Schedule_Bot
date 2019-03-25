package edu_bot.db_class.consumer;

import edu_bot.db_class.model.SubjectType;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class SubjectTypeMergeH2Consumer implements Consumer<SubjectType>
{
    private final JdbcTemplate jdbcTemplate;

    public SubjectTypeMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(SubjectType subjectType)
    {
        jdbcTemplate.update("MERGE INTO \"SubjectType\" (\"id\", \"typeName\") KEY (\"id\")" +
                " VALUES (?, ?)", subjectType.getId(), subjectType.getTypeName());
    }
}
