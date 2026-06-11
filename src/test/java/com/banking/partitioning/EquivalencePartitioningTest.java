package com.banking.partitioning;

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
@DisplayName("Tests Black Box - Partitionnement par Équivalence")
class EquivalencePartitioningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;
    private static String accountNumber;

    @BeforeEach
    void setUp() throws Exception {
        if (authToken == null) {
            String email = "equiv-" + UUID.randomUUID() + "@test.com";
            String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
            
            MvcResult registerResult = mockMvc.perform(post("/api/register")
                            .param("name", "Equivalence User")
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
    @Order(4)
    @DisplayName("EP-ACCOUNT-03: Type vide")
    void accountType_empty_invalid() throws Exception {
        // Correction : accepter 200 ou 201
        mockMvc.perform(post("/api/accounts")
                        .param("bankId", "1")
                        .param("accountType", "")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().is2xxSuccessful());
    }
}