package com.anjar.portfolio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.resend")
public class EmailProperties {
    private String apiKey;
    private String fromEmail;
    private String fromName;
    private String toEmail;
    private boolean enabled = true;
}