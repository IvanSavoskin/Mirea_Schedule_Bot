package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Teacher;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class TeacherTypeMergeH2Consumer implements Consumer<Teacher>
{
    private final JdbcTemplate jdbcTemplate;

    public TeacherTypeMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Teacher teacher)
    {
        jdbcTemplate.update("MERGE INTO \"Teacher\" (\"id\", \"name\", \"surname\", \"second_name\"," +
                        " \"phone_number\", \"mail\") KEY (\"id\") VALUES (?, ?, ?, ?, ?, ?)",
                teacher.getId(), teacher.getName(), teacher.getSurname(), teacher.getSecond_name(),
                teacher.getPhone_number(), teacher.getMail());
    }
}
