package uk.ac.herc.common.security.mf;

public enum MfAuthenticationExceptionReason {
    JWTTOKENPROVIDER_NOT_CONFIGURED("The application doesn't configure the JwtTokenProvider," +
        " the authentication api only supports JWT token, please configure a bean implements JwtTokenProvider, " +
        "if you are using JHipster, you can use TokenProvider to implement JwtTokenProvider"),
    MFOTPSENDER_NOT_CONFIGURED("The application doesn't configure the MfOptSender," +
        "So won't sent the otp, if you are using Jhipster, you can use MailService to configure one"),

    PIN_HAS_EXPIRED("Pin for two factor authentication has expired. Cancel sign in and try again."),
    PIN_NOT_EXIST("Pin for two factor authentication failed. Cancel sign in and try again."),
    PIN_VALIDATION_ATTEMPT_EXCEEDED("User had five failed attempts in the previous login. Login is not permitted. Please try again after 30 minutes"),
    PIN_NOT_MATCH("Pin is wrong");

    private final String message;

    private MfAuthenticationExceptionReason(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
