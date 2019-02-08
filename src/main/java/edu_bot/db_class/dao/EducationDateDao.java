package edu_bot.db_class.dao;

import edu_bot.db_class.model.EducationDate;

import java.util.Date;

public interface EducationDateDao
{
    EducationDate getEducationDate();

    void Merge(Date semesterStartDate, Date testSessionStartDate, Date examSessionStartDate, Date examSessionStopDate);

    void Update(Date semesterStartDate, Date testSessionStartDate, Date examSessionStartDate, Date examSessionStopDate);

    void DeleteAll();
}
