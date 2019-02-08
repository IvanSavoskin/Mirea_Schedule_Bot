package edu_bot.db_class.dao;

import edu_bot.db_class.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserJdbc implements UserDao
{

    private final  ScheduleDao scheduleDao;
    private final JdbcTemplate jdbcTemplate;

    public UserJdbc(ScheduleDao scheduleDao, JdbcTemplate jdbcTemplate)
    {
        this.scheduleDao = scheduleDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User getUser(Long chatId)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"User\" WHERE \"chatId\" = ?", this::mapUser, chatId);
    }

    @Override
    public List<User> getUsers()
    {
        return jdbcTemplate.query("SELECT * FROM \"User\"", (rs,row) -> new User(rs.getLong("chatId"),
                rs.getString("chatName")));
    }

    @Override
    public List<User> getUsersById(Long chatId)
    {
        return jdbcTemplate.query("SELECT * FROM \"User\" WHERE \"chatId\" = ?", (rs,row) -> new User(rs.getLong("chatId"),
                rs.getString("chatName")), chatId);
    }

    @Override
    public List<User> getUsersForGroup(Integer groupId) {
        return jdbcTemplate.query("SELECT * FROM \"User\" JOIN \"Group\" ON " +
                "\"User\".\"groupId\"=\"Group\".\"id\" WHERE \"Group\".\"id\" = ?",
                (rs,row) -> new User(rs.getLong("chatId"),
                rs.getString("chatName"), rs.getString("groupName")), groupId);
    }

    @Override
    public User getUserForOutput(Long chatId, Integer numberOfWeek, Integer dayOfWeek)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"User\" WHERE \"chatId\" = ?", (rs, row) ->
                new User(rs.getLong("chatId"), rs.getString("chatName"),
                        scheduleDao.getSchedulesForUserForDay(chatId, numberOfWeek, dayOfWeek)),chatId);
    }

    private User mapUser(ResultSet rs, int row) throws SQLException
    {
        return new User(rs.getLong("chatId"), rs.getString("chatName"),
                rs.getInt("groupId"));
    }

    @Override
    public void Insert(Long chatId, String chatName, Integer groupId)
    {
        jdbcTemplate.update("INSERT INTO \"User\" (\"chatId\", \"chatName\", \"groupId\") VALUES (?, ?, ?)",
                chatId, chatName, groupId);

    }

    @Override
    public void Merge(Long chatId, String chatName, Integer groupId)
    {
        jdbcTemplate.update("MERGE INTO \"User\" (\"chatId\", \"chatName\", \"groupId\") KEY(\"chatId\") " +
                "VALUES (?, ?, ?)", chatId, chatName, groupId);
        }

    /*@Override
    public void Merge(Long chatId, String chatName, Integer groupId)
    {
        jdbcTemplate.update("INSERT INTO \"User\" (\"chatId\", \"chatName\", \"groupId\") VALUES (?, ?, ?) ON CONFLICT " +
                "(\"chatId\") DO UPDATE SET \"chatName\" = ?, \"groupId\" = ?", chatId, chatName, groupId, chatName, groupId);

    }*/

    @Override
    public void Update(Long chatId, String chatName, Integer groupId)
    {
        jdbcTemplate.update("UPDATE \"User\" SET \"chatName\" = ?, \"groupId\" = ? WHERE \"chatId\" = ?",
                chatName, groupId, chatId);
    }

    @Override
    public void Delete(Long chatId)
    {
        jdbcTemplate.update("DELETE FROM \"User\" WHERE \"chatId\" = ?", chatId);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"User\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"User\"", Integer.class);
    }

}
