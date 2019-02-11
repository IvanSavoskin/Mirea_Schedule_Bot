package edu_bot.schedule_class;

import edu_bot.main_class.Main;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download_Schedule
{
    private static void deleteFolder()
    {
        try
        {
            FileUtils.deleteDirectory(new File("schedule"));
            Main._Log.info("Папка удалена");
        }
        catch (Exception e)
        {
            Main._Log.warn("Папка не была удалена:\n" + e);
        }
    }

    private static void createFolder()
    {
        File folder = new File("schedule");
        try
        {
            folder.mkdir();
            Main._Log.info("Папка создана");
        }
        catch (Exception e)
        {
            Main._Log.warn("Папка не была создана:\n" + e);
        }

    }

    /** Метод закачки файлов */
    private static void download(String href, String href_name) throws IOException
    {

        URL url = new URL(href);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

        File f1 = new File("schedule/" + href_name);
        FileOutputStream fw = new FileOutputStream(f1);

        byte[] b = new byte[1024];
        int count;

        while ((count=bis.read(b)) != -1)
            fw.write(b, 0, count);

        fw.close();

    }

    /** Метод закачки файлов по данным ссылкам */
    public static void autoDownload()
    {
        Web_Page_Parser.web_pars();
        deleteFolder();
        createFolder();
        try
        {
            for (int i = 0; i < Web_Page_Parser.href.size(); i++)
            {
                Main._Log.info("Начинается загрузка файла " +  Web_Page_Parser.href_name.get(i));
                Download_Schedule.download(Web_Page_Parser.href.get(i), Web_Page_Parser.href_name.get(i));
                Main._Log.info("Файл " +  Web_Page_Parser.href_name.get(i) + " закачан");
            }
            Main._Log.info("Загрузка всех файлов расписаний завершена");
        }
        catch (IOException e)
        {
            Main._Log.warn("Загрузка файлов с расписаниями не удалась", e);
        }
    }
}

