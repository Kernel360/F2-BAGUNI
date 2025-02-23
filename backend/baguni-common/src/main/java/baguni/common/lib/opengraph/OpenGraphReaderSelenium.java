package baguni.common.lib.opengraph;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("selenium")
public class OpenGraphReaderSelenium implements OpenGraphReader {

	private final OpenGraphOption openGraphOption;

	public OpenGraphReaderSelenium(OpenGraphOption openGraphOption) {
		this.openGraphOption = openGraphOption;
	}

	/**
	 * @author sangwon
	 * Selenium을 이용하여 JavaScript에 의해 동적으로 만들어지는 OG 데이터가 있는 경우도 처리가 가능
	 */
	@Override
	public Map<String, String> read(URI uri) throws OpenGraphException {
		Map<String, String> result = new HashMap<>();

		// 1. WebDriver 설정
		// WebDriverManager 사용 시, 별도의 드라이버 설치 및 경로 지정 없이 자동 세팅
		WebDriverManager.chromedriver().setup();

		// 2. 크롬 옵션 설정
		WebDriver driver = chromeOptionSetting();

		try {
			// 페이지 로딩 타임아웃 설정
			driver.manage().timeouts().pageLoadTimeout(openGraphOption.getHttpRequestTimeoutyDuration());
			log.info("Selenium 페이지 로딩 타임아웃 설정");

			long start = System.currentTimeMillis();
			// 3. 페이지 로딩
			driver.get(uri.toString());
			long end = System.currentTimeMillis();
			log.info("Selenium 페이지 로딩 시간 : {}ms", end - start);

			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(webDriver -> ((JavascriptExecutor)driver)
				.executeScript("return document.readyState").equals("complete")); // 초기 로딩 완료될 때까지 대기
			Thread.sleep(300);
			log.info("Selenium 페이지 초기 로딩 완료 및 Threed sleep 종료");

			// 제목 저장
			String title = driver.getTitle();
			result.put("title", title);

			// favicon 저장
			// 모든 사이트에 /favicon.ico가 있는 것이 아닌 것으로 확인하여 selenium으로 favicon url을 가져오도록 함.
			try {
				WebElement icon = driver.findElement(By.cssSelector("link[rel='icon'], link[rel='shortcut icon']"));
				result.put("icon", icon.getAttribute("href"));
			} catch (org.openqa.selenium.NoSuchElementException e) {
				log.info("해당 사이트에 icon 없음 : {}", uri);
			}

			// 4. og 태그, 메타태그 가져오기
			List<WebElement> metaTags = driver.findElements(By.tagName("meta"));
			for (WebElement meta : metaTags) {
				String propertyAttr = meta.getAttribute("property"); // property 속성
				String nameAttr = meta.getAttribute("name"); // name 속성
				String itempropAttr = meta.getAttribute("itemprop");  // itemprop 속성

				boolean isOgProperty = propertyAttr != null && propertyAttr.startsWith("og:");
				boolean isOgNameImage = nameAttr != null && nameAttr.contains("image");
				boolean isOgNameDescription = nameAttr != null && nameAttr.contains("description");
				boolean isItempropImage = itempropAttr != null && itempropAttr.equals("image");

				if (isOgProperty || isOgNameImage || isItempropImage) {
					String key = isOgProperty ? propertyAttr : (isOgNameImage ? nameAttr : itempropAttr);
					String value = meta.getAttribute("content");
					result.put(key, value);
				}

				if (isOgNameDescription) {
					String value = meta.getAttribute("content");
					result.put(nameAttr, value);
				}
			}
		} catch (org.openqa.selenium.TimeoutException e) {
			log.error("Selenium TimeoutException 발생 : {}, url : {}", e.getMessage(), uri);
		} catch (Exception e) {
			throw new OpenGraphException("Error occurred when reading OG tags via Selenium, url : " + uri, e);
		} finally {
			// 5. 리소스 해제
			driver.quit();
		}

		return result;
	}

	private WebDriver chromeOptionSetting() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new"); // 브라우저 UI 없이 백그라운드로 동작
		options.addArguments("--disable-gpu"); // gpu 비활성화
		options.addArguments("--user-agent=" + openGraphOption.getUserAgent());
		options.addArguments("--no-sandbox");
		options.addArguments("--start-maximized");
		options.addArguments("--disable-popup-blocking"); // 팝업 안뜨게
		options.addArguments("--remote-allow-origins=*"); // 모든 출처에서의 연결을 허용, 자동화된 테스트나 CORS 제한을 우회할 때 유용
		options.addArguments("--disable-dev-shm-usage"); // Chrome이 /dev/shm 대신 /tmp 디렉토리를 사용, /tmp는 일반적인 파일 시스템으로, 크기 제한이 없어 메모리 부족

		// WebDriver 생성
		return new ChromeDriver(options);
	}
}
