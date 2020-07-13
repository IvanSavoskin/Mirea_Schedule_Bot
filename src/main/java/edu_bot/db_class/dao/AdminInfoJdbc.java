package edu_bot.db_class.dao;

import edu_bot.db_class.consumer.AdminInfoMergeH2Consumer;
import edu_bot.db_class.consumer.AdminInfoMergePostgresConsumer;
import edu_bot.db_class.model.AdminInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class AdminInfoJdbc implements AdminInfoDao
{
    private final JdbcTemplate jdbcTemplate;
    private final Consumer<AdminInfo> adminInfoConsumer;

    public AdminInfoJdbc(JdbcTemplate jdbcTemplate, Consumer<AdminInfo> adminInfoConsumer)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.adminInfoConsumer = adminInfoConsumer;
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
    public void insert(AdminInfo adminInfo)
    {
        jdbcTemplate.update("INSERT INTO \"AdminInfo\" (\"chatId\", \"login\", \"password\") VALUES (?, ?, ?)",
                adminInfo.getChatId(), adminInfo.getLogin(), adminInfo.getPassword());
    }

    @Override
    public void merge(AdminInfo adminInfo)
    {
        adminInfoConsumer.accept(adminInfo);
    }

    @Override
    public void update(AdminInfo adminInfo)
    {
        jdbcTemplate.update("UPDATE \"AdminInfo\" SET \"login\" = ?, \"password\" = ? WHERE \"chatId\" = ?",
                adminInfo.getLogin(), adminInfo.getPassword(), adminInfo.getChatId());
    }

    @Override
    public void delete(long chatId)
    {
        jdbcTemplate.update("DELETE FROM \"AdminInfo\" WHERE \"chatId\" = ?",  chatId);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"AdminInfo\"");
    }

    private AdminInfo mapFileSum(ResultSet rs, int row) throws SQLException
    {
        return new AdminInfo(rs.getLong("chatId"), rs.getString("login"),
                rs.getString("password"));
    }
}
