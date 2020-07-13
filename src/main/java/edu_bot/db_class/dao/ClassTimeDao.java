package edu_bot.db_class.dao;

import edu_bot.db_class.model.ClassTime;

import java.util.List;

public interface ClassTimeDao
{

    ClassTime getClassTime(Integer classNumber);

    List<ClassTime> getClassTimes();

    ClassTime getClassTimeForSchedule(Integer classNumber, Integer scheduleId);

    void insert(ClassTime classTime);

    void merge(ClassTime classTime);

    void update(ClassTime classTime);

    void delete(Integer classNumber);

    void deleteAll();

    Integer count();

}
