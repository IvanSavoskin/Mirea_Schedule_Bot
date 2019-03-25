package edu_bot.db_class.consumer;

import edu_bot.db_class.model.Schedule;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class ScheduleMergePostgresConsumer implements Consumer<Schedule>
{
    private final JdbcTemplate jdbcTemplate;

    public ScheduleMergePostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(Schedule schedule)
    {
        jdbcTemplate.update("INSERT INTO \"Schedule\" (\"id\", \"classNumber\", \"classroomId\",  " +
                        "\"subjectId\", \"subjectTypeId\", \"dayOfWeek\", \"numberOfWeek\") VALUES (?, ?, ?, ?, ?, ?, ?)" +
                        "ON CONFLICT (\"id\") DO UPDATE SET \"classNumber\" = ?, \"classroomId\" = ?, \"subjectId\" = ?, " +
                        "\"subjectTypeId\" = ?, \"dayOfWeek\" = ?, \"numberOfWeek\" = ?", schedule.getId(),
                schedule.getClassTime(), schedule.getClassroomId(), schedule.getSubjectId(),
                schedule.getSubjectTypeId(), schedule.getDayOfWeek(), schedule.getNumberOfWeek(),
                schedule.getClassTime(), schedule.getClassroomId(), schedule.getSubjectId(),
                schedule.getSubjectTypeId(), schedule.getDayOfWeek(), schedule.getNumberOfWeek());
    }
}
