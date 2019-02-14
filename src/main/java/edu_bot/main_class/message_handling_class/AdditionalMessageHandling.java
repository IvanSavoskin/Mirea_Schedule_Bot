package edu_bot.main_class.message_handling_class;

import edu_bot.db_class.dao.*;
import edu_bot.db_class.model.*;
import edu_bot.main_class.Bot;
import edu_bot.main_class.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AdditionalMessageHandling
{
    private final UserDao userDao;
    private final ScheduleDao scheduleDao;
    private final GroupDao groupDao;
    private final TeacherDao teacherDao;
    private final SubjectDao subjectDao;
    private final ClassTimeDao classTimeDao;
    private final EducationDateDao educationDateDao;
    private final Bot bot;

    @Autowired
    public AdditionalMessageHandling(UserDao userDao, ScheduleDao scheduleDao, GroupDao groupDao, TeacherDao teacherDao,
                                     ClassTimeDao classTime, EducationDateDao educationDateDao, SubjectDao subjectDao,
                                     @Lazy Bot bot)
    {
        this.userDao = userDao;
        this.scheduleDao = scheduleDao;
        this.groupDao = groupDao;
        this.teacherDao = teacherDao;
        this.subjectDao = subjectDao;
        this.classTimeDao = classTime;
        this.educationDateDao = educationDateDao;
        this.bot = bot;
    }

    /** Входит ли текущая дата в семестр */
    protected Boolean isSemester(Calendar c)
    {
        boolean isSemester = false;

        Date currentDate = c.getTime();

        EducationDate educationDate = educationDateDao.getEducationDate();
        Date semesterStartDate = educationDate.getSemesterStartDate();
        Date testSessionStartDate = educationDate.getTestSessionStartDate();

        if (testSessionStartDate != null && semesterStartDate != null && testSessionStartDate.after(semesterStartDate))
        {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(testSessionStartDate);
            c1.add(Calendar.DATE, -1);
            Date semesterStopDate = c1.getTime();

            if ((currentDate.after(semesterStartDate) || currentDate.equals(semesterStartDate)) &&
                    (currentDate.before(semesterStopDate) || currentDate.equals(semesterStopDate)))
                isSemester = true;
        }
        else if (semesterStartDate != null && (testSessionStartDate == null || testSessionStartDate.before(semesterStartDate)))
        {
            if (currentDate.after(semesterStartDate) || currentDate.equals(semesterStartDate) )
                isSemester = true;
        }

        return isSemester;
    }

    /** Входит ли текущая дата в зачетную сессию */
    protected Boolean isTestSession(Calendar c)
    {
        boolean isTestSession = false;

        EducationDate educationDate = educationDateDao.getEducationDate();
        Date testSessionStartDate = educationDate.getTestSessionStartDate();
        Date examSessionStartDate = educationDate.getExamSessionStartDate();

        Date currentDate = c.getTime();

        if (testSessionStartDate != null && examSessionStartDate != null && examSessionStartDate.after(testSessionStartDate))
        {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(examSessionStartDate);
            c1.add(Calendar.DATE, -1);
            Date testSessionStopDate = c1.getTime();

            if ((currentDate.after(testSessionStartDate) || currentDate.equals(testSessionStartDate)) &&
                    (currentDate.before(testSessionStopDate) || currentDate.equals(testSessionStopDate)))
                isTestSession = true;
        }
        else if (testSessionStartDate != null && (examSessionStartDate == null || examSessionStartDate.before(testSessionStartDate)))
        {
            if (currentDate.after(testSessionStartDate) || currentDate.equals(testSessionStartDate))
                isTestSession = true;
        }
        return isTestSession;
    }

    /** Входит ли текущая дата в экзаменационную сессию */
    protected Boolean isExamSession(Calendar c)
    {
        boolean isExamSession = false;

        Date currentDate = c.getTime();

        EducationDate educationDate = educationDateDao.getEducationDate();
        Date examSessionStartDate = educationDate.getExamSessionStartDate();
        Date examSessionStopDate = educationDate.getExamSessionStopDate();

        if (examSessionStartDate != null && examSessionStopDate != null && examSessionStopDate.after(examSessionStartDate))
        {
            if ((currentDate.after(examSessionStartDate) || currentDate.equals(examSessionStartDate)) &&
                    (currentDate.before(examSessionStopDate) || currentDate.equals(examSessionStopDate)))
                isExamSession = true;
        }
        else if (examSessionStartDate != null && (examSessionStopDate == null || examSessionStopDate.after(examSessionStartDate)))
        {
            if (currentDate.after(examSessionStartDate) || currentDate.equals(examSessionStartDate))
                isExamSession = true;
        }

        return isExamSession;
    }

    /** Вычисляет текущую неделю */
    protected Integer getCurrentWeek(Calendar c)
    {
        Integer currentWeek = null;

        if (isSemester(c))
        {
            Date semesterStartDate = educationDateDao.getEducationDate().getSemesterStartDate();

            int week = Integer.parseInt(new SimpleDateFormat( "ww", new
                    Locale("ru")).format(semesterStartDate));
            currentWeek = Math.abs(c.get(Calendar.WEEK_OF_YEAR) - week) + 1;
        }
        else if (isTestSession(c))
        {
            Date semesterStartDate = educationDateDao.getEducationDate().getTestSessionStartDate();

            int week = Integer.parseInt(new SimpleDateFormat( "ww", new
                    Locale("ru")).format(semesterStartDate));
            currentWeek = Math.abs(c.get(Calendar.WEEK_OF_YEAR) - week) + 1;
        }
        else if (isExamSession(c))
        {
            Date semesterStartDate = educationDateDao.getEducationDate().getExamSessionStartDate();

            int week = Integer.parseInt(new SimpleDateFormat( "ww", new
                    Locale("ru")).format(semesterStartDate));
            currentWeek = Math.abs(c.get(Calendar.WEEK_OF_YEAR) - week) + 1;
        }

        return currentWeek;
    }

    /** Отправка обратной связи админу */
    protected void sendFeedbackToAdmin(Message msg_final, String theme, String txt)
    {
        bot.sendMsgToAdmin("Пользователь с chatId " + msg_final.getChatId() +
                ", именем и фамилией " + msg_final.getFrom().getFirstName() + " " + msg_final.getFrom().getLastName()
                + ", логином " + msg_final.getFrom().getUserName() + " отправил вам сообщение по обратной связи\n\n"
                + "*!!!" + theme + "!!!*\n" + txt);
        bot.sendChatActionTyping(msg_final);
        bot.sendMsg(msg_final, "Спасибо за обратную связь, сообщение отправлено " +
                "разработчику", true);
        bot.sendChatActionTyping(msg_final);
        bot.sendFeedbackKeyboard(msg_final, false);
    }

    /** Отправка расписания по дате */
    protected void sendScheduleForDate(Message msg_final, Integer dayOfWeek, Integer numberOfWeek, String day)
    {
        User user = userDao.getUser(msg_final.getChatId());
        if ((user.getGroupId() != 0) && (user.getGroupId() != null))
        {
            int groupId = user.getGroupId();
            int newNumberOfWeek;
            if (numberOfWeek%2 == 0)
                newNumberOfWeek = -2;
            else
                newNumberOfWeek = -1;
            List<Schedule> schedules = scheduleDao.getSchedulesForGroupForDay(groupId, newNumberOfWeek,
                    dayOfWeek, msg_final.getChatId());
            if (schedules.size() == 0)
                schedules = scheduleDao.getSchedulesForGroupForDay(groupId, numberOfWeek, dayOfWeek, msg_final.getChatId());

            Main._Log.info("Отправка блока расписаний для группы " + groupDao.getGroup(groupId).getGroupName()
                    + " для дня недели " + day + ". Количество пар равно " + schedules.size() + "\n");

            if (schedules.size() > 0)
            {
                for (Schedule schedule: schedules)
                {
                    bot.sendChatActionTyping(msg_final);
                    Main._Log.info("Отправка " + (schedule.getClassNumber()) + " пары\n");
                    String subjectName = "";
                    String subjectType = "";
                    String className = "";
                    String surname = "";
                    String name = "";
                    String second_name = "";
                    String classTime;

                    if (schedule.getSubjectName() != null)
                        subjectName = schedule.getSubjectName() + " ";

                    if (schedule.getTypeName() != null)
                        subjectType = schedule.getTypeName() + " ";

                    if (schedule.getClassName() != null)
                        className = schedule.getClassName() + " ";

                    if (schedule.getSurname() != null)
                        surname = schedule.getSurname() + " ";

                    if (schedule.getName() != null)
                        name = schedule.getName() + " ";

                    if (schedule.getSecond_name() != null)
                        second_name = schedule.getSecond_name();

                    classTime = schedule.getClassNumber().toString() + " пара (" + schedule.getClassStart()
                            + "-" + schedule.getClassStop() + ")\n";

                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, classTime + subjectName + subjectType + className
                            + surname + name + second_name, false);
                }
            }
            else bot.sendMsg(msg_final, "Пары дома!", false);
        }
        else if (scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() != 0)
        {
            int newNumberOfWeek;
            if (numberOfWeek%2 == 0)
                newNumberOfWeek = -2;
            else
                newNumberOfWeek = -1;
            List<Schedule> schedules = scheduleDao.getSchedulesForUserForDay(msg_final.getChatId(), newNumberOfWeek,
                    dayOfWeek);
            if (schedules.size() == 0)
                schedules = scheduleDao.getSchedulesForUserForDay(msg_final.getChatId(), numberOfWeek, dayOfWeek);

            Main._Log.info("Отправка блока расписаний пользователя " + msg_final.getChat().getTitle()
                    + " для дня недели " + day + ". Количество пар равно " + schedules.size() + "\n");

            if (schedules.size() > 0)
            {
                for (Schedule schedule: schedules)
                {
                    bot.sendChatActionTyping(msg_final);
                    Main._Log.info("Отправка " + (schedule.getClassNumber()) + " пары\n");
                    String subjectName = "";
                    String subjectType = "";
                    String className = "";
                    String surname = "";
                    String name = "";
                    String second_name = "";
                    String classTime;

                    if (schedule.getSubjectName() != null)
                        subjectName = schedule.getSubjectName() + " ";

                    if (schedule.getTypeName() != null)
                        subjectType = schedule.getTypeName() + " ";

                    if (schedule.getClassName() != null)
                        className = schedule.getClassName() + " ";

                    if (schedule.getSurname() != null)
                        surname = schedule.getSurname() + " ";

                    if (schedule.getName() != null)
                        name = schedule.getName() + " ";

                    if (schedule.getSecond_name() != null)
                        second_name = schedule.getSecond_name();

                    classTime = schedule.getClassNumber().toString() + " пара (" + schedule.getClassStart()
                            + "-" + schedule.getClassStop() + ")\n";

                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, classTime + subjectName + subjectType + className
                            + surname + name + second_name, false);
                }
            }
            else bot.sendMsg(msg_final, "Пары дома!", false);
        }
        else if ((scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0) &&
                ((user.getGroupId() == 0) || (user.getGroupId() == null)))
        {
            bot.sendMsg(msg_final, "К вашему профилю не подключены расписания. " +
                    "Выберете группу в меню настроек или установите свое расписание", true);
        }
    }

    /** Подготовка к отправке расписания */
    protected void preparationScheduleSend(Message msg_final, Calendar c)
    {
        Date date = c.getTime();
        String writeDate = new SimpleDateFormat( "dd MMMM yyyy", new Locale("ru")).format(date);
        if (isSemester(c))
        {
            String day = new SimpleDateFormat( "EEEE", new Locale("ru")).format(date);
            int dayOfWeek = 7 - (8 - c.get(Calendar.DAY_OF_WEEK))%7;
            int numberOfWeek = getCurrentWeek(c);
            if (dayOfWeek == 7)
            {
                numberOfWeek = numberOfWeek - 1;
            }
            bot.sendMsg(msg_final, writeDate + "\n*" + firstUpperCase(day) + ", " + numberOfWeek +
                    " неделя*", false);
            bot.sendChatActionTyping(msg_final);
            sendScheduleForDate(msg_final, dayOfWeek, numberOfWeek, day);
        }
        else if (isTestSession(c))
        {
            bot.sendMsg(msg_final,  writeDate + "\nСейчас идет зачетная сессия", true);
        }
        else if (isExamSession(c))
        {

            bot.sendMsg(msg_final, writeDate + "\nСейчас идет экзаменационная сессия", true);
        }
        else
        {
            bot.sendMsg(msg_final,  writeDate + "\n*Сейчас каникулы!*", true);
        }
    }

    /** Отправка номера пары */
    protected Integer getClassNumber(String time)
    {
        Date timeStart = null;
        Date timeStop = null;
        Date date = null;
        int classNumber = 0;
        List<ClassTime> classTimes = classTimeDao.getClassTimes();
        for (int i = 1; i <= 8; i++)
        {

            String classStart = classTimes.get(i).getClassStart();
            String classStop = classTimes.get(i).getClassStop();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            try
            {
                timeStart = format.parse(classStart);
                timeStop = format.parse(classStop);
                date = format.parse(time);
            }
            catch (ParseException e)
            {
                Main._Log.warn("Время не переведено в формат дата:\n" + e.toString() + "\n");
            }
            if (timeStart != null && timeStop != null)
            {
                if (((date.after(timeStart)) || date.equals(timeStart)) && ((date.before(timeStop)) || date.equals(timeStop)))
                    classNumber = i;
            }
        }
        return classNumber;
    }

    /** Отправка расписания конкретной пары */
    protected void sendScheduleForTime(Message msg_final, Integer dayOfWeek, Integer numberOfWeek, Integer classNumber, String day)
    {

        User user = userDao.getUser(msg_final.getChatId());
        if ((user.getGroupId() != 0) && (user.getGroupId() != null))
        {
            Integer groupId = user.getGroupId();
            int newNumberOfWeek;
            if (numberOfWeek%2 == 0)
                newNumberOfWeek = -2;
            else
                newNumberOfWeek = -1;
            List<Schedule> schedules = scheduleDao.getSchedulesForGroupForDay(groupId, newNumberOfWeek, dayOfWeek,
                    msg_final.getChatId());
            if (schedules.size() == 0)
                schedules = scheduleDao.getSchedulesForGroupForDay(groupId, numberOfWeek, dayOfWeek, msg_final.getChatId());

            Main._Log.info("Отправка расписания " + classNumber + " пары для группы " + user.getGroupName()
                    + " для дня недели " + day + "\n");

            if (schedules.size() > 0)
            {
                Integer scheduleId = -1;
                for (Schedule schedule : schedules)
                {
                    if (classNumber.equals(schedule.getClassNumber()))
                    {
                        scheduleId = schedule.getId();
                    }
                }

                if (scheduleId != -1)
                {
                    String subjectName = "";
                    String subjectType = "";
                    String className = "";
                    String surname = "";
                    String name = "";
                    String second_name = "";
                    String classTime;

                    Schedule schedule = schedules.get(scheduleId);

                    if (schedule.getSubjectName() != null)
                        subjectName = schedule.getSubjectName() + " ";

                    if (schedule.getTypeName() != null)
                        subjectType = schedule.getTypeName() + " ";

                    if (schedule.getClassName() != null)
                        className = schedule.getClassName() + " ";

                    if (schedule.getSurname() != null)
                        surname = schedule.getSurname() + " ";

                    if (schedule.getName() != null)
                        name = schedule.getName() + " ";

                    if (schedule.getSecond_name() != null)
                        second_name = schedule.getSecond_name();

                    classTime = schedule.getClassNumber().toString() + " пара (" + schedule.getClassStart()
                            + "-" + schedule.getClassStop() + ")\n";

                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, classTime + subjectName + subjectType +
                            className + surname + name + second_name, true);
                }
                else bot.sendMsg(msg_final,  "Сегодня у вас нет " + classNumber + " пары!",
                        true);
            }
            else bot.sendMsg(msg_final,  "Сегодня у вас нет пар",
                    true);
        }
        else if (scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() != 0)
        {
            int newNumberOfWeek;
            if (numberOfWeek%2 == 0)
                newNumberOfWeek = -2;
            else
                newNumberOfWeek = -1;
            List<Schedule> schedules = scheduleDao.getSchedulesForUserForDay(msg_final.getChatId(),
                    newNumberOfWeek, dayOfWeek);
            if (schedules.size() == 0)
                schedules = scheduleDao.getSchedulesForUserForDay(msg_final.getChatId(), numberOfWeek, dayOfWeek);

            Main._Log.info("Отправка расписания " + classNumber + " пары пользовтеля "
                    + msg_final.getChat().getTitle() + " для дня недели " + day + "\n");

            if (schedules.size() > 0)
            {
                Integer scheduleId = -1;
                for (Schedule schedule : schedules)
                {
                    if (classNumber.equals(schedule.getClassNumber()))
                    {
                        scheduleId = schedule.getId();
                    }
                }

                if (scheduleId != -1)
                {
                    String subjectName = "";
                    String subjectType = "";
                    String className = "";
                    String surname = "";
                    String name = "";
                    String second_name = "";
                    String classTime;

                    Schedule schedule = schedules.get(scheduleId);

                    if (schedule.getSubjectName() != null)
                        subjectName = schedule.getSubjectName() + " ";

                    if (schedule.getTypeName() != null)
                        subjectType = schedule.getTypeName() + " ";

                    if (schedule.getClassName() != null)
                        className = schedule.getClassName() + " ";

                    if (schedule.getSurname() != null)
                        surname = schedule.getSurname() + " ";

                    if (schedule.getName() != null)
                        name = schedule.getName() + " ";

                    if (schedule.getSecond_name() != null)
                        second_name = schedule.getSecond_name();

                    classTime = schedule.getClassNumber().toString() + " пара (" + schedule.getClassStart()
                            + "-" + schedule.getClassStop() + ")\n";

                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, classTime + subjectName + subjectType +
                            className + surname + name + second_name, true);
                }
                else bot.sendMsg(msg_final,  "Сегодня у вас нет " + classNumber + " пары!",
                        true);
            }
            else bot.sendMsg(msg_final,  "Сегодня у вас нет пар",
                    true);
        }
        else if ((scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0) &&
                ((user.getGroupId() == 0) || (user.getGroupId() == null)))
        {
            bot.sendMsg(msg_final, "К вашему профилю не подключены расписания. " +
                    "Выберете группу в меню настроек или установите свое расписание", true);
        }
    }

    protected void preparationTeacherBySubjectSend(String txt, Message msg_final)
    {
        User user = userDao.getUser(msg_final.getChatId());
        List<Schedule> schedules = scheduleDao.getSchedulesForUser(user.getChatId());
        if (((user.getGroupId() != 0) && (user.getGroupId() != null)) || schedules.size() != 0)
        {
            List<Teacher> teachers = new ArrayList<>();

            if ((user.getGroupId() != 0) && (user.getGroupId() != null))
                teachers = teacherDao.getTeacherForSubject(txt);
            else if (schedules.size() != 0)
                teachers = teacherDao.getTeacherForSubjectForCustomSchedule(txt, user.getChatId());

            if (teachers.size() != 0)
            {
                if (teachers.size() == 1)
                {
                    Teacher teacher = teachers.get(0);
                    if (teacher.getSurname() == null || teacher.getSurname().equals(""))
                    {
                        bot.sendMsg(msg_final, "Преподавателей данного предмета не " +
                                "найдено, так как они не указаны в расписании", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendTeacherKeyboard(msg_final, false);
                        return;
                    }
                    else
                    {
                        getTeacherBySubject(txt, msg_final, teachers);
                        bot.sendTeacherKeyboard(msg_final, false);
                    }
                }
                getTeacherBySubject(txt, msg_final, teachers);
                bot.sendTeacherKeyboard(msg_final, false);
            }
            else
            {
                if (subjectDao.getSubjectForName(txt).size() == 0)
                {
                    bot.sendMsg(msg_final, "Не найденна введенная дисциплина. Возможно " +
                            "вы ошиблись в написании дисицплины или в базе нет информации о ней", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendTeacherKeyboard(msg_final, false);
                }
            }
        }
        else if ((scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0) &&
                ((user.getGroupId() == 0) || (user.getGroupId() == null)))
        {
            bot.sendMsg(msg_final, "К вашему профилю не подключены расписания. " +
                    "Выберете группу в меню настроек или установите свое расписание", true);
        }
    }

    protected void getTeacherBySubject(String txt, Message msg_final, List<Teacher> teachers)
    {
        Main._Log.info("Отправка преподавателей для дисциплины " + txt + "\n");

        if (teachers.size() > 0)
        {
            for (Teacher teacher: teachers)
            {
                String teacherName = "";
                String teacherSurname = "";
                String teacherSecond_Name = "";

                if (teacher.getSurname() != null && !teacher.getSurname().equals(""))
                    teacherSurname = teacher.getSurname() + " ";

                if (teacher.getName() != null && !teacher.getName().equals(""))
                    teacherName = teacher.getName() + " ";

                if (teacher.getSecond_name() != null && !teacher.getSecond_name().equals(""))
                    teacherSecond_Name = teacher.getSecond_name();

                bot.sendChatActionTyping(msg_final);
                if (!teacherSurname.equals(""))
                    bot.sendMsg(msg_final, teacherSurname + teacherName + teacherSecond_Name,
                            false);
            }
        }
        else
            bot.sendMsg(msg_final, "Преподаватели по данному предмету не найдено. " +
                    "Возможно вы ошиблись в написании ФИО преподавателя или в базе нет информации о нем", true);
    }

    protected void getTeacherBySurname(String txt, Message msg_final)
    {
        User user = userDao.getUser(msg_final.getChatId());
        List<Schedule> schedules = scheduleDao.getSchedulesForUser(user.getChatId());
        if (((user.getGroupId() != 0) && (user.getGroupId() != null)) || schedules.size() != 0)
        {
            List<Teacher> teachers = new ArrayList<>();

            if ((user.getGroupId() != 0) && (user.getGroupId() != null))
                teachers = teacherDao.getTeacherForSurname(txt);
            else if (schedules.size() != 0)
                teachers = teacherDao.getTeacherForSurnameForCustomSchedule(txt, user.getChatId());

            if ((teachers.size() > 0))
            {
                for (Teacher teacher: teachers)
                {
                    if (teacher.getName() != null)
                    {
                        String name = teacher.getName() + " ";

                        if (teacher.getSecond_name() != null)
                        {
                            String second_name = teacher.getSecond_name();
                            Main._Log.info("Получены имя: " + name + " и отчество: " + second_name +
                                    " преподавателя: " + txt + "\n");
                            bot.sendMsg(msg_final, txt + " " + name + second_name,
                                    false);
                        }
                        else
                        {
                            Main._Log.info("Получены имя: " + name + " преподавателя: " + txt + "\n");
                            bot.sendMsg(msg_final, txt + " " + name, false);
                        }
                    }
                }
            }
            else
            {
                bot.sendMsg(msg_final, "Преподаватель с такой фамилией не найден в базе. " +
                        "Пожалуйста повторите запрос.", true);
            }
        }
        else if ((scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0) &&
                ((user.getGroupId() == 0) || (user.getGroupId() == null)))
        {
            bot.sendMsg(msg_final, "К вашему профилю не подключены расписания. " +
                    "Выберете группу в меню настроек или установите свое расписание", true);
        }
    }

    protected void getTeacherInfo(String txt, Message msg_final)
    {
        List<Teacher> teachers = teacherDao.getTeacherForSurnameForCustomSchedule(txt, msg_final.getChatId());

        if ((teachers.size() > 0))
        {
            for (Teacher teacher : teachers)
            {
                if (teacher.getPhone_number() != null & !teacher.getPhone_number().trim().equals("") &
                        teacher.getMail() != null & !teacher.getMail().trim().equals(""))
                {
                    String name = "";
                    String second_name = "";
                    String phone_number = teacher.getPhone_number() + " ";
                    String mail = teacher.getMail() + " ";

                    if (teacher.getName() != null)
                    {
                        name = teacher.getName() + " ";
                    }
                    if (teacher.getSecond_name() != null)
                    {
                        second_name = teacher.getSecond_name() + " ";
                    }
                    bot.sendMsg(msg_final, txt + " " + name + second_name + phone_number +
                            mail, false);
                }
                else
                {
                    bot.sendMsg(msg_final, "У преподавателя " + txt + " нет " +
                            "информации о контактах", false);
                }
            }
        }
        else
        {
            bot.sendMsg(msg_final, "Преподаватель с такой фамилией не найден в базе. " +
                    "Пожалуйста повторите запрос.", true);
        }
    }


    protected String firstUpperCase(String word)
    {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    protected boolean checkDate(String date)
    {
        Pattern p = Pattern.compile("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d");
        Matcher m = p.matcher(date);
        return  m.matches();
    }

}

