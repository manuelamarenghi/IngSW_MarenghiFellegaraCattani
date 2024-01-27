package ckb.AccountManager.controller;

import ckb.AccountManager.dto.SignInRequest;
import ckb.AccountManager.dto.answers.SignInAnswer;
import ckb.AccountManager.model.User;
import ckb.AccountManager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/sign-in")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SignInController extends Controller {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> signIn(@RequestBody SignInRequest request) {
        if (credentialsNotValid(request.getEmail(), request.getPassword())) {
            log.error("Credentials not valid for email {}", request.getEmail());
            return new ResponseEntity<>("Credentials not valid", getHeaders(), HttpStatus.BAD_REQUEST);
        }

        Long userID = userService.getUserIDByEmail(request.getEmail());
        User user = userService.getUserById(userID);
        SignInAnswer answer = SignInAnswer.builder()
                .userID(userID.toString())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
        return new ResponseEntity<>(answer, getHeaders(), HttpStatus.OK);
    }

    private boolean credentialsNotValid(String email, String password) {
        return !userService.accountExists(email, password);
    }
}
