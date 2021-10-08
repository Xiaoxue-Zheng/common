package uk.ac.herc.common.security.mf;

public class MfAuthenticationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private MfAuthenticationExceptionReason reason;

    public MfAuthenticationException(MfAuthenticationExceptionReason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public String getMessage() {
        return reason.getMessage();
    }

    public MfAuthenticationExceptionReason getReason() {
        return reason;
    }

    public void setReason(MfAuthenticationExceptionReason reason) {
        this.reason = reason;
    }

}
