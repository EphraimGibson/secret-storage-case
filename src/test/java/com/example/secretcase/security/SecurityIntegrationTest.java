package com.example.secretcase.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "app.admin-username=testUsername",
        "app.admin-password-hash=$2a$12$lBv8QyPgufoFoZmlEHls1uCUVEVUbgzSNgGgLH97YdjUVLpXDR60K"
})
class SecurityIntegrationTest {
    private static final String TEST_USERNAME = "testUsername";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testShouldAllowValidCredentials() throws Exception {
        mockMvc.perform(get("/").with(httpBasic(TEST_USERNAME, "password1")))
                .andExpect(status().isOk());
    }

    @Test
    void testShouldRejectRequestWithoutCredentials() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testShouldRejectUnknownUsername() throws Exception {
        mockMvc.perform(get("/").with(httpBasic("unknown-user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testShouldRejectWrongPassword() throws Exception {
        mockMvc.perform(get("/").with(httpBasic(TEST_USERNAME, "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testShouldRejectSixthFailedPasswordAttemptWithTooManyRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/").with(httpBasic(TEST_USERNAME, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(get("/").with(httpBasic(TEST_USERNAME, "wrong-password")))
                .andExpect(status().isTooManyRequests());
    }
}