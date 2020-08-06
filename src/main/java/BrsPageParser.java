import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class BrsPageParser {

  private static final String BASE_URL = "http://www.brsgolf.com/";
  private static final String MONTH_URL = "/visitor_month.php?d_date=";
  private static final String DAY_URL = "/visitor_day.php?d_date=";
  private static final String END_URL = "&course_id1=1";
  private static HashMap<String, String> coursesUrls = new HashMap<>();

  static {
    coursesUrls.put("chorltoncumhardy", BASE_URL + "chorltoncumhardy");
    coursesUrls.put("manchester", BASE_URL + "manchester");
    coursesUrls.put("northmanchester", BASE_URL + "northmanchester");
    coursesUrls.put("davyhulme", BASE_URL + "davyhulme");
    coursesUrls.put("bury", BASE_URL + "bury");
    coursesUrls.put("brookdale", BASE_URL + "brookdale");
    coursesUrls.put("werneth", BASE_URL + "werneth");
    coursesUrls.put("breightmet", BASE_URL + "breightmet");
    coursesUrls.put("harwood", BASE_URL + "harwood");
    coursesUrls.put("hazelgrove", BASE_URL + "hazelgrove");
    coursesUrls.put("davenport", BASE_URL + "davenport");
    coursesUrls.put("oldham", BASE_URL + "oldham");
  }

  public static String formatReducedDayTimePrice(Map<String, Map<String, String>> dayTimePrice) {

    // TODO make good format

    String outStr = "No reduced fees found for period";

    if (dayTimePrice.size() > 0) {
      StringBuilder formatted = new StringBuilder();
      dayTimePrice.entrySet().stream().forEach(e -> {
        formatted.append(e.getKey());
        e.getValue().entrySet().stream().forEach(y -> {
          formatted.append("|" + y.getKey() + y.getValue());
        });
        formatted.append("\n");
      });
      outStr = formatted.toString();
    }

    return outStr;
  }

  public static String formatReducedTimePrice(Map<String, String> timePrice) {

    // TODO make good format

    String outStr = "No reduced fees found for period";

    if (timePrice.size() > 0) {
      StringBuilder formatted = new StringBuilder();
      timePrice.entrySet().stream().forEach(e -> {
        formatted.append(e.getKey());
        formatted.append(e.getValue());
        formatted.append("\n");
      });
      outStr = formatted.toString();
    }

    return outStr;
  }

  public static Map<String, Map<String, String>> getReducedDayTimePriceFromMonthCourse(String course, String date) throws IOException {
    // assumes course exists in coursesUrls and date is format yyyy-mm-dd
    Map<String, Map<String, String>> dayTimePrice = new HashMap<>();
    String courseUrl = coursesUrls.get(course);
    String fullUrl = teeTimesMonthUrlConstructor(courseUrl, date);
    Document monthDoc = getDocumentFromUrl(fullUrl);
    HashMap<String, String> reducedDayUrl = findDaysUrlWithReducedTts(monthDoc);
    reducedDayUrl.entrySet().stream().forEach(e -> {
      try {
        HashMap<String, String> reducedTimePrice = getReducedTimePricesFromDayUrl(e.getValue());
        dayTimePrice.put(e.getKey(), reducedTimePrice);
      } catch (IOException ioException) {
        ioException.printStackTrace();
      }
    });

    return dayTimePrice;
  }

  public static Map<String, String> getReducedTimePriceFromDayCourse(String course, String date) {
    // assumes course exists in coursesUrls and date is format yyyy-mm-dd
    String courseUrl = coursesUrls.get(course);
    String fullUrl = teeTimesDayUrlConstructor(courseUrl, date);
    Map<String, String> timePrice = new HashMap<>();

    try {
      timePrice = getReducedTimePricesFromDayUrl(fullUrl);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return timePrice;
  }

  public static Object[] getCoursesNames() {
    return coursesUrls.keySet().toArray();
  }

  public static String getCourseUrl(String courseName) {
    return coursesUrls.getOrDefault(courseName, "not stored");
  }

  private static HashMap<String, String> getReducedTimePricesFromDayUrl(String dayUrl) throws IOException {

    HashMap<String, String> timePrice = new HashMap<>();

    Document dayTtsDoc = Jsoup.connect(dayUrl).get();
    Elements reducedSlots = dayTtsDoc.select("table td[data-rate-type='Reduced']");
    String dayMonth = dayTtsDoc.select(".date_box_selected a div").get(0).text();

    for (Element cell : reducedSlots) {
      timePrice.put(
              cell.previousElementSibling().text(),
              cell.parent().selectFirst("td.visitor_day_cell label").text()
      );
    }

    return timePrice;
  }

  private static HashMap<String, String> findDaysUrlWithReducedTts(Document doc) {

    HashMap<String, String> daysUrls = new HashMap<>();

    Elements ttsTable = doc.select("table.tableList.tight.display_month_table tbody");
    Elements daysRows = ttsTable.select("tr");

    for (Element row : daysRows) {
      Element reducedTts = row.selectFirst(".tee_time_for_sale");

      if (reducedTts != null) {
        Element dayNum = row.selectFirst(".day_num a");
        String dayUrl = dayNum.select("a").attr("abs:href");
        daysUrls.put(dayNum.text(), dayUrl);
      }
    }
    return daysUrls;
  }

  private static Document getDocumentFromUrl(String url) throws IOException {
    Document doc = Jsoup.connect(url)
            .get();

    return doc;
  }

  private static String teeTimesDayUrlConstructor(String courseUrl, String date) {
    return courseUrl + DAY_URL + date + END_URL;
  }

  private static String teeTimesMonthUrlConstructor(String courseUrl, String date) {
    return courseUrl + MONTH_URL + date + END_URL;
  }

  public static String compareDayReducedTtsFromCourses(HashSet<String> subs, String day) {
    // assumes day is format yyyy-mm-dd
    String timePriceCoursesList = "No courses with reduced TTs on day";
    Map<String, List<List<String>>> collatedTimeCoursePrice = new HashMap<>();

    for (String course : subs) {
      Map<String, String> timePrice = getReducedTimePriceFromDayCourse(course, day);
      timePrice.forEach((time, price) -> {
        List<String> coursePrice = Arrays.asList(course, price);
        collatedTimeCoursePrice.computeIfAbsent(time, k -> new ArrayList<>()).add(coursePrice);
      });
    }

    if (!collatedTimeCoursePrice.isEmpty()) {
      StringBuilder outStr = new StringBuilder();
      collatedTimeCoursePrice.forEach((key, value) -> {
        outStr.append(key);
        outStr.append(" | ");
        outStr.append(value);
        outStr.append("\n");
      });

      timePriceCoursesList = outStr.toString();
    }

    return timePriceCoursesList;
  }

}
