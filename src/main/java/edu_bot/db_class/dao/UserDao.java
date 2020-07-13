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

    void insert(User user);

    void merge(User user);

    void update(User user);

    void delete(Long chatId);

    void deleteAll();

    Integer count();

}
