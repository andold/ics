package kr.andold.ics.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.andold.ics.ApplicationContextProvider;
import kr.andold.ics.domain.IcsParam;
import kr.andold.utils.ChromeDriverWrapper;
import kr.andold.utils.Utility;
import kr.andold.utils.job.JobInterface;
import kr.andold.utils.job.STATUS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Service
public class CrawlNaverJob implements JobInterface {
	@Builder.Default @Getter @Setter private Long timeout = 60L * 5L;

	public static final String URL = "https://calendar.naver.com/";
	private static final Duration DEFAULT_TIMEOUT_DURATION = Duration.ofSeconds(4);
	private static final Duration DEFAULT_TIMEOUT_DURATION_LONG = Duration.ofMinutes(5);
	private static final Integer VCALENDAR_ID = 1028;

	@Autowired private IcsService service;

	@Override
	public STATUS call() throws Exception {
		log.info("{} CrawlNaverJob::call()", Utility.indentStart());
		long started = System.currentTimeMillis();

		CrawlNaverJob that = (CrawlNaverJob) ApplicationContextProvider.getBean(CrawlNaverJob.class);
		STATUS result = that.main();

		log.info("{} {} CrawlNaverJob::call() - {}", Utility.indentEnd(), result, Utility.toStringPastTimeReadable(started));
		return result;
	}

	protected STATUS main() throws IOException, ParserException, ParseException {
		log.info("{} CrawlNaverJob::main()", Utility.indentStart());
		long started = System.currentTimeMillis();

		String filename = download();
		if (filename == null || filename.isBlank()) {
			log.info("{} {} CrawlNaverJob::main() - {}", Utility.indentEnd(), STATUS.FAIL_NO_RESULT, Utility.toStringPastTimeReadable(started));
			return STATUS.FAIL_NO_RESULT;
		}

		int count = upload(filename);

		log.info("{} {}:#{} CrawlNaverJob::main() - {}", Utility.indentEnd(), STATUS.SUCCESS, count, Utility.toStringPastTimeReadable(started));
		return STATUS.SUCCESS;
	}

	private int upload(String filename) throws IOException, ParserException, ParseException {
		log.info("{} CrawlNaverJob::upload({})", Utility.indentStart(), filename);

		File file = new File(String.format("%s/Downloads/%s", System.getProperty("user.home"), filename));
		String text = Utility.extractStringFromText(file);
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(new StringReader(text));
		IcsParam param = service.differ(calendar, VCALENDAR_ID);
		param.getRemoves().clear();
		int count = service.batch(param);

		log.info("{} #{} - CrawlNaverJob::upload({}) - {}", Utility.indentEnd(), count, filename, param);
		return count;
	}

