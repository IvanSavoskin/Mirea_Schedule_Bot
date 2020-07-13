package edu_bot.db_class.model;

public class UserSchedule
{
    private Long userId;
    private Integer scheduleId;

    public UserSchedule(long userId, int scheduleId)
    {
        this.userId = userId;
        this.scheduleId = scheduleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }
}
