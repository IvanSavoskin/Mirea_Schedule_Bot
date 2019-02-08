package edu_bot.db_class.model;

public class Subject
{
    private Integer id;
    private String subjectName;
    private Integer teacherId;
    private SubjectType subjectTypes;

    public Subject(int id, String subjectName, int teacherId, SubjectType subjectTypes)
    {
        this.id = id;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
        this.subjectTypes = subjectTypes;
    }

    public Subject(int id, String subjectName, int teacherId)
    {
        this.id = id;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
    }

    public Subject(int id, String subjectName)
    {
        this.id = id;
        this.subjectName = subjectName;
    }

    public Subject(String subjectName, int teacherId, SubjectType subjectTypes)
    {
        this.subjectName = subjectName;
        this.teacherId = teacherId;
        this.subjectTypes = subjectTypes;
    }

    public Subject(String subjectName)
    {
        this.subjectName = subjectName;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getSubjectName()
    {
        return subjectName;
    }

    public void setSubjectName(String subjectName)
    {
        this.subjectName = subjectName;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public SubjectType getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(SubjectType subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

}
