INSERT INTO users (id, role, gender, dni, password, first_name, last_name, creation_date)
VALUES (1, 'ADMIN', 'MALE', '46622977', '$2a$10$Pr3bROhCE33NacH0R7XDMenztJ0EXVi6NwAaPGJIhr/mXdGCwq986', 'Lorenzo', 'Sarlo', '2025-01-01');

INSERT INTO users (id, role, gender, dni, password, first_name, last_name, creation_date)
VALUES (2, 'STAFF', 'MALE', '12345678', '$2a$10$Pr3bROhCE33NacH0R7XDMenztJ0EXVi6NwAaPGJIhr/mXdGCwq986', 'Matias', 'Freccero', '2025-01-01');

INSERT INTO users (id, role, gender, dni, password, first_name, last_name, creation_date)
VALUES (3, 'USER', 'MALE', '87654321', '$2a$10$Pr3bROhCE33NacH0R7XDMenztJ0EXVi6NwAaPGJIhr/mXdGCwq986', 'Franco', 'Cataldi', '2025-01-01');

INSERT INTO memberships (id, user_id, user_dni, status, type, payment_method, payment_date, next_payment_date, created_by, last_modified_by)
VALUES (1, 3, '87654321', 'INACTIVE', 'MONTHLY', 'CASH', '2024-12-01', '2025-01-01', '87654321', null);

INSERT INTO memberships (id, user_id, user_dni, status, type, payment_method, payment_date, next_payment_date, created_by, last_modified_by)
VALUES (2, 3, '87654321', 'ACTIVE', 'ANNUALLY', 'CARD', '2025-01-01', '2099-01-01', '46622977', null);

INSERT INTO memberships (id, user_id, user_dni, status, type, payment_method, payment_date, next_payment_date, created_by, last_modified_by)
VALUES (3, null, '99999999', 'INACTIVE', 'THREE_MONTHS', 'CASH', '2025-01-01', '2025-04-01', '12345678', null);

ALTER TABLE memberships ALTER COLUMN id RESTART WITH 4;
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
