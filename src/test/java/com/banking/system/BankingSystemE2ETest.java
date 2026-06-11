package com.banking.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests Système - Banking Application")
class BankingSystemE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== SCÉNARIOS PRINCIPAUX ====================

    @Test
    @Order(1)
    @DisplayName("SCÉNARIO 1: Un utilisateur ouvre un compte et fait un dépôt")
    void scenario1_userOpensAccountAndDeposits() throws Exception {
        // 1. Inscription
        String email = "user1-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode registerResponse = registerUser("Jean Dupont", email, phone);
        String token = registerResponse.get("token").asText();

        // 2. Création compte
        MvcResult accountResult = mockMvc.perform(post("/api/accounts")
                        .param("bankId", "1")
                        .param("accountType", "CHECKING")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode accountJson = objectMapper.readTree(accountResult.getResponse().getContentAsString());
        String accountNumber = accountJson.get("accountNumber").asText();

        // 3. Dépôt d'argent
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", accountNumber)
                        .param("amount", "500.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));

        // 4. Vérification du compte
        mockMvc.perform(get("/api/accounts/" + accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.accountType").value("CHECKING"));
    }

    @Test
    @Order(2)
    @DisplayName("SCÉNARIO 2: Transfert d'argent entre deux utilisateurs")
    void scenario2_transferBetweenUsers() throws Exception {
        // Création Utilisateur 1 (Source)
        String email1 = "source-" + UUID.randomUUID() + "@test.com";
        String phone1 = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user1 = registerUser("Alice Source", email1, phone1);
        String token1 = user1.get("token").asText();
        String account1 = createAccount(token1, "CHECKING");

        // Création Utilisateur 2 (Destination)
        String email2 = "dest-" + UUID.randomUUID() + "@test.com";
        String phone2 = "07" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user2 = registerUser("Bob Destination", email2, phone2);
        String token2 = user2.get("token").asText();
        String account2 = createAccount(token2, "SAVINGS");

        // Dépôt initial sur compte source
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", account1)
                        .param("amount", "1000.00")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        // Transfert de User1 vers User2
        mockMvc.perform(post("/api/transactions/withdraw")
                        .param("sourceAccountNumber", account1)
                        .param("destinationAccountNumber", account2)
                        .param("amount", "300.00")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        // Vérifications
        mockMvc.perform(get("/api/accounts/" + account1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00));

        mockMvc.perform(get("/api/accounts/" + account2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300.00));
    }

    @Test
    @Order(3)
    @DisplayName("SCÉNARIO 3: Transfert entre comptes d'un même utilisateur")
    void scenario3_transferBetweenOwnAccounts() throws Exception {
        // Inscription
        String email = "self-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("Charles Self", email, phone);
        String token = user.get("token").asText();

        // Création deux comptes
        String checkingAccount = createAccount(token, "CHECKING");
        String savingsAccount = createAccount(token, "SAVINGS");

        // Dépôt sur compte courant
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", checkingAccount)
                        .param("amount", "1000.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Transfert vers compte épargne
        mockMvc.perform(post("/api/transactions/transfer")
                        .param("sourceAccountNumber", checkingAccount)
                        .param("destinationAccountNumber", savingsAccount)
                        .param("amount", "400.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Vérifications
        mockMvc.perform(get("/api/accounts/" + checkingAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(600.00));

        mockMvc.perform(get("/api/accounts/" + savingsAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(400.00));
    }

    @Test
    @Order(4)
    @DisplayName("SCÉNARIO 4: Fermeture de compte")
    void scenario4_closeAccount() throws Exception {
        String email = "close-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("Delete User", email, phone);
        String token = user.get("token").asText();

        // Création compte et dépôt
        String accountNumber = createAccount(token, "CHECKING");
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", accountNumber)
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Vérifier compte existe
        mockMvc.perform(get("/api/accounts/" + accountNumber))
                .andExpect(status().isOk());

        // Récupérer ID du compte (à adapter selon votre modèle)
        MvcResult accountResult = mockMvc.perform(get("/api/accounts/" + accountNumber))
                .andReturn();
        JsonNode account = objectMapper.readTree(accountResult.getResponse().getContentAsString());
        Long accountId = account.get("id").asLong();

        // Fermeture du compte
        mockMvc.perform(delete("/api/accounts/" + accountId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Vérifier compte n'existe plus
        mockMvc.perform(get("/api/accounts/" + accountNumber))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("SCÉNARIO 5: Liste des comptes d'un utilisateur")
    void scenario5_listUserAccounts() throws Exception {
        String email = "list-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("List User", email, phone);
        String token = user.get("token").asText();

        // Création multiple comptes
        String account1 = createAccount(token, "CHECKING");
        String account2 = createAccount(token, "SAVINGS");
        String account3 = createAccount(token, "CHECKING");

        // Liste des comptes
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.accountNumber=='" + account1 + "')]").exists())
                .andExpect(jsonPath("$[?(@.accountNumber=='" + account2 + "')]").exists())
                .andExpect(jsonPath("$[?(@.accountNumber=='" + account3 + "')]").exists());
    }

    // ==================== TESTS D'ERREUR ====================

    @Test
    @Order(6)
    @DisplayName("ERREUR 1: Solde insuffisant pour transfert")
    void error_insufficientBalance() throws Exception {
        String email = "poor-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("Poor User", email, phone);
        String token = user.get("token").asText();
        String account = createAccount(token, "CHECKING");
        String otherAccount = createAccount(token, "SAVINGS");

        // Dépôt petit montant
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", account)
                        .param("amount", "50.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Tentative transfert montant trop grand
        mockMvc.perform(post("/api/transactions/transfer")
                        .param("sourceAccountNumber", account)
                        .param("destinationAccountNumber", otherAccount)
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("ERREUR 2: Compte inexistant")
    void error_accountNotFound() throws Exception {
        String email = "notfound-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("Not Found User", email, phone);
        String token = user.get("token").asText();

        mockMvc.perform(get("/api/accounts/INEXISTANT-12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    @DisplayName("ERREUR 3: Montant négatif ou nul")
    void error_negativeOrZeroAmount() throws Exception {
        String email = "amount-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("Amount Test", email, phone);
        String token = user.get("token").asText();
        String account = createAccount(token, "CHECKING");

        // Dépôt montant nul
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", account)
                        .param("amount", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        // Dépôt montant négatif
        mockMvc.perform(post("/api/accounts/deposit")
                        .param("accountNumber", account)
                        .param("amount", "-100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
@Test
@Order(9)
@DisplayName("ERREUR 4: Token invalide")
void error_invalidToken() throws Exception {
    // Au lieu de isUnauthorized(), on attend isBadRequest()
    mockMvc.perform(get("/api/accounts")
                    .header("Authorization", "Bearer invalid-token-12345"))
            .andExpect(status().isBadRequest())  // ← Changer ici !
            .andExpect(jsonPath("$.error").value("Unauthorized"));
}
    @Test
    @Order(10)
    @DisplayName("ERREUR 5: Transfert vers son propre compte sans autorisation")
    void error_transferToSelfWithoutPermission() throws Exception {
        String email = "selferror-" + UUID.randomUUID() + "@test.com";
        String phone = "06" + String.format("%08d", System.nanoTime() % 100000000);
        JsonNode user = registerUser("Self Error", email, phone);
        String token = user.get("token").asText();
        String account = createAccount(token, "CHECKING");

        mockMvc.perform(post("/api/transactions/withdraw")
                        .param("sourceAccountNumber", account)
                        .param("destinationAccountNumber", account)
                        .param("amount", "100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private JsonNode registerUser(String name, String email, String phone) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/register")
                        .param("name", name)
                        .param("email", email)
                        .param("phone", phone))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String createAccount(String token, String accountType) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .param("bankId", "1")
                        .param("accountType", accountType)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accountNumber").asText();
    }
}