package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Teacher;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class TeacherTypeMergePostgresConsumer implements Consumer<Teacher>
{
    private final JdbcTemplate jdbcTemplate;

    public TeacherTypeMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Teacher teacher)
    {
        jdbcTemplate.update("INSERT INTO \"Teacher\" (\"id\", \"name\", \"surname\", \"second_name\"," +
                        " \"phone_number\", \"mail\") VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET \"name\" = ?," +
                        "\"surname\" = ?, \"second_name\" = ?, \"phone_number\" = ?, \"mail\" = ?", teacher.getId(), teacher.getName(),
                teacher.getSurname(), teacher.getSecond_name(), teacher.getPhone_number(), teacher.getMail(),
                teacher.getName(), teacher.getSurname(), teacher.getSecond_name(), teacher.getPhone_number(),
                teacher.getMail());
    }
}
