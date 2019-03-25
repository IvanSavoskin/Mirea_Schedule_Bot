package edu_bot.db_class.model;

import java.util.List;

public class Teacher {
    private Integer id;
    private String name;
    private String surname;
    private String second_name;
    private String phone_number;
    private String mail;
    private List<Subject> subjects;

    public Teacher(int id, String name, String surname, String second_name, String phone_number, String mail,
                   List<Subject> subjects)
    {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.second_name = second_name;
        this.phone_number = phone_number;
        this.mail = mail;
        this.subjects = subjects;
    }

    public Teacher(int id, String name, String surname, String second_name, String phone_number, String mail)
    {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.second_name = second_name;
        this.phone_number = phone_number;
        this.mail = mail;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSecond_name() {
        return second_name;
    }

    public void setSecond_name(String second_name) {
        this.second_name = second_name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public List<Subject> getSubjects()
    {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects)
    {
        this.subjects = subjects;
    }
}