package com.banking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🏦 Banking Application API")
                        .description("""
                                API RESTful pour la gestion bancaire
                                
                                ## 🔑 Comment utiliser :
                                
                                ### 1. Créer un compte
                                Utilisez **POST /api/register** avec votre nom, email et téléphone
                                
                                ### 2. S'authentifier dans Swagger
                                - Copiez le token reçu
                                - Cliquez sur le bouton **Authorize** 🔒 en haut
                                - Collez votre token et validez
                                
                                ### 3. Utiliser l'API
                                Une fois authentifié, vous pouvez :
                                - ✅ Créer des comptes bancaires
                                - ✅ Faire des dépôts
                                - ✅ Effectuer des retraits
                                - ✅ Faire des transferts entre vos comptes
                                
                                > 💡 Le token sera automatiquement ajouté à toutes vos requêtes
                                """)
                        .version("1.0")
                        .contact(new Contact()
                                .name("Support Banking App")
                                .email("support@banking.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Collez votre token JWT ici (sans 'Bearer ')")));
    }
}