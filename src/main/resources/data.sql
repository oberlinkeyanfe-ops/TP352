-- Insertion des banques initiales
INSERT INTO banks (name, code, created_at) VALUES ('Banque Nationale', 'BNAT', CURRENT_TIMESTAMP);
INSERT INTO banks (name, code, created_at) VALUES ('Crédit Agricole', 'CAG', CURRENT_TIMESTAMP);
INSERT INTO banks (name, code, created_at) VALUES ('Société Générale', 'SG', CURRENT_TIMESTAMP);
INSERT INTO banks (name, code, created_at) VALUES ('Union Banque Africaine', 'UBA', CURRENT_TIMESTAMP);
INSERT INTO banks (name, code, created_at) VALUES ('CCA', 'CCA', CURRENT_TIMESTAMP);

-- Insertion d'un utilisateur test
INSERT INTO users (name, email, created_at, updated_at) VALUES ('Jean', 'jean@email.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);