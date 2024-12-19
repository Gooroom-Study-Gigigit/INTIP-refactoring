package kr.inuappcenterportal.inuportal.domain.cafeteria.service;

import static kr.inuappcenterportal.inuportal.domain.cafeteria.model.MealType.BREAKFAST;
import static kr.inuappcenterportal.inuportal.domain.cafeteria.model.MealType.DINNER;
import static kr.inuappcenterportal.inuportal.domain.cafeteria.model.MealType.LUNCH;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import kr.inuappcenterportal.inuportal.domain.cafeteria.model.Day;
import kr.inuappcenterportal.inuportal.domain.cafeteria.model.MealType;
import kr.inuappcenterportal.inuportal.global.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CafeteriaService {
    private static final String BREAK_INFO_MESSAGE = "오늘은 쉽니다";
    private static final String MENU_NON_EXIST_SIGN = "-";

    private final RedisService redisService;
    private final String url = "https://www.inu.ac.kr/inu/643/subview.do";
    @Value("${installPath}")
    private String installPath;

    /*@PostConstruct
    @Transactional
    public void initCafeteria() throws InterruptedException {
        crawlCafeteria();
    }

    @Scheduled(cron = "0 10 0 ? * MON-SAT")
    @Transactional
    public void jobCafeteria() throws InterruptedException {
        crawlCafeteria();
    }*/


    public List<String> getCafeteria(String cafeteria, int day) {
        if (Day.findBySign(day) == null) {
            LocalDate today = LocalDate.now();
            day = today.getDayOfWeek().getValue();
        }

        List<String> menu = new ArrayList<>();

        List<MealType> mealTypes = List.of(BREAKFAST, LUNCH, DINNER);
        for (MealType mealType : mealTypes) {
            menu.add(redisService.getMeal(cafeteria, day, mealType.getIntValue()));
        }
        return menu;
    }

    public void crawlCafeteria() throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", installPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver webDriver = new ChromeDriver(options);

        try {
            webDriver.get(url);
            Thread.sleep(1500);

            WebElement linkElement = webDriver.findElement(
                    By.xpath("//*[@id=\"menu643_obj4031\"]/div[2]/form/div[2]/div/a[2]"));
            linkElement.click();
            Thread.sleep(1500);
            storeStudentCafeteria(webDriver.findElements(By.className("wrap-week")));
            log.info("학생식당 저장 완료");

            linkElement = webDriver.findElement(By.xpath("//a[span[text()='제1기숙사식당']]"));
            linkElement.click();
            Thread.sleep(1500);
            storeDormitoryCafeteria(webDriver.findElements(By.className("wrap-week")));
            log.info("제1기숙사식당 저장 완료");

            linkElement = webDriver.findElement(By.xpath("//a[span[text()='2호관(교직원)식당']]"));
            linkElement.click();
            Thread.sleep(1500);
            storeEmployeeCafeteria(webDriver.findElements(By.className("wrap-week")));
            log.info("2호관(교직원)식당 저장 완료");

            linkElement = webDriver.findElement(By.xpath("//a[span[text()='27호관식당']]"));
            linkElement.click();
            Thread.sleep(1500);
            store27Cafeteria(webDriver.findElements(By.className("wrap-week")));
            log.info("27호관식당 저장 완료");

            linkElement = webDriver.findElement(By.xpath("//a[span[text()='사범대식당']]"));
            linkElement.click();
            Thread.sleep(1500);
            storeTeacherCafeteria(webDriver.findElements(By.className("wrap-week")));
            log.info("사범대식당 저장 완료");

        } finally {
            webDriver.quit();
        }
    }


    public void storeStudentCafeteria(List<WebElement> wrapWeekDivs) {
        int day = 1;
        for (WebElement wrapWeekDiv : wrapWeekDivs) {
            WebElement tbody = wrapWeekDiv.findElement(By.tagName("tbody"));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));

            List<MealType> mealTypesToStore = List.of(BREAKFAST, LUNCH, DINNER);
            for (MealType mealType : mealTypesToStore) {
                redisService.storeMeal("학생식당", day, mealType.getIntValue(), getMenu(mealType, rows));
            }
            day++;
        }
    }

    private String getMenu(MealType mealType, List<WebElement> rows) {
        List<WebElement> foods = rows.get(mealType.getIntValue()).findElements(By.tagName("td"));
        if (mealType.equals(LUNCH)) {
            return getMenuText(foods.get(0).getText()).replace("\\", "").replace("\"", "").trim();
        }
        if (mealType.equals(DINNER)) {
            return getMenuText(foods.get(0).getText()).replace("\"", "");
        }
        return getMenuText(foods.get(0).getText());
    }

    private String getMenuText(String menu) {
        if (menu.isEmpty()) {
            return BREAK_INFO_MESSAGE;
        }
        return menu;
    }

    public void storeDormitoryCafeteria(List<WebElement> wrapWeekDivs) {
        int day = 1;
        for (WebElement wrapWeekDiv : wrapWeekDivs) {
            WebElement tbody = wrapWeekDiv.findElement(By.tagName("tbody"));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));

            List<MealType> mealTypesToStore = List.of(BREAKFAST, LUNCH, DINNER);
            for (int i = 0; i < mealTypesToStore.size(); i++) {
                List<WebElement> foods = rows.get(i).findElements(By.tagName("td"));
                String menu = getMenuOfDormitoryCafeteria(foods.get(0).getText());
                redisService.storeMeal("제1기숙사식당", day, mealTypesToStore.get(i).getIntValue(), menu);
            }
            day++;
        }
    }

    private String getMenuOfDormitoryCafeteria(String menu) {
        while (menu.contains("*")) {
            int start = menu.indexOf("*");
            int end = menu.indexOf("*", start + 1);
            if (end == -1) {
                break;
            }
            menu = menu.substring(0, start) + menu.substring(end + 1);
        }
        return menu.trim();
    }

    public void storeEmployeeCafeteria(List<WebElement> wrapWeekDivs) {
        int day = 1;
        for (WebElement wrapWeekDiv : wrapWeekDivs) {
            WebElement tbody = wrapWeekDiv.findElement(By.tagName("tbody"));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));
            redisService.storeMeal("2호관(교직원)식당", day, 1, MENU_NON_EXIST_SIGN);

            List<MealType> mealTypesToStore = List.of(LUNCH, DINNER);
            for (int i = 0; i < mealTypesToStore.size(); i++) {
                List<WebElement> foods = rows.get(i).findElements(By.tagName("td"));
                String menu = getMenuOfEmployeeCafeteria(foods.get(0).getText());
                redisService.storeMeal("2호관(교직원)식당", day, mealTypesToStore.get(i).getIntValue(), menu);
            }
            day++;
        }
    }

    private String getMenuOfEmployeeCafeteria(String menu) {
        if (menu.contains("-")) {
            return menu.substring(0, menu.indexOf("-"));
        }
        return menu;
    }


    public void store27Cafeteria(List<WebElement> wrapWeekDivs) {
        int day = 1;
        for (WebElement wrapWeekDiv : wrapWeekDivs) {
            WebElement tbody = wrapWeekDiv.findElement(By.tagName("tbody"));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));

            List<MealType> mealTypesToStore = List.of(BREAKFAST, LUNCH, DINNER);
            for (int i = 0; i < mealTypesToStore.size(); i++) {
                List<WebElement> foods = rows.get(i).findElements(By.tagName("td"));
                redisService.storeMeal("27호관식당", day, mealTypesToStore.get(i).getIntValue(), foods.get(0).getText());
            }
            day++;
        }
    }

    public void storeTeacherCafeteria(List<WebElement> wrapWeekDivs) {
        int day = 1;
        for (WebElement wrapWeekDiv : wrapWeekDivs) {
            WebElement tbody = wrapWeekDiv.findElement(By.tagName("tbody"));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));
            redisService.storeMeal("사범대식당", day, MealType.BREAKFAST.getIntValue(), MENU_NON_EXIST_SIGN);

            List<MealType> mealTypesToStore = List.of(LUNCH, DINNER);
            for (int i = 0; i < mealTypesToStore.size(); i++) {
                List<WebElement> foods = rows.get(i).findElements(By.tagName("td"));
                String menu = getMenuOfTeacherCafeteria(foods.get(0).getText());
                redisService.storeMeal("사범대식당", day, mealTypesToStore.get(i).getIntValue(), menu);
            }
            day++;
        }
    }

    private String getMenuOfTeacherCafeteria(String menu) {
        if (menu.isEmpty()) {
            return BREAK_INFO_MESSAGE;
        }

        if (menu.contains("-")) {
            return menu.substring(0, menu.indexOf("-"));
        }

        return menu;
    }


}
