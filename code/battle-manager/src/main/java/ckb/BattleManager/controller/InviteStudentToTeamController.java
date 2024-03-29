package ckb.BattleManager.controller;

import ckb.BattleManager.dto.in.InviteStudentTeamRequest;
import ckb.BattleManager.dto.out.DirectMailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@RestController
@RequestMapping("/api/battle")
@Slf4j
public class InviteStudentToTeamController extends Controller {
    private final WebClient.Builder webClientBuilder;

    public InviteStudentToTeamController() {
        this.webClientBuilder = WebClient.builder();
    }

    /**
     * Invite a student to a Team: send a request to the mail service to send an email
     * to the student with the link to join the team
     *
     * @param request
     * @return
     */
    @PostMapping("/invite-student-to-team")
    public ResponseEntity<Object> inviteStudentToTeam(@RequestBody InviteStudentTeamRequest request) {
        log.info("[API REQUEST] Invite student to team request with id_team: {}, id_student: {}", request.getIdTeam(), request.getIdStudent());
        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .post()
                    .uri(mailServiceUri + "/api/mail/direct")
                    .bodyValue(
                            new DirectMailRequest(List.of(request.getIdStudent().toString()),
                                    "You have been invited to join the team: " + request.getIdTeam()
                                            + ". Please join the team by clicking on the link below:\n" +
                                            "link: " + "http://localhost:3000/invite.html?idTeam=" + request.getIdTeam() +
                                            "&idUser=" + request.getIdStudent()
                            )
                    )
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            log.info("Successfully sent email: {}", response);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("[EXCEPTION] {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
