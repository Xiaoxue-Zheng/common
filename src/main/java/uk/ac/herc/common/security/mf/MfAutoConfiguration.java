package uk.ac.herc.common.security.mf;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "mf.enable", havingValue = "true")
@Import(MfProperties.class)
@AutoConfigurationPackage
@EnableJpaRepositories
public class MfAutoConfiguration {

    @Bean
    MfController mfController() {
        return new MfController();
    }

    @Bean
    MfService mfService() {
        return new MfServiceImpl();
    }

}
