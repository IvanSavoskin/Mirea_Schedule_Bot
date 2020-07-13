package edu_bot.db_class.consumer;

import edu_bot.db_class.model.GroupSchedule;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("postgres")
public class ScheduleMergeGroupPostgresConsumer implements Consumer<GroupSchedule>
{
    private final JdbcTemplate jdbcTemplate;

    public ScheduleMergeGroupPostgresConsumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(GroupSchedule groupSchedule)
    {
        jdbcTemplate.update("INSERT INTO \"Group_Schedule\" (\"groupId\", \"scheduleId\") VALUES (?, ?)" +
                        "ON CONFLICT (\"groupId\", \"scheduleId\") DO UPDATE SET \"groupId\" = ?, \"scheduleId\" = ?",
                groupSchedule.getGroupId(), groupSchedule.getScheduleId(), groupSchedule.getGroupId(),
                groupSchedule.getScheduleId());
    }
}
