package ckb.AccountManager.controller;

import ckb.AccountManager.dto.SignUpRequest;
import ckb.AccountManager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account/sign-up")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SignUpController extends Controller {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> signUp(@RequestBody SignUpRequest request) {
        if (emailInUse(request.getEmail())) {
            log.error("Email {} already in use", request.getEmail());
            return new ResponseEntity<>("Email already in use", getHeaders(), HttpStatus.CONFLICT);
        }
        if(roleNotValid(request.getRole())) {
            log.error("Role {} is not valid, valid roles are: Student, Educator", request.getRole());
            return new ResponseEntity<>("Role not valid", getHeaders(), HttpStatus.BAD_REQUEST);
        }

        userService.createUser(request);
        log.info("Account created for email {}", request.getEmail());

        return new ResponseEntity<>( "Account created", getHeaders(), HttpStatus.CREATED);
    }

    private boolean roleNotValid(String role) {
        return !role.equals("student") && !role.equals("educator");
    }

    private boolean emailInUse(String email) {
        return userService.emailInUse(email);
    }
}
