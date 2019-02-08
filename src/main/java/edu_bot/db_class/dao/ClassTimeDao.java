package edu_bot.db_class.dao;

import edu_bot.db_class.model.ClassTime;

import java.util.List;

public interface ClassTimeDao
{

    ClassTime getClassTime(Integer classNumber);

    List<ClassTime> getClassTimes();

    ClassTime getClassTimeForSchedule(Integer classNumber, Integer scheduleId);

    void Insert(Integer classNumber, String classStart, String classStop);

    void Merge(Integer classNumber, String classStart, String classStop);

    void Update(Integer classNumber, String classStart, String classStop);

    void Delete(Integer classNumber);

    void DeleteAll();

    Integer Count();

}
