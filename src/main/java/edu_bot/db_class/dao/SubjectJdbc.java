package edu_bot.db_class.dao;

import edu_bot.db_class.model.Subject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class SubjectJdbc implements SubjectDao
{
    private final SubjectTypeDao subjectTypeDao;
    private final JdbcTemplate jdbcTemplate;
    private final Consumer<Subject> subjectConsumer;

    public SubjectJdbc(SubjectTypeDao subjectTypeDao, JdbcTemplate jdbcTemplate, Consumer<Subject> subjectConsumer)
    {
        this.subjectTypeDao = subjectTypeDao;
        this.jdbcTemplate = jdbcTemplate;
        this.subjectConsumer = subjectConsumer;
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
    public  void insert(Subject subject)
    {
        jdbcTemplate.update("INSERT INTO \"Subject\" (\"id\", \"subjectName\", \"teacherId\")" +
                "VALUES (?, ?, ?)", subject.getId(), subject.getSubjectName(), subject.getTeacherId());

    }

    @Override
    public  void merge(Subject subject)
    {
        subjectConsumer.accept(subject);
    }

    @Override
    public void update(Subject subject)
    {
        jdbcTemplate.update("UPDATE \"Subject\" SET \"subjectName\" = ?, \"teacherId\" = ? WHERE \"id\" = ?",
                subject.getSubjectName(), subject.getTeacherId(), subject.getId());
    }

    @Override
    public void delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"Subject\" WHERE \"id\" = ?", id);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"Subject\"");
    }

    @Override
    public Integer count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"Subject\"", Integer.class);
    }
}
