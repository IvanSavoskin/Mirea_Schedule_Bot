package edu_bot.db_class.dao;

import edu_bot.db_class.model.Subject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SubjectJdbc implements SubjectDao
{
    private final SubjectTypeDao subjectTypeDao;
    private final JdbcTemplate jdbcTemplate;

    public SubjectJdbc(SubjectTypeDao subjectTypeDao, JdbcTemplate jdbcTemplate)
    {
        this.subjectTypeDao = subjectTypeDao;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Subject getSubject(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Subject\" WHERE \"id\" = ?", this::mapSubject, id);
    }

    @Override
    public List<Subject> getSubjectForName(String subjectName) {
        return jdbcTemplate.query("SELECT * FROM \"Subject\" WHERE \"subjectName\" = ?", this::mapSubject, subjectName);
    }

    @Override
    public List<Subject> getSubjectForParse(String subjectName, Integer teacherId) {
        return jdbcTemplate.query("SELECT * FROM \"Subject\" WHERE \"subjectName\" = ? AND \"teacherId\" = ?",
                this::mapSubject, subjectName, teacherId);
    }

    @Override
    public List<Subject> getSubjects() {
        return jdbcTemplate.query("SELECT * FROM \"Subject\"",
                (rs, row) -> new Subject(rs.getInt("id"), rs.getString("subjectName")));
    }

    @Override
    public Subject getSubjectForSchedule(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"Subject\" JOIN \"Schedule\" ON " +
                        "\"Subject\".\"id\"=\"Schedule\".\"subjectId\" WHERE \"Subject\".\"id\" = ?",
                (rs, row) -> new Subject(rs.getString("subjectName"), rs.getInt("teacherId"),
                        subjectTypeDao.getSubjectTypeForSubject(rs.getInt("id"))), id);
    }

    @Override
    public List<Subject> getSubjectForTeacher(Integer teacherId) {
        return jdbcTemplate.query("SELECT * FROM \"Subject\" JOIN \"Teacher\" ON " +
                        "\"Subject\".\"teacherId\"=\"Teacher\".\"id\" WHERE \"teacherId\" = ?",
                (rs,row) -> new Subject(rs.getString("subjectName")), teacherId);
    }

    private Subject mapSubject(ResultSet rs, int row) throws SQLException
    {
        return new Subject(rs.getInt("id"), rs.getString("subjectName"),
                rs.getInt("teacherId"));
    }

    @Override
    public  void  Insert(Integer id, String subjectName, Integer teacherId)
    {
        jdbcTemplate.update("INSERT INTO \"Subject\" (\"id\", \"subjectName\", \"teacherId\")" +
                "VALUES (?, ?, ?)", id, subjectName, teacherId);

    }

    @Override
    public  void  Merge(Integer id, String subjectName, Integer teacherId)
    {
        jdbcTemplate.update("MERGE INTO \"Subject\" (\"id\", \"subjectName\", \"teacherId\") KEY(\"id\")" +
                " VALUES (?, ?, ?)", id, subjectName, teacherId);
       }

    /*@Override
    public  void  Merge(Integer id, String subjectName, Integer teacherId)
    {
        jdbcTemplate.update("INSERT INTO \"Subject\" (\"id\", \"subjectName\", \"teacherId\")  VALUES (?, ?, ?)" +
                "ON CONFLICT (\"id\") DO UPDATE SET \"subjectName\" = ?, \"teacherId\" = ?", id, subjectName, teacherId,
                subjectName, teacherId);

    }*/

    @Override
    public void Update(Integer id, String subjectName, Integer teacherId)
    {
        jdbcTemplate.update("UPDATE \"Subject\" SET \"subjectName\" = ?, \"teacherId\" = ? WHERE \"id\" = ?",
                subjectName, teacherId, id);
    }

    @Override
    public void Delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Subject\" WHERE \"id\" = ?", id);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Subject\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Subject\"", Integer.class);
    }
}
