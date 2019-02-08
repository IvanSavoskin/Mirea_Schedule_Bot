package edu_bot.db_class.dao;

import edu_bot.db_class.model.SubjectType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SubjectTypeJdbc implements SubjectTypeDao
{
    private final JdbcTemplate jdbcTemplate;

    public SubjectTypeJdbc(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
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
    public void Insert(Integer id, Integer subjectId, String typeName)
    {
        jdbcTemplate.update("INSERT INTO \"SubjectType\" (\"id\", \"typeName\")  VALUES (?, ?)",
                id, typeName);

    }

    @Override
    public void Merge(Integer id, Integer subjectId, String typeName)
    {
        jdbcTemplate.update("MERGE INTO \"SubjectType\" (\"id\", \"typeName\") KEY (\"id\")" +
                " VALUES (?, ?)", id, typeName);
        }

    /*@Override
    public void Merge(Integer id, Integer subjectId, String typeName)
    {
        jdbcTemplate.update("INSERT INTO \"SubjectType\" (\"id\", \"typeName\") VALUES (?, ?) ON CONFLICT (\"id\")" +
                "DO UPDATE SET \"typeName\" = ?", id, typeName, typeName);

    }*/

    @Override
    public void Update(Integer id, Integer subjectId, String typeName)
    {
        jdbcTemplate.update("UPDATE \"SubjectType\" SET \"typeName\" = ? WHERE \"id\" = ?",
                typeName, id);
    }

    @Override
    public void Delete(Integer id)
    {
        jdbcTemplate.update("DELETE FROM \"SubjectType\" WHERE \"id\" = ?", id);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"SubjectType\"");
    }

    @Override
    public Integer Count()
    {
        return jdbcTemplate.queryForObject("SELECT COUNT (*) FROM \"SubjectType\"", Integer.class);
    }

}
