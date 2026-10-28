package pe.upeu.ecorecicla.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ZoneConfig {

    @Value("${app.business-zone:America/Lima}")
    private String businessZone;

    @Bean
    public ZoneId businessZoneId() {
        return ZoneId.of(businessZone);
    }

    @Bean
    public Clock businessClock(ZoneId businessZoneId) {
        return Clock.system(businessZoneId);
    }
}

