package edu_bot.db_class.dao;

import edu_bot.db_class.model.AdminInfo;

import java.util.List;

public interface AdminInfoDao
{
    AdminInfo getAdminInfo(long chatName);

    List<AdminInfo> getAdminInfos();

    List<AdminInfo> getAdminInfosForChatId(long chatName);

    List<AdminInfo> getAdminInfosForLogin(String login);

    void Insert(long chatName, String login, String password);

    void Merge(long chatName, String login, String password);

    void Update(long chatName, String login, String password);

    void Delete(long chatName);

    void DeleteAll();
}
