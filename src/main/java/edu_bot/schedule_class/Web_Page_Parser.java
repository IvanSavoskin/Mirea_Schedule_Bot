package edu_bot.schedule_class;

import edu_bot.main_class.Main;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Web_Page_Parser
{
    //TODO: Исправить парсинг веб-страницы, чтобы парсились только расписания занятий и не парсились расписания сессий


    public static ArrayList<String> href = new ArrayList<>();
    public static ArrayList<String> href_name = new ArrayList<>();
    private static  Document doc;
    private static Integer beginIndex;


    /** Метод получения ссылок с веб-страницы */
    public static void web_pars()
    {
        try
        {
            doc = Jsoup.connect("https://www.mirea.ru/education/schedule-main/schedule/").get();
        }
        catch (IOException e)
        {
            Main._Log.warn("Не удалось получить web-страницу", e);
        }

        Elements links = doc.select("a[href]");

        Main._Log.info("Начинается парсинг веб-страницы");

        for (Element link : links)
        {
            if (link.attr("abs:href").contains(".xlsx") &&
                    !checkHref(getName(link.attr("abs:href"))) &&
                    !link.attr("abs:href").toLowerCase().contains("zaochn") &&
                    !link.attr("abs:href").toLowerCase().contains("institut") &&
                    !link.attr("abs:href").contains("Magistry-FTI-1-kurs.xlsx") &&
                    !link.attr("abs:href").contains("FTI_Stromynka-5-kurs-1-sem.xlsx")&&
                    !link.attr("abs:href").contains("FTI_Stromynka-1-kurs-1-sem-.xlsx")&&
                    !link.attr("abs:href").contains("itht_bak_3k_18_19_osen-1_8.xlsx") &&
                    !link.attr("abs:href").contains("itht_bak_3k_18_19_osen.xlsx") &&
                    !link.attr("abs:href").contains("itht_bak_4k_18_19_osen.xlsx") &&
                    !link.attr("abs:href").contains("itht_bak_4k_18_19_osen-1_8.xlsx") &&
                    !link.attr("abs:href").toLowerCase().contains("zach")&&
                    !link.attr("abs:href").toLowerCase().contains("zima") &&
                    !link.attr("abs:href").toLowerCase().contains("ekzameny"))

            {
                href.add(link.attr("abs:href"));
                href_name.add(getName(link.attr("abs:href")));
            }
        }

        Main._Log.info("Парсинг веб-страницы прошел успешно");
    }

    /** Метод для получения имени файла из адресса ссылки */
    private static String getName(String s)
    {
        String linkName;

        for (int i=0; i < s.length(); i++)
        {
            if (s.charAt(i) == '/')
            {
                beginIndex = i;
            }
        }

        linkName = s.subSequence(beginIndex+1, s.length()).toString();

        return(linkName);
    }

    private static boolean checkHref(String href)
    {
        Pattern p = Pattern.compile("^[1-5]-kurs.*");
        Matcher m = p.matcher(href);
        return  m.matches();
    }
}


