package com.banking.unitaires;

import com.banking.controller.*;
import com.banking.exception.ResourceNotFoundException;
import com.banking.model.*;
import com.banking.service.*;

import io.jsonwebtoken.MalformedJwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires Complets - Banking App")
class AllUnitTests {

    private MockMvc mockMvc;

    @Mock private AccountService accountService;
    @Mock private TransactionService transactionService;
    @Mock private TokenService tokenService;
    @Mock private BankService bankService;
    @Mock private UserService userService;

    @InjectMocks private AccountController accountController;
    @InjectMocks private TransactionController transactionController;
    @InjectMocks private TokenController tokenController;
    @InjectMocks private BankController bankController;
    @InjectMocks private UserController userController;

    private User sampleUser;
    private Account sampleAccount;
    private Bank sampleBank;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController, transactionController, 
                tokenController, bankController, userController).build();

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("Jean Dupont");
        sampleUser.setEmail("jean@email.com");
        sampleUser.setPhone("0612345678");
        sampleUser.setToken("token123");

        sampleAccount = new Account();
        sampleAccount.setId(1L);
        sampleAccount.setAccountNumber("ACC-12345678");
        sampleAccount.setBalance(new BigDecimal("1000.00"));
        sampleAccount.setAccountType("CHECKING");

        sampleBank = new Bank();
        sampleBank.setId(1L);
        sampleBank.setName("Banque Populaire");
        sampleBank.setCode("BP");
    }

    // ==================== TESTS ACCOUNT CONTROLLER ====================

    @Test
    @DisplayName("ACCOUNT: Creation compte avec succes")
    void account_create_success() throws Exception {
        String authHeader = "Bearer token123";
        when(accountService.createAccount(eq(authHeader), eq(1L), eq("CHECKING")))
            .thenReturn(sampleAccount);

        mockMvc.perform(post("/api/accounts")
                .header("Authorization", authHeader)
                .param("bankId", "1")
                .param("accountType", "CHECKING"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC-12345678"));
    }

    @Test
    @DisplayName("ACCOUNT: Creation compte avec type null retourne 400")
    void account_create_null_type() throws Exception {
        String authHeader = "Bearer token123";
        
        when(accountService.createAccount(anyString(), anyLong(), isNull()))
            .thenThrow(new RuntimeException("Le type de compte est obligatoire"));

        mockMvc.perform(post("/api/accounts")
                .header("Authorization", authHeader)
                .param("bankId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ACCOUNT: Creation compte avec type invalide retourne 400")
    void account_create_invalid_type() throws Exception {
        String authHeader = "Bearer token123";
        
        when(accountService.createAccount(anyString(), anyLong(), eq("INVALID")))
            .thenThrow(new RuntimeException("Type de compte invalide"));

        mockMvc.perform(post("/api/accounts")
                .header("Authorization", authHeader)
                .param("bankId", "1")
                .param("accountType", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ACCOUNT: Dépôt montant valide")
    void account_deposit_success() throws Exception {
        String authHeader = "Bearer token123";
        Account updatedAccount = new Account();
        updatedAccount.setBalance(new BigDecimal("1500.00"));
        
        when(accountService.deposit(eq(authHeader), eq("ACC-12345678"), eq(new BigDecimal("500.00"))))
            .thenReturn(updatedAccount);

        mockMvc.perform(post("/api/accounts/deposit")
                .header("Authorization", authHeader)
                .param("accountNumber", "ACC-12345678")
                .param("amount", "500.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    @DisplayName("ACCOUNT: Dépôt montant nul retourne 400")
    void account_deposit_zero_fails() throws Exception {
        String authHeader = "Bearer token123";
        
        when(accountService.deposit(anyString(), anyString(), eq(BigDecimal.ZERO)))
            .thenThrow(new RuntimeException("Le montant doit être positif"));

        mockMvc.perform(post("/api/accounts/deposit")
                .header("Authorization", authHeader)
                .param("accountNumber", "ACC-12345678")
                .param("amount", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ACCOUNT: Dépôt montant negatif retourne 400")
    void account_deposit_negative_fails() throws Exception {
        String authHeader = "Bearer token123";
        
        when(accountService.deposit(anyString(), anyString(), eq(new BigDecimal("-100.00"))))
            .thenThrow(new RuntimeException("Le montant doit être positif"));

        mockMvc.perform(post("/api/accounts/deposit")
                .header("Authorization", authHeader)
                .param("accountNumber", "ACC-12345678")
                .param("amount", "-100.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ACCOUNT: Recuperation liste comptes")
    void account_getAll_success() throws Exception {
        String authHeader = "Bearer token123";
        when(accountService.getUserAccounts(eq(authHeader)))
            .thenReturn(Arrays.asList(sampleAccount, new Account()));

        mockMvc.perform(get("/api/accounts")
                .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("ACCOUNT: Recuperation compte par numero")
    void account_getByNumber_success() throws Exception {
        when(accountService.getAccountByNumber("ACC-12345678")).thenReturn(sampleAccount);

        mockMvc.perform(get("/api/accounts/ACC-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-12345678"));
    }

    @Test
    @DisplayName("ACCOUNT: Recuperation compte inexistant retourne 404")
    void account_getByNumber_notFound() throws Exception {
        when(accountService.getAccountByNumber("INEXISTANT"))
            .thenThrow(new RuntimeException("Compte non trouvé"));

        mockMvc.perform(get("/api/accounts/INEXISTANT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ACCOUNT: Suppression compte avec succes")
    void account_delete_success() throws Exception {
        String authHeader = "Bearer token123";
        doNothing().when(accountService).deleteAccount(eq(authHeader), eq(1L));

        mockMvc.perform(delete("/api/accounts/1")
                .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ACCOUNT: Suppression compte non autorise retourne 400")
    void account_delete_unauthorized() throws Exception {
        String authHeader = "Bearer token123";
        doThrow(new RuntimeException("Non autorisé à supprimer ce compte"))
            .when(accountService).deleteAccount(eq(authHeader), eq(1L));

        mockMvc.perform(delete("/api/accounts/1")
                .header("Authorization", authHeader))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS TRANSACTION CONTROLLER ====================

    @Test
    @DisplayName("TRANSACTION: Transfert avec token valide")
    void transaction_transfer_success() throws Exception {
        String authHeader = "Bearer token123";
        doNothing().when(transactionService).transfer(
            eq(authHeader), eq("ACC-SOURCE"), eq("ACC-DEST"), eq(new BigDecimal("200.00")));

        mockMvc.perform(post("/api/transactions/transfer")
                .header("Authorization", authHeader)
                .param("sourceAccountNumber", "ACC-SOURCE")
                .param("destinationAccountNumber", "ACC-DEST")
                .param("amount", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("TRANSACTION: Transfert sans token doit retourner 401")
    void transaction_without_token_fails() throws Exception {
        mockMvc.perform(post("/api/transactions/transfer")
                        .param("sourceAccountNumber", "ACC-SOURCE")
                        .param("destinationAccountNumber", "ACC-DEST")
                        .param("amount", "100.00"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TRANSACTION: Transfert solde insuffisant retourne 400")
    void transaction_transfer_insufficient() throws Exception {
        String authHeader = "Bearer token123";
        doThrow(new RuntimeException("Solde insuffisant"))
            .when(transactionService).transfer(anyString(), anyString(), anyString(), any());

        mockMvc.perform(post("/api/transactions/transfer")
                .header("Authorization", authHeader)
                .param("sourceAccountNumber", "ACC-SOURCE")
                .param("destinationAccountNumber", "ACC-DEST")
                .param("amount", "999999.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TRANSACTION: Retrait vers autre utilisateur avec succes")
    void transaction_withdraw_success() throws Exception {
        String authHeader = "Bearer token123";
        doNothing().when(transactionService).withdraw(
            eq(authHeader), eq("ACC-SOURCE"), eq("ACC-DEST"), eq(new BigDecimal("100.00")));

        mockMvc.perform(post("/api/transactions/withdraw")
                .header("Authorization", authHeader)
                .param("sourceAccountNumber", "ACC-SOURCE")
                .param("destinationAccountNumber", "ACC-DEST")
                .param("amount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    // ==================== TESTS TOKEN CONTROLLER ====================

    @Test
    @DisplayName("TOKEN: Inscription avec succes")
    void token_register_success() throws Exception {
        when(tokenService.createUserAndGetToken(eq("Jean Dupont"), eq("jean@email.com"), eq("0612345678")))
            .thenReturn(sampleUser);

        mockMvc.perform(post("/api/register")
                .param("name", "Jean Dupont")
                .param("email", "jean@email.com")
                .param("phone", "0612345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.message").value("Compte créé avec succès"));
    }

    @Test
    @DisplayName("TOKEN: Inscription avec email invalide retourne 400")
    void token_register_invalid_email_fails() throws Exception {
        mockMvc.perform(post("/api/register")
                        .param("name", "Jean Dupont")
                        .param("email", "email-invalide")
                        .param("phone", "0612345678"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TOKEN: Inscription avec email deja existant retourne 409")
    void token_register_duplicate_email_fails() throws Exception {
        when(tokenService.createUserAndGetToken(anyString(), eq("existant@email.com"), anyString()))
                .thenThrow(new RuntimeException("Email déjà utilisé"));

        mockMvc.perform(post("/api/register")
                        .param("name", "Jean Dupont")
                        .param("email", "existant@email.com")
                        .param("phone", "0699999999"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("TOKEN: Connexion avec succes")
    void token_login_success() throws Exception {
        when(tokenService.loginAndGetToken(eq("jean@email.com"), eq("0612345678")))
            .thenReturn(sampleUser);

        mockMvc.perform(post("/api/login")
                .param("email", "jean@email.com")
                .param("phone", "0612345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.message").value("Connexion réussie"));
    }

    @Test
    @DisplayName("TOKEN: Connexion avec identifiants incorrects retourne 401")
    void token_login_wrong_credentials_fails() throws Exception {
        when(tokenService.loginAndGetToken(eq("wrong@email.com"), eq("0000000000")))
                .thenThrow(new RuntimeException("Email ou telephone incorrect"));

        mockMvc.perform(post("/api/login")
                        .param("email", "wrong@email.com")
                        .param("phone", "0000000000"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== TESTS BANK CONTROLLER ====================

    @Test
    @DisplayName("BANK: Liste toutes les banques")
    void bank_getAll_success() throws Exception {
        when(bankService.getAllBanks()).thenReturn(Arrays.asList(sampleBank, new Bank()));

        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("BANK: Detail banque par ID")
    void bank_getById_success() throws Exception {
        when(bankService.getBankById(1L)).thenReturn(sampleBank);

        mockMvc.perform(get("/api/banks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Banque Populaire"));
    }

    @Test
    @DisplayName("BANK: Banque non trouvee retourne 404")
    void bank_getById_notFound() throws Exception {
        when(bankService.getBankById(999L)).thenThrow(new RuntimeException("Banque non trouvée"));

        mockMvc.perform(get("/api/banks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("BANK: Creation banque avec succes")
    void bank_create_success() throws Exception {
        when(bankService.createBank(any(Bank.class))).thenReturn(sampleBank);

        mockMvc.perform(post("/api/banks")
                .param("name", "Banque Populaire")
                .param("code", "BP"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("BANK: Creation banque nom vide retourne 400")
    void bank_create_empty_name_fails() throws Exception {
        mockMvc.perform(post("/api/banks")
                        .param("name", "")
                        .param("code", "BP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("BANK: Suppression banque avec succes")
    void bank_delete_success() throws Exception {
        doNothing().when(bankService).deleteBank(1L);

        mockMvc.perform(delete("/api/banks/1"))
                .andExpect(status().isNoContent());
    }

    // ==================== TESTS USER CONTROLLER ====================

    @Test
    @DisplayName("USER: Liste tous les utilisateurs")
    void user_getAll_success() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(sampleUser, new User()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("USER: Detail utilisateur par ID")
    void user_getById_success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Jean Dupont"));
    }

    @Test
    @DisplayName("USER: Utilisateur non trouve retourne 404")
    void user_getById_notFound() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new RuntimeException("Utilisateur non trouvé"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("USER: Creation utilisateur avec succes")
    void user_create_success() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/api/users")
                .param("name", "Jean Dupont")
                .param("email", "jean@email.com")
                .param("phone", "0612345678"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("USER: Mise a jour utilisateur avec succes")
    void user_update_success() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Jean Martin");
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                .param("name", "Jean Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jean Martin"));
    }

    @Test
    @DisplayName("USER: Suppression utilisateur avec succes")
    void user_delete_success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Exception - ResourceNotFoundException")
    void exception_resourceNotFound() throws Exception {
        when(accountService.getAccountByNumber("INEXISTANT"))
            .thenThrow(new ResourceNotFoundException("Compte non trouvé: INEXISTANT"));

        mockMvc.perform(get("/api/accounts/INEXISTANT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("Exception - MalformedJwt")
    void exception_malformedJwt() throws Exception {
        String authHeader = "Bearer invalid-token";
        
        when(accountService.getUserAccounts(authHeader))
            .thenThrow(new MalformedJwtException("Token mal formé"));

        mockMvc.perform(get("/api/accounts")
                .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Exception - RuntimeException avec message token")
    void exception_runtimeException_token() throws Exception {
        String authHeader = "Bearer token";
        
        when(accountService.getUserAccounts(authHeader))
            .thenThrow(new RuntimeException("Token invalide"));

        mockMvc.perform(get("/api/accounts")
                .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("Transfer - compte source = compte destination")
    void transfer_same_account() throws Exception {
        String authHeader = "Bearer token123";
        doThrow(new RuntimeException("Le compte source et destination doivent être différents"))
            .when(transactionService).transfer(anyString(), eq("ACC-123"), eq("ACC-123"), any());

        mockMvc.perform(post("/api/transactions/transfer")
                .header("Authorization", authHeader)
                .param("sourceAccountNumber", "ACC-123")
                .param("destinationAccountNumber", "ACC-123")
                .param("amount", "100.00"))
                .andExpect(status().isBadRequest());
    }
}