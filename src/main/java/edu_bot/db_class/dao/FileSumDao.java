package edu_bot.db_class.dao;

import edu_bot.db_class.model.FileSum;

import java.util.List;

public interface FileSumDao
{
    FileSum getFileSum(String fileName);

    List<FileSum> getFileSums();

    List<FileSum> getFileSumsForName(String fileName);

    void insert(FileSum fileSum);

    void merge(FileSum fileSum);

    void update(FileSum fileSum);

    void delete(String fileName);

    void deleteAll();
}
