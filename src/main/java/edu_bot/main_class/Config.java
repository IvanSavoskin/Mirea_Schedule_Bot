package edu_bot.main_class;

import java.io.InputStream;
import java.util.Properties;

    public class Config
    {

        /** Путь к bot.properties */
        private static final  String CONFIGURATION_BOT_FILE = "/config/bot/bot.properties";

        /** Путь к db.properties */
        private static final  String CONFIGURATION_DB_FILE = "/config/db/db.properties";

        /** =================# Блок настройки значений конфига бота #================= */

        /** Имя бота */
        public static String BOT_NAME;
        /** Токен бота */
        public static String BOT_TOKEN;

        /** =================# Блок настройки значений конфига базы данных #================= */

        /** Имя пользователя H2 */
        public static String H2_USERNAME;
        /** Имя пользователя Amazon */
        public static String AMAZON_USERNAME;
        /** Пароль Amazon */
        public static String AMAZON_PASSWORD;
        /** Хост Amazon */
        public static String AMAZON_HOST;
        /** Имя базы данных Amazon */
        public static String AMAZON_DATABASE;
        /** Порт Amazon */
        public static String AMAZON_PORT;

        /** Метод загрузки конфига бота */
        public static void botConfigLoad()
        {
            Properties botSettings = new Properties();
            try
            {
                InputStream is = Main.class.getResourceAsStream(CONFIGURATION_BOT_FILE);
                botSettings.load(is);
                Main._Log.info("Конфиг бота загружается");
                is.close();
                Main._Log.info("Конфиг бота успешно загружен");
            }
            catch (Exception e)
            {
                Main._Log.fatal("Ошибка загрузки конфига бота", e);
            }

            BOT_NAME = botSettings.getProperty("BotName", "");
            BOT_TOKEN = botSettings.getProperty("BotToken", "");
        }

        /** Метод загрузки конфига базы данных */
        public static void dbConfigLoad()
        {
            Properties botSettings = new Properties();
            try
            {
                InputStream is = Main.class.getResourceAsStream(CONFIGURATION_DB_FILE);
                botSettings.load(is);
                Main._Log.info("Конфиг базы данных загружается");
                is.close();
                Main._Log.info("Конфиг базы данных успешно загружен");
            }
            catch (Exception e)
            {
                Main._Log.fatal("Ошибка загрузки конфига базы данных", e);
            }

            H2_USERNAME = botSettings.getProperty("H2_Username", "");
            AMAZON_USERNAME = botSettings.getProperty("Username", "");
            AMAZON_PASSWORD = botSettings.getProperty("Password", "");
            AMAZON_HOST = botSettings.getProperty("DataBase", "");
            AMAZON_DATABASE = botSettings.getProperty("HostName", "");
            AMAZON_PORT = botSettings.getProperty("Port", "");
        }
    }
