package edu_bot.db_class.dao;

import edu_bot.db_class.model.GroupSchedule;
import edu_bot.db_class.model.Schedule;
import edu_bot.db_class.model.UserSchedule;

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

    List<Schedule> getSchedulesForGroup(Integer groupId);

    List<Schedule> getSchedulesForGroupForDay(Integer groupId, Integer numberOfWeek, Integer dayOfWeek, Long chatId);

    List<Schedule> getGroupSchedules(Integer groupId);

    void insert(Schedule schedule);

    void merge(Schedule schedule);

    void update(Schedule schedule);

    void insertGroupSchedule(GroupSchedule groupSchedule);

    void mergeGroupSchedule(GroupSchedule groupSchedule);

    void insertUserSchedule(UserSchedule userSchedule);

    void mergeUserSchedule(UserSchedule userSchedule);

    void delete(Integer id);

    void deleteAll();

    void deleteUserSchedule(Long userId);

    void deleteGroupSchedule(Integer groupId);

    Integer Count();



}
