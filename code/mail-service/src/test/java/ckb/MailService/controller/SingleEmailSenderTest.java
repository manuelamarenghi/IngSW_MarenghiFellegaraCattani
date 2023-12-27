package ckb.MailService.controller;

import ckb.MailService.dto.SingleMailRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SingleEmailSenderTest {
    private static SingleEmailSender singleEmailSender;
    private static ClientAndServer mockServer;

    @BeforeAll
    public static void setUp() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("polimi.ckb@gmail.com");
        mailSender.setPassword("soitdnqbieeqoagj");


        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        singleEmailSender = new SingleEmailSender(mailSender, WebClient.builder().build());
        mockServer = ClientAndServer.startClientAndServer(8080);

        mockServer
                .when(request().withMethod("GET").withPath("/api/account/mail")
                        .withQueryStringParameter("userID", "userID"))
                .respond(response().withStatusCode(200).withBody("luca.cattani@mail.polimi.it"));
    }

    @AfterAll
    public static void tearDown() {
        mockServer.stop();
    }

    @Test
    public void singleMailRequestTest() {

        SingleMailRequest request = new SingleMailRequest("userID", "content");

        ResponseEntity<Object> response = singleEmailSender.sendEmail(request);

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void noMailFoundTest() {

        SingleMailRequest request = new SingleMailRequest("wrongID", "content");

        ResponseEntity<Object> response = singleEmailSender.sendEmail(request);

        assertTrue(response.getStatusCode().is4xxClientError());
    }
}
