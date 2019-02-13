package edu_bot.schedule_class;

import edu_bot.db_class.dao.*;
import edu_bot.db_class.model.Group;
import edu_bot.db_class.model.User;
import edu_bot.main_class.AppConfig;
import edu_bot.main_class.Bot;
import edu_bot.main_class.Main;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: Придумать, как парсить все файлы расписаний
//TODO: Решить вопрос с несколькими преподавателями и аудиториями на один предмет
//TODO: Решить вопрос с неделями перед названием предмета

@Component
public class Excel_Parser
{
    private final GroupDao groupDao;
    private final ClassroomDao classroomDao;
    private final SubjectDao subjectDao;
    private final SubjectTypeDao subjectTypeDao;
    private final ScheduleDao scheduleDao;
    private final TeacherDao teacherDao;
    private final EducationDateDao educationDateDao;
    private final UserDao userDao;
    private final Bot bot;

    public Excel_Parser(GroupDao groupDao, ClassroomDao classroomDao, SubjectDao subjectDao, ScheduleDao scheduleDao,
                        SubjectTypeDao subjectTypeDao, TeacherDao teacherDao, EducationDateDao educationDateDao,
                        UserDao userDao, Bot bot)
    {

        this.classroomDao = classroomDao;
        this.groupDao = groupDao;
        this.scheduleDao = scheduleDao;
        this.subjectDao = subjectDao;
        this.subjectTypeDao = subjectTypeDao;
        this.teacherDao = teacherDao;
        this.educationDateDao = educationDateDao;
        this.userDao = userDao;
        this.bot = bot;
    }

    private void excelParser(String fileName) throws IOException
    {
        Integer groupId = groupDao.Count();
        Integer classroomId = classroomDao.Count();
        Integer subjectId = subjectDao.Count();
        Integer subjectTypeId = subjectTypeDao.Count();
        Integer teacherId = teacherDao.Count();
        Integer scheduleId = scheduleDao.Count();
        String group;
        String text1 = "В данный момент расписание для группы, расписание которого вы " +
                "используете обновляется, так что в следующие 10 минут оно может быть неполным " +
                "или неверным. Как только обновление завершиться придет еще одно сообщение";
        String text2 = "Расписание обновлено. При обноружении каких-либо ошибок в нем просьба написать разработчику " +
                "с помощью обратной связи";

        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream("schedule/" + fileName));

        workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);


        for (Sheet sheet: workbook)
        {

            Main._Log.info("Разбирается лист: " + sheet.getSheetName() + "\n");

            for (Row row: sheet)
            {

                int rowNum = row.getRowNum();

                for (int i = 0; i < row.getLastCellNum(); i++)
                    row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                int cellNumber = -1;

                for (Cell cell: row)
                {
                    cellNumber++;

                    CellType cellType = cell.getCellTypeEnum();

                    switch (cellType)
                    {
                        case STRING:
                            if (checkGroupCellPlus(cell.getStringCellValue()))
                            {
                                group = cell.getStringCellValue();

                                Main._Log.info("Группа " + group + " подана на разбор\n");

                                Integer currentGroupId;

                                if (groupDao.getGroupForName(group).size() == 0)
                                {
                                    groupId++;
                                    groupDao.Merge(groupId, group, fileName);
                                    currentGroupId = groupId;
                                }
                                else
                                {
                                    Group gr = groupDao.getGroupForName(group).get(0);
                                    currentGroupId = gr.getId();
                                    groupDao.Merge(currentGroupId, gr.getGroupName(), fileName);
                                }

                                List<User> users = userDao.getUsersForGroup(currentGroupId);

                                for (User user : users)
                                {
                                    bot.sendMsgToUser(user.getChatId(), text1);
                                }

                                if (scheduleDao.getGroupSchedules(currentGroupId).size() > 0)
                                {
                                    scheduleDao.DeleteGroupSchedule(currentGroupId);
                                }

                                int classTime = 1;
                                int dayOfWeek = 0;
                                int numberOfWeek = 0;
                                int condition = 0;

                                Row rowSchedule;
                                int rowNumSchedule = rowNum + 2;

                                while (condition != 1)
                                {

                                    rowSchedule = sheet.getRow(rowNumSchedule);

                                    String subject = "";
                                    String subjectType = "";
                                    String teacher = "";
                                    String classroom = "";
                                    String teacherName = "";
                                    String teacherSurname;
                                    String teacherSecondName = "";

                                    for (int i = 0; i < cellNumber; i++)
                                        rowSchedule.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                                    Iterator<Cell> cellIteratorSchedule = rowSchedule.iterator();

                                    Cell cellSchedule = cellIteratorSchedule.next();

                                    String day = cellSchedule.getStringCellValue();

                                    switch (day.toLowerCase())
                                    {
                                        case ("понедельник"):
                                            dayOfWeek = 1;
                                            break;
                                        case ("вторник"):
                                            dayOfWeek = 2;
                                            break;
                                        case ("среда"):
                                            dayOfWeek = 3;
                                            break;
                                        case ("четверг"):
                                            dayOfWeek = 4;
                                            break;
                                        case ("пятница"):
                                            dayOfWeek = 5;
                                            break;
                                        case ("суббота"):
                                            dayOfWeek = 6;
                                            break;
                                    }

                                    cellSchedule = cellIteratorSchedule.next();

                                    if (cellSchedule.getCellTypeEnum() == CellType.NUMERIC)
                                    {
                                        Double time = cellSchedule.getNumericCellValue();

                                        switch (time.toString())
                                        {
                                            case ("1.0"):
                                                classTime = 1;
                                                break;
                                            case ("2.0"):
                                                classTime = 2;
                                                break;
                                            case ("3.0"):
                                                classTime = 3;
                                                break;
                                            case ("4.0"):
                                                classTime = 4;
                                                break;
                                            case ("5.0"):
                                                classTime = 5;
                                                break;
                                            case ("6.0"):
                                                classTime = 6;
                                                break;
                                            case ("7.0"):
                                                classTime = 7;
                                                break;
                                            case ("8.0"):
                                                classTime = 8;
                                                break;
                                        }
                                    }

                                    cellIteratorSchedule.next();
                                    cellIteratorSchedule.next();
                                    cellSchedule = cellIteratorSchedule.next();

                                    if (cellSchedule.getCellTypeEnum() != CellType.BLANK)
                                    {
                                        String week = cellSchedule.getStringCellValue();

                                        switch (week.toLowerCase())
                                        {
                                            case ("i"):
                                                numberOfWeek = 1;
                                                break;
                                            case ("ii"):
                                                numberOfWeek = 2;
                                                break;
                                        }
                                    }
                                    else if (cellSchedule.getCellTypeEnum() == CellType.BLANK && !cellSchedule.getCellStyle().getBorderTopEnum().
                                            equals(BorderStyle.NONE) && !cellSchedule.getCellStyle().getBorderBottomEnum().
                                            equals(BorderStyle.NONE))
                                        if (numberOfWeek == 1 && dayOfWeek != 0)
                                            numberOfWeek++;
                                        else if (numberOfWeek == 2 && dayOfWeek != 0)
                                            numberOfWeek = 1;

                                    if ((classTime == 6) && (dayOfWeek == 6) && (numberOfWeek == 2))
                                        condition++;

                                    for (int i = 4; i < cellNumber; i++)
                                    {
                                        cellSchedule = cellIteratorSchedule.next();
                                    }

                                    switch (cellSchedule.getCellTypeEnum())
                                    {
                                        case STRING:
                                            if (!cellSchedule.getStringCellValue().toLowerCase().
                                                    replaceAll("[^A-Za-zА-Яа-я]", "").equals("") &&
                                                    !cellSchedule.getStringCellValue().toLowerCase().equals("день") &&
                                                    !cellSchedule.getStringCellValue().toLowerCase().equals("самостоятельных")
                                                    && !cellSchedule.getStringCellValue().toLowerCase().equals("занятий") &&
                                                    !cellSchedule.getStringCellValue().toLowerCase().equals("военная") &&
                                                    !cellSchedule.getStringCellValue().toLowerCase().equals("подготовка") &&
                                                    !cellSchedule.getStringCellValue().toLowerCase().
                                                            equals("военная подготовка") &&
                                                    !cellSchedule.getStringCellValue().toLowerCase().
                                                            equals("занятия по адресу:") && !cellSchedule.getStringCellValue().
                                                    equals("ул. М.Пироговская, д.1") &&
                                                    !cellSchedule.getStringCellValue().equals("пр-т Вернадского, д.86"))
                                            {
                                                subject = cellSchedule.getStringCellValue().trim();
                                            }
                                            break;
                                        case NUMERIC:
                                            Double subject1 = cellSchedule.getNumericCellValue();
                                            subject = subject1.toString();
                                    }
                                    cellSchedule = cellIteratorSchedule.next();

                                    switch (cellSchedule.getCellTypeEnum())
                                    {
                                        case STRING:
                                            if (!cellSchedule.getStringCellValue().toLowerCase().
                                                    replaceAll("[^A-Za-zА-Яа-я]", "").equals(""))
                                            {
                                                subjectType = cellSchedule.getStringCellValue().replaceAll("\\.","").trim();
                                            }
                                            break;

                                        case NUMERIC:
                                            Double subjectType1 = cellSchedule.getNumericCellValue();
                                            subjectType = subjectType1.toString();
                                            break;
                                    }

                                    cellSchedule = cellIteratorSchedule.next();

                                    switch (cellSchedule.getCellTypeEnum())
                                    {
                                        case STRING:
                                            if (!cellSchedule.getStringCellValue().toLowerCase().
                                                    replaceAll("[^A-Za-zА-Яа-я]", "").equals(""))
                                            {
                                                    teacher = cellSchedule.getStringCellValue().trim();
                                            }
                                            break;

                                        case NUMERIC:
                                            Double teacher1 = cellSchedule.getNumericCellValue();
                                            teacher = teacher1.toString();
                                            break;
                                    }

                                    cellSchedule = cellIteratorSchedule.next();

                                    switch (cellSchedule.getCellTypeEnum())
                                    {
                                        case STRING:
                                            if (!cellSchedule.getStringCellValue().toLowerCase().
                                                    replaceAll("[^A-Za-zА-Яа-я]", "").equals(""))
                                            {
                                                classroom = cellSchedule.getStringCellValue().trim();
                                            }
                                            break;

                                        case NUMERIC:
                                            Double classroom1 = cellSchedule.getNumericCellValue();
                                            classroom = classroom1.toString();
                                            break;
                                    }

                                    if (teacher.matches("([А-Я])([а-я]+(.*))(\\s+)[А-Я](\\s+)\\.(\\s*)[А-Я](\\s+)\\.(\\s*)"))
                                    {

                                        teacherSurname = teacher.substring(0, teacher.indexOf(" "));
                                        teacher = teacher.replaceFirst(teacherSurname + " ", "");
                                        teacherName = teacher.substring(0, teacher.indexOf(".")+1);
                                        teacher = teacher.replaceFirst(teacherName, "");
                                        teacherSecondName = teacher.substring(0, teacher.indexOf(".")+1);
                                    }
                                    else teacherSurname = teacher;

                                    if (!subject.equals("") && (dayOfWeek != 0))
                                    {
                                        Integer currentTeacherId;
                                        Integer currentSubjectId;
                                        Integer currentClassroomId;
                                        Integer currentSubjectTypeId;
                                        Integer currentScheduleId;
                                        Integer currentNumberOfWeek;

                                        if (teacherDao.getTeacherForParse(teacherSurname, teacherName, teacherSecondName).size() == 0)
                                        {
                                            teacherDao.Merge(teacherId, teacherName, teacherSurname, teacherSecondName,
                                                    null, null);
                                            currentTeacherId = teacherId;
                                            teacherId++;
                                        }
                                        else
                                            currentTeacherId = teacherDao.getTeacherForParse(teacherSurname, teacherName,
                                                    teacherSecondName).get(0).getId();

                                        if (subjectDao.getSubjectForParse(subject, currentTeacherId).size() == 0)
                                        {
                                            subjectDao.Merge(subjectId, subject, currentTeacherId);
                                            currentSubjectId = subjectId;
                                            subjectId++;
                                        }
                                        else
                                            currentSubjectId = subjectDao.getSubjectForParse(subject,
                                                    currentTeacherId).get(0).getId();

                                        if (classroomDao.getClassroomForParse(classroom).size() == 0)
                                        {
                                            classroomDao.Merge(classroomId, classroom, null);
                                            currentClassroomId = classroomId;
                                            classroomId++;
                                        }
                                        else
                                            currentClassroomId = classroomDao.getClassroomForParse(classroom).get(0).getId();

                                        if (subjectTypeDao.getSubjectTypeForParse(subjectType).size() == 0)
                                        {
                                            subjectTypeDao.Merge(subjectTypeId, currentSubjectId, subjectType);
                                            currentSubjectTypeId = subjectTypeId;
                                            subjectTypeId++;
                                        }
                                        else
                                            currentSubjectTypeId = subjectTypeDao.getSubjectTypeForParse(subjectType).get(0).getId();

                                        if (numberOfWeek == 1)
                                            currentNumberOfWeek = -1;
                                        else currentNumberOfWeek = -2;

                                        if (scheduleDao.getScheduleForParse(classTime, currentClassroomId, currentSubjectId,
                                                currentSubjectTypeId, currentNumberOfWeek, dayOfWeek).size() == 0)
                                        {
                                            scheduleDao.Merge(scheduleId, classTime, currentClassroomId, currentSubjectId,
                                                    currentSubjectTypeId, dayOfWeek, currentNumberOfWeek);
                                            scheduleDao.Merge_Group_Schedule(currentGroupId, scheduleId);
                                            scheduleId++;
                                        }
                                        else
                                        {
                                            currentScheduleId = scheduleDao.getScheduleForParse(classTime,
                                                    currentClassroomId, currentSubjectId, currentSubjectTypeId,
                                                    currentNumberOfWeek, dayOfWeek).get(0).getId();
                                            scheduleDao.Merge_Group_Schedule(currentGroupId, currentScheduleId);
                                        }

                                    }
                                    rowNumSchedule++;
                                }
                                for (User user : users)
                                {
                                    bot.sendMsgToUser(user.getChatId(), text2);
                                }
                            }
                            break;

                        case NUMERIC:
                            break;
                    }
                }
            }
        }
    }

    private boolean checkGroupCellPlus(String cell)
    {
        Pattern p = Pattern.compile("(\\W*)([А-Я]{4})-(\\d\\d)-(\\d\\d)(\\s*\\S*)");
        Matcher m = p.matcher(cell);
        return  m.matches();
    }

    private void mainScheduleExcelParser() throws IOException
    {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream("main_schedule/main_schedule.xlsx"));

        workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        Sheet sheet = workbook.getSheetAt(0);

        Main._Log.info("Разбирается лист: " + sheet.getSheetName() + "\n");

        Row row = sheet.getRow(0);

        row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        Cell cell = row.getCell(1);

        Date semesterStartDate = cell.getDateCellValue();

        Main._Log.info("Получена дата начала семестра: " + semesterStartDate + "\n");

        row = sheet.getRow(1);

        cell = row.getCell(1);

        Date testSessionStartDate = cell.getDateCellValue();

        Main._Log.info("Получена дата начала зачетной сессии: " + testSessionStartDate + "\n");

        row = sheet.getRow(2);

        cell = row.getCell(1);

        Date examSessionStartDate = cell.getDateCellValue();

        Main._Log.info("Получена дата начала экзаменационной сессии: " + examSessionStartDate + "\n");

        row = sheet.getRow(3);

        cell = row.getCell(1);

        Date examSessionStopDate = cell.getDateCellValue();

        Main._Log.info("Получена дата конца экзаменационной сессии: " + examSessionStopDate + "\n");

        educationDateDao.DeleteAll();

        educationDateDao.Merge(semesterStartDate, testSessionStartDate, examSessionStartDate, examSessionStopDate);
    }

    public void autoExcelParser()
    {
        ApiContextInitializer.init();
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        boolean md5Check;
            try
            {
                Main._Log.info("Проверка md5-суммы файла: основного расписания\n");
                md5Check = context.getBean(File_Sum_Search.class).checkMd5("main_schedule/main_schedule.xlsx");
                Main._Log.info("Md5-сумма файла основного расписания проверена\n");
                if (md5Check)
                {
                    try
                    {
                        mainScheduleExcelParser();
                        context.getBean(File_Sum_Search.class).addMd5("main_schedule/main_schedule.xlsx");
                    }
                    catch (Exception e)
                    {
                        Main._Log.warn("Не удалось распарсить расписание:\n" + e);
                    }
                }
                else
                {
                    Main._Log.info("Файл основного расписания не изменился, переходим к разбору расписаний\n");
                }
            }
            catch (IOException e)
            {
                Main._Log.warn("Не удалось открыть файлы с расписаниями\n", e);
            }

            for (int i=0; i < Web_Page_Parser.href_name.size(); i++)
            {
                String fileName = Web_Page_Parser.href_name.get(i);
                Main._Log.info("Проверка md5-суммы файла: " + fileName + "\n");
                try
                {
                    md5Check = context.getBean(File_Sum_Search.class).checkMd5("schedule/" + fileName);
                    Main._Log.info("Md5-сумма файла " + fileName + " проверена\n");

                    if (md5Check)
                    {
                        Main._Log.info("Начинается парсинг файла " + fileName + "\n");

                            excelParser(fileName);
                            Main._Log.info("Парсинг файла " + fileName + " закончен\n");
                            Main._Log.info("Добавление md5-суммы для файла " + fileName + "\n");
                            context.getBean(File_Sum_Search.class).addMd5("schedule/" + fileName);
                            Main._Log.info("Md5-сумма файла " + fileName + " добавлена\n");
                    }
                    else
                    {
                        Main._Log.info("Файл " + fileName + " не изменился, переходим к следующему файлу\n");
                    }
                }
                catch (IOException e)
                {
                    Main._Log.warn("Не удалось открыть файлы с расписаниями\n", e);
                }
                catch (Exception e)
                {
                    Main._Log.warn("Не удалось распарсить расписание:\n" + e);
                }
            }
            Main._Log.info("Парсинг всех расписаний закончен\n");


    }

}
