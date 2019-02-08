package edu_bot.db_class.dao;

import edu_bot.db_class.model.ClassTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ClassTimeJdbc implements ClassTimeDao
{

    private final JdbcTemplate jdbcTemplate;

    public ClassTimeJdbc(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
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
    public void Insert(Integer classNumber, String classStart, String classStop)
    {
        jdbcTemplate.update("INSERT INTO \"ClassTime\" (\"classNumber\", \"classStart\", \"classStop\") " +
                "VALUES (?, ?, ?)", classNumber, classStart, classStop);
    }

    @Override
    public void Merge(Integer classNumber, String classStart, String classStop)
    {
        jdbcTemplate.update("MERGE INTO \"ClassTime\" (\"classNumber\", \"classStart\", \"classStop\") " + 
                        "KEY(\"classNumber\") VALUES (?, ?, ?)", classNumber, classStart, classStop);
    }

    /*@Override
    public void Merge(Integer classNumber, String classStart, String classStop)
    {
        jdbcTemplate.update("INSERT INTO \"ClassTime\" (\"classNumber\", \"classStart\", \"classStop\") " +
                "VALUES (?, ?, ?) ON CONFLICT ( \"classNumber\") DO UPDATE SET \"classStart\" = ?, \"classStop\" = ?",
                classNumber, classStart, classStop, classStart, classStop);
    }*/

    @Override
    public void Update(Integer classNumber, String classStart, String classStop)
    {
        jdbcTemplate.update("UPDATE \"ClassTime\" SET \"classStart\" = ?, \"classStop\" = ? " +
                "WHERE \"classNumber\" = ?", classStart, classStop, classNumber);
    }

    @Override
    public void Delete(Integer classNumber)
    {
        jdbcTemplate.update("DELETE FROM \"ClassTime\" WHERE \"classNumber\" = ?", classNumber);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"ClassTime\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"ClassTime\"", Integer.class);
    }

}
