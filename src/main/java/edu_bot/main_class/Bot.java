package edu_bot.main_class;

import edu_bot.additional_class.Emoji;
import edu_bot.additional_class.Sticker;
import edu_bot.db_class.dao.*;
import edu_bot.db_class.model.User;
import edu_bot.main_class.message_handling_class.MessageCheck;
import edu_bot.schedule_class.JsonWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.util.*;

@Component
    public class Bot extends TelegramLongPollingBot
    {
       
        private final UserDao userDao;
        private final MessageCheck messageCheck;
        private final JsonWorker jsonWorker;

        @Autowired
        public Bot(UserDao userDao, @Lazy MessageCheck messageCheck, JsonWorker jsonWorker)
        {
            this.userDao = userDao;
            this.messageCheck = messageCheck;
            this.jsonWorker = jsonWorker;
        }

        /** Получение имени бота */
        @Override
        public String getBotUsername() {
            return Config.BOT_NAME;
        }

        /** Получение конфига бота */
        @Override
        public String getBotToken() {
            return Config.BOT_TOKEN;
        }

        /** Получение новых сообщений в диалогах */
        @Override
        public void onUpdateReceived(Update update) {
            Message msg = update.getMessage();
            Message msg_update = update.getEditedMessage();

            /** Получение сообщений от пользователя */
            if (update.hasMessage() && msg.hasText())
            {
                messageCheck.checkUpdate(update, msg);
            }
            else if (update.hasEditedMessage() && msg_update.hasText())
            {
                messageCheck.checkUpdate(update, msg_update);
            }
            else if (update.hasMessage() && update.getMessage().getSticker() != null)
            {
                sendMsg(msg, "Очень мило, что вы прислали нам стикер, но к сожалению он не являются командой." +
                        " Но чтобы вам было не так грустно, мы отправим вам стикер в ответ" + Emoji.Smiling_Face, true);
                sendSticker(msg, Sticker.randomSticker());
            }
            else if (update.hasMessage() && update.getMessage().hasDocument())
            {
                if (msg.getReplyToMessage().getText().contains(Emoji.Scroll + "Загрузить расписание: Отправьте в ответ " +
                        "документ"))
                {
                    String uploadedFileId = update.getMessage().getDocument().getFileId();
                    GetFile uploadedFile = new GetFile();
                    uploadedFile.setFileId(uploadedFileId);
                    String json = "";
                    try
                    {
                        String uploadedFilePath = execute(uploadedFile).getFilePath();
                        File file = downloadFile(uploadedFilePath);
                        Scanner scanner = new Scanner(file);
                        json = scanner.useDelimiter("\\A").next();
                        scanner.close();
                    }
                    catch (TelegramApiException c)
                    {
                        Main._Log.error("Не удалось получить путь файла: " + c);
                        sendChatActionTyping(msg);
                        sendMsg(msg, "Произошла ошибка, попробуйте еще раз позднее", true);
                        sendChatActionTyping(msg);
                        sendCustomScheduleKeyboard(msg, false);
                    }
                    catch (IOException c)
                    {
                        Main._Log.error("Не удалось получить строку из файла: " + c);
                        sendChatActionTyping(msg);
                        sendMsg(msg, "Произошла ошибка, попробуйте еще раз позднее", true);
                        sendChatActionTyping(msg);
                        sendCustomScheduleKeyboard(msg, false);
                    }

                    if (jsonWorker.loadJson(json, msg.getChatId()))
                    {
                        sendChatActionTyping(msg);
                        sendMsg(msg, "Расписание успешно загружено", true);
                        sendChatActionTyping(msg);
                        sendCustomScheduleKeyboard(msg, false);
                    }
                    else
                    {
                        sendChatActionTyping(msg);
                        sendMsg(msg, "В файле с расписанием обнаружена ошибка. Такое может быть, если файл был " +
                                "изменен вручную. Попробуйте использовать другой файл", true);
                        sendChatActionTyping(msg);
                        sendCustomScheduleKeyboard(msg, false);
                    }
                }
            }

            /** Метод получения кода отправленных стикеров */
            /**else if (update.hasMessage() && update.getMessage().getSticker() != null)
            {
                sendMsg(msg, msg.getSticker().getFileId(), true);
            }*/

            /** Метод получения кода отправленных фото */
            /**else if (update.hasMessage() && update.getMessage().getPhoto() != null)
             {
             List<PhotoSize> photos = update.getMessage().getPhoto();
             String f_id = photos.stream()
                                .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                                .findFirst()
                                .orElse(null).getFileId();
             sendMsg(msg, f_id); // Call method to send the message
             }*/
        }

        /** Метод отправки сообщений */
        public void sendMsg(Message msg, String text, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText(text);
            try
            {
                execute(s);
                Main._Log.info("Отправлено сообщение: \"" + s.getText() + "\"\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Сообщение не отправлено: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки сообщений разработчику*/
        public void sendMsgToAdmin(String text)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(230001881L);
            s.setText(text);
            try
            {
                execute(s);
                Main._Log.info("Cообщение: '" + s.getText() + "' отправлено админу\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Сообщение не отправлено: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки сообщений определенному пользователю*/
        public void sendMsgToUser(Long chatId, String text)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(chatId);
            s.setText(text);
            try
            {
                execute(s);
                Main._Log.info("Cообщение: '" + s.getText() + "' отправлено пользователю " + chatId + "\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Сообщение не отправлено: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки сообщений всем пользователям*/
        public void sendMsgToAllUser(String text)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            List<User> users = userDao.getUsers();
            for (User user : users)
            {
                long chatId = user.getChatId();
                s.setChatId(chatId);
                s.setText(text);
                try
                {
                    execute(s);
                    Main._Log.info("Cообщение: '" + s.getText() + "' отправлено пользователю " + chatId + "\n");
                }
                catch (TelegramApiException e)
                {
                    Main._Log.warn("Сообщение не отправлено: \n" + e.toString() + "\n");
                }
            }
        }

        /** Метод отправки сообщений c принудительным ответом для пользователя*/
        public void sendMsgReply(Message msg, String text)
        {
            ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
            forceReplyKeyboard.getForceReply();
            forceReplyKeyboard.setSelective(true);

            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            s.setText(text);
            s.setReplyToMessageId(msg.getMessageId());
            s.setReplyMarkup(forceReplyKeyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлено сообщение с ответом: '" + s.getText() + "'\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Сообщение с ответом не отправлено: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки фото */
        public void sendPhoto(Message msg, String photo)
        {
            SendPhoto ph = new SendPhoto();
            ph.setChatId(msg.getChatId());
            ph.setPhoto(photo);
            try
            {
                execute(ph);
                Main._Log.info("Отправлено фото\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Фото не отправлено: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки новых документов */
        public void sendDocument(Message msg, String name, InputStream document)
        {
            SendDocument doc = new SendDocument();
            doc.setDocument(name, document);
            doc.setChatId(msg.getChatId());
            try
            {
                execute(doc);
                Main._Log.info("Документ отправлен\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Документ не отправлен: \n" + e.toString() + "\n");
            }
        }

        /**Отправка активности печати в телеграме*/
        public void sendChatActionTyping(Message msg)
        {
            SendChatAction sca = new SendChatAction();
            sca.setChatId(msg.getChatId());
            sca.setAction(ActionType.TYPING);
            try
            {
                execute(sca);
                Main._Log.info("Активность печати отправлена\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Активность печати не была отправлена: \n" + e.toString() + "\n");
            }
        }

        /**Отправка активности отправки файла в телеграме*/
        public void sendChatActionDocument(Message msg)
        {
            SendChatAction sca = new SendChatAction();
            sca.setChatId(msg.getChatId());
            sca.setAction(ActionType.UPLOADDOCUMENT);
            try
            {
                execute(sca);
                Main._Log.info("Активность загрузки документа отправлена\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Активность загрузки документа не была отправлена: \n" + e.toString() + "\n");
            }
        }

        /**Отправка активности отправки фото в телеграме*/
        public void sendChatActionPhoto(Message msg)
        {
            SendChatAction sca = new SendChatAction();
            sca.setChatId(msg.getChatId());
            sca.setAction(ActionType.UPLOADPHOTO);
            try
            {
                execute(sca);
                Main._Log.info("Активность загрузки фото отправлена\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Активность загрузки фото не была отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки стикеров*/
        public void sendSticker(Message msg, String sticker)
        {
            SendSticker stc = new SendSticker();
            stc.setChatId(msg.getChatId());
            stc.setSticker(sticker);
            try
            {
                execute(stc);
                Main._Log.info("Отправлен стикер\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Стикер не отпарвлен: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки стартовой клавиатуры */
        public void sendStartKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Добро пожаловать в \"Расписание МИРЭА\"!*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Play_Button + "*Команды:* Начните взаимдействовать с ботом!\n" +
                    Emoji.Gear + "*Настройки:* Настройте бота под себя\n" +
                    Emoji.Question_Mark + "*Помощь:* Если вы не знаете, что делать\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Play_Button + "Команды"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Gear + "Настройки"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Question_Mark + "Помощь"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Стартовая клавиатура отправлена\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Стартовая клавиатура не отправлена: \n" + e.toString() + "\n", e);
            }
        }

        /** Метод отправки клавиатуры помощи */
        public void sendHelpKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню помощи*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Question_Mark + "*Помощь по командам:* Узнайте какие можно использовать команды\n" +
                    Emoji.White_Question_Mark + "*Помощь по настройкам:* Узнайте, что можно настроить\n" +
                    Emoji.Exclamation_Question_Mark + "*О боте:* Информация о боте\n" +
                    Emoji.Incoming_Envelope + "*Обратная связь:* Напишите разработчику о проблемах или " +
                    "предложениях связанных с ботом" +
                    Emoji.Back_Arrow + "*Назад:* Возвращает в стартовое меню\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонкп размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Question_Mark + "Помощь по командам"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.White_Question_Mark + "Помощь по настройкам"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThirdRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThirdRow.add(new KeyboardButton(Emoji.Exclamation_Question_Mark + "О боте"));
            keyboardThirdRow.add(new KeyboardButton(Emoji.Incoming_Envelope + "Обратная связь"));

            /** Четвертая строчка клавиатуры */
            KeyboardRow keyboardFourthRow = new KeyboardRow();
            /** Добавляем кнопки в четвертую строчку клавиатуры */
            keyboardFourthRow.add(new KeyboardButton(Emoji.Back_Arrow + "Назад"));
            keyboardFourthRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThirdRow);
            keyboard.add(keyboardFourthRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура помощи\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура помощи не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры настроек */
        public void sendSettingsKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню настроек*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Wrench + "*Установить группу:* Задать группу, расписание которой вам будет приходить\n" +
                    Emoji.Wrench + "*Удалить группу:* Удалить группу, расписание которой вам приходит\n" +
                    Emoji.Wrench + "*Текущая группа:* Вывод навзания группы, расписание которой вам приходит\n" +
                    Emoji.Scroll + "*Свое расписание:* создать свое распсиание\n" +
                    Emoji.Back_Arrow + "*Назад:* В стартовое меню\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Wrench + "Установить группу"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Wrench + "Удалить группу"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Wrench + "Текущая группа"));
            keyboardSecondRow.add(new KeyboardButton( Emoji.Scroll + "Свое расписание"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThirdRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThirdRow.add(new KeyboardButton(Emoji.Back_Arrow + "Назад"));
            keyboardThirdRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThirdRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура настроек\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура настроек не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры команд */
        public void sendCommandKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню команд*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Page_With_Curl + "*Расписание:* Меню, в котором можно получить расписание\n" +
                    Emoji.World_Map + "*Пара:* Меню, в котором вы можете узнать, где проходит пара\n" +
                    Emoji.Man_Student + "*Преподаватель:* Меню, в котором вы можете узнать информацию о преподавателях\n" +
                    Emoji.Watch + "*Время:* Меню, в котором можно узнать все, что связано с врменными периодами\n" +
                    Emoji.Back_Arrow + "*Назад:* В стартовое меню\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Page_With_Curl + "Расписание"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.World_Map + "Пара"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Man_Student + "Преподаватель"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Watch + "Время"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThirdRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThirdRow.add(new KeyboardButton(Emoji.Back_Arrow + "Назад"));
            keyboardThirdRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThirdRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура команд\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура команд не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры команд расписания */
        public void sendScheduleKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню команд расписания*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Open_Book + "*Сегодня:* Выводит расписание на сегодня\n" +
                    Emoji.Green_Book + "*Завтра:* Выводит расписание на завтра\n" +
                    Emoji.Orange_Book + "*Дата:* Выводит расписание на определенный день\n" +
                    Emoji.Closed_Book + "*Неделя:* Выводит расписание на неделю вперед\n" +
                    Emoji.Paperclip + "*Файл расписания:* Присылает файл расписания\n" +
                    Emoji.Left_Arrow + "*Назад к командам:* В меню команд\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Open_Book + "Сегодня"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Green_Book + "Завтра" ));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Orange_Book + "Дата"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Paperclip + "Файл расписания"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Closed_Book + "Неделя"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThirdRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThirdRow.add(new KeyboardButton(Emoji.Left_Arrow + "Назад к командам"));
            keyboardThirdRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThirdRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура команд расписания\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура команд расписания не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры команд пары */
        public void sendClassKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню команд пары*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Pen + "*Текущая:* В какой аудитории текущая пара\n" +
                    Emoji.Pencil + "*Следующая:*  В какой аудитории следующая пара\n" +
                    Emoji.Fountain_Pen + "*Определенная:*  В какой аудитории определенная пара\n" +
                    Emoji.Crayon + "*Аудитория:* Где расположена аудитория\n" +
                    Emoji.Left_Arrow + "*Назад к командам:* В меню команд\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Pen + "Текущая"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Pencil + "Следующая" ));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton( Emoji.Fountain_Pen + "Определенная"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Crayon + "Аудитория"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThirdRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThirdRow.add(new KeyboardButton(Emoji.Left_Arrow + "Назад к командам"));
            keyboardThirdRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThirdRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура команд пары\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура команд пары не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры команд преподавателя */
        public void sendTeacherKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню команд преподавателя*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Man + "*Имя:* Полное имя преподавателя по фамилии\n" +
                    Emoji.Man_Pouting + "*Дисциплина:* Полное имя преподавателя по названию дисциплины\n" +
                    Emoji.Memo + "*Контакты:* Телефон и почта преподавателя (если их внесли в базу)\n" +
                    Emoji.Left_Arrow + "*Назад к командам:* В меню команд\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Man + "Имя"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Man_Pouting + "Дисциплина"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Memo + "Контакты"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Left_Arrow + "Назад к командам"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);

            /** Устанваливаем этот список нашей клавиатуре */
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура команд преподавателя\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура команд преподавателя не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры команд времени */
        public void sendTimeKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню команд времени*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Twelve_O_clock + "*Сессия:* Сколько времени осталось до сессии\n" +
                    Emoji.Three_O_clock + "*Неделя:* Какая сейчас идет неделя\n" +
                    Emoji.Six_O_clock + "*Пары:* Временное расписание пар\n" +
                    Emoji.Left_Arrow + "*Назад к командам:* В меню команд\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Twelve_O_clock + "Сессия"));
            keyboardFirstRow.add(new KeyboardButton( Emoji.Three_O_clock + "Неделя"));
            keyboardFirstRow.add(new KeyboardButton(Emoji.Six_O_clock + "Пары"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Left_Arrow + "Назад к командам"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);

            /** Устанваливаем этот список нашей клавиатуре*/
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура команд времени\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура команд времени не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры админки */
        public void sendAdminKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню администратора*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Keyboard + "*Лог:* Выводит файл лога\n" +
                    Emoji.Keyboard + "*Запрос:* Позволяет ввести необходимый запрос\n" +
                    Emoji.Keyboard + "*Добавить админа:* Позволяет добавить пользователя с правами администратора\n" +
                    Emoji.Keyboard + "*Удалить админа:* Позволяет удалить пользователя с правами администратора\n" +
                    Emoji.Incoming_Envelope + "*Отправить сообщение пользователю:* Позволяет дотправить сообщение " +
                    "определенному пользователю\n" +
                    Emoji.Incoming_Envelope + "*Отправить сообщение всем пользователям:* Позволяет отправить " +
                    "сообщение всем пользователям\n" +
                    Emoji.Keyboard + "*Обновить пароль:* Позволяет обновить пароль вашего админского аккаунта\n" +
                    Emoji.Left_Arrow + "*Выход  из админки:* выход из меню администратора и переход к основному меню\n");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Keyboard + "Лог"));
            keyboardFirstRow.add(new KeyboardButton( Emoji.Keyboard + "Запрос"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton( Emoji.Keyboard + "Добавить админа"));
            keyboardSecondRow.add(new KeyboardButton( Emoji.Keyboard + "Удалить админа"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThridRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThridRow.add(new KeyboardButton( Emoji.Incoming_Envelope + "Отправить сообщение пользователю"));
            keyboardThridRow.add(new KeyboardButton( Emoji.Incoming_Envelope + "Отправить сообщение всем пользователям"));

            /** Четвертая строчка клавиатуры */
            KeyboardRow keyboardFourthRow = new KeyboardRow();
            /** Добавляем кнопки в четвертую строчку клавиатуры */
            keyboardFourthRow.add(new KeyboardButton( Emoji.Keyboard + "Обновить пароль"));
            keyboardFourthRow.add(new KeyboardButton( Emoji.Left_Arrow + "Выход  из админки"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThridRow);
            keyboard.add(keyboardFourthRow);

            /** Устанваливаем этот список нашей клавиатуре*/
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура администратора\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура администратора не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры обратной связи*/
        public void sendFeedbackKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню обратной связи*\n" +
                    "*Выберите тему вашего сообщения для разработчика:*\n" +
                    Emoji.Incoming_Envelope + "*Ошибка:* Сообщите об ошибке в работе бота\n" +
                    Emoji.Incoming_Envelope + "*Предложение:* Предложите новые функции или улучшения для бота\n" +
                    Emoji.Incoming_Envelope + "*Другое:* Сообщите о чем-то другом\n" +
                    Emoji.Left_Arrow + "*Назад к помощи:* В меню помощи\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Incoming_Envelope + "Ошибка"));
            keyboardFirstRow.add(new KeyboardButton( Emoji.Incoming_Envelope + "Предложение"));
            keyboardFirstRow.add(new KeyboardButton( Emoji.Incoming_Envelope + "Другое"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Left_Arrow + "Назад к помощи"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);

            /** Устанваливаем этот список нашей клавиатуре*/
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура обратной связи\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура обратной связи не отправлена: \n" + e.toString() + "\n");
            }
        }

        /** Метод отправки клавиатуры обратной связи*/
        public void sendCustomScheduleKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.enableMarkdown(true);
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.setText("*Вы находитесь в меню пользовательского расписания*\n" +
                    "*Выберите одну из опций:*\n" +
                    Emoji.Scroll + "*Создать расписание:* Создайте свое собственное расписание занятий\n" +
                    Emoji.Scroll + "*Обновить расписание:* Обновите существующее расписание\n" +
                    Emoji.Scroll + "*Удалить расписание:* Удалите существующее распсисание\n" +
                    Emoji.Scroll + "*Сохранить расписание:* сохранить существующее распсисание в файл\n" +
                    Emoji.Scroll + "*Загрузить расписание:* Загрузить существующее распсисание из файла\n" +
                    Emoji.Left_Arrow + "*Назад к настройкам:* В меню настроек\n" +
                    Emoji.Cross_Mark + "*Закрыть:* Закрывает меню");

            /** Cоздаем клавиатуру */

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            s.setReplyMarkup(replyKeyboardMarkup);

            /** Устанавливаем свойства */

            /** Выборочное открытие клавиатуры (открывается только у человека, который ее запросил) */
            replyKeyboardMarkup.setSelective(true);
            /** Подгонка размера клавиатуры по высоте */
            replyKeyboardMarkup.setResizeKeyboard(true);
            /** Закрытие клавиатуры после единократного использования */
            replyKeyboardMarkup.setOneTimeKeyboard(false);

            /** Создаем список строк клавиатуры */
            List<KeyboardRow> keyboard = new ArrayList<>();

            /** Первая строчка клавиатуры */
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            /** Добавляем кнопки в первую строчку клавиатуры */
            keyboardFirstRow.add(new KeyboardButton(Emoji.Scroll + "Создать расписание"));
            keyboardFirstRow.add(new KeyboardButton( Emoji.Scroll + "Обновить расписание"));
            keyboardFirstRow.add(new KeyboardButton( Emoji.Scroll + "Удалить расписание"));

            /** Вторая строчка клавиатуры */
            KeyboardRow keyboardSecondRow = new KeyboardRow();
            /** Добавляем кнопки во вторую строчку клавиатуры */
            keyboardSecondRow.add(new KeyboardButton(Emoji.Scroll + "Сохранить расписание"));
            keyboardSecondRow.add(new KeyboardButton(Emoji.Scroll + "Загрузить расписание"));

            /** Третья строчка клавиатуры */
            KeyboardRow keyboardThirdRow = new KeyboardRow();
            /** Добавляем кнопки в третью строчку клавиатуры */
            keyboardThirdRow.add(new KeyboardButton(Emoji.Left_Arrow + "Назад к настройкам"));
            keyboardThirdRow.add(new KeyboardButton(Emoji.Cross_Mark + "Закрыть"));

            /** Добавляем все строчки клавиатуры в список */
            keyboard.add(keyboardFirstRow);
            keyboard.add(keyboardSecondRow);
            keyboard.add(keyboardThirdRow);

            /** Устанваливаем этот список нашей клавиатуре*/
            replyKeyboardMarkup.setKeyboard(keyboard);
            try
            {
                execute(s);
                Main._Log.info("Отправлена клавиатура пользовательского расписания\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура пользовательского расписания не отправлена: \n" + e.toString() + "\n");
            }
        }

        public void sendHideKeyboard(Message msg, Boolean replyMod)
        {
            SendMessage s = new SendMessage();
            s.setChatId(msg.getChatId());
            if (replyMod)
                s.setReplyToMessageId(msg.getMessageId());
            s.enableMarkdown(true);
            s.setText("*Меню закрыто!*\n" +
                    "Для повторного вызова меню введите */bot*");

            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
            replyKeyboardRemove.setSelective(true);
            s.setReplyMarkup(replyKeyboardRemove);

            try
            {
                execute(s);
                Main._Log.info("Отправлено скрытие клавиатуры\n");
            }
            catch (TelegramApiException e)
            {
                Main._Log.warn("Клавиатура не скрыта: \n" + e.toString() + "\n");
            }
        }
    }

