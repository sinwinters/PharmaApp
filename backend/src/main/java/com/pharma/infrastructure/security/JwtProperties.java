package com.pharma.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long accessTtl = 3600000L;
    private long refreshTtl = 604800000L;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public long getAccessTtl() { return accessTtl; }
    public void setAccessTtl(long accessTtl) { this.accessTtl = accessTtl; }
    public long getRefreshTtl() { return refreshTtl; }
    public void setRefreshTtl(long refreshTtl) { this.refreshTtl = refreshTtl; }
}
