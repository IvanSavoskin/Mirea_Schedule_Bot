package edu_bot.db_class.dao;

import edu_bot.db_class.model.AdminInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AdminInfoJdbc implements AdminInfoDao
{
    private final JdbcTemplate jdbcTemplate;

    public AdminInfoJdbc(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public AdminInfo getAdminInfo(long chatId)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"AdminInfo\" WHERE \"chatId\" = ?", this::mapFileSum, chatId);
    }

    @Override
    public List<AdminInfo> getAdminInfos()
    {
        return jdbcTemplate.query("SELECT * FROM \"AdminInfo\"", this::mapFileSum);
    }

    @Override
    public List<AdminInfo> getAdminInfosForChatId(long chatId)
    {
        return jdbcTemplate.query("SELECT * FROM \"AdminInfo\" WHERE \"chatId\" = ?", this::mapFileSum, chatId);
    }

    @Override
    public List<AdminInfo> getAdminInfosForLogin(String login)
    {
        return jdbcTemplate.query("SELECT * FROM \"AdminInfo\" WHERE \"login\" = ?", this::mapFileSum, login);
    }

    @Override
    public void Insert(long chatId, String login, String password)
    {
        jdbcTemplate.update("INSERT INTO \"AdminInfo\" (\"chatId\", \"login\", \"password\") VALUES (?, ?, ?)", chatId, login, password);
    }

    @Override
    public void Merge(long chatId, String login, String password)
    {
        jdbcTemplate.update("MERGE INTO \"AdminInfo\" (\"chatId\", \"login\", \"password\") KEY(\"chatId\") VALUES (?, ?, ?)", chatId, login, password);
    }

    @Override
    public void Update(long chatId, String login, String password)
    {
        jdbcTemplate.update("UPDATE \"AdminInfo\" SET \"login\" = ?, \"password\" = ? WHERE \"chatId\" = ?", login, password, chatId);
    }

    @Override
    public void Delete(long chatId)
    {
        jdbcTemplate.update("DELETE FROM \"AdminInfo\" WHERE \"chatId\" = ?",  chatId);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"AdminInfo\"");
    }

    private AdminInfo mapFileSum(ResultSet rs, int row) throws SQLException
    {
        return new AdminInfo(rs.getLong("chatId"), rs.getString("login"),
                rs.getString("password"));
    }
}
