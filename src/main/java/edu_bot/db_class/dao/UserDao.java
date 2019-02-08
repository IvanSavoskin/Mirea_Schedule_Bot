package edu_bot.db_class.dao;

import edu_bot.db_class.model.User;

import java.util.List;

public interface UserDao
{

    User getUser(Long chatId);

    List<User> getUsers();

    List<User> getUsersById(Long chatId);

    List<User> getUsersForGroup(Integer groupId);

    User getUserForOutput(Long chatId, Integer numberOfWeek, Integer dayOfWeek);

    void Insert(Long chatId, String chatName, Integer groupId);

    void Merge(Long chatId, String chatName, Integer groupId);

    void Update(Long chatId, String chatName, Integer groupId);

    void Delete(Long chatId);

    void DeleteAll();

    Integer Count();

}
