package edu_bot.db_class.model;

import java.util.List;

public class Group
{
    private Integer id;
    private String groupName;
    private String fileName;
    private List<User> users;
    private List<Schedule> schedules;

    public Group(int id, String groupName, String fileName, List<User> users, List<Schedule> schedules)
    {
        this.id = id;
        this.groupName = groupName;
        this.fileName = fileName;
        this.users = users;
        this.schedules = schedules;
    }

    public Group(int id, String groupName, List<User> users, List<Schedule> schedules)
    {
        this.id = id;
        this.groupName = groupName;
        this.users = users;
        this.schedules = schedules;
    }

    public Group(int id, String groupName, String fileName)
    {
        this.id = id;
        this.groupName = groupName;
        this.fileName = fileName;
    }

    public Group(int id, String groupName)
    {
        this.id = id;
        this.groupName = groupName;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
