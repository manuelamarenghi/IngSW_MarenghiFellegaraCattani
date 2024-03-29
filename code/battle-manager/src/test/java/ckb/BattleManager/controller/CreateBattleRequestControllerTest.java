package ckb.BattleManager.controller;

import ckb.BattleManager.dto.in.CreateBattleRequest;
import ckb.BattleManager.model.WorkingPair;
import ckb.BattleManager.repository.BattleRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateBattleRequestControllerTest {
    @Autowired
    private CreateBattleController createBattleController;

    @Autowired
    private BattleRepository battleRepository;

    private ClientAndServer mockServerAccountManager;

    private ClientAndServer mockServerTournamentManager;
    private ClientAndServer mockServerGithubManager;


    @BeforeAll
    public void startServer() {
        createBattleController.initTestMode();
    }

    @BeforeEach
    public void setUp() throws JSONException {
        mockServerAccountManager = ClientAndServer.startClientAndServer(8086);
        mockServerTournamentManager = ClientAndServer.startClientAndServer(8087);
        mockServerGithubManager = ClientAndServer.startClientAndServer(8083);
        setMockServers();
    }

    @AfterEach
    public void tearDown() {
        battleRepository.deleteAll();
        mockServerAccountManager.stop();
        mockServerTournamentManager.stop();
        mockServerGithubManager.stop();
    }

    private void setMockServers() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 1);
        jsonObject.put("email", "example@example.com");
        jsonObject.put("fullName", "John Doe");
        jsonObject.put("password", "password123");
        jsonObject.put("role", "EDUCATOR");
        mockServerAccountManager
                .when(request().withMethod("POST").withPath("/api/account/user"))
                .respond(response().withStatusCode(200).withBody(
                        jsonObject.toString()
                ).withContentType(MediaType.APPLICATION_JSON));

        mockServerTournamentManager
                .when(request().withMethod("POST").withPath("/api/tournament/inform-students"))
                .respond(response().withStatusCode(200));

        mockServerTournamentManager
                .when(request().withMethod("POST").withPath("/api/tournament/check-permission"))
                .respond(response().withStatusCode(200));

        mockServerGithubManager
                .when(request().withMethod("POST").withPath("/api/github/create-repo"))
                .respond(response().withStatusCode(200)
                        .withBody("https://github.com/Code-Kata-Battle/Test-Battle-xvmudweqnxshqtehmcgq")
                        .withContentType(MediaType.TEXT_PLAIN));
    }

    @Test
    public void createBattle() {
        CreateBattleRequest battle = new CreateBattleRequest(
                1L,
                "Battle 1",
                1L,
                1,
                2,
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                List.of(
                        new WorkingPair<>(
                                "tests/input_",
                                "1010001"
                        )
                ),
                true,
                true,
                true
        );

        ResponseEntity<Object> retrievedBattle = createBattleController.createBattle(battle);
        assertTrue(retrievedBattle.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void createBattleNonExistingEducator() {
        mockServerAccountManager.stop();
        mockServerAccountManager = ClientAndServer.startClientAndServer(8086);
        mockServerAccountManager
                .when(request().withMethod("POST").withPath("/api/account/user"))
                .respond(response().withStatusCode(404));

        CreateBattleRequest battle = new CreateBattleRequest(
                1L,
                "Battle 1",
                9999L,
                1,
                2,
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                List.of(
                        new WorkingPair<>(
                                "tests/input_",
                                "1010001"
                        )
                ),
                true,
                true,
                true
        );

        ResponseEntity<Object> retrievedBattle = createBattleController.createBattle(battle);
        assertTrue(retrievedBattle.getStatusCode().is4xxClientError());
    }
    
    @Test
    public void createTwoBattlesWithSameName() {
        CreateBattleRequest battle1 = new CreateBattleRequest(
                1L,
                "Battle 1",
                1L,
                1,
                2,
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                List.of(
                new WorkingPair<>(
                        "tests/input_",
                        "1010001"
                )),
                true,
                true,
                true

        );

        ResponseEntity<Object> retrievedBattle = createBattleController.createBattle(battle1);
        assertTrue(retrievedBattle.getStatusCode().is2xxSuccessful());

        CreateBattleRequest battle2 = new CreateBattleRequest(
                1L,
                "Battle 1",
                1L,
                1,
                2,
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                List.of(
                        new WorkingPair<>(
                                "tests/input_",
                                "1010001"
                        )
                ),
                true,
                true,
                true
        );

        retrievedBattle = createBattleController.createBattle(battle2);
        assertTrue(retrievedBattle.getStatusCode().is4xxClientError());
    }

    @Test
    public void wrongInputCreateTournament()  {
        CreateBattleRequest battle = new CreateBattleRequest(
                -1L,
                "Battle 1",
                1L,
                1,
                2,
                false,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                List.of(
                        new WorkingPair<>(
                                "tests/input_",
                                "1010001"
                        )
                ),
                true,
                true,
                true
        );

        ResponseEntity<Object> retrievedBattle = createBattleController.createBattle(battle);
        assertTrue(retrievedBattle.getStatusCode().is4xxClientError());
    }

    @Test
    public void wrongDate() {
        CreateBattleRequest battle = new CreateBattleRequest(
                1L,
                "Battle 1",
                1L,
                1,
                2,
                false,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2),
                List.of(
                        new WorkingPair<>(
                                "tests/input_",
                                "1010001"
                        )
                ),
                true,
                true,
                true
        );

        ResponseEntity<Object> retrievedBattle = createBattleController.createBattle(battle);
        assertTrue(retrievedBattle.getStatusCode().is4xxClientError());
    }
}