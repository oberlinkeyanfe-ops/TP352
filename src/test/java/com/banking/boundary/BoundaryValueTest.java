package com.banking.boundary;

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
@DisplayName("Tests Black Box - Valeurs Limites")
class BoundaryValueTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;
    private static String accountNumber;

    @BeforeEach
    void setUp() throws Exception {
        if (authToken == null) {
            String email = "boundary-" + UUID.randomUUID() + "@test.com";
            String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
            
            MvcResult registerResult = mockMvc.perform(post("/api/register")
                            .param("name", "Boundary User")
                            .param("email", email)
                            .param("phone", phone))
                    .andExpect(status().isOk())
                    .andReturn();
            
            JsonNode json = objectMapper.readTree(registerResult.getResponse().getContentAsString());
            authToken = json.get("token").asText();

            MvcResult accountResult = mockMvc.perform(post("/api/accounts")
                            .param("bankId", "1")
                            .param("accountType", "CHECKING")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isCreated())
                    .andReturn();
            
            JsonNode accountJson = objectMapper.readTree(accountResult.getResponse().getContentAsString());
            accountNumber = accountJson.get("accountNumber").asText();
        }
    }

    @Test
    @Order(14)
    @DisplayName("BV-TRANSFER-01: Montant = 0.01")
    void transfer_just_above_zero() throws Exception {
        String email = "transfermin-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        
        MvcResult registerResult = mockMvc.perform(post("/api/register")
                        .param("name", "Transfer Min")
                        .param("email", email)
                        .param("phone", phone))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String token = json.get("token").asText();

        MvcResult accountResult = mockMvc.perform(post("/api/accounts")
                        .param("bankId", "1")
                        .param("accountType", "CHECKING")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode accountJson = objectMapper.readTree(accountResult.getResponse().getContentAsString());
        String accNumber = accountJson.get("accountNumber").asText();

        MvcResult accountResult2 = mockMvc.perform(post("/api/accounts")
                        .param("bankId", "1")
                        .param("accountType", "SAVINGS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode accountJson2 = objectMapper.readTree(accountResult2.getResponse().getContentAsString());
        String destNumber = accountJson2.get("accountNumber").asText();

        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", accNumber)
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transactions/transfer")
                        .param("sourceAccountNumber", accNumber)
                        .param("destinationAccountNumber", destNumber)
                        .param("amount", "0.01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}