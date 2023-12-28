package ckb.MailService.controller;

import ckb.MailService.dto.MultipleMailRequest;
import ckb.MailService.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/mail/direct")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DirectEmailSender extends EmailSender {

    private final WebClient webClient;
    private final MailService mailService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> sendEmail(@RequestBody MultipleMailRequest request) {

        List<String> userIDs = request.getUserIDs();
        List<String> addresses;
        try {
            addresses = getEmailAddresses(userIDs);
        } catch (Exception e) {
            log.error("Error while retrieving email address for users {}\n", userIDs);
            return new ResponseEntity<>(getHeaders(), HttpStatus.BAD_REQUEST);
        }

        if (mailService.sendEmail(addresses, request.getContent())) {
            log.info("Email sent to {}\n", addresses);
            return new ResponseEntity<>(getHeaders(), HttpStatus.OK);
        }

        log.error("No valid address found for: {}\n", userIDs);
        return new ResponseEntity<>(getHeaders(), HttpStatus.BAD_REQUEST);
    }

    private List<String> getEmailAddresses(List<String> userIDs) {
        // request will be constructed like this: http://localhost:8080/api/mail/single?userID=1&userID=2&userID=3 ...
        return webClient.get()
                .uri("http://localhost:8080/api/account/mail",
                        uriBuilder -> uriBuilder.queryParam("userID", userIDs).build())
                .retrieve()
                .bodyToMono(String.class) // we expect the response to only be a String containing the email addresses
                .flatMapMany(responseBody -> Flux.fromArray(responseBody.split(",")))
                .map(String::trim)
                .collectList() // collect the response into a list
                .block(); // block until the response is received
    }
}
