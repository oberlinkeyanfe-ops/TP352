```mermaid
flowchart TD
    Start(["Début"]) --> Input["Recevoir: name, code"]
    Input --> CheckCode{"Code existe?"}
    CheckCode -->|"Oui"| ErrorCode["Exception: Code déjà utilisé"]
    CheckCode -->|"Non"| CreateBank["Créer nouvelle Bank"]
    CreateBank --> SaveBank["Sauvegarder en BDD"]
    SaveBank --> AutoGen["Générer: id, createdAt"]
    AutoGen --> ReturnBank["Retourner 201 Created"]
    ErrorCode --> BuildError["Construire erreur 400"]
    ReturnBank --> Return(["Fin"])
    BuildError --> Return
```