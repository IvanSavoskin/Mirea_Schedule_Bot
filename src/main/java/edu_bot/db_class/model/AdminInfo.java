package edu_bot.db_class.model;

public class AdminInfo
{
    private long chatId;
    private String login;
    private String  password;

    public AdminInfo(long chatId, String login, String password)
    {
        this.chatId = chatId;
        this.login = login;
        this.password = password;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

