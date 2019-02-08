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

            if (jsonObject.get("dayOfWeek").toString().matches("[1-7]"))
                dayOfWeek = toIntExact((long)jsonObject.get("dayOfWeek"));
            else
            {
                check = false;
                break;
            }

            if (jsonObject.get("numberOfWeek").toString().matches("[1-7]{1,2}") ||
                    jsonObject.get("numberOfWeek").toString().equals("-2") ||
                    jsonObject.get("numberOfWeek").toString().equals("-1"))
                numberOfWeek = toIntExact((long)jsonObject.get("numberOfWeek"));
            else
            {
                check = false;
                break;
            }

            if (jsonObject.get("classNumber").toString().matches("[1-8]"))
                classNumber = toIntExact((long)jsonObject.get("classNumber"));
            else
            {
                check = false;
                break;
            }


            List<Teacher> teacher = teacherDao.getTeacherForCustomSchedule(jsonObject.get("teacherSurname").toString(),
                    jsonObject.get("teacherName").toString(), jsonObject.get("teacherSecondName").toString(),
                    jsonObject.get("teacherPhone").toString(), jsonObject.get("teacherMail").toString());
            if (teacher.size() == 1)
                teacherId = teacher.get(0).getId();
            else
            {
                teacherId = teacherDao.Count() + 1;
                teacherDao.Merge(teacherId, jsonObject.get("teacherName").toString(),
                        jsonObject.get("teacherSurname").toString(), jsonObject.get("teacherSecondName").toString(),
                        jsonObject.get("teacherPhone").toString(), jsonObject.get("teacherMail").toString());
            }

            List<Subject> subject = subjectDao.getSubjectForParse(jsonObject.get("subjectName").toString(), teacherId);
            if (subject.size() == 1)
                subjectId = subject.get(0).getId();
            else
            {
                subjectId = subjectDao.Count() + 1;
                subjectDao.Merge(subjectId, jsonObject.get("subjectName").toString(), teacherId);
            }

            List<SubjectType> subjectType = subjectTypeDao.getSubjectTypeForParse(jsonObject.get("subjectType").toString());
            if (subjectType.size() == 1)
                subjectTypeId = subjectType.get(0).getId();
            else
            {
                subjectTypeId = subjectTypeDao.Count() + 1;
                subjectTypeDao.Merge(subjectTypeId, subjectId, jsonObject.get("subjectType").toString());
            }

            List<Classroom> classroom = classroomDao.getClassroomForParse(jsonObject.get("classroom").toString());
            if (classroom.size() == 1)
                classroomId = classroom.get(0).getId();
            else
            {
                classroomId = classroomDao.Count() + 1;
                classroomDao.Merge(classroomId, jsonObject.get("classroom").toString(), null);
            }

            List<Schedule> schedules = scheduleDao.getScheduleForParse(classNumber, classroomId, subjectId,
                    subjectTypeId, numberOfWeek, dayOfWeek);

            if (schedules.size() == 1)
                scheduleId = schedules.get(0).getId();
            else
            {
                scheduleId = scheduleDao.Count() + 1;
                scheduleDao.Merge(scheduleId, classNumber, classroomId, subjectId, subjectTypeId, dayOfWeek,
                        numberOfWeek);
            }

            scheduleDao.Insert_User_Schedule(chatId, scheduleId);

        }

        return check;
    }

}
