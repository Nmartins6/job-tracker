package dev.nicolas.JobTracker.interfaces.rest.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jobtracker.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowUserCreationWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserPayload("nicolas.public@example.com")))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAuthenticatedUserWhenCredentialsAreValid() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserPayload("nicolas.auth@example.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(httpBasic("nicolas.auth@example.com", "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nicolas.auth@example.com"));
    }

    private String createUserPayload(String email) {
        return """
                {
                  "name": "Nicolas",
                  "email": "%s",
                  "password": "123456",
                  "headline": "Backend Developer",
                  "location": "Brasil",
                  "bio": "Bio"
                }
                """.formatted(email);
    }
}
