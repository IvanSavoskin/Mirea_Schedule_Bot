package edu_bot.db_class.consumer;

import edu_bot.db_class.model.SubjectType;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class SubjectTypeMergePostgresConsumer implements Consumer<SubjectType>
{
    private final JdbcTemplate jdbcTemplate;

    public SubjectTypeMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(SubjectType subjectType)
    {
        jdbcTemplate.update("INSERT INTO \"SubjectType\" (\"id\", \"typeName\") VALUES (?, ?) ON CONFLICT (\"id\")" +
                "DO UPDATE SET \"typeName\" = ?", subjectType.getId(), subjectType.getTypeName(), subjectType.getTypeName());
    }
}
