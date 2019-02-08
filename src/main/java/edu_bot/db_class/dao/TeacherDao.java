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

    void Insert(Integer id, String name, String surname, String second_name, String phone_number, String mail);

    void Merge(Integer id, String name, String surname, String second_name, String phone_number, String mail);

    void Update(Integer id, String name, String surname, String second_name, String phone_number, String mail);

    void Delete(Integer id);

    void DeleteAll();

    Integer Count();

}
