package edu_bot.db_class.dao;

import edu_bot.db_class.model.SubjectType;

import java.util.List;

public interface SubjectTypeDao
{

    SubjectType getSubjectType(Integer id);

    List<SubjectType> getSubjectTypeForParse(String typeName);

    List<SubjectType> getSubjectTypes();

    SubjectType getSubjectTypeForSchedule(Integer id);

    SubjectType getSubjectTypeForSubject(Integer subjectId);

    void insert(SubjectType subjectType);

    void merge(SubjectType subjectType);

    void update(SubjectType subjectType);

    void delete(Integer id);

    void deleteAll();

    Integer count();

}
