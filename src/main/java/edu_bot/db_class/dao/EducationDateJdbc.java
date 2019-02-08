package edu_bot.db_class.dao;

import edu_bot.db_class.model.EducationDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
    public void Merge(Date semesterStartDate, Date testSessionStartDate, Date examSessionStartDate, Date examSessionStopDate)
    {
        jdbcTemplate.update("MERGE INTO \"EducationDate\" (\"semesterStartDate\", \"testSessionStartDate\"," +
                        " \"examSessionStartDate\", \"examSessionStopDate\") KEY(\"semesterStartDate\", \"testSessionStartDate\"," +
                        "\"examSessionStartDate\", \"examSessionStopDate\") VALUES (?, ?, ?, ?)",
                semesterStartDate, testSessionStartDate, examSessionStartDate, examSessionStopDate);
    }

    /*@Override
    public void Merge(Date semesterStartDate, Date testSessionStartDate, Date examSessionStartDate, Date examSessionStopDate)
    {
        jdbcTemplate.update("INSERT INTO \"EducationDate\" (\"semesterStartDate\", \"testSessionStartDate\"," +
                        "\"examSessionStartDate\", \"examSessionStopDate\") VALUES (?, ?, ?, ?) " +
                        "ON CONFLICT (\"semesterStartDate\") DO UPDATE SET \"semesterStartDate\" = ?, \"testSessionStartDate\" = ?, " +
                        "\"examSessionStartDate\" = ?, \"examSessionStopDate\" = ?", semesterStartDate, testSessionStartDate,
                examSessionStartDate, examSessionStopDate, semesterStartDate, testSessionStartDate, examSessionStartDate,
                examSessionStopDate);
    }*/

    @Override
    public void Update(Date semesterStartDate, Date testSessionStartDate, Date examSessionStartDate, Date examSessionStopDate)
    {
        jdbcTemplate.update("UPDATE \"EducationDate\" SET \"semesterStartDate\" = ?, \"testSessionStartDate\" = ?, " +
                "\"examSessionStartDate\" = ?, \"examSessionStopDate\" = ? ", semesterStartDate, testSessionStartDate,
                examSessionStartDate, examSessionStopDate);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"EducationDate\"");
    }

    private EducationDate mapEducationDate(ResultSet rs, int row) throws SQLException
    {
        return new EducationDate(rs.getDate("semesterStartDate"), rs.getDate("testSessionStartDate"),
                rs.getDate("examSessionStartDate"), rs.getDate("examSessionStopDate"));
    }
}
