package edu_bot.db_class.dao;

import edu_bot.db_class.model.SubjectType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class SubjectTypeJdbc implements SubjectTypeDao
{
    private final JdbcTemplate jdbcTemplate;
    private final Consumer<SubjectType> subjectTypeConsumer;

    public SubjectTypeJdbc(JdbcTemplate jdbcTemplate, Consumer<SubjectType> subjectTypeConsumer)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.subjectTypeConsumer = subjectTypeConsumer;
    }

    @Override
    public SubjectType getSubjectType(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"SubjectType\" WHERE \"id\" = ?",
                this::mapSubjectType, id);
    }

    @Override
    public List<SubjectType> getSubjectTypeForParse(String typeName) {
        return jdbcTemplate.query("SELECT * FROM \"SubjectType\" WHERE \"typeName\" = ?",
                this::mapSubjectType, typeName);

    }

    @Override
    public List<SubjectType> getSubjectTypes() {
        return jdbcTemplate.query("SELECT * FROM \"SubjectType\"",
                (rs,row) -> new SubjectType(rs.getInt("id"), rs.getString("typeName")));

    }

    @Override
    public SubjectType getSubjectTypeForSchedule(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"SubjectType\" JOIN \"Schedule\" ON " +
                        "\"SubjectType\".\"id\"=\"Schedule\".\"subjectTypeId\" WHERE \"SubjectType\".\"id\" = ?",
                this::mapSubjectType, id);
    }

    @Override
    public SubjectType getSubjectTypeForSubject(Integer subjectId) {
        return jdbcTemplate.queryForObject("SELECT * FROM \"SubjectType\" JOIN \"Schedule\" " +
                        "ON \"SubjectType\".\"id\" = \"Schedule\".\"subjectTypeId\" JOIN \"Subject\" " +
                        "ON \"Schedule\".\"subjectId\" = \"Subject\".\"id\" WHERE \"subjectId\" = ?",
                (rs, row) -> new SubjectType(rs.getInt("id"), rs.getString("typeName"),
                        rs.getString("subjectName")), subjectId);
    }

    private SubjectType mapSubjectType (ResultSet rs, int row) throws SQLException
    {
        return new SubjectType(rs.getInt("id"), rs.getString("typeName"));
    }

    @Override
    public void insert(SubjectType subjectType)
    {
        jdbcTemplate.update("INSERT INTO \"SubjectType\" (\"id\", \"typeName\")  VALUES (?, ?)",
                subjectType.getId(), subjectType.getTypeName());

    }

    @Override
    public void merge(SubjectType subjectType)
    {
        subjectTypeConsumer.accept(subjectType);
    }

    @Override
    public void update(SubjectType subjectType)
    {
        jdbcTemplate.update("UPDATE \"SubjectType\" SET \"typeName\" = ? WHERE \"id\" = ?",
                subjectType.getTypeName(), subjectType.getId());
    }

    @Override
    public void delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"SubjectType\" WHERE \"id\" = ?", id);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"SubjectType\"");
    }

    @Override
    public Integer count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"SubjectType\"", Integer.class);
    }

}
