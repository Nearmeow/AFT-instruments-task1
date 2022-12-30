package com.mariakh.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RgsTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void before() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); //ожидание везде, где вызван findByElement
        driver.get("http://www.rgs.ru");
        wait = new WebDriverWait(driver, 20, 2000);
    }

    @Test
    public void test() {

        //Закрыть всплывающее окно с подпиской
        WebElement iframe = driver.findElement(By.xpath("//iframe[@id = 'fl-616371']"));
        wait.until(ExpectedConditions.visibilityOf(iframe));
        driver.switchTo().frame(iframe);
        WebElement overlayCross = driver.findElement(By.xpath("//div[@data-fl-track = 'click-close-login']"));
        overlayCross.click();
        driver.switchTo().defaultContent();

        //Закрыть окно с куки
        WebElement notificationElement = driver.findElement(By.xpath("//div[@class = 'notifications-block']"));
        List<WebElement> elements = notificationElement.findElements(By.xpath("./child::*"));
        if (elements.size() > 1) {
            WebElement cookieAcceptButton = driver.findElement(By.xpath("//button[@class = 'btn--text']"));
            cookieAcceptButton.click();
        }

        //Кликнуть на раздел 'Компаниям'
        WebElement companyMenu = driver.findElement(By.xpath("//a[@href = '/for-companies']"));
        companyMenu.click();

        //Подожать и проверить урл
        wait.until(ExpectedConditions.urlToBe("https://www.rgs.ru/for-companies"));
        Assert.assertEquals("Не перешли в раздел 'Компаниям'", "https://www.rgs.ru/for-companies", driver.getCurrentUrl());

        //Кликнуть на раздел 'Здоровье'
        WebElement healthMenu = driver.findElement(By.xpath("//span[@class = 'padding' and text() = 'Здоровье']"));
        healthMenu.click();

        //В выпадающем меню выбрать 'Добровольное медицинское страхование', проверить его видимость и кликнуть
        WebElement healthMenuDD = driver.findElement(By.xpath("//a[contains(@href, 'dobrovolnoe')]"));
        wait.until(ExpectedConditions.visibilityOf(healthMenuDD));
        healthMenuDD.click();

        //Найти заголовок 'Добровольное медицинское страхование' и проверить его наличие
        WebElement healthTitle = driver.findElement(By.xpath("//h1[contains(@class, 'title')]"));
        Assert.assertEquals("Не найден заголовок 'Добровольное медицинское страхование'"
                , "Добровольное медицинское страхование"
                , healthTitle.getText()
        );

        //Найти кнопку 'Отправить заявку', проверить её кликабельность и кликнуть
        WebElement sendRequest = driver.findElement(By.xpath("//button[@class = 'action-item btn--basic']"));
        wait.until(ExpectedConditions.elementToBeClickable(sendRequest));
        sendRequest.click();

        //Проверить наличие формы для заполнения
        WebElement formToFill = driver.findElement(By.xpath("//section[contains(@class, 'section-form-anchor')]"));
        Assert.assertTrue(formToFill.isDisplayed());

        //Заполнить имя и проверить, что поле с именем заполнено
        WebElement nameField = driver.findElement(By.xpath("//input[@name = 'userName']"));
        nameField.sendKeys("Kotov Oleg Petrovich");
        Assert.assertEquals("Имена не совпадают", "Kotov Oleg Petrovich", nameField.getAttribute("value"));

        //Заполнить номер телефона и проверить, что номер введен
        WebElement phoneField = driver.findElement(By.xpath("//input[@name = 'userTel']"));
        phoneField.sendKeys("9998887766");
        Assert.assertEquals("Номера телефонов не совпадают", "+7 (999) 888-7766", phoneField.getAttribute("value"));

        //Заполнить адрес и проверить, что адрес введен
        WebElement addressField = driver.findElement(By.xpath("//input[@type = 'text' and contains(@data, vue-dadata)]"));
        addressField.sendKeys("г Пермь, ул Революции, д 6, кв 71");
        Assert.assertEquals("Адреса не совпадают", "г Пермь, ул Революции, д 6, кв 71", addressField.getAttribute("value"));

        //Заполнить адрес электронной почты и проверить, что он введен
        WebElement emailField = driver.findElement(By.xpath("//input[@name = 'userEmail']"));
        emailField.sendKeys("qwertyqwerty");
        Assert.assertEquals("Почты не совпадают", "qwertyqwerty", emailField.getAttribute("value"));

        //Кликнуть на чекбокс и проверить, что он не пуст
        WebElement checkboxParent = driver.findElement(By.xpath("//input[@type='checkbox']/.."));
        WebElement checkbox = checkboxParent.findElement(By.xpath("./p/span[2]/span"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", checkbox);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        checkbox.click();
        Assert.assertTrue("Пустой чекбокс", checkboxParent.getAttribute("class").contains("is-checked"));

        WebElement submitButton = driver.findElement(By.xpath("//button[@type = 'submit']"));
        submitButton.click();

        WebElement wrongEmailMsg = driver.findElement(By.xpath("//span[contains(@class, 'input__error') and text() = " +
                "'Введите корректный адрес электронной почты']")
        );
        Assert.assertEquals("Сообщение об ошибке отсутствует",
                "Введите корректный адрес электронной почты",
                wrongEmailMsg.getText()
        );

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void after() {
        driver.quit();
    }

}
