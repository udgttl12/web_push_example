package com.example.webpush.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.Security;

@Configuration
public class WebPushConfig {

    @Value("${web-push.vapid.public-key}")
    private String publicKey;

    @Value("${web-push.vapid.private-key}")
    private String privateKey;

    @Value("${web-push.vapid.subject}")
    private String subject;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public PushService pushService() throws GeneralSecurityException {
        PushService pushService = new PushService();
        pushService.setPublicKey(publicKey);
        pushService.setPrivateKey(privateKey);
        pushService.setSubject(subject);
        return pushService;
    }

    @Bean
    public String vapidPublicKey() {
        return publicKey;
    }
}
