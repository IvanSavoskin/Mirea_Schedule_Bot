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

    void Insert(Integer id, String subjectName, Integer teacherId);

    void Merge(Integer id, String subjectName, Integer teacherId);

    void Update(Integer id, String subjectName, Integer teacherId);

    void Delete(Integer id);

    void DeleteAll();

    Integer Count();

}
