package edu_bot.schedule_class;


import edu_bot.db_class.dao.*;
import edu_bot.db_class.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
public class JsonWorker
{
    private final ScheduleDao scheduleDao;
    private final TeacherDao teacherDao;
    private final SubjectDao subjectDao;
    private final SubjectTypeDao subjectTypeDao;
    private final ClassroomDao classroomDao;

    @Autowired
    public JsonWorker(ScheduleDao scheduleDao, TeacherDao teacherDao, SubjectDao subjectDao,
                      SubjectTypeDao subjectTypeDao, ClassroomDao classroomDao)
    {
        this.scheduleDao = scheduleDao;
        this.teacherDao = teacherDao;
        this.subjectDao = subjectDao;
        this.subjectTypeDao = subjectTypeDao;
        this.classroomDao = classroomDao;
    }

    public InputStream createJson(List<Schedule> schedules)
    {

        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();

        for (Schedule schedule : schedules)
        {
            jsonObject = new JSONObject();

            jsonObject.put("dayOfWeek", schedule.getDayOfWeek());
            jsonObject.put("numberOfWeek", schedule.getNumberOfWeek());
            jsonObject.put("classNumber", schedule.getClassNumber());
            jsonObject.put("subjectName", schedule.getSubjectName());
            jsonObject.put("subjectType", schedule.getTypeName());
            jsonObject.put("teacherName", schedule.getName());
            jsonObject.put("teacherSecondName", schedule.getSecond_name());
            jsonObject.put("teacherSurname", schedule.getSurname());
            jsonObject.put("teacherPhone", schedule.getPhone_number());
            jsonObject.put("teacherMail", schedule.getMail());
            jsonObject.put("classroom", schedule.getClassName());

            jsonArray.add(jsonObject);
        }
        String resultJson = jsonArray.toJSONString();

        return new ByteArrayInputStream(resultJson.getBytes());
    }

    public boolean loadJson(String json, Long chatId)
    {
        JSONArray ar = (JSONArray)JSONValue.parse(json);

        int dayOfWeek;
        int classNumber;
        int numberOfWeek;
        int subjectId;
        int teacherId;
        int subjectTypeId;
        int classroomId;
        int scheduleId;

        boolean check = true;

        JSONObject jsonObject;

        for (int i = 0; i < ar.size(); i++)
        {
            jsonObject = new JSONObject();
            jsonObject = (JSONObject)ar.get(i);

            Object jsonDayOfWeek = jsonObject.get("dayOfWeek");
            Object jsonNumberOfWeek = jsonObject.get("numberOfWeek");
            Object jsonClassNumber = jsonObject.get("classNumber");

            String teacherSurname = jsonObject.get("teacherSurname").toString();
            String teacherName = jsonObject.get("teacherName").toString();
            String teacherSecondName = jsonObject.get("teacherSecondName").toString();
            String teacherPhone = jsonObject.get("teacherPhone").toString();
            String teacherMail = jsonObject.get("teacherMail").toString();

            String subjectName = jsonObject.get("subjectName").toString();

            String subjectTypeName = jsonObject.get("subjectType").toString();

            String classroomName = jsonObject.get("classroom").toString();


            if (jsonDayOfWeek.toString().matches("[1-7]"))
                dayOfWeek = toIntExact((long)jsonDayOfWeek);
            else
            {
                check = false;
                break;
            }

            if (jsonNumberOfWeek.toString().matches("[1-7]{1,2}") || jsonNumberOfWeek.toString().equals("-2") ||
                    jsonNumberOfWeek.toString().equals("-1"))
                numberOfWeek = toIntExact((long)jsonNumberOfWeek);
            else
            {
                check = false;
                break;
            }

            if (jsonClassNumber.toString().matches("[1-8]"))
                classNumber = toIntExact((long)jsonClassNumber);
            else
            {
                check = false;
                break;
            }


            List<Teacher> teachers = teacherDao.getTeacherForCustomSchedule(teacherSurname, teacherName,
                    teacherSecondName, teacherPhone, teacherMail);
            if (teachers.size() == 1)
                teacherId = teachers.get(0).getId();
            else
            {
                teacherId = teacherDao.count() + 1;
                Teacher teacher = new Teacher(teacherId, teacherName, teacherSurname, teacherSecondName, teacherPhone,
                        teacherMail);
                teacherDao.merge(teacher);
            }

            List<Subject> subjects = subjectDao.getSubjectForParse(subjectName, teacherId);
            if (subjects.size() == 1)
                subjectId = subjects.get(0).getId();
            else
            {
                subjectId = subjectDao.count() + 1;
                Subject subject = new Subject(subjectId, subjectName, teacherId);
                subjectDao.merge(subject);
            }

            List<SubjectType> subjectTypes = subjectTypeDao.getSubjectTypeForParse(subjectTypeName);
            if (subjectTypes.size() == 1)
                subjectTypeId = subjectTypes.get(0).getId();
            else
            {
                subjectTypeId = subjectTypeDao.count() + 1;
                SubjectType subjectType = new SubjectType(subjectTypeId, subjectTypeName);
                subjectTypeDao.merge(subjectType);
            }

            List<Classroom> classrooms = classroomDao.getClassroomForParse(classroomName);
            if (classrooms.size() == 1)
                classroomId = classrooms.get(0).getId();
            else
            {
                classroomId = classroomDao.count() + 1;
                Classroom classroom = new Classroom(classroomId, classroomName, null);
                classroomDao.merge(classroom);
            }

            List<Schedule> schedules = scheduleDao.getScheduleForParse(classNumber, classroomId, subjectId,
                    subjectTypeId, numberOfWeek, dayOfWeek);

            if (schedules.size() == 1)
                scheduleId = schedules.get(0).getId();
            else
            {
                scheduleId = scheduleDao.Count() + 1;
                Schedule schedule = new Schedule(scheduleId, classNumber, classroomId, subjectId, subjectTypeId,
                        numberOfWeek, dayOfWeek);
                scheduleDao.merge(schedule);
            }

            UserSchedule userSchedule = new UserSchedule(chatId, scheduleId);
            scheduleDao.insertUserSchedule(userSchedule);
        }

        return check;
    }
}
