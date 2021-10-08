package uk.ac.herc.common.security.mf;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class MfProperties {

    private Long codeTimeout = 30L;

    private String timeUnit = TimeUnit.MINUTES.name();

    private Integer maxTryTime = 5;

    private Boolean authBeforeSendOTP = false;

    public Long getCodeTimeout() {
        return codeTimeout;
    }

    public void setCodeTimeout(Long codeTimeout) {
        this.codeTimeout = codeTimeout;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Integer getMaxTryTime() {
        return maxTryTime;
    }

    public void setMaxTryTime(Integer maxTryTime) {
        this.maxTryTime = maxTryTime;
    }

    public Boolean getAuthBeforeSendOTP() {
        return authBeforeSendOTP;
    }

    public void setAuthBeforeSendOTP(Boolean authBeforeSendOTP) {
        this.authBeforeSendOTP = authBeforeSendOTP;
    }
}
