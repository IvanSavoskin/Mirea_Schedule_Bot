package edu_bot.db_class.dao;

import edu_bot.db_class.model.FileSum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FileSumJdbc implements FileSumDao
{
    private final JdbcTemplate jdbcTemplate;

    public FileSumJdbc(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public FileSum getFileSum(String fileName)
    {
        return jdbcTemplate.queryForObject("SELECT * FROM \"FileSum\" WHERE \"fileName\" = ?", this::mapFileSum, fileName);
    }

    @Override
    public List<FileSum> getFileSums()
    {
        return jdbcTemplate.query("SELECT * FROM \"FileSum\"", this::mapFileSum);
    }

    @Override
    public List<FileSum> getFileSumsForName(String fileName)
    {
        return jdbcTemplate.query("SELECT * FROM \"FileSum\" WHERE \"fileName\" = ?", this::mapFileSum, fileName);
    }

    @Override
    public void Insert(String fileName, String md5)
    {
        jdbcTemplate.update("INSERT INTO \"FileSum\" (\"fileName\", \"md5\") VALUES (?, ?)", fileName, md5);
    }

    @Override
    public void Merge(String fileName, String md5)
    {
        jdbcTemplate.update("MERGE INTO \"FileSum\" (\"fileName\", \"md5\") KEY(\"fileName\") VALUES (?, ?)", fileName, md5);
    }

    @Override
    public void Update(String fileName, String md5)
    {
        jdbcTemplate.update("UPDATE \"FileSum\" SET \"md5\" = ? WHERE \"fileName\" = ?", md5, fileName);
    }

    @Override
    public void Delete(String fileName)
    {
        jdbcTemplate.update("DELETE FROM \"FileSum\" WHERE \"fileName\" = ?",  fileName);
    }

    @Override
    public void DeleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"FileSum\"");
    }

    private FileSum mapFileSum(ResultSet rs, int row) throws SQLException
    {
        return new FileSum(rs.getString("FileName"), rs.getString("md5"));
    }
}
