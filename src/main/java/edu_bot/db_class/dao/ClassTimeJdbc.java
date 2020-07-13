package edu_bot.db_class.dao;

import edu_bot.db_class.model.ClassTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class ClassTimeJdbc implements ClassTimeDao
{

    private final JdbcTemplate jdbcTemplate;
    private final Consumer<ClassTime> classTimeConsumer;

    public ClassTimeJdbc(JdbcTemplate jdbcTemplate, Consumer<ClassTime> classTimeConsumer)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.classTimeConsumer = classTimeConsumer;
    }

    @Override
    public ClassTime getClassTime(Integer classNumber) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"ClassTime\" WHERE \"classNumber\" = ?", this::mapClassTime, classNumber);
    }

    @Override
    public List<ClassTime> getClassTimes() {
        return jdbcTemplate.query("SELECT * FROM \"ClassTime\"", this::mapClassTime);
    }

    @Override
    public ClassTime getClassTimeForSchedule(Integer classNumber, Integer scheduleId) {
        return jdbcTemplate.queryForObject("SELECT \"classStart\", \"classStop\" FROM \"ClassTime\" JOIN \"Schedule\" ON " +
                        "\"ClassTime\".\"classNumber\" = \"Schedule\".\"classNumber\" WHERE \"ClassTime\"." +
                        "\"classNumber\" = ? AND \"Schedule\".\"id\" = ?",
                (rs, row) -> new ClassTime(rs.getString("classStart"),
                        rs.getString("classStop")), classNumber, scheduleId);
    }

    private ClassTime mapClassTime(ResultSet rs, int row) throws SQLException
    {
        return new ClassTime(rs.getInt("classNumber"), rs.getString("classStart"),
                rs.getString("classStop"));
    }

    @Override
    public void insert(ClassTime classTime)
    {
        jdbcTemplate.update("INSERT INTO \"ClassTime\" (\"classNumber\", \"classStart\", \"classStop\") " +
                "VALUES (?, ?, ?)", classTime.getClassNumber(), classTime.getClassStart(), classTime.getClassStop());
    }

    @Override
    public void merge(ClassTime classTime)
    {
        classTimeConsumer.accept(classTime);
    }

    @Override
    public void update(ClassTime classTime)
    {
        jdbcTemplate.update("UPDATE \"ClassTime\" SET \"classStart\" = ?, \"classStop\" = ? " +
                "WHERE \"classNumber\" = ?", classTime.getClassStart(), classTime.getClassStop(), classTime.getClassNumber());
    }

    @Override
    public void delete(Integer classNumber)
    {
        jdbcTemplate.update("DELETE FROM \"ClassTime\" WHERE \"classNumber\" = ?", classNumber);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"ClassTime\"");
    }

    @Override
    public Integer count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"ClassTime\"", Integer.class);
    }

}
