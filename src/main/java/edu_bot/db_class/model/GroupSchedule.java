package edu_bot.db_class.model;

public class GroupSchedule
{
    private Integer groupId;
    private Integer scheduleId;

    public GroupSchedule(int groupId, int scheduleId)
    {
        this.groupId = groupId;
        this.scheduleId = scheduleId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }
}
