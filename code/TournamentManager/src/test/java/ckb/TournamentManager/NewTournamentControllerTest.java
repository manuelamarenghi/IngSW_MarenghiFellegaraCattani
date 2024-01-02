package ckb.TournamentManager;

import ckb.TournamentManager.controller.NewTournamentController;
import ckb.TournamentManager.dto.NewTournamentRequest;
import ckb.TournamentManager.repo.TournamentRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
public class NewTournamentControllerTest {
    @Autowired
    private NewTournamentController newTournamentController;
    @Autowired
    private TournamentRepo tournamentRepo;
    private static ClientAndServer mockServer;

    @BeforeEach
    public void setUpServer() {
        mockServer = ClientAndServer.startClientAndServer(8085);
    }

    @AfterEach
    public void tearDownServer() {
        mockServer.stop();
    }
    @Test
    public void correctRequestTest() {
        Date d = new Date((2024-1900),01,20);
        NewTournamentRequest request = new NewTournamentRequest(d);
        ResponseEntity<Object> response = newTournamentController.newTournament(request);
        mockServer
                .when(request().withMethod("POST").withPath("/api/mail/all-students"))
                .respond(response().withStatusCode(200).withBody("ok!"));
        assertTrue(response.getBody().equals("Tournament created"));
    }

    @Test
    public void invalidDeadlineTest() {
         Date d = new Date((2020-1900),01,20);
         NewTournamentRequest request = new NewTournamentRequest(d);
         ResponseEntity<Object> response = newTournamentController.newTournament(request);
         assertTrue(response.getBody().equals("Invalid data request"));
    }
    @Test
    public void nullDeadlineTest() {
        Date d = null;
        NewTournamentRequest request = new NewTournamentRequest(d);
        ResponseEntity<Object> response = newTournamentController.newTournament(request);
        assertTrue(response.getBody().equals("Invalid data request"));
    }
}