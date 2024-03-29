package ckb.TournamentManager.controller;

import ckb.TournamentManager.dto.in.PermissionRequest;
import ckb.TournamentManager.dto.out.DirectMailRequest;
import ckb.TournamentManager.dto.out.UserRequest;
import ckb.TournamentManager.model.Role;
import ckb.TournamentManager.model.User;
import ckb.TournamentManager.service.TournamentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

@RestController
@Slf4j
@RequestMapping("/api/tournament/permission")
@RequiredArgsConstructor

public class PermissionController extends Controller {
    private final TournamentService tournamentService;
    @Autowired
    private final WebClient webClient;

    @PostMapping
    public ResponseEntity<Object> createPermission(@RequestBody PermissionRequest request) {
        // check if the request has valid data
        ResponseEntity<Object> response = checkRequest(request);
        if (response.getStatusCode().is4xxClientError()) return response;
        log.info("The permission request is correctly sent");

        User e = checkEducator(request);
        if (e == null || e.getRole() != Role.EDUCATOR) {
            log.error("Invalid Request");
            return new ResponseEntity<>("Invalid Request", getHeaders(), HttpStatus.BAD_REQUEST);
        } else {
            log.info("The permission request is valid, the person who granted the permission has the permission");
            String x = tournamentService.addPermission(request);
            if (x == null) {
                log.error("Illegal request to give permission");
                return new ResponseEntity<>("Illegal request to give permission", getHeaders(), HttpStatus.BAD_REQUEST);
            }
            String content = "You've gained permission to create battles in tournament: " + x;
            log.info("Permission inserted");
            try {
                sendRequest(mailServiceUri + "/api/mail/direct", content, request.getUserID());
            } catch (Exception exp) {
                log.error("Error while retrieving send request to mail service\n");
                return new ResponseEntity<>(getHeaders(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Permission inserted", getHeaders(), HttpStatus.CREATED);
        }
    }

    private User checkEducator(PermissionRequest request) {
        try {
            String c = webClient
                    .post()
                    .uri(accountManagerUri + "/api/account/user")
                    .bodyValue(new UserRequest(request.getUserID()))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Error during the HTTP call")))
                    .bodyToMono(String.class)
                    .block();
            if (c != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(c, User.class);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void sendRequest(String s, String content, Long userId) {
        Mono<String> c = webClient
                .post()
                .uri(s)
                .bodyValue(new DirectMailRequest(Collections.singletonList(userId.toString()), content))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Error during the HTTP call")))
                .bodyToMono(String.class);
    }

    private ResponseEntity<Object> checkRequest(PermissionRequest request) {
        if (request.getUserID() == null || request.getTournamentID() == null || request.getCreatorID() == null) {
            log.error("Invalid user or tournament id request");
            return new ResponseEntity<>("Invalid user or tournament id request", getHeaders(), HttpStatus.BAD_REQUEST);
        }

        if (tournamentService.getTournament(request.getTournamentID()) == null) {
            log.error("Invalid tournament id request");
            return new ResponseEntity<>("Invalid tournament id request", getHeaders(), HttpStatus.BAD_REQUEST);
        }

        if (!tournamentService.getTournament(request.getTournamentID()).getStatus()) {
            log.error("Tournament already ended");
            return new ResponseEntity<>("Tournament already ended", getHeaders(), HttpStatus.BAD_REQUEST);
        }

        if (tournamentService.permissionExists(request.getTournamentID(), request.getUserID())) {
            log.error("Permission already inserted");
            return new ResponseEntity<>("Permission already inserted", getHeaders(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Valid request", getHeaders(), HttpStatus.OK);
    }
}