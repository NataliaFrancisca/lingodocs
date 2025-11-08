package br.com.nat.lingodocs.controller;

import br.com.nat.lingodocs.dto.auth.request.ConfirmSignUpRequest;
import br.com.nat.lingodocs.dto.auth.response.MessageResponse;
import br.com.nat.lingodocs.dto.auth.request.RefreshTokenRequest;
import br.com.nat.lingodocs.dto.auth.request.SignUpRequest;
import br.com.nat.lingodocs.service.CognitoAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final CognitoAuthService authService;

    public AuthController(CognitoAuthService authService){
        this.authService = authService;
    }

    @PostMapping("/sigup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request){
        try{
            String userSub = authService.signUp(
                request.email(), request.password(), request.name()
            );

            return ResponseEntity.ok(new MessageResponse("Usuário registrado. Verifique seu e-mail", userSub));
        }catch (Exception ex){
            return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage(), null));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmSignUp(@RequestBody ConfirmSignUpRequest request){
        try{
            authService.confirmSignUp(request.email(), request.code());
            return ResponseEntity.ok(new MessageResponse("Usuário confirmado com sucesso.", null));
        }catch (Exception ex){
            return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage(), null));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignUpRequest request){
        try{
            var tokens = authService.signIn(
              request.email(),
              request.password()
            );

            return ResponseEntity.ok(tokens);
        }catch (Exception ex){
            return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage(), null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request){
        try{
            var tokens = authService.refreshToken(request.refreshToken());
            return ResponseEntity.ok(tokens);
        }catch (Exception ex){
            return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage(), null));
        }
    }
}
