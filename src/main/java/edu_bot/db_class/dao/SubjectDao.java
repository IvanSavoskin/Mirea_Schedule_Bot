package edu_bot.db_class.dao;

import edu_bot.db_class.model.Subject;

import java.util.List;

public interface SubjectDao
{
    Subject getSubject(Integer id);

    List<Subject> getSubjectForName(String subjectName);

    List<Subject> getSubjectForParse(String subjectName, Integer teacherId);

    List<Subject> getSubjects();

    List<Subject> getSubjectForTeacher(Integer teacherId);

    Subject getSubjectForSchedule(Integer id);

    void insert(Subject subject);

    void merge(Subject subject);

    void update(Subject subject);

    void delete(Integer id);

    void deleteAll();

    Integer count();

}
