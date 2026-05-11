DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO column_count
    FROM user_tab_columns
    WHERE table_name = 'USERS' AND column_name = 'ROLE';

    IF column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE users ADD role VARCHAR2(20) DEFAULT ''USER''';
    END IF;
END;
/

DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO column_count
    FROM user_tab_columns
    WHERE table_name = 'USERS' AND column_name = 'ACTIVE';

    IF column_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE users ADD active NUMBER(1) DEFAULT 1';
    END IF;
END;
/

UPDATE users SET role = 'USER' WHERE role IS NULL;
UPDATE users SET active = 1 WHERE active IS NULL;
UPDATE items SET status = 'OPEN' WHERE status IS NULL;

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE users MODIFY role DEFAULT ''USER'' NOT NULL';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1442 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE users MODIFY active DEFAULT 1 NOT NULL';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1442 THEN
            RAISE;
        END IF;
END;
/

DECLARE
    constraint_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO constraint_count
    FROM user_constraints
    WHERE table_name = 'USERS' AND constraint_name = 'CHK_USERS_ROLE';

    IF constraint_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN (''USER'', ''ADMIN''))';
    END IF;
END;
/

DECLARE
    constraint_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO constraint_count
    FROM user_constraints
    WHERE table_name = 'USERS' AND constraint_name = 'CHK_USERS_ACTIVE';

    IF constraint_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE users ADD CONSTRAINT chk_users_active CHECK (active IN (0, 1))';
    END IF;
END;
/

DECLARE
    CURSOR status_constraints IS
        SELECT uc.constraint_name
        FROM user_constraints uc
        JOIN user_cons_columns ucc ON uc.constraint_name = ucc.constraint_name
        WHERE uc.table_name = 'ITEMS'
          AND ucc.table_name = 'ITEMS'
          AND ucc.column_name = 'STATUS'
          AND uc.constraint_type = 'C';
BEGIN
    FOR item_constraint IN status_constraints LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE items DROP CONSTRAINT ' || item_constraint.constraint_name;
    END LOOP;
END;
/

ALTER TABLE items ADD CONSTRAINT chk_items_status
CHECK (status IN ('OPEN', 'CLAIMED', 'CLOSED', 'RESOLVED'));

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE items MODIFY status DEFAULT ''OPEN'' NOT NULL';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1442 THEN
            RAISE;
        END IF;
END;
/

INSERT INTO users (name, email, password, role, active)
SELECT 'System Administrator',
       'admin@campus.edu',
       '008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601',
       'ADMIN',
       1
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE LOWER(email) = 'admin@campus.edu'
);

UPDATE users
SET role = 'ADMIN', active = 1
WHERE LOWER(email) = 'admin@campus.edu';

COMMIT;
