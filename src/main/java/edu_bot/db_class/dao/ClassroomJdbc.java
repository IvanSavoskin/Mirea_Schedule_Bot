package edu_bot.db_class.dao;

import edu_bot.db_class.model.Classroom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ClassroomJdbc implements ClassroomDao
{
    private final JdbcTemplate jdbcTemplate;

    public ClassroomJdbc(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Classroom getClassroom(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Classroom\" WHERE \"id\" = ?", this::mapClassroom, id);
    }

    @Override
    public List<Classroom> getClassroomForParse(String className) {
        return jdbcTemplate.query("SELECT * FROM \"Classroom\" WHERE \"className\" = ?", this::mapClassroom, className);
    }

    @Override
    public List<Classroom> getClassrooms() {
        return jdbcTemplate.query("SELECT * FROM \"Classroom\"", this::mapClassroom);
    }

    @Override
    public Classroom getClassroomForSchedule(Integer id) {
        return jdbcTemplate.queryForObject("SELECT \"className\" FROM \"Classroom\" JOIN \"Schedule\" ON " +
                        "\"Classroom\".\"id\"=\"Schedule\".\"classroomId\" WHERE \"Classroom\".\"id\" = ?",
                (rs, row) -> new Classroom(rs.getString("className")), id);
    }

    private Classroom mapClassroom(ResultSet rs, int row) throws SQLException
    {
        return new Classroom(rs.getInt("id"), rs.getString("className"),
                rs.getString("pic"));
    }

    @Override
    public void Insert(Integer id, String name, String pic)
    {
        jdbcTemplate.update("INSERT INTO \"Classroom\" (\"id\", \"className\", \"pic\")  VALUES (?, ?, ?)",
                id, name, pic);
    }

    @Override
    public void Merge(Integer id, String name, String pic)
    {
        jdbcTemplate.update("MERGE INTO \"Classroom\" (\"id\", \"className\", \"pic\") KEY(\"id\") VALUES (?, ?, ?)",
                id, name, pic);
    }

    /*@Override
    public void Merge(Integer id, String name, String pic)
    {
        jdbcTemplate.update("INSERT INTO \"Classroom\" (\"id\", \"className\", \"pic\")  VALUES (?, ?, ?) " +
                        "ON CONFLICT (\"id\") DO UPDATE SET \"className\" = ?, \"pic\" = ?",
                id, name, pic, name, pic);
    }*/

    @Override
    public void Update(Integer id, String name, String pic)
    {
        jdbcTemplate.update("UPDATE \"Classroom\" SET \"className\" = ?, \"pic\" = ? WHERE \"id\" = ?",
                name, pic, id);
    }

    @Override
    public void Delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Classroom\" WHERE \"id\" = ?", id);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Classroom\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Classroom\"", Integer.class);
    }
}
