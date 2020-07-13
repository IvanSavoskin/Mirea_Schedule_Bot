package edu_bot.db_class.dao;

import edu_bot.db_class.model.EducationDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class EducationDateJdbc implements EducationDateDao
{
    private final JdbcTemplate jdbcTemplate;

    public EducationDateJdbc(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public EducationDate getEducationDate() {
        return jdbcTemplate.queryForObject("SELECT * FROM \"EducationDate\"", this::mapEducationDate);
    }

    @Override
    public void insert(EducationDate educationDate)
    {
        jdbcTemplate.update("INSERT INTO \"EducationDate\" (\"semesterStartDate\", \"testSessionStartDate\"," +
                        "\"examSessionStartDate\", \"examSessionStopDate\") VALUES (?, ?, ?, ?)",
                educationDate.getSemesterStartDate(), educationDate.getTestSessionStartDate(),
                educationDate.getExamSessionStartDate(), educationDate.getExamSessionStopDate());
    }

    @Override
    public void update(EducationDate educationDate)
    {
        jdbcTemplate.update("UPDATE \"EducationDate\" SET \"semesterStartDate\" = ?, \"testSessionStartDate\" = ?, " +
                "\"examSessionStartDate\" = ?, \"examSessionStopDate\" = ? ", educationDate.getSemesterStartDate(),
                educationDate.getTestSessionStartDate(), educationDate.getExamSessionStartDate(),
                educationDate.getExamSessionStopDate());
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"EducationDate\"");
    }

    private EducationDate mapEducationDate(ResultSet rs, int row) throws SQLException
    {
        return new EducationDate(rs.getDate("semesterStartDate"), rs.getDate("testSessionStartDate"),
                rs.getDate("examSessionStartDate"), rs.getDate("examSessionStopDate"));
    }
}
