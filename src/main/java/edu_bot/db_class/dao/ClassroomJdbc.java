package edu_bot.db_class.dao;

import edu_bot.db_class.model.Classroom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class ClassroomJdbc implements ClassroomDao
{
    private final JdbcTemplate jdbcTemplate;
    private final Consumer<Classroom> classroomConsumer;

    public ClassroomJdbc(JdbcTemplate jdbcTemplate, Consumer<Classroom> classroomConsumer)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.classroomConsumer = classroomConsumer;
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
    public void insert(Classroom classroom)
    {
        jdbcTemplate.update("INSERT INTO \"Classroom\" (\"id\", \"className\", \"pic\")  VALUES (?, ?, ?)",
                classroom.getId(), classroom.getClassName(), classroom.getPic());
    }

    @Override
    public void merge(Classroom classroom)
    {
        classroomConsumer.accept(classroom);
    }

    @Override
    public void update(Classroom classroom)
    {
        jdbcTemplate.update("UPDATE \"Classroom\" SET \"className\" = ?, \"pic\" = ? WHERE \"id\" = ?",
                classroom.getClassName(), classroom.getPic(), classroom.getId());
    }

    @Override
    public void delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Classroom\" WHERE \"id\" = ?", id);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Classroom\"");
    }

    @Override
    public Integer count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Classroom\"", Integer.class);
    }
}
