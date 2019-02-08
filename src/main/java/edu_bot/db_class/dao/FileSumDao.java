package edu_bot.db_class.dao;

import edu_bot.db_class.model.FileSum;

import java.util.List;

public interface FileSumDao
{
    FileSum getFileSum(String fileName);

    List<FileSum> getFileSums();

    List<FileSum> getFileSumsForName(String fileName);

    void Insert(String fileName, String md5);

    void Merge(String fileName, String md5);

    void Update(String fileName, String md5);

    void Delete(String fileName);

    void DeleteAll();
}
