package edu_bot.db_class.dao;

import edu_bot.db_class.model.Group;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class GroupJdbc implements GroupDao
{

    private final JdbcTemplate jdbcTemplate;
    private final ScheduleDao scheduleDao;
    private final UserDao userDao;
    private final Consumer<Group> groupConsumer;

    public GroupJdbc(JdbcTemplate jdbcTemplate, ScheduleDao scheduleDao, UserDao userDao, Consumer<Group> groupConsumer)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.scheduleDao = scheduleDao;
        this.userDao = userDao;
        this.groupConsumer = groupConsumer;
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
                        getSchedulesForGroupForDay(userDao.getUser(chatId).getGroupId(), numberOfWeek, dayOfWeek, chatId)),
                userDao.getUser(chatId).getGroupId());
    }

    @Override
    public void insert(Group group)
    {
        jdbcTemplate.update("INSERT INTO \"Group\" (\"id\", \"groupName\", \"fileName\") VALUES (?, ?, ?)",
                group.getId(), group.getGroupName(), group.getFileName());
    }

    @Override
    public void merge(Group group)
    {
        groupConsumer.accept(group);
    }

    @Override
    public void update(Group group)
    {
        jdbcTemplate.update("UPDATE \"Group\" SET \"groupName\" = ?, \"fileName\" = ? WHERE \"id\" = ?",
                group.getGroupName(), group.getFileName(), group.getId());
    }

    @Override
    public void delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Group\" WHERE \"id\" = ?", id);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Group\"");
    }

    @Override
    public Integer count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Group\"", Integer.class);
    }

}
