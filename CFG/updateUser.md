```mermaid
flowchart TD
    Start(["Début"]) --> Input["Recevoir: id, name (optionnel), email (optionnel), phone (optionnel)"]
    Input --> FindUser{"User trouvé?"}
    FindUser -->|"Non"| Error404["Exception: User not found<br/>Retour 404"]
    FindUser -->|"Oui"| CheckName{"name fourni et non vide?"}
    CheckName -->|"Oui"| SetName["Mettre à jour name"]
    CheckName -->|"Non"| CheckEmail{"email fourni et format valide?"}
    SetName --> CheckEmail
    CheckEmail -->|"Oui"| ValidateEmailFormat{"Format email valide?"}
    ValidateEmailFormat -->|"Non"| ErrorEmailFormat["Exception: Format email invalide"]
    ValidateEmailFormat -->|"Oui"| SetEmail["Mettre à jour email"]
    CheckEmail -->|"Non"| CheckPhone{"phone fourni?"}
    SetEmail --> CheckPhone
    CheckPhone -->|"Oui"| SetPhone["Mettre à jour phone"]
    CheckPhone -->|"Non"| SaveUser["Sauvegarder User"]
    SetPhone --> SaveUser
    SaveUser --> AutoUpdate["Mettre à jour updatedAt"]
    AutoUpdate --> ReturnUser["Retourner 200 OK"]
    Error404 --> BuildError["Construire erreur 404"]
    ErrorEmailFormat --> BuildError400["Construire erreur 400"]
    ReturnUser --> Return(["Fin"])
    BuildError --> Return
    BuildError400 --> Returns
```