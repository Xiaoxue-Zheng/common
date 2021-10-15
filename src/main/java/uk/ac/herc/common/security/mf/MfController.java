package uk.ac.herc.common.security.mf;

import org.springframework.beans.factory.annotation.Autowired;
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
import uk.ac.herc.common.security.JwtTokenProvider;

import javax.validation.Valid;

import static uk.ac.herc.common.security.Constants.AUTHORIZATION_HEADER;
import static uk.ac.herc.common.security.mf.MfAuthenticationExceptionReason.MFOTPSENDER_NOT_CONFIGURED;


@Controller
@RequestMapping("/api")
public class MfController {

    @Autowired
    private MfProperties properties;

    @Autowired
    private MfService mfService;

    @PostMapping(path = "/mf-authenticate/code")
    public ResponseEntity<Boolean> sendMfCode(@Valid @RequestBody MfCodeDTO login) {
        if (properties.getAuthBeforeGenerateOtp()) {
            AuthenticationManager authenticationManager = ApplicationContextHolder.getBean(AuthenticationManager.class);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(login.getUserName(), login.getPassword());
            authenticationManager.authenticate(authenticationToken);
        }
        String otp = mfService.generateOTP(login.getUserName());
        if (properties.getSendOtp()) {
            MfOptSender sender = ApplicationContextHolder.getBeanIfExist(MfOptSender.class);
            if (null == sender) {
                throw new MfAuthenticationException(MFOTPSENDER_NOT_CONFIGURED);
            }
            sender.sendUserOpt(login.getUserName(), otp);
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping("/mf-authenticate")
    public ResponseEntity<JWTToken> authenticate(@Valid @RequestBody MfAuthDTO loginVM) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginVM.getUserName(),
            loginVM.getPassword()
        );
        AuthenticationManager authenticationManager = ApplicationContextHolder.getBean(AuthenticationManager.class);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        MfValidateResultDTO validateOTPResult = mfService.validateOTP(loginVM.getUserName(), loginVM.getCode());
        if (!validateOTPResult.getSuccess()) {
            throw new MfAuthenticationException(validateOTPResult.getFailReason(), validateOTPResult.getFailedAttempts());
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        JwtTokenProvider jwtTokenProvider = ApplicationContextHolder.getBeanIfExist(JwtTokenProvider.class);
        if (null == jwtTokenProvider)
            throw new MfAuthenticationException(MfAuthenticationExceptionReason.JWTTOKENPROVIDER_NOT_CONFIGURED);
        String jwt = jwtTokenProvider.createToken(authentication, loginVM.isRememberMe());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    public MfProperties getProperties() {
        return properties;
    }

    public void setProperties(MfProperties properties) {
        this.properties = properties;
    }

    public MfService getMfService() {
        return mfService;
    }

    public void setMfService(MfService mfService) {
        this.mfService = mfService;
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
