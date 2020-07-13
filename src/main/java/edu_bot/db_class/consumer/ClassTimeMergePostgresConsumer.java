package edu_bot.db_class.consumer;

import edu_bot.db_class.model.ClassTime;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class ClassTimeMergePostgresConsumer implements Consumer<ClassTime>
{
    private final JdbcTemplate jdbcTemplate;

    public ClassTimeMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(ClassTime classTime)
    {
        jdbcTemplate.update("INSERT INTO \"ClassTime\" (\"classNumber\", \"classStart\", \"classStop\") " +
                        "VALUES (?, ?, ?) ON CONFLICT ( \"classNumber\") DO UPDATE SET \"classStart\" = ?, \"classStop\" = ?",
                classTime.getClassNumber(), classTime.getClassStart(), classTime.getClassStop(),
                classTime.getClassStart(), classTime.getClassStop());
    }
}
