package edu_bot.main_class.message_handling_class;

import edu_bot.additional_class.Emoji;
import edu_bot.additional_class.Sticker;
import edu_bot.db_class.dao.*;
import edu_bot.db_class.model.*;
import edu_bot.main_class.Bot;
import edu_bot.main_class.Main;
import edu_bot.schedule_class.JsonWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class MessageCheck
{
    private final UserDao userDao;
    private final ScheduleDao scheduleDao;
    private final GroupDao groupDao;
    private final TeacherDao teacherDao;
    private final ClassTimeDao classTimeDao;
    private final ClassroomDao classroomDao;
    private final SubjectDao subjectDao;
    private final SubjectTypeDao subjectTypeDao;
    private final EducationDateDao educationDateDao;
    private final AdminInfoDao adminInfoDao;
    private final JdbcTemplate jdbcTemplate;
    private final Bot bot;
    private final AdditionalMessageHandling additionalMessageHandling;
    private final JsonWorker jsonWorker;

    private final String addUserScheduleText = "Через точку с запятой (без пробела) введите номер дня недели " +
            "(Понедельник - 1, Вторник - 2 и т.д), номер недели (если пары проходят по всем четным неделям, то " +
            "напишите чет, если по всем нечетным, то нечет, если на разных неделях, то вводите по одной недели в " +
            "каждой строке расписания), номер пары, название предмета, фамилию преподавателя, имя преподавателя, " +
            "отчество преподавателя, телефон преподавателя, e-mail преподавателя тип пары (в удобном вам варианте), " +
            "аудиторию. Если что-то из этого вам неизвестно, то введите пробел. Если вы желаете закончить " +
            "составление расписания, то отправьте боту \"Стоп\". Если вы хотите отменить составление расписания, " +
            "то отправьте боту \"Отмена\"\n" +
            "Для примера, чтобы внести в свое расписание лекцию \"Русский язык\", которую ведет Иванов " +
            "Иван Иванович (его телефон и e-mail мы не знаем) и которая проходит по понедельникам на " +
            "четвертой неделе на 3 паре в аудитории А212, вам надо ввести строку: 1;4;3;Русский язык;" +
            "Иванов;Иван;Иванович; ; ;лк;А212\n";
    private final String refreshUserScheduleText = "Через точку с запятой (без пробела) введите номер дня недели " +
            "(Понедельник - 1, Вторник - 2 и т.д), номер недели (если пары проходят по всем четным неделям, то " +
            "напишите чет, если по всем нечетным, то нечет, если на разных неделях, то вводите по одной недели в " +
            "каждой строке расписания), номер пары, название предмета, фамилию преподавателя, имя преподавателя, " +
            "отчество преподавателя, телефон преподавателя, e-mail преподавателя тип пары (в удобном вам варианте), " +
            "аудиторию. Если что-то из этого вам неизвестно, то введите пробел. Если вы желаете закончить обновление " +
            "расписания, то отправьте боту \"Стоп\".\n" +
            "Для примера, чтобы внести в свое расписание лекцию \"Русский язык\", которую ведет Иванов " +
            "Иван Иванович (его телефон и e-mail мы не знаем) и которая проходит по понедельникам на " +
            "четвертой неделе на 3 паре в аудитории А212, вам надо ввести строку: 1;4;3;Русский язык;" +
            "Иванов;Иван;Иванович; ; ;лк;А212\n";

    @Autowired
    public MessageCheck(UserDao userDao, ScheduleDao scheduleDao, GroupDao groupDao, TeacherDao teacherDao,
                        ClassTimeDao classTime, SubjectDao subjectDao, SubjectTypeDao subjectTypeDao,
                        EducationDateDao educationDateDao, AdminInfoDao adminInfoDao, JdbcTemplate jdbcTemplate,
                        ClassroomDao classroomDao, Bot bot, AdditionalMessageHandling additionalMessageHandling,
                        JsonWorker jsonWorker)
    {
        this.userDao = userDao;
        this.scheduleDao = scheduleDao;
        this.groupDao = groupDao;
        this.teacherDao = teacherDao;
        this.classTimeDao = classTime;
        this.classroomDao = classroomDao;
        this.subjectDao = subjectDao;
        this.subjectTypeDao = subjectTypeDao;
        this.educationDateDao = educationDateDao;
        this.adminInfoDao = adminInfoDao;
        this.jdbcTemplate = jdbcTemplate;
        this.bot = bot;
        this.additionalMessageHandling = additionalMessageHandling;
        this.jsonWorker = jsonWorker;
    }

    /** Получение финального сообщения в зависимости от полученного */
    private Message unionMessage(Update update)
    {
        String txt;
        Message msg = update.getMessage();
        Message msg_update = update.getEditedMessage();
        Message msg_final = null;

        if (update.hasMessage() && update.getMessage().hasText())
        {
            txt = msg.getText();
            Main._Log.info("Пользователь " + msg.getFrom().getUserName() + " написал '" + txt + "'\n");
            msg_final = msg;
        }
        else if (update.hasEditedMessage() && msg_update.hasText())
        {
            txt = msg_update.getText();
            Main._Log.info("Пользователь " + msg_update.getFrom().getUserName() + " изменил сообщение на '"
                    + txt + "'\n");
            msg_final = msg_update;
        }
        return msg_final;
    }

    /** Проверка на регистрацию пользователя */
    public void checkUpdate (Update update, Message msg)
    {
        String txt = msg.getText();
        if (!msg.isReply() && (userDao.getUsersById(msg.getChatId()).size() != 0 || (txt.equals("/start")
                || txt.equals("/start@MireaSchedule_Bot") || txt.equals("/start@HappyEduBot"))))
        {
            Message msg_final = unionMessage(update);
            checkMessage(msg_final);
        }
        else if (msg.isReply() && (userDao.getUsersById(msg.getChatId()).size() != 0
                || (msg.getReplyToMessage().getText().contains(Emoji.Card_Index + "Регистрация:\n"))))
        {
            Message msg_final = unionMessage(update);
            checkReplyMessage(msg_final);
        }
        else if (userDao.getUsersById(msg.getChatId()).size() == 0 && !(txt.equals("/start")
                || txt.equals("/start@MireaSchedule_Bot") || txt.equals("/start@HappyEduBot")))
        {
            bot.sendMsg(msg, "К сожалению вы не зарегистрированы. Чтобы " +
                    "зарегистрироваться введите /start", true);
        }
    }

    /** Выполнение необходимого условия в зависимости от полученного сообщения */
    private void checkMessage(Message msg_final)
    {

        String txt = msg_final.getText();

        String noSchedule = "К вашему профилю не подключены расписания. Выберете группу в меню настроек " +
                "или установите свое расписание";

        if (txt.equals("/start") || txt.equals("/start@MireaSchedule_Bot") || txt.equals("/start@HappyEduBot"))
        {
            bot.sendChatActionTyping(msg_final);
            if (userDao.getUsersById(msg_final.getChatId()).size() == 0)
            {

                bot.sendMsgReply(msg_final, Emoji.Card_Index + "Регистрация:\n*Приветствую!*\n" +
                        "Хотите ли вы сразу добавить группу, расписание которой будете получать в дальнейшем?\n" +
                        "Если да, то введите группу в формате XXXX-YY-YY (X - буквы, Y - цифры),\n" +
                        "если нет, то введите в ответ слово \"Нет\"");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы уже зарегистрированы!", true);
                bot.sendStartKeyboard(msg_final, false);
            }

        }
        else if (txt.equals("/stop") || txt.equals("/stop@MireaSchedule_Bot") || txt.equals("/stop@HappyEduBot"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsgReply(msg_final, "Вы уверены, что хотите отключиться от бота? Если вы это сделаете, " +
                    "то ваши данные удаляться. В ответ пришлите *Да* или *Нет*");
        }
        else if (txt.equals(Emoji.Left_Arrow + "Выход  из админки") || txt.equals(Emoji.Back_Arrow + "Назад"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendStartKeyboard(msg_final, true);
        }
        else if (txt.equals("/bot") || txt.equals("/bot@MireaSchedule_Bot") || txt.equals("/bot@HappyEduBot"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendStartKeyboard(msg_final, true);
        }
        else if (txt.equals("/help") || txt.equals(Emoji.Question_Mark + "Помощь") ||
                txt.equals("/help@MireaSchedule_Bot") || txt.equals("/help@HappyEduBot") ||
                txt.equals(Emoji.Left_Arrow + "Назад к помощи"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendHelpKeyboard(msg_final, true);
        }
        else if (txt.equals("/settings") || txt.equals(Emoji.Gear + "Настройки") ||
                txt.equals("/settings@MireaSchedule_Bot") || txt.equals("/settings@HappyEduBot") ||
                txt.equals(Emoji.Left_Arrow + "Назад к настройкам"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendSettingsKeyboard(msg_final, true);
        }
        else if (txt.equals("/admin_panel/"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendAdminKeyboard(msg_final, true);
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }

        }
        else if (txt.equals(Emoji.Keyboard + "Добавить админа"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Keyboard + "Добавить админа: Введите " +
                        "пароль для выполнения команды");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }

        }
        else if (txt.equals(Emoji.Keyboard + "Удалить админа"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Keyboard + "Удалить админа: Введите " +
                        "пароль для выполнения команды");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }

        }
        else if (txt.equals(Emoji.Keyboard + "Обновить пароль"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Keyboard + "Обновить пароль: Введите " +
                        "старый пароль");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }

        }
        else if (txt.equals(Emoji.Keyboard + "Лог"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Лог*: Введите пароль для " +
                        "выполнения команды");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }
        }
        else if (txt.equals(Emoji.Keyboard + "Запрос"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Запрос*: Введите пароль " +
                        "для выполнения команды");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }
        }
        else if (txt.equals(Emoji.Incoming_Envelope + "Отправить сообщение пользователю"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope + "*Отправить сообщение " +
                        "пользователю*: Введите пароль для выполнения команды");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }
        }
        else if (txt.equals(Emoji.Incoming_Envelope + "Отправить сообщение всем пользователям"))
        {
            bot.sendChatActionTyping(msg_final);
            if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
            {
                bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope + "*Отправить сообщение " +
                        "всем пользователям*: Введите пароль для выполнения команды");
            }
            else
            {
                bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                bot.sendStartKeyboard(msg_final, false);
            }
        }
        else if (txt.equals(Emoji.Play_Button + "Команды") || txt.equals(Emoji.Left_Arrow + "Назад к командам"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendCommandKeyboard(msg_final, true);
        }
        else if (txt.equals("/close") || txt.equals(Emoji.Cross_Mark + "Закрыть"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendHideKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.Page_With_Curl + "Расписание"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendScheduleKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.World_Map + "Пара"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendClassKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.Man_Student + "Преподаватель"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendTeacherKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.Watch + "Время"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendTimeKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.Wrench + "Текущая группа"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedule = scheduleDao.getSchedulesForUser(msg_final.getChatId());
            int size = schedule.size();
            bot.sendChatActionTyping(msg_final);
            if (groupId != -1 && size == 0)
                bot.sendMsg(msg_final, user.getGroupName(), true);
            else if (size > 0 && groupId == -1)
                bot.sendMsg(msg_final,"У вас установлено пользовательское расписание", true);
            else if (size == 0 && groupId == -1)
                bot.sendMsg(msg_final,"У вас еще не установлена группа!", true);
        }
        else if (txt.equals(Emoji.Man + "Имя"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsgReply(msg_final,Emoji.Man + "Имя: Введите фамилию преподавателя " +
                    "(с большой буквы)");
        }
        else if (txt.equals(Emoji.Wrench + "Установить группу"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedule = scheduleDao.getSchedulesForUser(msg_final.getChatId());
            int size = schedule.size();
            bot.sendChatActionTyping(msg_final);
            if (size == 0 && groupId == -1)
                bot.sendMsgReply(msg_final, Emoji.Wrench + "Установить группу: " +
                        "Введите группу в формате XXXX-YY-YY (X - буквы, Y - цифры)");
            else if (groupId != -1 && size == 0)
                bot.sendMsgReply(msg_final, Emoji.Wrench + "Установить группу: У вас уже " +
                        "установлена группа " +
                        groupDao.getGroup(userDao.getUser(msg_final.getChatId()).getGroupId()).getGroupName() +
                        ". Вы точно хотите сменить группу? (В ответ пришлите \"Да\", если хотите сменить " +
                        "группу и \"Нет\", если хотите оставить ее)");
            else if (size > 0 && groupId == -1)
                bot.sendMsgReply(msg_final, Emoji.Wrench + "Установить группу: У вас " +
                        "установлено пользовательское расписание, вы уверены, что хотите его удалить и установить " +
                        "расписание группы? (В ответ пришлите \"Да\", если хотите удалить расписание и \"Нет\", " +
                        "если хотите оставить его)");
        }
        else if (txt.equals(Emoji.Wrench + "Удалить группу"))
        {
            bot.sendChatActionTyping(msg_final);
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedule = scheduleDao.getSchedulesForUser(msg_final.getChatId());
            int size = schedule.size();
            if (size == 0 && groupId == -1)
            {
                bot.sendMsg(msg_final,"Вы не привязаны к какой-либо группе", true);
                bot.sendSettingsKeyboard(msg_final, false);
            }
            else if (groupId != -1 && size == 0)
            {
                bot.sendMsgReply(msg_final, Emoji.Wrench + "Удалить группу: У вас " +
                        "установлена группа " + user.getGroupName() + ". Вы точно хотите удалить группу? (В ответ" +
                        " пришлите \"Да\", если хотите удалить группу и \"Нет\", если хотите оставить ее)");
            }
            else if (size > 0 && groupId == -1)
            {
                bot.sendMsgReply(msg_final,Emoji.Wrench + "Установить группу: У вас " +
                        "установлено пользовательское расписание. Для того, чтобы удалить группу, необходимо ее " +
                        "сначала прикрепить к своему профилю");
            }
        }
        else if (txt.equals(Emoji.Open_Book + "Сегодня"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            bot.sendChatActionTyping(msg_final);
            List<Schedule> schedules = scheduleDao.getSchedulesForUser(msg_final.getChatId());
            int size = schedules.size();
            if (size == 0 && groupId == -1)
            {
                bot.sendMsg(msg_final, noSchedule, true);
            }
            else
            {
                Calendar c = Calendar.getInstance();
                additionalMessageHandling.preparationScheduleSend(msg_final, c);
            }
        }
        else if (txt.equals(Emoji.Green_Book + "Завтра"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            int groupId = user.getGroupId();
            bot.sendChatActionTyping(msg_final);
            if (scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0 && groupId == -1)
            {
                bot.sendMsg(msg_final, noSchedule, true);
            }
            else
            {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 1);
                additionalMessageHandling.preparationScheduleSend(msg_final, c);
            }
        }
        else if (txt.equals(Emoji.Orange_Book + "Дата"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            int groupId = user.getGroupId();
            bot.sendChatActionTyping(msg_final);
            if ((scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0) && groupId == -1)
            {
                bot.sendMsg(msg_final, noSchedule, true);
            }
            else
            {
                bot.sendMsgReply(msg_final, "Введите дату в формате дд.мм.гггг");
            }
        }
        else if (txt.equals(Emoji.Paperclip + "Файл расписания"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            int groupId = user.getGroupId();
            List<Schedule>schedules = scheduleDao.getSchedulesForUser(msg_final.getChatId());
            boolean haveCustomSchedule = false;
            if (schedules.size() > 0)
                haveCustomSchedule = true;

            bot.sendChatActionTyping(msg_final);
            if (!haveCustomSchedule && groupId == -1)
            {
                bot.sendMsg(msg_final, noSchedule, true);
            }
            else if (!haveCustomSchedule && groupId != -1)
            {
                String fileName = groupDao.getGroup(user.getGroupId()).getFileName();
                try
                {
                    FileInputStream schedule = new FileInputStream("./schedule/" + fileName);
                    bot.sendDocument(msg_final, fileName, schedule);
                }
                catch (FileNotFoundException e)
                {
                    Main._Log.warn("Не удалось отправить файл расписания:\n" + e);
                    bot.sendMsg(msg_final, "К сожалению в данный момент невозможно скачать " +
                                    "данное расписание. Возможно оно уже не используется или не еще не прикреплено к группе",
                            true);
                }
            }
            else if (haveCustomSchedule && groupId == -1)
            {
                bot.sendChatActionDocument(msg_final);
                bot.sendDocument(msg_final, "Расписание.json", jsonWorker.createJson(schedules));
            }
        }
        else if (txt.equals(Emoji.Closed_Book + "Неделя"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            int groupId = user.getGroupId();

            if ((scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() == 0) && groupId == -1)
            {
                bot.sendMsg(msg_final, noSchedule, true);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);

                Calendar c = Calendar.getInstance();

                for (int i = 1; i <= 7; i++)
                {
                    additionalMessageHandling.preparationScheduleSend(msg_final, c);
                    c.add(Calendar.DATE, 1);
                }
            }
        }
        else if (txt.equals(Emoji.Six_O_clock + "Пары"))
        {
            bot.sendChatActionTyping(msg_final);
            List<ClassTime> classTimes = classTimeDao.getClassTimes();
            for (ClassTime classTime : classTimes)
            {
                bot.sendMsg(msg_final, classTime.getClassNumber().toString() + " пара "
                        + classTime.getClassStart() + "-" + classTime.getClassStop() + "\n", false);
            }
        }
        else if (txt.equals(Emoji.Pen + "Текущая"))
        {
            bot.sendChatActionTyping(msg_final);
            Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            String time = new SimpleDateFormat( "HH:mm", new Locale("ru")).format(date);
            Integer classNumber = additionalMessageHandling.getClassNumber(time);
            String writeDate = new SimpleDateFormat( "dd MMMM yyyy", new Locale("ru")).format(date);
            String day = new SimpleDateFormat( "EEEE", new Locale("ru")).format(date);
            Integer numberOfWeek = additionalMessageHandling.getCurrentWeek(c);
            if (additionalMessageHandling.isSemester(c))
            {
                if (classNumber == 0)
                {
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, writeDate + "\n*" + additionalMessageHandling.firstUpperCase(day) + ", "
                            + numberOfWeek + " неделя*\n\n" + "Сегодня уже нет пар!", true);
                }
                else if (classNumber >= 1 && classNumber <= 8)
                {
                    additionalMessageHandling.sendScheduleForTime(msg_final, classNumber, c);
                }
                else if (classNumber >= -7 && classNumber <= -1)
                {
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, writeDate + "\n*" + additionalMessageHandling.firstUpperCase(day) + ", "
                                    + numberOfWeek + " неделя*\n\n" + "Сейчас перерыв после " + -1*classNumber + " пары",
                            true);
                }
            }
            else if (additionalMessageHandling.isTestSession(c))
            {
                bot.sendMsg(msg_final, writeDate + "Сейчас идет зачетная сессия", true);
            }
            else if (additionalMessageHandling.isExamSession(c))
            {
                bot.sendMsg(msg_final, writeDate  + "Сейчас идет экзаменационная сессия", true);
            }
            else
                bot.sendMsg(msg_final, writeDate + "*Сейчас каникулы!*", true);
        }
        else if (txt.equals(Emoji.Pencil + "Следующая"))
        {
            bot.sendChatActionTyping(msg_final);
            Calendar c = Calendar.getInstance();
            Date date = c.getTime();
            String time = new SimpleDateFormat( "HH:mm", new Locale("ru")).format(date);
            Integer classNumber = additionalMessageHandling.getClassNumber(time);
            String writeDate = new SimpleDateFormat( "dd MMMM yyyy", new Locale("ru")).format(date);

            if (additionalMessageHandling.isSemester(c))
            {
                if (classNumber == 0 || classNumber == 8)
                {
                    c.add(Calendar.DATE, 1);
                    classNumber = 1;
                    additionalMessageHandling.sendScheduleForTime(msg_final, classNumber, c);
                }
                else if (classNumber >= 1 && classNumber <= 7)
                {
                    classNumber++;
                    additionalMessageHandling.sendScheduleForTime(msg_final, classNumber, c);
                }
                else if (classNumber >= -7 && classNumber <= -1)
                {
                    classNumber = -1*classNumber + 1;
                    additionalMessageHandling.sendScheduleForTime(msg_final, classNumber, c);
                }
            }
            else if (additionalMessageHandling.isTestSession(c))
            {
                bot.sendMsg(msg_final,writeDate + "Сейчас идет зачетная сессия", true);
            }
            else if (additionalMessageHandling.isExamSession(c))
            {
                bot.sendMsg(msg_final, writeDate + "Сейчас идет экзаменационная сессия", true);
            }
            else
                bot.sendMsg(msg_final, writeDate + "*Сейчас каникулы!*", true);

        }
        else if (txt.equals(Emoji.Fountain_Pen + "Определенная"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsgReply(msg_final, "Введите номер пары");
        }
        else if (txt.equals(Emoji.Three_O_clock + "Неделя"))
        {
            bot.sendChatActionTyping(msg_final);
            Calendar c = Calendar.getInstance();
            if (additionalMessageHandling.isSemester(c))
            {
                bot.sendMsg(msg_final, "Сейчас идет " +
                        additionalMessageHandling.getCurrentWeek(c).toString() +
                        " неделя", true);
            }
            else if (additionalMessageHandling.isTestSession(c))
            {
                bot.sendMsg(msg_final, "Сейчас идет зачетная сессия", true);
            }
            else if (additionalMessageHandling.isExamSession(c))
            {
                bot.sendMsg(msg_final, "Сейчас идет экзаменационная сессия", true);
            }
            else
                bot.sendMsg(msg_final, "*Сейчас каникулы!*", true);
        }
        else if (txt.equals(Emoji.Twelve_O_clock + "Сессия"))
        {
            bot.sendChatActionTyping(msg_final);
            Calendar c = Calendar.getInstance();

            Date currentDate = c.getTime();
            if (additionalMessageHandling.isSemester(c))
            {
                EducationDate educationDate = educationDateDao.getEducationDate();
                Date testSessionStartDate = educationDate.getTestSessionStartDate();
                Date examSessionStartDate = educationDate.getExamSessionStartDate();
                if (testSessionStartDate != null && examSessionStartDate != null && testSessionStartDate.after(currentDate)
                        && examSessionStartDate.after(currentDate))
                {
                    int differenceTestSession = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                            - (int)(testSessionStartDate.getTime()/(24*60*60*1000)))) + 1;
                    int differenceExamSession = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                            - (int)(examSessionStartDate.getTime()/(24*60*60*1000)))) + 1;

                    if (differenceTestSession%10 == 1 && differenceTestSession%100 != 11)
                        bot.sendMsg(msg_final, "До зачетной сессии " +
                                differenceTestSession + " день", false);
                    else if ((differenceTestSession%10 == 2 || differenceTestSession%10 == 3
                            || differenceTestSession%10 == 4) && (differenceTestSession%100 != 12
                            && differenceTestSession%100 != 13 && differenceTestSession%100 != 14))
                        bot.sendMsg(msg_final, "До зачетной сессии " +
                                differenceTestSession + " дня" , false);
                    else
                        bot.sendMsg(msg_final, "До зачетной сессии " +
                                differenceTestSession + " дней", false);

                    bot.sendChatActionTyping(msg_final);

                    if (differenceExamSession%10 == 1 && differenceExamSession%100 != 11)
                        bot.sendMsg(msg_final, "До экзаменационной сессии " +
                                differenceExamSession + " день", false);
                    else if ((differenceExamSession%10 == 2 || differenceExamSession%10 == 3
                            || differenceExamSession%10 == 4) && (differenceExamSession%100 != 12
                            && differenceExamSession%100 != 13 && differenceExamSession%100 != 14))
                        bot.sendMsg(msg_final, "До экзаменационной сессии " +
                                differenceExamSession + " дня" , false);
                    else
                        bot.sendMsg(msg_final, "До экзаменационной сессии " +
                                differenceExamSession + " дней", false);
                }
                else if (testSessionStartDate != null && testSessionStartDate.after(currentDate)
                        && examSessionStartDate == null)
                {
                    int differenceTestSession = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                            - (int)(testSessionStartDate.getTime()/(24*60*60*1000)))) + 1;
                    if (differenceTestSession%10 == 1 && differenceTestSession%100 != 11)
                        bot.sendMsg(msg_final, "До зачетной сессии " +
                                differenceTestSession + " день", false);
                    else if ((differenceTestSession%10 == 2 || differenceTestSession%10 == 3
                            || differenceTestSession%10 == 4) && (differenceTestSession%100 != 12
                            && differenceTestSession%100 != 13 && differenceTestSession%100 != 14))
                        bot.sendMsg(msg_final, "До зачетной сессии " +
                                differenceTestSession + " дня" , false);
                    else
                        bot.sendMsg(msg_final, "До зачетной сессии " +
                                differenceTestSession + " дней", false);

                    bot.sendChatActionTyping(msg_final);

                    bot.sendMsg(msg_final, "Дата начала экзаменационной сессии еще " +
                            "не известна", false);
                }
                else if (testSessionStartDate == null && examSessionStartDate != null && examSessionStartDate.after(currentDate))
                {
                    bot.sendMsg(msg_final, "Дата начала зачетной сессии еще не известна",
                            false);

                    bot.sendChatActionTyping(msg_final);

                    int differenceExamSession = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                            - (int)(examSessionStartDate.getTime()/(24*60*60*1000)))) + 1;
                    if (differenceExamSession%10 == 1 && differenceExamSession%100 != 11)
                        bot.sendMsg(msg_final, "До экзаменационной сессии " +
                                differenceExamSession + " день", false);
                    else if ((differenceExamSession%10 == 2 || differenceExamSession%10 == 3
                            || differenceExamSession%10 == 4) && (differenceExamSession%100 != 12
                            && differenceExamSession%100 != 13 && differenceExamSession%100 != 14))
                        bot.sendMsg(msg_final, "До экзаменационной сессии " +
                                differenceExamSession + " дня" , false);
                    else
                        bot.sendMsg(msg_final, "До экзаменационной сессии " +
                                differenceExamSession + " дней", false);
                }
                else
                    bot.sendMsg(msg_final, "Даты начала зачетной и экзаменационной " +
                            "сессии еще не известны", true);

            }
            else if (additionalMessageHandling.isTestSession(c))
            {
                Date examSessionStartDate = educationDateDao.getEducationDate().getExamSessionStartDate();

                if (examSessionStartDate != null)
                {
                    int differenceSession = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                            - (int)(examSessionStartDate.getTime()/(24*60*60*1000)))) + 1;
                    if (differenceSession%10 == 1 && differenceSession%100 != 11)
                        bot.sendMsg(msg_final, "Идет зачетная сессия. До " +
                                "экзаменационной сессии " + differenceSession + " день", false);
                    else if ((differenceSession%10 == 2 || differenceSession%10 == 3
                            || differenceSession%10 == 4) && (differenceSession%100 != 12
                            && differenceSession%100 != 13 && differenceSession%100 != 14))
                        bot.sendMsg(msg_final, "Идет зачетная сессия. До " +
                                "экзаменационной сессии " + differenceSession + " дня" ,false);
                    else
                        bot.sendMsg(msg_final, "Идет зачетная сессия. До " +
                                "экзаменационной сессии " + differenceSession + " дней", false);
                }
                else
                    bot.sendMsg(msg_final, "Идет зачетная сессия. Дата начала " +
                                    "экзаменационной сессии еще неизвестна",
                            true);

            }
            else if (additionalMessageHandling.isExamSession(c))
            {
                Date examSessionStopDate = educationDateDao.getEducationDate().getExamSessionStopDate();

                if (examSessionStopDate != null)
                {
                    int differenceSession = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                            - (int)(examSessionStopDate.getTime()/(24*60*60*1000)))) + 1;
                    if (differenceSession%10 == 1 && differenceSession%100 != 11)
                        bot.sendMsg(msg_final, "Идет экзаменационная сессия. До конца " +
                                "экзаменационной сессии " + differenceSession + " день", false);
                    else if ((differenceSession%10 == 2 || differenceSession%10 == 3
                            || differenceSession%10 == 4) && (differenceSession%100 != 12
                            && differenceSession%100 != 13 && differenceSession%100 != 14))
                        bot.sendMsg(msg_final, "Идет экзаменационная сессия. До конца " +
                                "экзаменационной сессии " + differenceSession + " дня" ,false);
                    else
                        bot.sendMsg(msg_final, "Идет экзаменационная сессия. До конца " +
                                "экзаменационной сессии " + differenceSession + " дней", false);
                }
                else
                    bot.sendMsg(msg_final, "Идет экзаменационная сессия. Дата окнчания " +
                            "экзаменационной сессии еще неизвестна", false);
            }
            else
            {
                Date semesterStartDate;
                try
                {
                    semesterStartDate = new SimpleDateFormat("dd.MM.yyyy").parse("01.09." +
                            c.get(Calendar.YEAR));
                }
                catch (ParseException e)
                {
                    Main._Log.info("Не удалось поместить дату начала семестра:\n" + e);
                    return;
                }
                int difference = Math.abs((int)((currentDate.getTime()/(24*60*60*1000))
                        - (int)(semesterStartDate.getTime()/(24*60*60*1000)))) + 1;
                if (difference%10 == 1 && difference%100 != 11)
                    bot.sendMsg(msg_final, "*Каникулы!!!*. До начала семестра " +
                            difference + " день", false);
                else if ((difference%10 == 2 || difference%10 == 3
                        || difference%10 == 4) && (difference%100 != 12
                        && difference%100 != 13 && difference%100 != 14))
                    bot.sendMsg(msg_final, "*Каникулы!!!*. До начала семестра " +
                            difference + " дня" , false);
                else
                    bot.sendMsg(msg_final, "*Каникулы!!!*. До начала семестра " +
                            difference + " дней", false);
            }
        }
        else if (txt.equals(Emoji.Man_Pouting + "Дисциплина"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsgReply(msg_final, "Введите название дисциплины (как в расписании)");
        }
        else if (txt.equals(Emoji.Exclamation_Question_Mark + "О боте"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsg(msg_final, "Вам нужно узнать расписание пар в МИРЭА? *Вам сюда!* " +
                    "Вам нужно узнать, какая сейчас пара?? *Вам сюда!!* Не знаете стоит ли начать готовиться к сессии, " +
                    "потому что не помните, когда она начинается??? *Вам сюда!!!* Все это и многое другое позволяет " +
                    "наш бот.\n Мы стремимся сделать его лучшим помощником при обучении в МИРЭА и надеямся, что " +
                    "вы получите только положительные эмоции при работе с ним!", true);
        }
        else if (txt.equals(Emoji.Scroll + "Свое расписание"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendCustomScheduleKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.Incoming_Envelope + "Обратная связь"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendFeedbackKeyboard(msg_final, true);
        }
        else if (txt.equals(Emoji.Incoming_Envelope + "Ошибка") ||
                txt.equals(Emoji.Incoming_Envelope + "Предложение") ||
                txt.equals(Emoji.Incoming_Envelope + "Другое"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsgReply(msg_final, txt + ": Введите текст сообщения");
        }
        else if (txt.equals(Emoji.Scroll + "Удалить расписание"))
        {
            bot.sendChatActionTyping(msg_final);
            if(scheduleDao.getSchedulesForUser(msg_final.getChatId()).size() > 0)
            {
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Удалить расписание: Вы уверены, что " +
                        "хотите удалить свое расписание? (В ответ напишите \"Да\" или \"Нет\")");
            }
            else
            {
                bot.sendMsg(msg_final, Emoji.Scroll + "Удалить расписание: К сожалению у вас еще не установлено" +
                        " пользовательское распсиание", true);
            }
        }
        else if (txt.equals(Emoji.Scroll + "Создать расписание"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedule = scheduleDao.getSchedulesForUser(msg_final.getChatId());

            bot.sendChatActionTyping(msg_final);
            if (groupId == -1 && schedule.size() == 0)
            {
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: " + addUserScheduleText);
            }
            else if (groupId != -1)
            {
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: У вас " +
                        "установлена группа. Вы точно хотите ее удалить? В ответ напишите \"Да\" или \"Нет\"");
            }
            else if (schedule.size() != 0)
            {
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: У вас уже " +
                        "установлено свое расписание. Вы точно хотите его удалить и создать новое? " +
                        "В ответ напишите \"Да\" или \"Нет\"");
            }
        }
        else if (txt.equals(Emoji.Scroll + "Обновить расписание"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedule = scheduleDao.getSchedulesForUser(msg_final.getChatId());

            bot.sendChatActionTyping(msg_final);
            if (groupId == -1 && schedule.size() == 0)
            {
                bot.sendMsg(msg_final, Emoji.Scroll + "Обновить расписание: К " +
                        "сожалению вы еще не создали свое распсиание", true);
                bot.sendChatActionTyping(msg_final);
            }
            else if (groupId != -1)
            {
                bot.sendMsg(msg_final, Emoji.Scroll + "Обновить расписание: К " +
                        "сожалению вы еще не создали свое распсиание", true);
                bot.sendChatActionTyping(msg_final);
            }
            else if (schedule.size() != 0)
            {
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Обновить расписание: " + refreshUserScheduleText);
            }
        }
        else if (txt.equals(Emoji.Memo + "Контакты"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedule = scheduleDao.getSchedulesForUser(msg_final.getChatId());

            bot.sendChatActionTyping(msg_final);
            if (groupId != -1)
            {
                bot.sendMsg(msg_final, "К сожалению данная функция доступна только для " +
                        "пользователей создавших свое расписание", true);
            }
            else if (schedule.size() == 0 && groupId == -1)
            {
                bot.sendMsg(msg_final, "К вашему профилю не подключены расписания. Данная функция доступна " +
                                "только для пользовательских расписаний. Вы можете установить свое расписание в меню настроек",
                        true);
            }
            else if (schedule.size() != 0)
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Memo + "Контакты: Введите фамилию преподавателя");
            }
        }
        else if (txt.equals(Emoji.Scroll + "Сохранить расписание"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            List<Schedule> schedules = scheduleDao.getSchedulesForUser(msg_final.getChatId());

            bot.sendChatActionTyping(msg_final);
            if (schedules.size() == 0 && groupId != -1)
            {
                schedules = scheduleDao.getSchedulesForGroup(groupId);
                bot.sendChatActionTyping(msg_final);
                bot.sendDocument(msg_final, "Расписание.json", jsonWorker.createJson(schedules));
            }
            else if (schedules.size() > 0)
            {
                bot.sendChatActionDocument(msg_final);
                bot.sendDocument(msg_final, "Расписание.json", jsonWorker.createJson(schedules));
            }
            else
            {
                bot.sendChatActionDocument(msg_final);
                bot.sendMsg(msg_final, noSchedule, true);
            }
        }
        else if (txt.equals(Emoji.Scroll + "Загрузить расписание"))
        {
            List<Schedule> schedules = scheduleDao.getSchedulesForUser(msg_final.getChatId());
            User user = userDao.getUser(msg_final.getChatId());
            Integer groupId = user.getGroupId();
            if (schedules.size() > 0)
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: У вас уже " +
                        "установлено свое расписание. Вы точно хотите его удалить и загрузить новое из файла? " +
                        "В ответ напишите \"Да\" или \"Нет\"");
            }
            else if (schedules.size() == 0 && groupId != -1)
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: У вас уже " +
                        "установлено расписание группы. Вы точно хотите его удалить и загрузить новое из файла? " +
                        "В ответ напишите \"Да\" или \"Нет\"");
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Отправьте в ответ json файл с " +
                        "пользовательским расписанием");
            }
        }
        else if (txt.equals(Emoji.Crayon + "Аудитория") || txt.equals(Emoji.Question_Mark + "Помощь по командам") ||
                txt.equals(Emoji.White_Question_Mark + "Помощь по настройкам"))
        {
            bot.sendChatActionTyping(msg_final);
            String sorryText = "К сожалению, данная функция пока *не готова*.\n" +
                    "Но она станет доступна уже совсем *скоро*\n" +
                    "Ждите следующих обновлений";
            bot.sendMsg(msg_final, sorryText, true);
            bot.sendSticker(msg_final, Sticker.Cat_Sorry.toString());
        }
    }

    /** Выполнение необходимого условия в зависимости от полученного ответа на сообщение бота */
    private void checkReplyMessage(Message msg_final)
    {
        String txt = msg_final.getText();
        String replyTxt = msg_final.getReplyToMessage().getText();

        if (replyTxt.contains("Вы уверены, что хотите отключиться от бота?"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case "да":
                    userDao.delete(msg_final.getChatId());
                    bot.sendMsg(msg_final, "Вы успешно отключены от бота. Ваши данные удалены. Больше вам не " +
                            "будут приходить уведомления", true);
                    break;
                case "нет":
                    bot.sendMsg(msg_final, "Вы не стали отключаться от бота", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendStartKeyboard(msg_final, false);
                    break;
                default:
                    bot.sendMsgReply(msg_final,"Вы ввели некорректный ответ. Вы уверены, что хотите " +
                            "отключиться от бота? Если вы это сделаете, то ваши данные удаляться. В ответ пришлите " +
                            "*Да* или *Нет*");
                    break;
            }
        }
        else if (replyTxt.contains(Emoji.Man + "Имя"))
        {
            bot.sendChatActionTyping(msg_final);
            additionalMessageHandling.getTeacherBySurname(txt, msg_final);
            bot.sendChatActionTyping(msg_final);
            bot.sendTeacherKeyboard(msg_final, false);
        }
        else if (replyTxt.contains(Emoji.Memo + "Контакты"))
        {
            bot.sendChatActionTyping(msg_final);
            additionalMessageHandling.getTeacherInfo(txt, msg_final);
            bot.sendChatActionTyping(msg_final);
            bot.sendTeacherKeyboard(msg_final, false);
        }
        else if (replyTxt.contains(Emoji.Wrench + "Установить группу") && replyTxt.contains("Введите группу в " +
                "формате XXXX-YY-YY"))
        {
            bot.sendChatActionTyping(msg_final);
            User user = userDao.getUser(msg_final.getChatId());
            if (txt.equals(user.getGroupName()))
            {
                bot.sendMsg(msg_final, "Вы уже привязаны к этой группе", true);
                bot.sendSettingsKeyboard(msg_final, false);
            }
            else
            {
                if (groupDao.getGroupForName(txt).size() > 0)
                {
                    Integer groupId = groupDao.getGroupForName(txt).get(0).getId();
                    user.setGroupId(groupId);
                    userDao.merge(user);
                    bot.sendMsg(msg_final, "Вы успешно прикреплены к группе " +
                            groupDao.getGroup(user.getGroupId()).getGroupName(), true);
                }
                else
                {
                    bot.sendMsg(msg_final, "Введенной группы нет в базе. Возможно вы ввели " +
                            "название с ошибкой или данную группу еще не добавили в базу", true);
                }
                bot.sendSettingsKeyboard(msg_final, false);
            }
        }
        else if (replyTxt.contains(Emoji.Wrench + "Установить группу") && replyTxt.contains("У вас установлено " +
                "пользовательское расписание"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.toLowerCase())
            {
                case("да"):
                    scheduleDao.deleteUserSchedule(msg_final.getChatId());
                    bot.sendMsg(msg_final, "Пользовательское расписание *удалено*",
                            true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsgReply(msg_final, Emoji.Wrench + "Установить группу: " +
                            "Введите группу в формате XXXX-YY-YY (X - буквы, Y - цифры)");
                    break;
                case("нет"):
                    bot.sendMsg(msg_final, "Вы оставили свое расписание", true);
                    break;
                default:
                    bot.sendMsgReply(msg_final,Emoji.Wrench + "Установить группу: Вы " +
                            "ввели некорректный ответ. Введите \"Да\" или \"Нет\". У вас установлено пользовательское " +
                            "расписание, вы уверены, что хотите его удалить и установить расписание группы?");
                    break;
            }
            bot.sendSettingsKeyboard(msg_final, false);
        }
        else if (replyTxt.contains(Emoji.Wrench + "Установить группу") && replyTxt.contains("У вас уже установлена группа"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case("да"):
                    bot.sendMsgReply(msg_final, Emoji.Wrench + "Установить группу: " +
                            "Введите группу в формате XXXX-YY-YY (X - буквы, Y - цифры)");
                    break;
                case("нет"):
                    bot.sendMsg(msg_final, "Вы оставили расписание группы " +
                            groupDao.getGroup(userDao.getUser(msg_final.getChatId()).getGroupId()).getGroupName(), true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendSettingsKeyboard(msg_final, false);
                    break;
                default:
                    bot.sendMsgReply(msg_final,Emoji.Wrench + "Установить группу: Вы " +
                            "ввели некорректный ответ. Введите \"Да\" или \"Нет\". У вас уже установлена группа " +
                            groupDao.getGroup(userDao.getUser(msg_final.getChatId()).getGroupId()).getGroupName() +
                            ". Вы точно хотите сменить группу?");
                    break;
            }
        }
        else if (replyTxt.contains(Emoji.Wrench + "Удалить группу") && replyTxt.contains("Вы точно хотите удалить группу?"))
        {
            bot.sendChatActionTyping(msg_final);
            User user = userDao.getUser(msg_final.getChatId());
            switch (txt.trim().toLowerCase())
            {
                case("да"):
                    user.setGroupId(-1);
                    userDao.merge(user);
                    bot.sendMsg(msg_final, "Вы успешно отсоеденены от группы!", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendSettingsKeyboard(msg_final, false);
                    break;
                case("нет"):
                    bot.sendMsg(msg_final, "Вы оставили расписание группы " + user.getGroupName(), true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendSettingsKeyboard(msg_final, false);
                    break;
                default:
                    bot.sendMsgReply(msg_final,Emoji.Wrench + "Удалить группу: Вы ввели " +
                            "некорректный ответ. Введите \"Да\" или \"Нет\". У вас установлена группа " +
                            user.getGroupName() + ". Вы точно хотите удалить группу?");
                    break;
            }
        }
        else if (replyTxt.contains("Введите дату в формате"))
        {
            bot.sendChatActionTyping(msg_final);

            if (additionalMessageHandling.checkDate(txt))
            {
                Date time;
                try
                {
                    time = new SimpleDateFormat("dd.MM.yyyy").parse(txt);
                }
                catch (ParseException e) {
                    Main._Log.warn("Не удалось поместить дату " + e.toString() + "\n");
                    bot.sendMsgReply(msg_final, "Вы ввели дату *неверно*! Введите дату" +
                            " в формате дд.мм.гггг");
                    return;
                }
                Calendar c = Calendar.getInstance();
                c.setTime(time);
                additionalMessageHandling.preparationScheduleSend(msg_final, c);
                bot.sendScheduleKeyboard(msg_final, false);
            }
            else
                bot.sendMsgReply(msg_final, "Вы ввели дату *неверно*! Введите дату в " +
                        "формате дд.мм.гггг");

        }
        else if (replyTxt.contains("Введите номер пары"))
        {
            if (txt.trim().matches("[1-8]"))
            {
                bot.sendChatActionTyping(msg_final);
                Calendar c = Calendar.getInstance();
                Integer classNumber = Integer.parseInt(txt);
                Date date = c.getTime();
                String writeDate = new SimpleDateFormat( "dd MMMM yyyy", new Locale("ru")).format(date);

                if (additionalMessageHandling.isSemester(c))
                {
                    additionalMessageHandling.sendScheduleForTime(msg_final, classNumber, c);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendClassKeyboard(msg_final, false);
                }
                else if (additionalMessageHandling.isTestSession(c))
                {
                    bot.sendMsg(msg_final, writeDate + "Сейчас идет зачетная сессия", true);
                    bot.sendClassKeyboard(msg_final, false);
                }
                else if (additionalMessageHandling.isExamSession(c))
                {
                    bot.sendMsg(msg_final, writeDate + "Сейчас идет экзаменационная сессия", true);
                    bot.sendClassKeyboard(msg_final, false);
                }
                else
                {
                    bot.sendMsg(msg_final, writeDate + "*Сейчас каникулы!*", true);
                    bot.sendClassKeyboard(msg_final, false);
                }
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, "Вы ввели номер пары *неверно*! Введите номер пары");
            }
        }
        else if (replyTxt.contains(Emoji.Card_Index + "Регистрация:\n"))
        {
            List<Group> groups = groupDao.getGroupForName(txt);
            int size = groups.size();
            bot.sendChatActionTyping(msg_final);
            User user = new User(msg_final.getChatId(), msg_final.getFrom().getUserName(), -1);
            if (txt.toLowerCase().equals("нет"))
            {
                userDao.merge(user);
                Main._Log.info("Пользователь " + msg_final.getChat().getTitle() +
                        " с chatId = " + msg_final.getChatId() + " добавлен в базу\n");
                bot.sendMsg(msg_final, "Вы успешно зарегистрированы! Если вы захотите " +
                        "ввести группу в дальнейшем, то можете это сделать в меню настроек", true);
                bot.sendStartKeyboard(msg_final, false);
            }
            else if (!(txt.toLowerCase().equals("нет")) && size > 0)
            {
                Integer groupId = groups.get(0).getId();
                String groupName = groupDao.getGroup(groupId).getGroupName();
                user.setGroupId(groupId);
                userDao.merge(user);
                bot.sendMsg(msg_final, "Вы успешно зарегистрированы и прикреплены к группе " +
                                groupName,
                        true);
                Main._Log.info("Пользователь " + msg_final.getChat().getTitle() +
                        " с chatId = " + msg_final.getChatId() + " добавлен в базу и прикреплен к группе "
                        + groupName);
                bot.sendChatActionTyping(msg_final);
                bot.sendStartKeyboard(msg_final, false);
            }
            else if (!(txt.toLowerCase().equals("нет")) && size == 0)
                bot.sendMsgReply(msg_final, Emoji.Card_Index + "Регистрация:\nК " +
                        "сожелению, введенной вами группы не существует или она еще не внесена в базу. Попробуйте " +
                        "еще раз или напишите слово \"Нет\", чтобы установить группу позднее");
        }
        else if (replyTxt.contains("Введите название дисциплины"))
        {
            bot.sendChatActionTyping(msg_final);
            additionalMessageHandling.preparationTeacherBySubjectSend(txt, msg_final);
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Лог") && replyTxt.contains("Введите пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsg(msg_final, "Пароль введен верно", true);
                        try
                        {
                            FileInputStream log = new FileInputStream("./log/log4j2.log");
                            bot.sendChatActionDocument(msg_final);
                            bot.sendDocument(msg_final, "log4j2.log", log);
                        }
                        catch (FileNotFoundException e)
                        {
                            bot.sendMsg(msg_final, "Не получилось отправить файл лога:\n" +
                                    e, true);
                        }
                        try
                        {
                            FileInputStream nohup = new FileInputStream("./nohup.out");
                            bot.sendChatActionDocument(msg_final);
                            bot.sendDocument(msg_final, "nohup.out", nohup);
                        }
                        catch (FileNotFoundException e)
                        {
                            bot.sendMsg(msg_final, "Не получилось отправить файл вывода " +
                                    "в консоль:\n" + e, true);
                        }

                        bot.sendAdminKeyboard(msg_final, false);
                    }
                    else
                    {
                        bot.sendMsg(msg_final, Emoji.Keyboard + "*Лог*: Пароль введен " +
                                "неверно. Введите пароль", true);
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Запрос") && replyTxt.contains("Введите пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsg(msg_final,"Пароль введен верно", true);
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Запрос*: Введите " +
                                "свой запрос");
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Запрос*: Пароль" +
                                " введен неверно. Введите пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains("Введите свой запрос"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                if (txt.toLowerCase().contains("select"))
                {
                    try
                    {
                        Integer start = 0;
                        Integer stop = 100;
                        Integer size = jdbcTemplate.queryForList(txt, String.class).size();
                        if (size > 0)
                        {
                            String log_message;
                            List<String> list = jdbcTemplate.queryForList(txt, String.class);
                            bot.sendMsg(msg_final, "Результат запроса: ", true);
                            while (start < size) {

                                StringBuilder sb = new StringBuilder();
                                for (int i = start; i < stop; i++)
                                {
                                    sb.append(list.get(i)).append("\n");
                                }
                                log_message = sb.toString();
                                bot.sendChatActionTyping(msg_final);
                                bot.sendMsg(msg_final, log_message, false);
                                start = stop;
                                if (size - stop > 100)
                                    stop += 100;
                                else
                                    stop = size;
                            }
                            bot.sendChatActionTyping(msg_final);
                            bot.sendMsgReply(msg_final, "Хотите ввести новый запрос?");
                        }
                        else
                        {
                            bot.sendMsg(msg_final, "Запрос выдал нулевой результат",
                                    true);
                            bot.sendChatActionTyping(msg_final);
                            bot.sendMsgReply(msg_final, "Хотите ввести новый запрос?");
                        }

                    }
                    catch (Exception e)
                    {
                        String error = e.toString();
                        error = error.replace("*", " ");
                        bot.sendMsg(msg_final, "Запрос выполнен с ошибкой: \n" + error, true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, "Хотите ввести новый запрос?");
                    }
                }
                else if (txt.toLowerCase().contains("update") || txt.toLowerCase().contains("delete") ||
                        txt.toLowerCase().contains("merge") || txt.toLowerCase().contains("insert")) {
                    try
                    {
                        jdbcTemplate.update(txt);
                        bot.sendMsg(msg_final, "Запрос выполнен успешно", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, "Хотите ввести новый запрос?");
                    }
                    catch (Exception e)
                    {
                        String error = e.toString();
                        error = error.replace("*", " ");
                        bot.sendMsg(msg_final, "Запрос выполнен с ошибкой: \n" + error,
                                true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, "Хотите ввести новый запрос?");
                    }
                }
            }
        }
        else if (replyTxt.contains("Хотите ввести новый запрос?"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.toLowerCase())
            {
                case("да"):
                    bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Запрос*: Введите " +
                            "свой запрос");
                    break;
                case("нет"):
                    bot.sendAdminKeyboard(msg_final, true);
                    break;
                default:
                    bot.sendMsgReply(msg_final, "Некорректный ответ. Хотите ввести " +
                            "новый запрос?");
                    break;
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Добавить админа") && replyTxt.contains("Введите пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Добавить админа*: " +
                                "Введите через пробел chatId, " +
                                "логин (одно слово без пробела) и пароль пользователя (одно слово без пробела), " +
                                "которого хотите добавить");
                    }
                    else
                    {
                        bot.sendMsg(msg_final, "Пароль введен верно", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Добавить админа*: " +
                                "Пароль введен неверно. Введите пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором",
                            true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Добавить админа") && replyTxt.contains("Введите через пробел"))
        {
            String text = "Введите через пробел chatId, логин (одно слово без пробела) и пароль пользователя (одно слово " +
                    "без пробела), которого хотите добавить";
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
                {
                    String[] data = txt.trim().split(" ");
                    if (data.length == 3)
                    {
                        if (adminInfoDao.getAdminInfosForLogin(data[0]).size() == 0)
                        {
                            if (adminInfoDao.getAdminInfosForLogin(data[1]).size() == 0)
                            {
                                AdminInfo adminInfo = new AdminInfo(Long.parseLong(data[0]), data[1], data[2]);
                                adminInfoDao.insert(adminInfo);
                                bot.sendMsg(msg_final, "Пользователь с chatId\"" +
                                        data[0] + "\", логином \"" + data[1] + "\" и паролем \"" + data[2] +
                                        "\" добавлен", true);
                                bot.sendAdminKeyboard(msg_final, false);
                            }
                            else
                            {
                                bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Добавить " +
                                        "админа*: Пользователь с таким логином уже существует. " + text);
                            }
                        }
                        else
                            bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Добавить " +
                                    "админа*: Пользователь с таким chatId уже существует. " + text);
                    }
                    else
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Добавить админа*: " +
                                "Вы ввели больше данных. " + text);
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Удалить админа") && replyTxt.contains("Введите пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsg(msg_final, "Пароль введен верно", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Удалить админа*: " +
                                "Введите логин пользователя, которого хотите удалить");
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Удалить админа*: " +
                                "Пароль введен неверно. Введите пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Удалить админа") && replyTxt.contains("Введите логин"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
                {
                    if (adminInfoDao.getAdminInfosForLogin(txt.trim()).size() == 1)
                    {
                        adminInfoDao.delete(adminInfoDao.getAdminInfosForLogin(txt.trim()).get(0).getChatId());
                        bot.sendMsg(msg_final, "Пользователь с логином \"" + txt.trim()
                                + "\" удален", true);
                        bot.sendAdminKeyboard(msg_final, false);
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Удалить админа*:" +
                                " Пользователя с данным логином не существует. Введите логин пользователя, которого " +
                                "хотите удалить");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Обновить пароль") && replyTxt.contains("Введите старый пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsg(msg_final, "Пароль введен верно", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Обновить пароль*: " +
                                "Введите новый пароль (без пробелов)");
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Обновить пароль*: " +
                                "Пароль введен неверно. Введите старый пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Keyboard + "Обновить пароль") && replyTxt.contains("Введите новый пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
                {
                    if (StringUtils.countMatches(txt.trim(), " ") == 0)
                    {
                        AdminInfo adminInfo = new AdminInfo(msg_final.getChatId(),
                                adminInfoDao.getAdminInfo(msg_final.getChatId()).getLogin(), txt.trim());
                        adminInfoDao.update(adminInfo);
                        bot.sendMsg(msg_final, "Пароль обновлен", true);
                        bot.sendAdminKeyboard(msg_final, false);
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Keyboard + "*Обновить пароль*: " +
                                "Пароль должен быть введен без пробелов. Введите новый пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Отправить сообщение пользователю") &&
                replyTxt.contains("Введите пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsg(msg_final, "Пароль введен верно", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope +
                                "*Отправить сообщение пользователю*: Введите chatId пользователя и через точку " +
                                "с запятой текст сообщения");
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope +
                                "*Отправить сообщение пользователю*: Пароль введен неверно. Введите пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Отправить сообщение пользователю") &&
                replyTxt.contains("Введите chatId"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
                {
                    String id = txt.substring(0, txt.indexOf(";"));
                    txt = txt.replace(id + ";", "");
                    if (!id.trim().equals("") && id.matches("\\d+"))
                    {
                        long chatId = Long.parseLong(id);
                        if (!(userDao.getUsersById(chatId).size() > 0))
                        {
                            bot.sendChatActionTyping(msg_final);
                            bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope +
                                    "*Отправить сообщение пользователю*: Вы ввели неверный chatId. Введите chatId " +
                                    "пользователя и через точку с запятой текст сообщения");
                        }
                        else
                        {
                            String text = txt.replace(id + "; ", "");
                            bot.sendMsgToUser(chatId, text);
                            bot.sendChatActionTyping(msg_final);
                            bot.sendMsg(msg_final, "Сообщение отправлено пользователю " +
                                    chatId, false);
                            bot.sendAdminKeyboard(msg_final, false);
                        }
                    }
                    else
                    {
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope +
                                "*Отправить сообщение пользователю*: Вы ввели неверный chatId. Введите chatId " +
                                "пользователя и через точку с запятой текст сообщения");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Отправить сообщение всем пользователям") &&
                replyTxt.contains("Введите пароль"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                String referencePassword;
                List<AdminInfo> adminInfos = adminInfoDao.getAdminInfosForChatId(msg_final.getChatId());
                if (adminInfos.size() == 1)
                {
                    referencePassword = adminInfos.get(0).getPassword();
                    if (txt.equals(referencePassword))
                    {
                        bot.sendMsg(msg_final, "Пароль введен верно", true);
                        bot.sendChatActionTyping(msg_final);
                        bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope +
                                "*Отправить сообщение всем пользователям*: Введите текст сообщения");
                    }
                    else
                    {
                        bot.sendMsgReply(msg_final, Emoji.Incoming_Envelope +
                                "*Отправить сообщение всем пользователям*: Пароль введен неверно. Введите пароль");
                    }
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Отправить сообщение всем пользователям") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            if (txt.trim().toLowerCase().equals("нет"))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendAdminKeyboard(msg_final, false);
            }
            else
            {
                if (adminInfoDao.getAdminInfosForChatId(msg_final.getChatId()).size() == 1)
                {
                    bot.sendMsgToAllUser(txt);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsg(msg_final, "Сообщение отправлено всем пользователям",
                            false);
                    bot.sendAdminKeyboard(msg_final, false);
                }
                else
                {
                    bot.sendMsg(msg_final, "Вы не являетесь администратором", true);
                    bot.sendStartKeyboard(msg_final, false);
                }
            }
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Предложение") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            additionalMessageHandling.sendFeedbackToAdmin(msg_final, "Предложение", txt);
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Ошибка") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            additionalMessageHandling.sendFeedbackToAdmin(msg_final, "Ошибка", txt);
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Другое") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            additionalMessageHandling.sendFeedbackToAdmin(msg_final, "Другое", txt);
        }
        else if (replyTxt.contains(Emoji.Scroll + "Удалить расписание") & replyTxt.contains("Вы уверены, что хотите удалить " +
                "свое расписание?"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case "да":
                    scheduleDao.deleteUserSchedule(msg_final.getChatId());
                    bot.sendMsg(msg_final, "Ваше расписание успешно удалено", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendCustomScheduleKeyboard(msg_final, false);
                    break;
                case "нет":
                    bot.sendMsg(msg_final, "Вы оставили свое расписание", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendCustomScheduleKeyboard(msg_final, false);
                default:
                    bot.sendMsgReply(msg_final,Emoji.Scroll + "Удалить расписание: " +
                            "Вы ввели некорректный ответ. Введите *Да* или *Нет*. Вы уверены, что хотите удалить " +
                            "свое расписание?");
                    break;

            }
        }
        else if (replyTxt.contains(Emoji.Scroll + "Создать расписание") & replyTxt.contains("Через точку с запятой")) //TODO: проверить при вводе пустых значений
        {
            bot.sendChatActionTyping(msg_final);
            int length;
            String[] schedule = null;
            if (txt.trim().toLowerCase().equals("стоп"))
                length = -1;
            else if (txt.trim().toLowerCase().equals("отмена"))
                length = -2;
            else
            {
                schedule = txt.trim().split(";");
                length = schedule.length;
            }

            int dayOfWeek = 0;
            int classNumber = 0;
            int numberOfWeek = 0;
            int subjectId;
            int teacherId;
            int subjectTypeId;
            int classroomId;
            int scheduleId;

            if (length == -1)
            {
                bot.sendMsg(msg_final, "Составление расписание успешно закончено", true);
                bot.sendChatActionTyping(msg_final);
                bot.sendCustomScheduleKeyboard(msg_final, false);
            }
            if (length == -2)
            {
                scheduleDao.deleteUserSchedule(msg_final.getChatId());
                bot.sendMsg(msg_final, "Составление расписания отменено. Созданные " +
                        "строки удалены", true);
                bot.sendChatActionTyping(msg_final);
                bot.sendCustomScheduleKeyboard(msg_final, false);
            }
            else if (length == 11)
            {
                if (schedule[0].trim().matches("[1-7]"))
                    dayOfWeek = Integer.parseInt(schedule[0]);
                else
                    bot.sendMsg(msg_final, "Неверно введен номер дня недели", true);

                if (schedule[1].trim().matches("[1-7]{1,2}"))
                    numberOfWeek = Integer.parseInt(schedule[1]);
                else if (schedule[1].trim().equals("чет"))
                    numberOfWeek = -2;
                else if (schedule[1].trim().equals("нечет"))
                    numberOfWeek = -1;
                else
                    bot.sendMsg(msg_final, "Неверно введен номер недели", true);

                if (schedule[2].trim().matches("[1-8]"))
                    classNumber = Integer.parseInt(schedule[2]);
                else
                    bot.sendMsg(msg_final, "Неверно введен номер пары", true);

                if (classNumber == 0 || numberOfWeek == 0 || dayOfWeek == 0)
                {
                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: " + addUserScheduleText);
                }
                else
                {
                    List<Teacher> teachers = teacherDao.getTeacherForCustomSchedule(schedule[4], schedule[5],
                            schedule[6], schedule[7], schedule[8]);
                    if (teachers.size() == 1)
                        teacherId = teachers.get(0).getId();
                    else
                    {
                        teacherId = teacherDao.count() + 1;
                        Teacher teacher = new Teacher(teacherId, schedule[4], schedule[5], schedule[6], schedule[7],
                                schedule[8]);
                        teacherDao.merge(teacher);
                    }

                    List<Subject> subjects = subjectDao.getSubjectForParse(schedule[3], teacherId);
                    if (subjects.size() == 1)
                        subjectId = subjects.get(0).getId();
                    else
                    {
                        subjectId = subjectDao.count() + 1;
                        Subject subject = new Subject(subjectId, schedule[3], teacherId);
                        subjectDao.merge(subject);
                    }

                    List<SubjectType> subjectTypes = subjectTypeDao.getSubjectTypeForParse(schedule[9]);
                    if (subjectTypes.size() == 1)
                        subjectTypeId = subjectTypes.get(0).getId();
                    else
                    {
                        subjectTypeId = subjectTypeDao.count() + 1;
                        SubjectType subjectType = new SubjectType(subjectTypeId, schedule[9]);
                        subjectTypeDao.merge(subjectType);
                    }

                    List<Classroom> classrooms = classroomDao.getClassroomForParse(schedule[10]);
                    if (classrooms.size() == 1)
                        classroomId = classrooms.get(0).getId();
                    else
                    {
                        classroomId = classroomDao.count() + 1;
                        Classroom classroom = new Classroom(classroomId, schedule[10], null);
                        classroomDao.merge(classroom);
                    }

                    List<Schedule> schedules = scheduleDao.getScheduleForParse(classNumber, classroomId, subjectId,
                            subjectTypeId, numberOfWeek, dayOfWeek);

                    if (schedules.size() == 1)
                        scheduleId = schedules.get(0).getId();
                    else
                    {
                        scheduleId = scheduleDao.Count() + 1;
                        Schedule newSchedule = new Schedule(scheduleId, classNumber, subjectId, subjectTypeId,
                                classroomId, dayOfWeek, numberOfWeek);
                        scheduleDao.merge(newSchedule);
                    }

                    UserSchedule userSchedule = new UserSchedule(msg_final.getChatId(), scheduleId);
                    scheduleDao.insertUserSchedule(userSchedule);

                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: " + addUserScheduleText);
                }
            }
            else if (length > 0 & length < 11)
            {
                bot.sendMsgReply(msg_final,Emoji.Scroll + "Создать расписание: " +
                        "Вы ввели недостаточно данных. " + addUserScheduleText);
            }
            else if (length > 11)
                bot.sendMsgReply(msg_final,Emoji.Scroll + "Создать расписание: " +
                        "Вы ввели слишком много данных данных. " + addUserScheduleText);
        }
        else if (replyTxt.contains(Emoji.Scroll + "Создать расписание") & replyTxt.contains("У вас установлена " +
                "группа"))
        {
            User user = userDao.getUser(msg_final.getChatId());
            String groupName = groupDao.getGroup(user.getGroupId()).getGroupName();
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case "да":
                    user.setGroupId(-1);
                    userDao.merge(user);
                    bot.sendMsg(msg_final, "Ваше успешно отсоединены от группы " +
                            groupName, true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: " + addUserScheduleText);
                    break;
                case "нет":
                    bot.sendMsg(msg_final, "Вы оставили группу " +
                            groupName, true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendCustomScheduleKeyboard(msg_final, false);
                    break;
                default:
                    bot.sendMsgReply(msg_final,Emoji.Scroll + "Создать расписание: " +
                            "Вы ввели некорректный ответ. Введите *Да* или *Нет*. У вас установлена группа. Вы точно " +
                            "хотите ее удалить?");
                    break;

            }
        }
        else if (replyTxt.contains(Emoji.Scroll + "Создать расписание") & replyTxt.contains("У вас уже установлено " +
                "свое расписание"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case "да":
                    scheduleDao.deleteUserSchedule(msg_final.getChatId());
                    bot.sendMsg(msg_final, "Ваше расписание успешно удалено", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: " + addUserScheduleText);
                    break;
                case "нет":
                    bot.sendMsg(msg_final, "Вы оставили свое расписание", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendCustomScheduleKeyboard(msg_final, false);
                default:
                    bot.sendMsgReply(msg_final,Emoji.Scroll + "Создать расписание: " +
                            "Вы ввели некорректный ответ. Введите *Да* или *Нет*. У вас уже установлено свое" +
                            " расписание. Вы точно хотите его удалить и создать новое?");
                    break;

            }
        }
        else if (replyTxt.contains(Emoji.Scroll + "Обновить расписание") & replyTxt.contains("Через точку с запятой"))
        {
            bot.sendChatActionTyping(msg_final);
            int length;
            String[] schedule = null;
            if (txt.trim().toLowerCase().equals("стоп"))
                length = -1;
            else
            {
                schedule = txt.trim().split(";");
                length = schedule.length;
            }

            int dayOfWeek = 0;
            int classNumber = 0;
            int numberOfWeek = 0;
            int subjectId;
            int teacherId;
            int subjectTypeId;
            int classroomId;
            int scheduleId;

            if (length == -1)
            {
                bot.sendMsg(msg_final, "Обновление расписание успешно закончено", true);
                bot.sendChatActionTyping(msg_final);
                bot.sendCustomScheduleKeyboard(msg_final, false);
            }
            else if (length == 11)
            {
                if (schedule[0].trim().matches("[1-7]"))
                    dayOfWeek = Integer.parseInt(schedule[0]);
                else
                    bot.sendMsg(msg_final, "Неверно введен номер дня недели", true);

                if (schedule[1].trim().matches("[1-7]{1,2}"))
                    numberOfWeek = Integer.parseInt(schedule[1]);
                else if (schedule[1].trim().equals("чет"))
                    numberOfWeek = -2;
                else if (schedule[1].trim().equals("нечет"))
                    numberOfWeek = -1;
                else
                    bot.sendMsg(msg_final, "Неверно введен номер недели", true);

                if (schedule[2].trim().matches("[1-8]"))
                    classNumber = Integer.parseInt(schedule[0]);
                else
                    bot.sendMsg(msg_final, "Неверно введен номер пары", true);

                if (classNumber == 0 || numberOfWeek == 0 || dayOfWeek == 0)
                {
                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Обновить расписание: " + refreshUserScheduleText);
                }
                else
                {
                    List<Teacher> teachers = teacherDao.getTeacherForCustomSchedule(schedule[4], schedule[5],
                            schedule[6], schedule[7], schedule[8]);
                    if (teachers.size() == 1)
                        teacherId = teachers.get(0).getId();
                    else
                    {
                        teacherId = teacherDao.count() + 1;
                        Teacher teacher = new Teacher(teacherId, schedule[4], schedule[5], schedule[6], schedule[7],
                                schedule[8]);
                        teacherDao.merge(teacher);
                    }

                    List<Subject> subjects = subjectDao.getSubjectForParse(schedule[3], teacherId);
                    if (subjects.size() == 1)
                        subjectId = subjects.get(0).getId();
                    else
                    {
                        subjectId = subjectDao.count() + 1;
                        Subject subject = new Subject(subjectId, schedule[3], teacherId);
                        subjectDao.merge(subject);
                    }

                    List<SubjectType> subjectTypes = subjectTypeDao.getSubjectTypeForParse(schedule[9]);
                    if (subjectTypes.size() == 1)
                        subjectTypeId = subjectTypes.get(0).getId();
                    else
                    {
                        subjectTypeId = subjectTypeDao.count() + 1;
                        SubjectType subjectType = new SubjectType(subjectTypeId, schedule[9]);
                        subjectTypeDao.merge(subjectType);
                    }

                    List<Classroom> classrooms = classroomDao.getClassroomForParse(schedule[10]);
                    if (classrooms.size() == 1)
                        classroomId = classrooms.get(0).getId();
                    else
                    {
                        classroomId = classroomDao.count() + 1;
                        Classroom classroom = new Classroom(classroomId, schedule[10], null);
                        classroomDao.merge(classroom);
                    }

                    List<Schedule> schedules = scheduleDao.getScheduleForParse(classNumber, classroomId, subjectId,
                            subjectTypeId, numberOfWeek, dayOfWeek);

                    if (schedules.size() == 1)
                        scheduleId = schedules.get(0).getId();
                    else
                    {
                        scheduleId = scheduleDao.Count() + 1;
                        Schedule newSchedule = new Schedule(scheduleId, classNumber, subjectId, subjectTypeId,
                                classroomId, dayOfWeek, numberOfWeek);
                        scheduleDao.merge(newSchedule);
                    }

                    UserSchedule userSchedule = new UserSchedule(msg_final.getChatId(), scheduleId);
                    scheduleDao.insertUserSchedule(userSchedule);

                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Обновить расписание: " + refreshUserScheduleText);
                }
            }
            else if (length > 0 & length < 11)
            {
                bot.sendMsgReply(msg_final,Emoji.Scroll + "Обновить расписание: " +
                        "Вы ввели недостаточно данных. " + refreshUserScheduleText);
            }
            else if (length > 11)
                bot.sendMsgReply(msg_final,Emoji.Scroll + "Обновить расписание: " +
                        "Вы ввели слишком много данных. " + refreshUserScheduleText);
        }
        else if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") && replyTxt.contains("У вас уже установлено свое расписание"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case "да":
                    scheduleDao.deleteUserSchedule(msg_final.getChatId());
                    bot.sendMsg(msg_final, "Ваше расписание успешно удалено", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Отправьте в ответ json файл с " +
                            "пользовательским расписанием");
                    break;
                case "нет":
                    bot.sendMsg(msg_final, "Вы оставили свое расписание", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendCustomScheduleKeyboard(msg_final, false);
                default:
                    bot.sendMsgReply(msg_final,Emoji.Scroll + "Загрузить расписание: " +
                            "Вы ввели некорректный ответ. Введите *Да* или *Нет*. У вас уже установлено свое" +
                            " расписание. Вы точно хотите его удалить и загрузить новое?");
                    break;

            }
        }
        else if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") && replyTxt.contains("У вас уже " +
                "установлено расписание группы"))
        {
            bot.sendChatActionTyping(msg_final);
            switch (txt.trim().toLowerCase())
            {
                case "да":
                    User user = new User(msg_final.getChatId(), msg_final.getChat().getUserName(), -1);
                    userDao.merge(user);
                    bot.sendMsg(msg_final, "Вы успешно открепленны от группы", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Отправьте в ответ json файл с " +
                            "пользовательским расписанием");
                    break;
                case "нет":
                    bot.sendMsg(msg_final, "Вы оставили расписание группы", true);
                    bot.sendChatActionTyping(msg_final);
                    bot.sendCustomScheduleKeyboard(msg_final, false);
                default:
                    bot.sendMsgReply(msg_final,Emoji.Scroll + "Загрузить расписание: " +
                            "Вы ввели некорректный ответ. Введите *Да* или *Нет*. У вас уже установлено расписание " +
                            "группы. Вы точно хотите его удалить и загрузить новое?");
                    break;
            }
        }
        else if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") && replyTxt.contains("Отправьте в ответ " +
                "json файл"))
        {
            bot.sendChatActionTyping(msg_final);
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Для загрузки необходимо " +
                    "отправить файл. Отправьте в ответ json файл с пользовательским расписанием");
        }
    }

    public void checkDocumentMessage(Message msg_final, String uploadedFileId)
    {
        String txt = msg_final.getText();
        String replyTxt = msg_final.getReplyToMessage().getText();
        if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") &&
                msg_final.getReplyToMessage().getText().contains("Отправьте в ответ json файл"))
        {

            GetFile uploadedFile = new GetFile();
            uploadedFile.setFileId(uploadedFileId);
            String json = "";
            try
            {
                String uploadedFilePath = bot.execute(uploadedFile).getFilePath();
                File file = bot.downloadFile(uploadedFilePath);
                Scanner scanner = new Scanner(file);
                json = scanner.useDelimiter("\\A").next();
                scanner.close();
            }
            catch (TelegramApiException e)
            {
                Main._Log.error("Не удалось получить путь файла: " + e);
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Произошла ошибка. Отправьте в " +
                        "ответ json файл с пользовательским расписанием");
            }
            catch (IOException e)
            {
                Main._Log.error("Не удалось получить строку из файла: " + e);
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Произошла ошибка. Отправьте в " +
                        "ответ json файл с пользовательским расписанием");
            }
            catch (NoSuchElementException e)
            {
                Main._Log.error("Не удалось распарсить файл: " + e);
                bot.sendChatActionTyping(msg_final);
                bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: Возможно вы загрузили " +
                        "некорректный файл. Отправьте в ответ json файл с пользовательским расписанием");
            }

            if (jsonWorker.loadJson(json, msg_final.getChatId()))
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsg(msg_final, "Расписание успешно загружено", true);
                bot.sendChatActionTyping(msg_final);
                bot.sendCustomScheduleKeyboard(msg_final, false);
            }
            else
            {
                bot.sendChatActionTyping(msg_final);
                bot.sendMsg(msg_final, "В файле с расписанием обнаружена ошибка. Такое может быть, если файл был " +
                        "изменен вручную. Попробуйте использовать другой файл", true);
                bot.sendChatActionTyping(msg_final);
                bot.sendCustomScheduleKeyboard(msg_final, false);
            }
        }
        else if (replyTxt.contains("Вы уверены, что хотите отключиться от бота?"))
        {
            bot.sendMsgReply(msg_final, "В ответ необходимо прислать не документ. Вы уверены, " +
                    "что хотите отключиться от бота? Если вы это сделаете, то ваши данные удаляться. В ответ " +
                    "пришлите *Да* или *Нет*");
        }
        else if (replyTxt.contains(Emoji.Man + "Имя"))
        {
            bot.sendMsgReply(msg_final,Emoji.Man + "Имя: В ответ необходимо прислать не документ. Введите " +
                    "фамилию преподавателя (с большой буквы)");
        }
        else if (replyTxt.contains(Emoji.Memo + "Контакты"))
        {
            bot.sendMsgReply(msg_final, Emoji.Memo + "Контакты: В ответ необходимо прислать не документ. " +
                    "Введите фамилию преподавателя");
        }
        else if (replyTxt.contains(Emoji.Wrench + "Установить группу") && replyTxt.contains(Emoji.Wrench + "Введите " +
                "группу в формате XXXX-YY-YY"))
        {
            bot.sendMsgReply(msg_final,Emoji.Wrench + "Установить группу: В ответ необходимо прислать не документ. " +
                    "Введите группу в формате XXXX-YY-YY (X - буквы, Y - цифры)");
        }
        else if (replyTxt.contains(Emoji.Wrench + "Установить группу") && replyTxt.contains("У вас установлено " +
                "пользовательское расписание"))
        {
            bot.sendMsgReply(msg_final,Emoji.Wrench + "Установить группу: В ответ необходимо прислать не документ. " +
                    "У вас установлено пользовательское расписание, вы уверены, что хотите его удалить и установить " +
                    "расписание группы? (В ответ пришлите \"Да\", если хотите удалить расписание и \"Нет\", " +
                    "если хотите оставить его)");
        }
        else if (replyTxt.contains(Emoji.Wrench + "Установить группу") & replyTxt.contains("У вас уже установлена группа"))
        {
            bot.sendMsgReply(msg_final,Emoji.Wrench + "Установить группу: В ответ необходимо прислать не документ. " +
                    "У вас уже установлена группа " +
                    groupDao.getGroup(userDao.getUser(msg_final.getChatId()).getGroupId()).getGroupName() +
                    ". Вы точно хотите сменить группу? (В ответ пришлите \"Да\", если хотите сменить " +
                    "группу и \"Нет\", если хотите оставить ее)");
        }
        else if (replyTxt.contains(Emoji.Wrench + "Удалить группу") && replyTxt.contains("Вы точно хотите удалить группу?"))
        {
            bot.sendMsgReply(msg_final,Emoji.Wrench + "Удалить группу: В ответ необходимо прислать не документ. " +
                    "У вас установлена группа " +
                    groupDao.getGroup(userDao.getUser(msg_final.getChatId()).getGroupId()).getGroupName() +
                    ". Вы точно хотите удалить группу? (В ответ пришлите \"Да\", если хотите удалить " +
                    "группу и \"Нет\", если хотите оставить ее)");
        }
        else if (replyTxt.contains("Введите дату в формате"))
        {
            bot.sendMsgReply(msg_final, "В ответ необходимо прислать не документ. Введите дату в формате " +
                    "дд.мм.гггг");
        }
        else if (replyTxt.contains("Введите номер пары"))
        {
            bot.sendMsgReply(msg_final, "В ответ необходимо прислать не документ. Введите номер пары");
        }
        else if (replyTxt.contains(Emoji.Card_Index + "Регистрация:\n"))
        {
            bot.sendMsgReply(msg_final, Emoji.Card_Index + "Регистрация: В ответ необходимо прислать не документ. " +
                    "\n*Приветствую!*\nХотите ли вы сразу добавить группу, расписание которой будете получать в " +
                    "дальнейшем?\nЕсли да, то введите группу в формате XXXX-YY-YY (X - буквы, Y - цифры),\n" +
                    "если нет, то введите в ответ слово \"Нет\"");
        }
        else if (replyTxt.contains("Введите название дисциплины"))
        {
            bot.sendMsgReply(msg_final, "В ответ необходимо прислать не документ. Введите название дисциплины " +
                    "(как в расписании)");
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Предложение") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            bot.sendMsgReply(msg_final, txt + ": В ответ необходимо прислать не документ. Введите текст сообщения");
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Ошибка") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            bot.sendMsgReply(msg_final, txt + ": В ответ необходимо прислать не документ. Введите текст сообщения");
        }
        else if (replyTxt.contains(Emoji.Incoming_Envelope + "Другое") &&
                replyTxt.contains("Введите текст сообщения"))
        {
            bot.sendMsgReply(msg_final, txt + ": В ответ необходимо прислать не документ. Введите текст сообщения");
        }
        else if (replyTxt.contains(Emoji.Scroll + "Удалить расписание") & replyTxt.contains("Вы уверены, что хотите удалить " +
                "свое расписание?"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Удалить расписание: В ответ необходимо прислать не документ." +
                    " Вы уверены, что хотите удалить свое расписание? (В ответ напишите \"Да\" или \"Нет\")");
        }
        else if (replyTxt.contains(Emoji.Scroll + "Создать расписание") & replyTxt.contains("Через точку с запятой"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: В ответ необходимо прислать не " +
                    "документ. " + addUserScheduleText);
        }
        else if (replyTxt.contains(Emoji.Scroll + "Создать расписание") & replyTxt.contains("У вас установлена " +
                "группа"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: В ответ необходимо прислать не " +
                    "документ. У вас установлена группа. Вы точно хотите ее удалить? В ответ напишите \"Да\" " +
                    "или \"Нет\"");
        }
        else if (replyTxt.contains(Emoji.Scroll + "Создать расписание") & replyTxt.contains("У вас уже установлено " +
                "свое расписание"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Создать расписание: В ответ необходимо прислать не " +
                    "документ. У вас уже установлено свое расписание. Вы точно хотите его удалить и создать новое? " +
                    "В ответ напишите \"Да\" или \"Нет\"");
        }
        else if (replyTxt.contains(Emoji.Scroll + "Обновить расписание") & replyTxt.contains("Через точку с запятой"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Обновить расписание: В ответ необходимо прислать не " +
                    "документ. " + refreshUserScheduleText);
        }
        else if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") && replyTxt.contains("У вас уже " +
                "установлено свое расписание"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: В ответ необходимо прислать не " +
                    "документ. У вас уже установлено свое расписание. Вы точно хотите его удалить и загрузить новое " +
                    "из файла? В ответ напишите \"Да\" или \"Нет\"");
        }
        else if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") && replyTxt.contains("У вас уже " +
                "установлено расписание группы"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: В ответ необходимо прислать не " +
                    "документ. У вас уже установлено расписание группы. Вы точно хотите его удалить и загрузить " +
                    "новое из файла? В ответ напишите \"Да\" или \"Нет\"");
        }
        else if (replyTxt.contains(Emoji.Scroll + "Загрузить расписание:") && replyTxt.contains("Отправьте в ответ " +
                "json файл"))
        {
            bot.sendMsgReply(msg_final, Emoji.Scroll + "Загрузить расписание: В ответ необходимо прислать не " +
                    "документ. Отправьте в ответ json файл с пользовательским расписанием");
        }
    }
}
