package edu_bot.db_class.consumer;

import edu_bot.db_class.model.UserSchedule;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class ScheduleMergeUserPostgresConsumer implements Consumer<UserSchedule>
{
    private final JdbcTemplate jdbcTemplate;

    public ScheduleMergeUserPostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(UserSchedule userSchedule)
    {
        jdbcTemplate.update("INSERT INTO \"User_Schedule\" (\"userId\", \"scheduleId\") VALUES (?, ?)" +
                        "ON CONFLICT (\"userId\", \"scheduleId\") DO UPDATE SET \"userId\" = ?, \"scheduleId\" = ?",
                userSchedule.getUserId(), userSchedule.getScheduleId(), userSchedule.getUserId(),
                userSchedule.getScheduleId());
    }
}
