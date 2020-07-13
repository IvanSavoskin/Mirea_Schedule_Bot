package edu_bot.db_class.model;

public class ClassTime
{
    private Integer classNumber;
    private String classStart;
    private String classStop;

    public ClassTime (Integer classNumber, String classStart, String classStop)
    {
        this.classNumber = classNumber;
        this.classStart = classStart;
        this.classStop = classStop;
    }

    public ClassTime (String classStart, String classStop)
    {
        this.classStart = classStart;
        this.classStop = classStop;
    }

    public Integer getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(Integer classNumber) {
        this.classNumber = classNumber;
    }

    public String getClassStart() {
        return classStart;
    }

    public void setClassStart(String classStart) {
        this.classStart = classStart;
    }

    public String getClassStop() {
        return classStop;
    }

    public void setClassStop(String classStop) {
        this.classStop = classStop;
    }

}
