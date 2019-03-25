package edu_bot.db_class.dao;

import edu_bot.db_class.model.Teacher;

import java.util.List;

public interface TeacherDao
{

    Teacher getTeacher(Integer id);

    List<Teacher> getTeacherForSurname(String surname);

    List<Teacher> getTeacherForSurnameForCustomSchedule(String surname, Long userId);

    List<Teacher> getTeacherForSubject(String subjectName);

    List<Teacher> getTeacherForSubjectForCustomSchedule(String subjectName, Long userId);

    List<Teacher> getTeacherForParse(String surname, String name, String second_name);

    public List<Teacher> getTeacherForCustomSchedule(String surname, String name, String second_name,
                                                     String phone_number, String mail);

    List<Teacher> getTeachers();

    void insert(Teacher teacher);

    void merge(Teacher teacher);

    void update(Teacher teacher);

    void delete(Integer id);

    void deleteAll();

    Integer count();

}
