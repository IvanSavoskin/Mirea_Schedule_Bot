package edu_bot.db_class.model;

public class Classroom
{
    private Integer id;
    private String className;
    private String pic;

    public Classroom(int id, String className, String pic)
    {
        this.id = id;
        this.className = className;
        this.pic = pic;
    }

    public Classroom(String className)
    {
        this.className = className;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

}
