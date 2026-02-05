package com.soniya.expense_tracker.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import io.github.cdimascio.dotenv.Dotenv;

@Component
public class GoogleTokenVerifier {

    private String clientId;

    public GoogleTokenVerifier() {
        Dotenv dotenv = Dotenv.load();
        this.clientId = dotenv.get("GOOGLE_CLIENT_ID");
    }

    public String getEmailFromToken(String idTokenString) throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            return payload.getEmail();
        }

        return null;

    }

}
