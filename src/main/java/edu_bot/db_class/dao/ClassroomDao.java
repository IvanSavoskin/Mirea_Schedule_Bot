package edu_bot.db_class.dao;

import edu_bot.db_class.model.Classroom;

import java.util.List;

public interface ClassroomDao
{

    Classroom getClassroom(Integer id);

    List<Classroom> getClassroomForParse(String className);

    List<Classroom> getClassrooms();

    Classroom getClassroomForSchedule(Integer classroomId);

    void Insert(Integer id, String name, String pic);

    void Merge(Integer id, String name, String pic);

    void Update(Integer id, String name, String pic);

    void Delete(Integer id);

    void DeleteAll();

    Integer Count();

}
