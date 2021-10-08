package uk.ac.herc.common.security.mf;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.ac.herc.common.ApplicationContextHolder;
import uk.ac.herc.common.security.JwtTokenProvider;

import static uk.ac.herc.common.security.Constants.AUTHORIZATION_HEADER;


@Controller
@RequestMapping("/api")
public class MfController {
    private final MfProperties configuration;
    private final MfService mfService;

    public MfController(MfProperties configuration, MfService mfService) {
        this.configuration = configuration;
        this.mfService = mfService;
    }

    @PostMapping(path = "/mf-authenticate/code")
    public ResponseEntity<Boolean> sendMfCode(@RequestBody MfCodeDTO login) {
        if(configuration.getAuthBeforeSendOTP()){
            AuthenticationManager authenticationManager = ApplicationContextHolder.getContext().getBean(AuthenticationManager.class);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getUserName(), login.getPassword());
            authenticationManager.authenticate(authenticationToken);
        }
        mfService.generateAndSendOTP(login.getUserName());
        return ResponseEntity.ok(true);
    }

    @PostMapping("/mf-authenticate")
    public ResponseEntity<JWTToken> authenticate(@RequestBody MfAuthDTO loginVM) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginVM.getUserName(),
            loginVM.getPassword()
        );
        AuthenticationManager authenticationManager = ApplicationContextHolder.getContext().getBean(AuthenticationManager.class);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        MfLoginResultDTO validateOTPResult = mfService.validateOTP(loginVM.getUserName(), loginVM.getCode());
        if(!validateOTPResult.getAuthenticated()){
            throw new MfAuthenticationException(validateOTPResult.getFailReason());
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        JwtTokenProvider jwtTokenProvider = ApplicationContextHolder.getBean(JwtTokenProvider.class);
        String jwt = jwtTokenProvider.createToken(authentication, loginVM.isRememberMe());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
