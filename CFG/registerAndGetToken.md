```mermaid
flowchart TD
    Start(["Début"]) --> Input["Recevoir: name, email, phone"]
    Input --> ValidateName{"Nom valide?<br/>(non vide)"}
    ValidateName -->|"Non"| ErrorName["Exception: Nom obligatoire"]
    ValidateName -->|"Oui"| ValidateEmailFormat{"Email format valide?"}
    ValidateEmailFormat -->|"Non"| ErrorEmailFormat["Exception: Format email invalide"]
    ValidateEmailFormat -->|"Oui"| CheckEmail{"Email existe déjà?"}
    CheckEmail -->|"Oui"| ErrorEmail["Exception: Email déjà utilisé"]
    CheckEmail -->|"Non"| NormalizePhone["Normaliser téléphone<br/>(+33X -> 0X)"]
    NormalizePhone --> CheckPhone{"Phone existe déjà?"}
    CheckPhone -->|"Oui"| ErrorPhone["Exception: Téléphone déjà utilisé"]
    CheckPhone -->|"Non"| CreateUser["Créer nouvel User"]
    CreateUser --> SetFields["Définir: name, email, phone normalisé"]
    SetFields --> SaveUser["Sauvegarder User en BDD"]
    SaveUser --> GenToken["Générer token JWT"]
    GenToken --> SetToken["Attribuer token au User"]
    SetToken --> UpdateUser["Mettre à jour User avec token"]
    UpdateUser --> BuildResponse["Construire réponse 200 + token"]
    ErrorName --> BuildError["Construire réponse erreur 400"]
    ErrorEmailFormat --> BuildError
    ErrorEmail --> BuildError["Construire réponse erreur 409"]
    ErrorPhone --> BuildError["Construire réponse erreur 409"]
    BuildResponse --> Return(["Fin"])
    BuildError --> Return
```