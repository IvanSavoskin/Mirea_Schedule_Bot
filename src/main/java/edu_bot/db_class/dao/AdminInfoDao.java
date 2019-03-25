package edu_bot.db_class.dao;

import edu_bot.db_class.model.AdminInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface AdminInfoDao
{
    AdminInfo getAdminInfo(long chatName);

    List<AdminInfo> getAdminInfos();

    List<AdminInfo> getAdminInfosForChatId(long chatName);

    List<AdminInfo> getAdminInfosForLogin(String login);

    void insert(AdminInfo adminInfo);

    void merge(AdminInfo adminInfo);

    void update(AdminInfo adminInfo);

    void delete(long chatName);

    void deleteAll();
}
