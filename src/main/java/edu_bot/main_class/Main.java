package edu_bot.main_class;

import edu_bot.db_class.dao.FileSumDao;
import edu_bot.schedule_class.Download_Schedule;
import edu_bot.schedule_class.Excel_Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Timer;
import java.util.TimerTask;

@Component
public class Main
{
    public static final Logger _Log = LogManager.getLogger(Main.class);
    private static final  String LOGGING_BOT_FILE = "./log/log4j2.xml";

    public static void main (String[] args)
    {
        Configurator.initialize(null, LOGGING_BOT_FILE);

        _Log.info("Логгер начал работу");

        Config.dbConfigLoad();

        ApiContextInitializer.init();
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        _Log.info("Удаление всех md5-сумм");
        context.getBean(FileSumDao.class).DeleteAll();
        _Log.info("Md5-суммы удалены");

        /** Загрузка файлов с расписаниями по таймеру (каждые день) */
        long period = 86400000L;
        new Timer().schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        Download_Schedule.autoDownload();
                        context.getBean(Excel_Parser.class).autoExcelParser();
                    }
                },
                1, period);

        Config.botConfigLoad();

        TelegramBotsApi botsApi = new TelegramBotsApi();
        try
        {
            _Log.info("Регистрация бота");
            botsApi.registerBot(context.getBean(Bot.class));
            _Log.info("Бот зарегистрирован");
        }
        catch (TelegramApiRequestException e)
        {
            _Log.fatal("Регистрация бота прошла с ошибкой", e);
        }
    }
}
