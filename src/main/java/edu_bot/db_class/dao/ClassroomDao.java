package edu_bot.db_class.dao;

import edu_bot.db_class.model.Classroom;

import java.util.List;

public interface ClassroomDao
{

    Classroom getClassroom(Integer id);

    List<Classroom> getClassroomForParse(String className);

    List<Classroom> getClassrooms();

    Classroom getClassroomForSchedule(Integer classroomId);

    void insert(Classroom classroom);

    void merge(Classroom classroom);

    void update(Classroom classroom);

    void delete(Integer id);

    void deleteAll();

    Integer count();

}
