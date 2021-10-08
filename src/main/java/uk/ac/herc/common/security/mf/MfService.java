package uk.ac.herc.common.security.mf;

public interface MfService {
    void generateAndSendOTP(String userName);

    MfLoginResultDTO validateOTP(String userName, String pin);
}