	protected String download() {
		log.info("{} CrawlNaverJob::download()", Utility.indentStart());
		long started = System.currentTimeMillis();

		try {
			ChromeDriverWrapper driver = driver(true);

			if (notBackupNaver(driver)) {
				navigateBackupNaver(driver);
			}

			driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT_DURATION);
			
			//	내보내기(백업)	//*[@id="tabcontrol"]/ul/li[2]/a
			By BY_XPATH_TAB_EXPORT = By.xpath("//*[@id='tabcontrol']/ul/li/a[contains(text(),'내보내기')]");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_TAB_EXPORT, Duration.ZERO));
			driver.presenceOfElementLocated(BY_XPATH_TAB_EXPORT, DEFAULT_TIMEOUT_DURATION);
			driver.clickIfExist(BY_XPATH_TAB_EXPORT);
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_TAB_EXPORT, Duration.ZERO));

			//	캘린더::선택활성화	//*[@id="export"]/div[1]/div[3]/div/div[1]/div
			By BY_XPATH_SELECT_BUTTON = By.xpath("//*[@id='export']//div[contains(@class,'selectbox-box')]/div[contains(@class,'selectbox-label')]");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_SELECT_BUTTON, Duration.ZERO));

			//	선택창		//*[@id="export"]/div[1]/div[3]/div/div[2]
			By BY_XPATH_SELECT_POPUP = By.xpath("//*[@id='export']//div[contains(@class,'_calendar_selectbox')]/div[contains(@class,'selectbox-layer')]");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "선택창", driver.getText(BY_XPATH_SELECT_POPUP, Duration.ZERO));

			if (!driver.isDisplayed(BY_XPATH_SELECT_POPUP)) {
				log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_SELECT_BUTTON, Duration.ZERO));
				driver.clickIfExist(BY_XPATH_SELECT_BUTTON);
				log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_SELECT_BUTTON, Duration.ZERO));
			}

			//	개인 달력	//*[@id="export"]/div[1]/div[3]/div/div[2]/div/ul/li[4]
			By BY_XPATH_SELECT_PERSONAL = By.xpath("//*[@id='productIndex']//ul/li[contains(@class,'selectbox-item') AND contains(text(),'개인')]");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "개인 달력", driver.getText(BY_XPATH_SELECT_PERSONAL, Duration.ZERO));
			driver.clickIfExist(BY_XPATH_SELECT_PERSONAL);
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "개인 달력", driver.getText(BY_XPATH_SELECT_PERSONAL, Duration.ZERO));

			//	전체 일정	//*[@id="export"]/div[1]/ul/li[1]/label
			By BY_XPATH_ALL_SCHEDULE = By.xpath("//*[@id='export']//ul/li/label[contains(text(),'전체 일정')]");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "전체 일정", driver.getText(BY_XPATH_ALL_SCHEDULE, Duration.ZERO));
			driver.clickIfExist(BY_XPATH_ALL_SCHEDULE);
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "전체 일정", driver.getText(BY_XPATH_ALL_SCHEDULE, Duration.ZERO));
			
			Set<String> donwloadFiles = donwloadFiles(null);

			//	내보내기(백업)	//*[@id="footer"]/button[1]
			By BY_XPATH_BACKUP_BUTTON = By.xpath("//*[@id='footer']/button/strong[contains(text(),'내보내기(백업)')]/..");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_BACKUP_BUTTON, Duration.ZERO));
			driver.clickIfExist(BY_XPATH_BACKUP_BUTTON);
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "내보내기(백업)", driver.getText(BY_XPATH_BACKUP_BUTTON, Duration.ZERO));
			
			//	취소	//*[@id="footer"]/button[3]
			By BY_XPATH_CANCEL = By.xpath("//*[@id='footer']/button[contains(text(),'취소')]");
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "취소", driver.getText(BY_XPATH_CANCEL, Duration.ZERO));
			driver.clickIfExist(BY_XPATH_CANCEL);
			log.debug("{} CrawlNaverJob::download(...) - 『{}』『{}』", Utility.indentMiddle(), "취소", driver.getText(BY_XPATH_CANCEL, Duration.ZERO));
			
			String filename = waitUntilDownloadComplete(donwloadFiles);
			
			driver.quit();

			log.info("{} 『{}』 CrawlNaverJob::download() - {}", Utility.indentEnd(), filename, Utility.toStringPastTimeReadable(started));
			return filename;
		} catch (Exception e) {
			log.error("Exception:: {}", e.getLocalizedMessage(), e);
		}
		
		log.info("{} {} CrawlNaverJob::download() - {}", Utility.indentEnd(), STATUS.SUCCESS, Utility.toStringPastTimeReadable(started));
		return "";
	}

	private Set<String> donwloadFiles(Set<String> setPrevious) {
		log.info("{} donwloadFiles({})", Utility.indentStart(), setPrevious);
		File fileLocation = new File(String.format("%s/Downloads", System.getProperty("user.home")));

		// Get the list of files in the directory
		File[] files = fileLocation.listFiles();
		if (setPrevious == null) {
			Set<String> set = new LinkedHashSet<>();
			for (File file : files) {
				set.add(file.getName());
			}

			log.info("{} {} - donwloadFiles({})", Utility.indentEnd(), set, setPrevious);
			return set;
		}

		Set<String> set = new LinkedHashSet<>();
		for (File file : files) {
			if (setPrevious.contains(file.getName())) {
				continue;
			}
			
			set.add(file.getName());
		}

		log.info("{} {} - donwloadFiles({})", Utility.indentEnd(), set, setPrevious);
		return set;
	}

	private String waitUntilDownloadComplete(Set<String> donwloadFiles) {
		for (int cx = 0; cx < 32; cx++) {
			Set<String> neo  = donwloadFiles(donwloadFiles);
			
			for (String filename : neo) {
				if (filename.matches("Calendar_andold_[0-9\\-]+\\.ics")) {
					return filename;
				}
			}
			
			Utility.sleep(1000);
		}
		
		return "";
	}

	private void navigateBackupNaver(ChromeDriverWrapper driver) {
		driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT_DURATION_LONG);
		driver.get(URL);
		
		//	개인	//*[@id="calendar_list_container"]/div[2]/ul/li[4]/a[2]/text()
		By BY_XPATH_FOOTER = By.xpath("//*[@id='footer']");
		driver.presenceOfElementLocated(BY_XPATH_FOOTER, DEFAULT_TIMEOUT_DURATION_LONG);
		driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT_DURATION);

		Set<String> windowHandles = driver.getWindowHandles();

		//	개인	//*[@id="calendar_list_container"]/div[2]/ul/li[4]/a[2]/text()
		By BY_XPATH_PERSONAL = By.xpath("//*[@id='calendar_list_container']//li[contains(@calendarid,'60828852')]/a[contains(@title,'개인')]");
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), "개인", driver.getText(BY_XPATH_PERSONAL, Duration.ZERO));
		driver.presenceOfElementLocated(BY_XPATH_PERSONAL, DEFAULT_TIMEOUT_DURATION);
		driver.mouseHover(BY_XPATH_PERSONAL);
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), "개인", driver.getText(BY_XPATH_PERSONAL, Duration.ZERO));
		

		//	메뉴 열기	//*[@id="calendar_list_container"]/div[2]/ul/li[4]/a[3]/span
		By BY_XPATH_OPEN_MENU = By.xpath("//*[@id='calendar_list_container']//li[contains(@calendarid,'60828852')]/a[contains(@class,'_open_menu show_menu')]");
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), "메뉴 열기", driver.getText(BY_XPATH_OPEN_MENU, Duration.ZERO));
		driver.waitUntilIsDisplayed(BY_XPATH_OPEN_MENU, true, 1000 * 4);
		driver.clickIfExist(BY_XPATH_OPEN_MENU);
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), "메뉴 열기", driver.getText(BY_XPATH_OPEN_MENU, Duration.ZERO));

		//	가져오기/내보내기(백업)	/html/body/div[5]/div/ul[1]/li[2]/a
		By BY_XPATH_BACKUP = By.xpath("//body/div//li[@class='_export_calendar']/a[contains(text(),'내보내기')]");
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), "가져오기/내보내기(백업)", driver.getText(BY_XPATH_BACKUP, Duration.ZERO));
		driver.waitUntilIsDisplayed(BY_XPATH_BACKUP, true, 1000 * 4);
		driver.clickIfExist(BY_XPATH_BACKUP);
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), "가져오기/내보내기(백업)", driver.getText(BY_XPATH_BACKUP, Duration.ZERO));

		//	팝업창
		String popup = neo(windowHandles, driver.getWindowHandles());
		driver.switchTo().window(popup);
		log.debug("{} navigateBackupNaver(...) - 『{}』『{}』", Utility.indentMiddle(), windowHandles, popup);
	}

	private String neo(Set<String> before, Set<String> now) {
		for (String handle : now) {
			if (before.contains(handle)) {
				continue;
			}
			
			return handle;
		}

		return "";
	}

	private boolean notBackupNaver(ChromeDriverWrapper driver) {
		// TODO Auto-generated method stub
		return true;
	}

	private ChromeDriverWrapper driver(boolean fHeadless) {
		log.info("{} driver({})", Utility.indentStart(), fHeadless);
		long started = System.currentTimeMillis();

		System.setProperty("webdriver.chrome.driver", IcsService.getUserSeleniumWebdriverChromeDriver());
		ChromeOptions chromeOptions = new ChromeOptions();
		chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
		chromeOptions.addArguments("--disable-dev-shm-usage");
		chromeOptions.addArguments("--disable-infobars");
		if (fHeadless) {
			chromeOptions.addArguments("--headless");
		}
		chromeOptions.addArguments("--remote-allow-origins=*");
		chromeOptions.addArguments("--window-size=2048,1024");
		chromeOptions.addArguments(String.format("--user-data-dir=%s", IcsService.getUserSeleniumUserDataDir()));
		chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
		ChromeDriverWrapper chromeDriver = new ChromeDriverWrapper(chromeOptions);

		log.info("{} 『{}』 driver({}) - {}", Utility.indentEnd(), chromeDriver, fHeadless, Utility.toStringPastTimeReadable(started));
		return chromeDriver;
	}

}
