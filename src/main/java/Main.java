import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;


public class Main {
  public static void main(String[] args)  {

    ApiContextInitializer.init();
    TelegramBotsApi botsApi = new TelegramBotsApi();
    try {
      botsApi.registerBot(new Bot());
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }

  }
}
