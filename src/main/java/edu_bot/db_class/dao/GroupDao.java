package edu_bot.db_class.dao;

import edu_bot.db_class.model.Group;

import java.util.List;

public interface GroupDao
{

    Group getGroup(Integer id);

    List<Group> getGroupForName(String groupName);

    List<Group> getGroups();

    Group getGroupForOutput(Long chatId, Integer numberOfWeek, Integer dayOfWeek);

    void Insert(Integer id, String groupName, String fileName);

    void Merge(Integer id, String groupName, String fileName);

    void Update(Integer id, String groupName, String fileName);

    void Delete(Integer id);

    void DeleteAll();

    Integer Count();

}
