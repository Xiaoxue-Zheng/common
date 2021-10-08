package uk.ac.herc.common.security.mf;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.ac.herc.common.security.mf.MfAuthenticationExceptionReason.*;

@Service
@Transactional
public class MfServiceImpl implements MfService{

    private final MfAuthenticationRepository repository;

    private final MfProperties configuration;

    private final MfOptSender mfOptSender;

    public MfServiceImpl(MfAuthenticationRepository repository, MfOptSender mfOptSender, MfProperties configuration) {
        this.repository = repository;
        this.configuration = configuration;
        this.mfOptSender = mfOptSender;
    }

    @Override
    public void generateAndSendOTP(String userName) {
        generateOneTimeCode(userName);
    }

    @Override
    public MfLoginResultDTO validateOTP(String userName, String pin) {
        if(skipValidate(pin)){
            return new MfLoginResultDTO(true, 0);
        }
        Optional<MfAuthenticationEntity> authenticationOptional = repository.findOneByUserName(userName);
        if(!authenticationOptional.isPresent()){
            return new MfLoginResultDTO(false, PIN_NOT_EXIST);
        }
        MfAuthenticationEntity authentication = authenticationOptional.get();
        if (authentication.isExpired()){
            return new MfLoginResultDTO(false, PIN_HAS_EXPIRED);
        }
        Integer failedAttempts = authentication.getFailedAttempts();
        if (failedAttempts >= 5) {
            return new MfLoginResultDTO(false, failedAttempts, PIN_VALIDATION_ATTEMPT_EXCEEDED);
        } else {
            Boolean isPinValid = authentication.getPin().equals(pin);
            if (!isPinValid) {
                failedAttempts++;
                authentication.setFailedAttempts(failedAttempts);
                repository.save(authentication);
                return new MfLoginResultDTO(isPinValid, authentication.getFailedAttempts(), PIN_NOT_MATCH);
            }
            return new MfLoginResultDTO(isPinValid, authentication.getFailedAttempts());
        }
    }

    protected boolean skipValidate(String pin) {
        return false;
    }

    private void generateOneTimeCode(String userName) {
        String otp = generateOtp();
        Date expiryDateTime = new Date(new Date().getTime() + TimeUnit.valueOf(configuration.getTimeUnit()).toMillis(configuration.getCodeTimeout()));;
        MfAuthenticationEntity authentication = null;
        Optional<MfAuthenticationEntity> authenticationOptional = repository.findOneByUserName(userName);
        if (!authenticationOptional.isPresent()) {
            authentication = new MfAuthenticationEntity(userName, otp, expiryDateTime, 0);
            repository.save(authentication);
        } else {
            authentication = authenticationOptional.get();
            if (authentication.getFailedAttempts() >= configuration.getMaxTryTime() && !authentication.isExpired()) {
                throw new MfAuthenticationException(
                    PIN_VALIDATION_ATTEMPT_EXCEEDED);
            }
            authentication.setPin(otp);
            authentication.setExpiryDateTime(expiryDateTime);
            authentication.setFailedAttempts(0);
            repository.save(authentication);
        }
        mfOptSender.sendUserOpt(userName, otp);
    }

    private String generateOtp() {
        String generatedPin = "";
        int pinLength = 6;
        for (int i = 0; i < pinLength; i++) {
            generatedPin += randomInteger(0, 9);
        }

        return generatedPin;
    }

    private int randomInteger(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }
}
