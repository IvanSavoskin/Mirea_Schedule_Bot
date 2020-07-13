package edu_bot.db_class.dao;

import edu_bot.db_class.model.GroupSchedule;
import edu_bot.db_class.model.Schedule;
import edu_bot.db_class.model.UserSchedule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class ScheduleJdbc implements ScheduleDao
{
    private final SubjectDao subjectDao;
    private final ClassTimeDao classTimeDao;
    private final ClassroomDao classroomDao;
    private final JdbcTemplate jdbcTemplate;
    private final SubjectTypeDao subjectTypeDao;
    private final Consumer<Schedule> scheduleConsumer;
    private final Consumer<GroupSchedule> groupScheduleConsumer;
    private final Consumer<UserSchedule> userScheduleConsumer;

    public ScheduleJdbc(SubjectDao subjectDao, ClassroomDao classroomDao, ClassTimeDao classTimeDao,
                        JdbcTemplate jdbcTemplate, SubjectTypeDao subjectTypeDao, Consumer<Schedule> scheduleConsumer,
                        Consumer<GroupSchedule> groupScheduleConsumer, Consumer<UserSchedule> userScheduleConsumer)
    {
        this.subjectDao = subjectDao;
        this.classroomDao = classroomDao;
        this.classTimeDao = classTimeDao;
        this.jdbcTemplate = jdbcTemplate;
        this.subjectTypeDao = subjectTypeDao;
        this.scheduleConsumer = scheduleConsumer;
        this.groupScheduleConsumer = groupScheduleConsumer;
        this.userScheduleConsumer = userScheduleConsumer;
    }

    @Override
    public Schedule getSchedule(Integer id)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Schedule\" WHERE \"id\" = ?", this::mapSchedule, id);
    }

    @Override
    public List<Schedule> getSchedules()
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\"",
                (rs,row) -> new Schedule(rs.getInt("id"), rs.getInt("dayOfWeek"),
                        rs.getInt("numberOfWeek")));
    }

    @Override
    public List<Schedule> getScheduleForParse(Integer classNumber, Integer classroomId, Integer subjectId,
                                              Integer subjectTypeId, Integer numberOfWeek, Integer dayOfWeek)
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\" WHERE \"classNumber\" = ? AND \"classroomId\" = ? " +
                        "AND \"subjectId\" = ? AND \"subjectTypeId\" = ? AND \"dayOfWeek\" = ? AND \"numberOfWeek\" = ?",
                        this::mapScheduleParse, classNumber, classroomId, subjectId, subjectTypeId, dayOfWeek, numberOfWeek);
    }

    @Override
    public List<Schedule> getScheduleForDelete(Integer classNumber, Integer subjectId, Integer numberOfWeek, Integer dayOfWeek)
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\" WHERE \"classNumber\" = ? AND \"subjectId\" = ? " +
                        "AND \"dayOfWeek\" = ? AND \"numberOfWeek\" = ?",
                this::mapScheduleParse, classNumber, subjectId, dayOfWeek, numberOfWeek);
    }

    @Override
    public List<Schedule> getSchedulesForUser(Long chatId)
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\" JOIN \"User_Schedule\" " +
                        "ON \"Schedule\".\"id\" = \"User_Schedule\".\"scheduleId\" JOIN \"ClassTime\" " +
                        "ON \"Schedule\".\"classNumber\" = \"ClassTime\".\"classNumber\" JOIN \"Classroom\" " +
                        "ON \"Schedule\".\"classroomId\" = \"Classroom\".\"id\" JOIN \"Subject\" " +
                        "ON \"Schedule\".\"subjectId\" = \"Subject\".\"id\" JOIN \"Teacher\" " +
                        "ON \"Subject\".\"teacherId\" = \"Teacher\".\"id\" JOIN \"SubjectType\" " +
                        "ON \"Schedule\".\"subjectTypeId\" = \"SubjectType\".\"id\" WHERE \"User_Schedule\".\"userId\" = ?",
                this::mapScheduleFor, chatId);
    }

    @Override
    public List<Schedule> getSchedulesForUserForDay(Long chatId, Integer numberOfWeek, Integer dayOfWeek)
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\" JOIN \"User_Schedule\" " +
                "ON \"Schedule\".\"id\" = \"User_Schedule\".\"scheduleId\" JOIN \"ClassTime\" " +
                "ON \"Schedule\".\"classNumber\" = \"ClassTime\".\"classNumber\" JOIN \"Classroom\" " +
                "ON \"Schedule\".\"classroomId\" = \"Classroom\".\"id\" JOIN \"Subject\" " +
                "ON \"Schedule\".\"subjectId\" = \"Subject\".\"id\" JOIN \"Teacher\" " +
                "ON \"Subject\".\"teacherId\" = \"Teacher\".\"id\" JOIN \"SubjectType\" " +
                "ON \"Schedule\".\"subjectTypeId\" = \"SubjectType\".\"id\" WHERE \"User_Schedule\".\"userId\" = ? " +
                "AND \"Schedule\".\"numberOfWeek\" = ? AND \"Schedule\".\"dayOfWeek\" = ?",
                this::mapScheduleFor, chatId, numberOfWeek, dayOfWeek);
    }

    @Override
    public List<Schedule> getSchedulesForGroupForDay(Integer groupId, Integer numberOfWeek, Integer dayOfWeek, Long chatId)
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\" JOIN \"Group_Schedule\" " +
                "ON \"Schedule\".\"id\" = \"Group_Schedule\".\"scheduleId\" JOIN \"Group\" ON \"Group\".\"id\" = " +
                "\"Group_Schedule\".\"groupId\" JOIN \"User\" ON \"Group\".\"id\" = \"User\".\"groupId\" " +
                "JOIN \"ClassTime\" ON \"Schedule\".\"classNumber\" = \"ClassTime\".\"classNumber\" " +
                "JOIN \"Classroom\" ON \"Schedule\".\"classroomId\" = \"Classroom\".\"id\" JOIN \"Subject\" " +
                "ON \"Schedule\".\"subjectId\" = \"Subject\".\"id\" JOIN \"Teacher\" ON \"Subject\".\"teacherId\" = " +
                "\"Teacher\".\"id\" JOIN \"SubjectType\" ON \"Schedule\".\"subjectTypeId\" = \"SubjectType\".\"id\"" +
                " WHERE \"Group_Schedule\".\"groupId\" = ? AND \"Schedule\".\"numberOfWeek\" = ? " +
                "AND \"Schedule\".\"dayOfWeek\" = ? AND \"User\".\"chatId\" = ?", this::mapScheduleFor,
                groupId, numberOfWeek, dayOfWeek, chatId);
    }

    @Override
    public List<Schedule> getSchedulesForGroup(Integer groupId)
    {
        return jdbcTemplate.query("SELECT * FROM \"Schedule\" JOIN \"Group_Schedule\" " +
                        "ON \"Schedule\".\"id\" = \"Group_Schedule\".\"scheduleId\" JOIN \"Group\" ON \"Group\".\"id\" = " +
                        "\"Group_Schedule\".\"groupId\" JOIN \"User\" ON \"Group\".\"id\" = \"User\".\"groupId\" " +
                        "JOIN \"ClassTime\" ON \"Schedule\".\"classNumber\" = \"ClassTime\".\"classNumber\" " +
                        "JOIN \"Classroom\" ON \"Schedule\".\"classroomId\" = \"Classroom\".\"id\" JOIN \"Subject\" " +
                        "ON \"Schedule\".\"subjectId\" = \"Subject\".\"id\" JOIN \"Teacher\" ON \"Subject\".\"teacherId\" = " +
                        "\"Teacher\".\"id\" JOIN \"SubjectType\" ON \"Schedule\".\"subjectTypeId\" = \"SubjectType\".\"id\"" +
                        " WHERE \"Group_Schedule\".\"groupId\" = ? ", this::mapScheduleFor, groupId);
    }

    @Override
    public List<Schedule> getGroupSchedules(Integer groupId)
    {
        return jdbcTemplate.query("SELECT * FROM \"Group_Schedule\"  WHERE \"groupId\" = ?",(rs,row) -> new Schedule(
                rs.getInt("scheduleId"), rs.getInt("groupId")), groupId);
    }

    private Schedule mapSchedule(ResultSet rs, int row) throws SQLException
    {
        return new Schedule(rs.getInt("id"), rs.getInt("classNumber"),
                rs.getInt("classroomId"), rs.getInt("subjectId"), rs.getInt("subjectTypeId"),
                rs.getInt("dayOfWeek"), rs.getInt("numberOfWeek"),
                classTimeDao.getClassTimeForSchedule(rs.getInt("classNumber"), rs.getInt("id")),
                classroomDao.getClassroomForSchedule(rs.getInt("classroomId")),
                subjectDao.getSubjectForSchedule(rs.getInt("subjectId")),
                subjectTypeDao.getSubjectTypeForSchedule(rs.getInt("subjectTypeId")));
    }

    private Schedule mapScheduleFor(ResultSet rs, int row) throws SQLException
    {
        return new Schedule(rs.getInt("classNumber"), rs.getString("classStart"),
                rs.getString("classStop"),  rs.getString("className"),
                rs.getString("subjectName"), rs.getString("typeName"),
                rs.getString("surname"), rs.getString("name"),
                rs.getString("second_name"), rs.getString("phone_number"),
                rs.getString("mail"), rs.getInt("dayOfWeek"), rs.getInt("numberOfWeek"));
    }

    private Schedule mapScheduleParse(ResultSet rs, int row) throws SQLException
    {
        return new Schedule(rs.getInt("id"), rs.getInt("classNumber"),
                rs.getInt("classroomId"), rs.getInt("subjectId"), rs.getInt("subjectTypeId"),
                rs.getInt("dayOfWeek"), rs.getInt("numberOfWeek"));
    }

    @Override
    public void insert(Schedule schedule)
    {
        jdbcTemplate.update("INSERT INTO \"Schedule\" (\"id\", \"classNumber\",  \"classroomId\",  " +
                "\"subjectId\", \"subjectTypeId\", \"dayOfWeek\", \"numberOfWeek\") VALUES (?, ?, ?, ?, ?, ?, ?)",
                schedule.getId(), schedule.getClassTime(), schedule.getClassroomId(), schedule.getSubjectId(),
                schedule.getSubjectTypeId(), schedule.getDayOfWeek(), schedule.getNumberOfWeek());
    }

    @Override
    public void insertGroupSchedule(GroupSchedule groupSchedule)
    {
        jdbcTemplate.update("INSERT INTO \"Group_Schedule\" (\"groupId\", \"scheduleId\") VALUES (?, ?)",
                groupSchedule.getGroupId(), groupSchedule.getScheduleId());
    }

    @Override
    public void insertUserSchedule(UserSchedule userSchedule)
    {
        jdbcTemplate.update("INSERT INTO \"User_Schedule\" (\"userId\", \"scheduleId\") VALUES (?, ?)",
                userSchedule.getUserId(), userSchedule.getScheduleId());
    }

    @Override
    public void merge(Schedule schedule)
    {
        scheduleConsumer.accept(schedule);
    }

    @Override
    public void mergeGroupSchedule(GroupSchedule groupSchedule)
    {
        groupScheduleConsumer.accept(groupSchedule);
    }

    @Override
    public void mergeUserSchedule(UserSchedule userSchedule)
    {
        userScheduleConsumer.accept(userSchedule);
    }

    @Override
    public void update(Schedule schedule)
    {
        jdbcTemplate.update("UPDATE \"Schedule\" SET \"groupName\" = ?, \"classNumber\" = ?," +
                " \"classroomId\" = ?, \"subjectId\" = ?, \"subjectTypeId\" = ?, \"dayOfWeek\" = ?, \"numberOfWeek\" = ?" +
                " WHERE \"id\" = ?", schedule.getClassTime(), schedule.getClassroomId(), schedule.getSubjectId(),
                schedule.getSubjectTypeId(), schedule.getDayOfWeek(), schedule.getNumberOfWeek(), schedule.getId());
    }

    @Override
    public void delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Schedule\" WHERE \"id\" = ?", id);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Schedule\"");
    }

    @Override
    public void deleteUserSchedule(Long userId)
    {
        jdbcTemplate.update("DELETE FROM \"Schedule\"  WHERE \"id\" IN (SELECT \"scheduleId\" " +
                "FROM \"User_Schedule\" WHERE \"userId\" =?)", userId);
    }

    @Override
    public void deleteGroupSchedule(Integer groupId)
    {
        jdbcTemplate.update("DELETE  FROM \"Group_Schedule\"  WHERE \"groupId\" = ?", groupId);
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Schedule\"", Integer.class);
    }
}
