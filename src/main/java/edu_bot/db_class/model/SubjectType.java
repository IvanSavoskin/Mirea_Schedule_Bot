package edu_bot.db_class.model;

public class SubjectType
{
    private Integer id;
    private Integer subjectId;
    private String typeName;
    private String subjectName;

    public SubjectType(Integer id, String typeName, String subjectName)
    {
        this.id = id;
        this.typeName = typeName;
        this.subjectName = subjectName;
    }

    public SubjectType(Integer id, String typeName)
    {
        this.id = id;
        this.typeName = typeName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
