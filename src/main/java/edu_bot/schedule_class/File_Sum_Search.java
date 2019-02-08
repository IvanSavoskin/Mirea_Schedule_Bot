package edu_bot.schedule_class;

import edu_bot.db_class.dao.FileSumDao;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Component
public class File_Sum_Search
{

    private final FileSumDao fileSumDao;

    public File_Sum_Search(FileSumDao fileSumDao)
    {

        this.fileSumDao = fileSumDao;
    }

    public String getMd5 (String fileName) throws IOException
    {
        FileInputStream fis = new FileInputStream(fileName);
        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        fis.close();

        return md5;
    }

    public boolean checkMd5 (String fileName) throws IOException
    {
        boolean result = false;
        String md5 = getMd5(fileName);

        if (fileSumDao.getFileSumsForName(fileName).size() == 1)
        {
            if (!fileSumDao.getFileSum(fileName).getMd5().equals(md5))
            {
                result = true;
            }
        }
        else if (fileSumDao.getFileSumsForName(fileName).size() == 0)
        {
            result = true;
        }

        return result;
    }

    public void addMd5 (String fileName) throws IOException
    {
        String md5 = getMd5(fileName);
        if (fileSumDao.getFileSumsForName(fileName).size() == 1)
        {
            if (!fileSumDao.getFileSum(fileName).getMd5().equals(md5))
            {
                fileSumDao.Update(fileName, md5);
            }
        }
        else if (fileSumDao.getFileSumsForName(fileName).size() == 0)
        {
            fileSumDao.Insert(fileName, md5);
        }
    }
}
