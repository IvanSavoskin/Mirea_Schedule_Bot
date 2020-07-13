package edu_bot.db_class.consumer;

import edu_bot.db_class.model.ClassTime;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class ClassTimeMergeH2Consumer implements Consumer<ClassTime>
{
    private final JdbcTemplate jdbcTemplate;

    public ClassTimeMergeH2Consumer(JdbcTemplate jdbcTemplate)
        {
        this.jdbcTemplate = jdbcTemplate;
        }

    @Override
    public void accept(ClassTime classTime)
        {
            jdbcTemplate.update("MERGE INTO \"ClassTime\" (\"classNumber\", \"classStart\", \"classStop\") " +
                            "KEY(\"classNumber\") VALUES (?, ?, ?)", classTime.getClassNumber(), classTime.getClassStart(),
                            classTime.getClassStop());
        }
}