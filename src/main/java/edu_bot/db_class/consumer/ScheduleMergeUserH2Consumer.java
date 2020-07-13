package edu_bot.db_class.consumer;

import edu_bot.db_class.model.UserSchedule;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class ScheduleMergeUserH2Consumer implements Consumer<UserSchedule>
{
    private final JdbcTemplate jdbcTemplate;

    public ScheduleMergeUserH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(UserSchedule userSchedule)
    {
        jdbcTemplate.update("MERGE INTO \"User_Schedule\" (\"userId\", \"scheduleId\") KEY(\"userId\"," +
                " \"scheduleId\") VALUES (?, ?)", userSchedule.getUserId(), userSchedule.getScheduleId());
    }
}
