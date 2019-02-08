package edu_bot.db_class.dao;

import edu_bot.db_class.model.Schedule;

import java.util.List;

public interface ScheduleDao
{
    Schedule getSchedule(Integer id);

    List<Schedule> getSchedules();

    List<Schedule> getScheduleForParse(Integer classNumber, Integer classroomId, Integer subjectId,
                                       Integer subjectTypeId, Integer numberOfWeek, Integer dayOfWeek);

    List<Schedule> getScheduleForDelete(Integer classNumber, Integer subjectId, Integer numberOfWeek,
                                        Integer dayOfWeek);

    List<Schedule> getSchedulesForUser(Long chatId);

    List<Schedule> getSchedulesForUserForDay(Long chatId, Integer numberOfWeek, Integer dayOfWeek);

    List<Schedule> getSchedulesForGroup(Integer groupId, Integer numberOfWeek, Integer dayOfWeek, Long chatId);

    List<Schedule> getGroupSchedules(Integer groupId);

    void Insert(Integer id, Integer classNumber, Integer classroomId, Integer subjectId, Integer subjectTypeId,
                Integer dayOfWeek, Integer numberOfWeek);

    void Merge(Integer id, Integer classNumber, Integer classroomId, Integer subjectId, Integer subjectTypeId,
               Integer dayOfWeek, Integer numberOfWeek);

    void Update(Integer id, Integer classNumber, Integer classroomId, Integer subjectId, Integer subjectTypeId,
                Integer dayOfWeek, Integer numberOfWeek);

    void Delete(Integer id);

    void DeleteAll();

    void DeleteUserSchedule(Long userId);

    void DeleteGroupSchedule(Integer groupId);

    Integer Count();

    void Insert_Group_Schedule(Integer groupId, Integer scheduleId);

    void Merge_Group_Schedule(Integer groupId, Integer scheduleId);

    void Insert_User_Schedule(Long userId, Integer scheduleId);

    void Merge_User_Schedule(Long userId, Integer scheduleId);

}
