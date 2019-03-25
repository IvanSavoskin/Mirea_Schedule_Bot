package edu_bot.db_class.consumer;

import edu_bot.db_class.model.GroupSchedule;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class ScheduleMergeGroupH2Consumer implements Consumer<GroupSchedule>
{
    private final JdbcTemplate jdbcTemplate;

    public ScheduleMergeGroupH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(GroupSchedule groupSchedule)
    {
        jdbcTemplate.update("MERGE INTO \"Group_Schedule\" (\"groupId\", \"scheduleId\") KEY(\"groupId\", " +
                "\"scheduleId\") VALUES (?, ?)", groupSchedule.getGroupId(), groupSchedule.getScheduleId());
    }
}
