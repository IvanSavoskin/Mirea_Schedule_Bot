package edu_bot.db_class.dao;

import edu_bot.db_class.model.EducationDate;

public interface EducationDateDao
{
    EducationDate getEducationDate();

    void insert(EducationDate educationDate);

    void update(EducationDate educationDate);

    void deleteAll();
}
