package edu_bot.db_class.dao;

import edu_bot.db_class.model.FileSum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

@Repository
public class FileSumJdbc implements FileSumDao
{
    private final JdbcTemplate jdbcTemplate;
    private final Consumer<FileSum> fileSumConsumer;

    public FileSumJdbc(JdbcTemplate jdbcTemplate, Consumer<FileSum> fileSumConsumer)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.fileSumConsumer = fileSumConsumer;
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
    public void insert(FileSum fileSum)
    {
        jdbcTemplate.update("INSERT INTO \"FileSum\" (\"fileName\", \"md5\") VALUES (?, ?)", fileSum.getFileName(),
                fileSum.getMd5());
    }

    @Override
    public void merge(FileSum fileSum)
    {
        fileSumConsumer.accept(fileSum);
    }

    @Override
    public void update(FileSum fileSum)
    {
        jdbcTemplate.update("UPDATE \"FileSum\" SET \"md5\" = ? WHERE \"fileName\" = ?", fileSum.getMd5(), fileSum.getFileName());
    }

    @Override
    public void delete(String fileName)
    {
        jdbcTemplate.update("DELETE FROM \"FileSum\" WHERE \"fileName\" = ?",  fileName);
    }

    @Override
    public void deleteAll()
    {
        jdbcTemplate.update("DELETE FROM \"FileSum\"");
    }

    private FileSum mapFileSum(ResultSet rs, int row) throws SQLException
    {
        return new FileSum(rs.getString("FileName"), rs.getString("md5"));
    }
}
