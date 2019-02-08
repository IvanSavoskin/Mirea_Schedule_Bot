package edu_bot.additional_class;

public enum Sticker
{
        Cat_Sorry("CAADAgADVwAD6VUFGJcpm0jgsOb1Ag"),
        Cat_Smile("CAADAgADRAAD6VUFGKydTkRq0tPgAg"),
        Senya_Smile("CAADAgAD-hIAAkKvaQABNaMIrSnNme4C"),
        Everyday_Fred_Smile("CAADAgADRgADihKqDpNhzzTM57IiAg"),
        Nichosi_Smile("CAADAgADIA4AAkKvaQABjCeTRdVDGtEC"),
        PersikSmile("CAADAgADrwoAAkKvaQABnH4fbcWpVGEC"),
        Doggy_Smile("CAADBAADVwcAAhXc8gIIXDOmyCMHiQI"),
        Kolya_Smile("CAADAgAD5wEAAkKvaQABqBTOn6gEkW8C"),
        Quip_Smile("CAADAgADowAD6VUFGFHo2D7mPF6OAg"),
        Pusheen_Smile("CAADBAADygIAAlI5kwY1OpJy7RHINwI");


    String sticker;

    Sticker(String sticker)
    {
        this.sticker = sticker;
    }

    @Override
    public String toString()
    {
        return this.sticker;
    }

    public static String randomSticker()
    {
        return Sticker.values()[1 + (int) (Math.random() * Sticker.values().length-1)].toString();
    }
}
