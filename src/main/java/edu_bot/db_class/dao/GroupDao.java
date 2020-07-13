package edu_bot.db_class.dao;

import edu_bot.db_class.model.Group;

import java.util.List;

public interface GroupDao
{

    Group getGroup(Integer id);

    List<Group> getGroupForName(String groupName);

    List<Group> getGroups();

    Group getGroupForOutput(Long chatId, Integer numberOfWeek, Integer dayOfWeek);

    void insert(Group group);

    void merge(Group group);

    void update(Group group);

    void delete(Integer id);

    void deleteAll();

    Integer count();

}
