package edu_bot.db_class.model;

public class Schedule
{
    private Integer id;
    private Integer groupId;
    private Integer classTime;
    private Integer subjectId;
    private  Integer subjectTypeId;
    private Integer classroomId;
    private Integer dayOfWeek;
    private Integer numberOfWeek;
    private ClassTime classTimes;
    private Subject subjects;
    private Classroom classrooms;
    private SubjectType subjectTypes;
    private String classStart;
    private String classStop;
    private String className;
    private String subjectName;
    private String typeName;
    private String surname;
    private String name;
    private String second_name;
    private String phone_number;
    private String mail;
    private Integer classNumber;

    public Schedule(int id, int classTime, int subjectId, int subjectTypeId, int classroomId, Integer dayOfWeek,
                    Integer numberOfWeek)
    {
        this.id = id;
        this.classTime = classTime;
        this.subjectId = subjectId;
        this.subjectTypeId = subjectTypeId;
        this.classroomId = classroomId;
        this.dayOfWeek = dayOfWeek;
        this.numberOfWeek = numberOfWeek;
    }

    public Schedule(Integer classNumber, String classStart, String classStop, String className, String subjectName,
                    String typeName, String surname, String name, String second_name, String phone_number, String mail,
                    Integer dayOfWeek, Integer numberOfWeek)
    {
        this.classNumber = classNumber;
        this.classStart = classStart;
        this.classStop = classStop;
        this.className = className;
        this.subjectName = subjectName;
        this.typeName = typeName;
        this.surname = surname;
        this.name = name;
        this.second_name = second_name;
        this.phone_number = phone_number;
        this.mail = mail;
        this.dayOfWeek = dayOfWeek;
        this.numberOfWeek = numberOfWeek;
    }

    public Schedule(int id, int classTime, int subjectId, int subjectTypeId, int classroomId, Integer dayOfWeek,
                    Integer numberOfWeek, ClassTime classTimes, Classroom classrooms, Subject subjects, SubjectType subjectTypes)
    {
        this.id = id;
        this.classTime = classTime;
        this.subjectId = subjectId;
        this.subjectTypeId = subjectTypeId;
        this.classroomId = classroomId;
        this.dayOfWeek = dayOfWeek;
        this.numberOfWeek = numberOfWeek;
        this.classTimes = classTimes;
        this.subjects = subjects;
        this.classrooms = classrooms;
        this.subjectTypes = subjectTypes;
    }

    public Schedule(int id, Integer dayOfWeek, Integer numberOfWeek)
    {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.numberOfWeek = numberOfWeek;
    }

    public Schedule(int id, Integer groupId)
    {
        this.id = id;
        this.groupId = groupId;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getClassTime() {
        return classTime;
    }

    public void setClassTime(Integer classTime) {
        this.classTime = classTime;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Integer classroomId) {
        this.classroomId = classroomId;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getNumberOfWeek() {
        return numberOfWeek;
    }

    public void setNumberOfWeek(Integer numberOfWeek) {
        this.numberOfWeek = numberOfWeek;
    }

    public ClassTime getClassTimes() {
        return classTimes;
    }

    public void setClassTimes(ClassTime classTimes) {
        this.classTimes = classTimes;
    }

    public void setSubjects(Subject subjects) {
        this.subjects = subjects;
    }

    public Subject getSubjects() {
        return subjects;
    }

    public Classroom getClassrooms() {
        return classrooms;
    }

    public void setClassrooms(Classroom classrooms) {
        this.classrooms = classrooms;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String name) {
        this.className = className;
    }

    public Integer getClassNumber()
    {
        return classNumber;
    }

    public void setClassNumber(Integer classNumber)
    {
        this.classNumber = classNumber;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public String getSurname()
    {
        return surname;
    }

    public void setSurname(String surname)
    {
        this.surname = surname;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSecond_name() {
        return second_name;
    }

    public void setSecond_name(String second_name) {
        this.second_name = second_name;
    }

    public Integer getSubjectTypeId() {
        return subjectTypeId;
    }

    public void setSubjectTypeId(Integer subjectTypeId) {
        this.subjectTypeId = subjectTypeId;
    }

    public SubjectType getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(SubjectType subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}