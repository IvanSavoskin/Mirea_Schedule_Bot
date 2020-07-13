package edu_bot.db_class.dao;

import edu_bot.db_class.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class UserJdbc implements UserDao
{

    private final  ScheduleDao scheduleDao;
    private final JdbcTemplate jdbcTemplate;
    private final Consumer<User> userConsumer;

    public UserJdbc(ScheduleDao scheduleDao, JdbcTemplate jdbcTemplate, Consumer<User> userConsumer)
    {
        this.scheduleDao = scheduleDao;
        this.jdbcTemplate = jdbcTemplate;
        this.userConsumer = userConsumer;
    }

    @Override
    public User getUser(Long chatId)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"User\" JOIN \"Group\" ON " +
                "\"User\".\"groupId\"=\"Group\".\"id\" WHERE \"chatId\" = ?", this::mapUser, chatId);
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
                rs.getInt("groupId"), rs.getString("groupName"));
    }

    @Override
    public void insert(User user)
    {
        jdbcTemplate.update("INSERT INTO \"User\" (\"chatId\", \"chatName\", \"groupId\") VALUES (?, ?, ?)",
                user.getChatId(), user.getChatName(), user.getGroupId());

    }

    @Override
    public void merge(User user)
    {
        userConsumer.accept(user);
    }

    @Override
    public void update(User user)
    {
        jdbcTemplate.update("UPDATE \"User\" SET \"chatName\" = ?, \"groupId\" = ? WHERE \"chatId\" = ?",
                user.getChatName(), user.getGroupId(), user.getChatId());
    }

    @Override
    public void delete(Long chatId)
    {
        jdbcTemplate.update("DELETE FROM \"User\" WHERE \"chatId\" = ?", chatId);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"User\"");
    }

    @Override
    public Integer count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"User\"", Integer.class);
    }

}
