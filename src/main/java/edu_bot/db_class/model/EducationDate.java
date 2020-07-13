package edu_bot.db_class.model;

import java.util.Date;

public class EducationDate
{
    private Date semesterStartDate;
    private Date testSessionStartDate;
    private Date examSessionStartDate;
    private Date examSessionStopDate;

    public EducationDate (Date semesterStartDate, Date testSessionStartDate, Date examSessionStartDate, Date examSessionStopDate)
    {
        this.semesterStartDate = semesterStartDate;
        this.testSessionStartDate = testSessionStartDate;
        this.examSessionStartDate = examSessionStartDate;
        this.examSessionStopDate = examSessionStopDate;
    }

    public Date getSemesterStartDate() {
        return semesterStartDate;
    }

    public void setSemesterStartDate(Date semesterStartDate) {
        this.semesterStartDate = semesterStartDate;
    }

    public Date getTestSessionStartDate() {
        return testSessionStartDate;
    }

    public void setTestSessionStartDate(Date testSessionStartDate) {
        this.testSessionStartDate = testSessionStartDate;
    }

    public Date getExamSessionStartDate() {
        return examSessionStartDate;
    }

    public void setExamSessionStartDate(Date examSessionStartDate) {
        this.examSessionStartDate = examSessionStartDate;
    }

    public Date getExamSessionStopDate() {
        return examSessionStopDate;
    }

    public void setExamSessionStopDate(Date examSessionStopDate) {
        this.examSessionStopDate = examSessionStopDate;
    }
}
