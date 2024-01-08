package ckb.MailService.controller;

import ckb.MailService.dto.in.AllStudentsMailRequest;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
public class AllStudentsEmailSenderTest {
    @Autowired
    private AllStudentsEmailSender allStudentsEmailSender;
    private ClientAndServer mockServer;


    @BeforeEach
    public void setUpServer() {
        mockServer = ClientAndServer.startClientAndServer(8086);
    }


    @AfterEach
    public void tearDownServer() {
        mockServer.stop();
    }

    @Test
    public void singleStudentTest() {
        allStudentsEmailSender.setAccountManagerUrl("http://localhost:8086");
        mockServer
                .when(request().withMethod("GET").withPath("/api/account/mail-students"))
                .respond(response().withStatusCode(200).withBody("luca.cattani@mail.polimi.it"));

        AllStudentsMailRequest request = new AllStudentsMailRequest("content");

        ResponseEntity<Object> response = allStudentsEmailSender.sendEmail(request);

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void multipleStudentsTest() {
        allStudentsEmailSender.setAccountManagerUrl("http://localhost:8086");


        JSONArray jsonArray = new JSONArray();
        jsonArray.put("luca.cattani@mail.polimi.it");
        jsonArray.put("lucacattani2001@gmail.com");

        String answer = jsonArray.toString().substring(1, jsonArray.toString().length() - 1);

        mockServer
                .when(request().withMethod("GET").withPath("/api/account/mail-students"))
                .respond(response().withStatusCode(200).withBody(answer));

        AllStudentsMailRequest request = new AllStudentsMailRequest("content");

        ResponseEntity<Object> response = allStudentsEmailSender.sendEmail(request);

        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void noStudentsTest() {
        allStudentsEmailSender.setAccountManagerUrl("http://localhost:8086");


        mockServer
                .when(request().withMethod("GET").withPath("/api/account/mail-students"))
                .respond(response().withStatusCode(404));

        AllStudentsMailRequest request = new AllStudentsMailRequest("content");

        ResponseEntity<Object> response = allStudentsEmailSender.sendEmail(request);

        assertTrue(response.getStatusCode().is4xxClientError());
    }
}
