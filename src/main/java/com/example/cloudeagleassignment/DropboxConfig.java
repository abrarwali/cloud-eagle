package com.example.cloudeagleassignment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dropbox")
@Setter
@Getter
public class DropboxConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}