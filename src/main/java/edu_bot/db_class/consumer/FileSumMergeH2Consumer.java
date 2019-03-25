package edu_bot.db_class.consumer;

import edu_bot.db_class.model.FileSum;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Profile("h2")
public class FileSumMergeH2Consumer implements Consumer<FileSum>
{
    private final JdbcTemplate jdbcTemplate;

    public FileSumMergeH2Consumer(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void accept(FileSum fileSum)
    {
        jdbcTemplate.update("MERGE INTO \"FileSum\" (\"fileName\", \"md5\") KEY(\"fileName\") VALUES (?, ?)", fileSum.getFileName(),
                fileSum.getMd5());
    }
}
