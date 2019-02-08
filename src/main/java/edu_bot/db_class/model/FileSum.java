package edu_bot.db_class.model;

public class FileSum
{
    private String fileName;
    private String  md5;

    public FileSum(String fileName, String md5)
    {
        this.fileName = fileName;
        this.md5 = md5;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
