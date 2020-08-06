import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;

public class Bot extends TelegramLongPollingBot {

  private HashSet<String> subscriptions = new HashSet<>();


  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText()) {

      if (update.getMessage().getText().equals("/course")) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Select course")
                .enableMarkdown(true);

        InlineKeyboardMarkup markUpInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyBoardRows = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        Object[] arrayNames = BrsPageParser.getCoursesNames();

        for (int i = 0; i < arrayNames.length; i++) {
          rowInline.add(new InlineKeyboardButton()
                  .setText(arrayNames[i].toString())
                  .setCallbackData("queryCourse_" + arrayNames[i].toString())
          );
          if ((i + 1) % 3 == 0 || i == arrayNames.length - 1) {
            keyBoardRows.add(rowInline);
            rowInline = new ArrayList<>();
          }
        }
        markUpInline.setKeyboard(keyBoardRows);
        sendMessage.setReplyMarkup(markUpInline);

        try {
          execute(sendMessage);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      } else if (update.getMessage().getText().equals("/subs")) {
        if (subscriptions.size() == 0) {
          SendMessage sendMessage = new SendMessage()
                  .setChatId(update.getMessage().getChatId())
                  .setText("No subscribed to any course\nEnter /addsub to add one");
          try {
            execute(sendMessage);
          } catch (TelegramApiException e) {
            e.printStackTrace();
          }
        } else {
          SendMessage sendMessage = new SendMessage()
                  .setChatId(update.getMessage().getChatId())
                  .setText("Available courses:")
                  .enableMarkdown(true);
          InlineKeyboardMarkup markUpInline = new InlineKeyboardMarkup();
          List<List<InlineKeyboardButton>> keyBoardRows = new ArrayList<>();
          List<InlineKeyboardButton> rowInline = new ArrayList<>();

          int iterCount = 0;
          for (String course : subscriptions) {
            rowInline.add(new InlineKeyboardButton()
                    .setText(course)
                    .setCallbackData("queryCourse_" + course)
            );
            if ((iterCount + 1) % 3 == 0 || iterCount == subscriptions.size() - 1) {
              keyBoardRows.add(rowInline);
              rowInline = new ArrayList<>();
            }
            iterCount++;
          }

          markUpInline.setKeyboard(keyBoardRows);
          sendMessage.setReplyMarkup(markUpInline);

          try {
            execute(sendMessage);
          } catch (TelegramApiException e) {
            e.printStackTrace();
          }

        }

      } else if (update.getMessage().getText().equals("/addsub")) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Available courses:")
                .enableMarkdown(true);
        InlineKeyboardMarkup markUpInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyBoardRows = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        Object[] arrayNames = BrsPageParser.getCoursesNames();

        for (int i = 0; i < arrayNames.length; i++) {
          rowInline.add(new InlineKeyboardButton()
                  .setText(arrayNames[i].toString())
                  .setCallbackData("addSubQuery_" + arrayNames[i].toString())
          );
          if ((i + 1) % 3 == 0 || i == arrayNames.length - 1) {
            keyBoardRows.add(rowInline);
            rowInline = new ArrayList<>();
          }
        }
        markUpInline.setKeyboard(keyBoardRows);
        sendMessage.setReplyMarkup(markUpInline);

        try {
          execute(sendMessage);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }


      } else if (update.getMessage().getText().equals("/cheapestday")) {
        SendMessage sendMessage = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Compare reduced fees between subbed courses on day:")
                .enableMarkdown(true);
        InlineKeyboardMarkup calendarKeyboard = buildInlineKeyboardCalendar("queryCheapSubbed_", "");
        sendMessage.setReplyMarkup(calendarKeyboard);

        try {
          execute(sendMessage);
        } catch (TelegramApiException e) {
          e.printStackTrace();
        }
      }
    } else if (update.hasCallbackQuery()) {
      CallbackQuery callback = update.getCallbackQuery();
      callBackHandler(callback.getData(), callback.getMessage().getChatId());
    }


  }

  private void callBackHandler(String callBackId, long chatId) {
    if (callBackId.contains("queryCourse_")) {
      SendMessage sendMessage = new SendMessage()
              .setChatId(chatId)
              .setText("See reduced tees for: ")
              .enableMarkdown(true);
      String courseName = callBackId.substring(12);
      InlineKeyboardMarkup markUpInline = new InlineKeyboardMarkup();
      List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
      List<InlineKeyboardButton> rowInline = new ArrayList<>();
      rowInline.add(new InlineKeyboardButton().setText("Specific day").setCallbackData("queryTtsDay_" + courseName));
      rowInline.add(new InlineKeyboardButton().setText("Whole month").setCallbackData("queryTtsMonth_" + courseName));
      rowsInline.add(rowInline);

      markUpInline.setKeyboard(rowsInline);
      sendMessage.setReplyMarkup(markUpInline);
      try {
        execute(sendMessage);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }

    } else if (callBackId.contains("queryTtsDay_")) {
      SendMessage sendMessage = new SendMessage()
              .setChatId(chatId)
              .setText("Select day")
              .enableMarkdown(true);
      String courseName = callBackId.substring(12);
      InlineKeyboardMarkup daysKeyboard = buildInlineKeyboardCalendar("queryTtsDayChosen_", courseName);
      sendMessage.setReplyMarkup(daysKeyboard);

      try {
        execute(sendMessage);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }

    } else if (callBackId.contains("queryTtsMonth_")) {
      SendMessage sendMessage = new SendMessage().
              setChatId(chatId);
      String courseName = callBackId.substring(14);
      try {
        Map<String, Map<String, String>> dayTimePrice = BrsPageParser.getReducedDayTimePriceFromMonthCourse(courseName, "2020-08-01");
        String formattedDayTimePrice = BrsPageParser.formatReducedDayTimePrice(dayTimePrice);
        sendMessage.setText(formattedDayTimePrice);
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        execute(sendMessage);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    } else if (callBackId.contains("queryTtsDayChosen_")) {
      String courseName = callBackId.substring(29);
      String date = callBackId.substring(18, 28);
      Map<String, String> timePrice = BrsPageParser.getReducedTimePriceFromDayCourse(courseName, date);
      String timePriceStr = BrsPageParser.formatReducedTimePrice(timePrice);

      SendMessage sendMessage = new SendMessage().
              setChatId(chatId)
              .setText(timePriceStr);
      try {
        execute(sendMessage);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }
    } else if (callBackId.contains("addSubQuery_")) {
      String courseName = callBackId.substring(12);
      subscriptions.add(courseName);
    } else if (callBackId.contains("queryCheapSubbed_")) {
      String day = callBackId.substring(17, 27);
      String messageText = BrsPageParser.compareDayReducedTtsFromCourses(subscriptions, day);
      SendMessage sendMessage = new SendMessage()
              .setChatId(chatId)
              .setText(messageText);
      try {
        execute(sendMessage);
      } catch (TelegramApiException e) {
        e.printStackTrace();
      }

    }

  }

  private static InlineKeyboardMarkup buildInlineKeyboardCalendar(String callbackText, String courseName) {
    // TODO calendar spawns more than one month
    LocalDate currentDate = LocalDate.now();
    InlineKeyboardMarkup markUpInline = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> keyBoardRows = new ArrayList<>();

    while (currentDate.getMonthValue() == currentDate.plusDays(1).getMonthValue()) {
      List<InlineKeyboardButton> rowInline = new ArrayList<>();
      for (int i = 0; i < 7; i++) {
        rowInline.add(new InlineKeyboardButton().setText(String.valueOf(currentDate.getDayOfMonth())).setCallbackData(callbackText + currentDate.toString() + "_" + courseName));
        if (currentDate.getMonthValue() != currentDate.plusDays(1).getMonthValue()) break;
        currentDate = currentDate.plusDays(1);
      }
      keyBoardRows.add(rowInline);
    }

    markUpInline.setKeyboard(keyBoardRows);

    return markUpInline;
  }

  @Override
  public String getBotUsername() {
    return "JavaFirstTgBot";
  }

  @Override
  public String getBotToken() {
    return "";
  }
}