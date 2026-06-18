package com.simpleerp.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * User management is admin-only: the demo admin can create and list users, the new MEMBER can log
 * in but is forbidden from the user endpoints, and a duplicate username is rejected.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserManagementTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    private String tokenFor(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    @Test
    void adminCreatesAndListsUsersWhileMembersAreForbidden() throws Exception {
        String adminToken = tokenFor("admin", "admin123");

        // Admin creates a MEMBER.
        mvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"casey","displayName":"Casey Member",
                                 "role":"MEMBER","password":"casey-password"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("casey"))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        // A duplicate username is rejected.
        mvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"casey","displayName":"Dup","role":"MEMBER","password":"another-pass"}
                                """))
                .andExpect(status().isConflict());

        // The new member can sign in but cannot reach the admin-only user endpoints.
        String memberToken = tokenFor("casey", "casey-password");
        mvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());

        // Admin can list users.
        mvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
