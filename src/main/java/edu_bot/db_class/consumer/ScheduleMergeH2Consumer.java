package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Schedule;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class ScheduleMergeH2Consumer implements Consumer<Schedule>
{
    private final JdbcTemplate jdbcTemplate;

    public ScheduleMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Schedule schedule)
    {
        jdbcTemplate.update("MERGE INTO \"Schedule\" (\"id\", \"classNumber\",  \"classroomId\",  " +
                        "\"subjectId\", \"subjectTypeId\", \"dayOfWeek\", \"numberOfWeek\") KEY(\"id\") VALUES (?, ?, ?, ?, ?, ?, ?)",
                schedule.getId(), schedule.getClassTime(), schedule.getClassroomId(), schedule.getSubjectId(),
                schedule.getSubjectTypeId(), schedule.getDayOfWeek(), schedule.getNumberOfWeek());
    }
}
