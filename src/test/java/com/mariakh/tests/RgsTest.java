package com.mariakh.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RgsTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void before() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);//проверяет загружена ли страница, прежде чем искать элемент
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS); //ожидание везде, где вызван findByElement
        driver.get("http://www.rgs.ru");
        wait = new WebDriverWait(driver, 10, 2000);
    }

    @Test
    public void test() {

        //Закрыть всплывающее окно с подпиской
        driver = wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("fl-616371")));
        WebElement overlayCross = driver.findElement(By.xpath("//div[@data-fl-track = 'click-close-login']"));
        overlayCross.click();
        driver.switchTo().defaultContent();

        //Закрыть окно с куки
        WebElement cookieAcceptButton = driver.findElement(By.xpath("//button[@class = 'btn--text']"));
        cookieAcceptButton.click();

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
        String fieldXPath = "//input[@name = '%s']";
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "userName"))), "Kotov Oleg Petrovich");

        //Заполнить адрес электронной почты и проверить, что он введен
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "userEmail"))), "qwertyqwerty");

        //Заполнить номер телефона и проверить, что номер введен
        WebElement phoneField = driver.findElement(By.xpath("//input[@name = 'userTel']"));
        phoneField.sendKeys("9998887766");
        Assert.assertEquals("Номера телефонов не совпадают", "+7 (999) 888-7766", phoneField.getAttribute("value"));

        //Заполнить адрес и проверить, что адрес введен
        WebElement addressField = driver.findElement(By.xpath("//input[@type = 'text' and contains(@class, 'vue-dadata')]"));
        fillInputField(addressField, "г Пермь, ул Революции, д 6, кв 71");
        WebElement addressToClick = driver.findElement(By.xpath("//span[@class = 'vue-dadata__suggestions-item']"));
        addressToClick.click();
        Assert.assertEquals("Адреса не совпадают", "г Пермь, ул Революции, д 6, кв 71", addressField.getAttribute("value"));

        //Кликнуть на чекбокс и проверить, что он не пуст
        WebElement checkboxParent = driver.findElement(By.xpath("//input[@type='checkbox']/.."));
        WebElement checkbox = checkboxParent.findElement(By.xpath("./p/span[2]/span"));
        scrollToElementJs(checkbox);
        checkbox.click();
        Assert.assertTrue("Пустой чекбокс", checkboxParent.getAttribute("class").contains("is-checked"));

        //Кликнуть на 'Свяжитесь со мной'
        WebElement submitButton = driver.findElement(By.xpath("//button[@type = 'submit']"));
        submitButton.click();

        //Найти сообщение о некорректном адресе электронной почты и проверить его
        WebElement wrongEmailMsg = driver.findElement(By.xpath("//div[@formkey = 'email']/div/span"));
        Assert.assertEquals("Сообщение об ошибке отсутствует или отличается от ожидаемого",
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

    private void scrollToElementJs(WebElement element) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillInputField(WebElement element, String value) {
        scrollToElementJs(element);
        element.sendKeys(value);
        boolean check = wait.until(ExpectedConditions.attributeContains(element, "value", value));
        Assert.assertTrue("Поле было заполнено некорректно", check);
    }

}
