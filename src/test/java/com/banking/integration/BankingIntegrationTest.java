package com.banking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'Intégration - Contrôleur ↔ Service")
class BankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String testToken;
    private static String testAccountNumber;

    @Test
    @Order(1)
    @DisplayName("IT-01: Cycle complet inscription → création compte → dépôt")
    void integration_completeUserJourney() throws Exception {
        // Inscription
        String email = "integ-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        
        MvcResult registerResult = mockMvc.perform(post("/api/register")
                        .param("name", "Integration User")
                        .param("email", email)
                        .param("phone", phone))
                .andExpect(status().isOk())
                .andReturn();
        
        JsonNode registerJson = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        testToken = registerJson.get("token").asText();
        Long userId = registerJson.get("userId").asLong();

        // Vérification création utilisateur
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // Création compte
        MvcResult accountResult = mockMvc.perform(post("/api/accounts")
                        .param("bankId", "1")
                        .param("accountType", "CHECKING")
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode accountJson = objectMapper.readTree(accountResult.getResponse().getContentAsString());
        testAccountNumber = accountJson.get("accountNumber").asText();

        // Dépôt
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", testAccountNumber)
                        .param("amount", "250.00")
                        .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250.00));
    }

    @Test
    @Order(2)
    @DisplayName("IT-02: Authentification et accès aux ressources protégées")
    void integration_transactionAfterAuth() throws Exception {
        // 1. Creer un utilisateur d'abord
        String email = "test-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        
        mockMvc.perform(post("/api/register")
                        .param("name", "Test User")
                        .param("email", email)
                        .param("phone", phone))
                        .andExpect(status().isOk());
        
        // 2. Maintenant la connexion fonctionne
        mockMvc.perform(post("/api/login")
                        .param("email", email)
                        .param("phone", phone))
                        .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("IT-03: Récupération liste banques")
    void integration_getBanks() throws Exception {
        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("IT-04: Création et suppression banque")
    void integration_createAndDeleteBank() throws Exception {
        // Création banque
        MvcResult createResult = mockMvc.perform(post("/api/banks")
                        .param("name", "Banque Test")
                        .param("code", "BTST"))
                .andExpect(status().isCreated())
                .andReturn();
        
        JsonNode bankJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long bankId = bankJson.get("id").asLong();

        // Vérification
        mockMvc.perform(get("/api/banks/" + bankId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Banque Test"));

        // Suppression
        mockMvc.perform(delete("/api/banks/" + bankId))
                .andExpect(status().isNoContent());

        // Vérification suppression
        mockMvc.perform(get("/api/banks/" + bankId))
                .andExpect(status().isNotFound());
    }
}