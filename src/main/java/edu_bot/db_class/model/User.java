package edu_bot.db_class.model;

import java.util.List;

public class User
{
    private Long chatId;
    private String chatName;
    private Integer groupId;
    private String groupName;
    private List<Schedule> schedules;

    public User(Long chatId, String chatName, Integer groupId)
    {
        this.chatId = chatId;
        this.chatName = chatName;
        this.groupId = groupId;
    }

    public User(Long chatId, String chatName, List<Schedule> schedules)
    {
        this.chatId = chatId;
        this.chatName = chatName;
        this.schedules = schedules;
    }

    public User(Long chatId, String chatName, String groupName)
    {
        this.chatId = chatId;
        this.chatName = chatName;
        this.groupName = groupName;
    }

    public  User(Long chatId, String chatName)
    {
        this.chatId = chatId;
        this.chatName = chatName;
    }

    public Long getChatId()
    {
        return chatId;
    }

    public void setChatId(Long chatId)
    {
        this.chatId = chatId;
    }

    public String getChatName()
    {
        return chatName;
    }

    public void setChatName(String chatName)
    {
        this.chatName = chatName;
    }

    public Integer getGroupId()
    {
        return groupId;
    }

    public void setGroupId(Integer groupId)
    {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Schedule> getSchedules()
    {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules)
    {
        this.schedules = schedules;
    }
}
