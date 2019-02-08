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

    void Insert(Integer id, Integer subjectId, String typeName);

    void Merge(Integer id, Integer subjectId, String typeName);

    void Update(Integer id, Integer subjectId, String typeName);

    void Delete(Integer id);

    void DeleteAll();

    Integer Count();

}
