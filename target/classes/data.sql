-- Insertion des banques initiales
INSERT INTO banks (name, code, created_at) 
SELECT 'Banque Nationale', 'BNAT', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM banks WHERE code = 'BNAT');

INSERT INTO banks (name, code, created_at) 
SELECT 'Crédit Agricole', 'CAG', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM banks WHERE code = 'CAG');

INSERT INTO banks (name, code, created_at) 
SELECT 'Société Générale', 'SG', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM banks WHERE code = 'SG');

INSERT INTO banks (name, code, created_at) 
SELECT 'Union Banque Africaine', 'UBA', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM banks WHERE code = 'UBA');

INSERT INTO banks (name, code, created_at) 
SELECT 'CCA', 'CCA', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM banks WHERE code = 'CCA');

