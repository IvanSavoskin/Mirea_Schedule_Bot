package edu_bot.db_class.dao;

import edu_bot.db_class.model.Group;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class GroupJdbc implements GroupDao
{

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleDao scheduleDao;
    private final UserDao userDao;

    public GroupJdbc(JdbcTemplate jdbcTemplate, ScheduleDao scheduleDao, UserDao userDao)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.scheduleDao = scheduleDao;
        this.userDao = userDao;
    }

    @Override
    public Group getGroup(Integer id)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Group\" WHERE \"id\" = ?", this::mapGroup, id);
    }

    @Override
    public List<Group> getGroupForName(String groupName)
    {
        return jdbcTemplate.query("SELECT * FROM \"Group\" WHERE \"groupName\" = ?", this::mapGroup, groupName);
    }

    @Override
    public List<Group> getGroups()
    {
        return jdbcTemplate.query("SELECT * FROM \"Group\"", (rs,row) -> new Group(rs.getInt("id"),
                rs.getString("groupName"), rs.getString("fileName")));
    }

    private Group mapGroup(ResultSet rs, int row) throws SQLException
    {
        return new Group(rs.getInt("id"), rs.getString("groupName"),
                rs.getString("fileName"));
    }

    @Override
    public Group getGroupForOutput(Long chatId, Integer numberOfWeek, Integer dayOfWeek)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Group\" WHERE \"id\" = ?", (rs,row) -> new Group(
                rs.getInt("id"), rs.getString("groupName"),
                userDao.getUsersForGroup(rs.getInt("id")), scheduleDao.
                getSchedulesForGroup(userDao.getUser(chatId).getGroupId(), numberOfWeek, dayOfWeek, chatId)),
                userDao.getUser(chatId).getGroupId());
    }

    @Override
    public void Insert(Integer id, String groupName, String fileName)
    {
        jdbcTemplate.update("INSERT INTO \"Group\" (\"id\", \"groupName\", \"fileName\") VALUES (?, ?, ?)",
                id, groupName, fileName);
    }

    @Override
    public void Merge(Integer id, String groupName, String fileName)
    {
        jdbcTemplate.update("MERGE INTO \"Group\" (\"id\", \"groupName\", \"fileName\") KEY(\"id\") " +
                "VALUES (?, ?, ?)", id, groupName, fileName);
    }

    /*@Override
    public void Merge(Integer id, String groupName, String fileName)
    {
        jdbcTemplate.update("INSERT INTO \"Group\" (\"id\", \"groupName\", \"fileName\") " +
                "VALUES (?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET \"groupName\" = ?, \"fileName\" = ?",
                id, groupName, fileName, groupName, fileName);
    }*/

    @Override
    public void Update(Integer id, String groupName, String fileName)
    {
        jdbcTemplate.update("UPDATE \"Group\" SET \"groupName\" = ?, \"fileName\" = ? WHERE \"id\" = ?",
                groupName, fileName, id);
    }

    @Override
    public void Delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Group\" WHERE \"id\" = ?", id);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Group\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Group\"", Integer.class);
    }

}
