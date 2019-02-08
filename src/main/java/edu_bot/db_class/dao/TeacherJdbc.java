package edu_bot.db_class.dao;

import edu_bot.db_class.model.Teacher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TeacherJdbc implements TeacherDao
{
    private final JdbcTemplate jdbcTemplate;
    private final SubjectDao subjectDao;

    public TeacherJdbc(JdbcTemplate jdbcTemplate, SubjectDao subjectDao)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.subjectDao = subjectDao;
    }

    @Override
    public Teacher getTeacher(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Teacher\" WHERE \"id\" = ?", this::mapTeacher, id);
    }

    @Override
    public List<Teacher> getTeacherForSurname(String surname) {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\" WHERE \"surname\" = ?", this::mapTeacher, surname);
    }

    @Override
    public List<Teacher> getTeacherForSurnameForCustomSchedule(String surname, Long userId) {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\" JOIN \"Subject\" ON \"Teacher\".\"id\" = " +
                "\"Subject\".\"teacherId\" JOIN \"Schedule\" ON \"Subject\".\"id\" = \"Schedule\".\"subjectId\" " +
                "JOIN \"User_Schedule\" ON \"Schedule\".\"id\" = \"User_Schedule\".\"scheduleId\" WHERE " +
                "\"surname\" = ? AND \"userId\" = ?", this::mapTeacher, surname, userId);
    }

    @Override
    public List<Teacher> getTeacherForSubject(String subjectName) {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\" JOIN \"Subject\" ON \"Teacher\".\"id\" " +
                "= \"Subject\".\"teacherId\" WHERE \"Subject\".\"subjectName\" = ?", this::mapTeacher, subjectName);
    }

    @Override
    public List<Teacher> getTeacherForSubjectForCustomSchedule(String subjectName, Long userId) {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\" JOIN \"Subject\" ON \"Teacher\".\"id\" " +
                "= \"Subject\".\"teacherId\" JOIN \"Schedule\" ON \"Subject\".\"id\" = \"Schedule\".\"subjectId\" " +
                "JOIN \"User_Schedule\" ON \"Schedule\".\"id\" = \"User_Schedule\".\"scheduleId\" WHERE " +
                "\"Subject\".\"subjectName\" = ? AND \"userId\" = ?", this::mapTeacher, subjectName, userId);
    }

    @Override
    public List<Teacher> getTeacherForParse(String surname, String name, String second_name) {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\" WHERE \"surname\" = ? AND \"name\" = ? " +
                        "AND \"second_name\" = ?", this::mapTeacher, surname, name, second_name);
    }

    @Override
    public List<Teacher> getTeacherForCustomSchedule(String surname, String name, String second_name,
                                                     String phone_number, String mail) {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\" WHERE \"surname\" = ? AND \"name\" = ? " +
                "AND \"second_name\" = ? AND \"phone_number\" = ? AND \"mail\" = ? ", this::mapTeacher, surname, name,
                second_name, phone_number, mail);
    }

    @Override
    public List<Teacher> getTeachers() {
        return jdbcTemplate.query("SELECT * FROM \"Teacher\"", this::mapTeacher);
    }

    private Teacher mapTeacher(ResultSet rs, int row) throws SQLException
    {
        return new Teacher(rs.getInt("id"), rs.getString("name"),
                rs.getString("surname"), rs.getString("second_name"),
                rs.getString("phone_number"), rs.getString("mail"), subjectDao.getSubjectForTeacher(rs.getInt("id")));
    }

    @Override
    public void Insert(Integer id, String name, String surname, String second_name, String phone_number, String mail)
    {
        jdbcTemplate.update("INSERT INTO \"Teacher\" (\"id\", \"name\", \"surname\", \"second_name\"," +
                " \"phone_number\", \"mail\") VALUES (?, ?, ?, ?, ?, ?)",
                id, name, surname, second_name, phone_number, mail);
    }

    @Override
    public void Merge(Integer id, String name, String surname, String second_name, String phone_number, String mail)
    {
        jdbcTemplate.update("MERGE INTO \"Teacher\" (\"id\", \"name\", \"surname\", \"second_name\"," +
                        " \"phone_number\", \"mail\") KEY (\"id\") VALUES (?, ?, ?, ?, ?, ?)",
                id, name, surname, second_name, phone_number, mail);
        }

    /*@Override
    public void Merge(Integer id, String name, String surname, String second_name, String phone_number, String mail)
    {
        jdbcTemplate.update("INSERT INTO \"Teacher\" (\"id\", \"name\", \"surname\", \"second_name\"," +
                " \"phone_number\", \"mail\") VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (\"id\") DO UPDATE SET \"name\" = ?," +
                "\"surname\" = ?, \"second_name\" = ?, \"phone_number\" = ?, \"mail\" = ?", id, name, surname,
                second_name, phone_number, mail, name, surname, second_name, phone_number, mail);
    }*/

    @Override
    public void Update(Integer id, String name, String surname, String second_name, String phone_number, String mail)
    {
        jdbcTemplate.update("UPDATE \"Teacher\" SET \"name\" = ?, \"surname\" = ?, \"second_name\" = ?," +
                " \"phone_number\" = ?, \"mail\" = ? WHERE \"id\" = ?", name, surname, second_name, phone_number,
                mail, id);
    }

    @Override
    public void Delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Teacher\" WHERE \"id\" = ?", id);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Teacher\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Teacher\"", Integer.class);
    }

}
