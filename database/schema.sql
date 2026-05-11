CREATE TABLE users (
    user_id NUMBER PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    email VARCHAR2(150) NOT NULL UNIQUE,
    password VARCHAR2(64) NOT NULL,
    role VARCHAR2(20) DEFAULT 'USER' NOT NULL,
    active NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT chk_users_active CHECK (active IN (0, 1))
);

CREATE TABLE items (
    item_id NUMBER PRIMARY KEY,
    user_id NUMBER NOT NULL,
    type VARCHAR2(10) NOT NULL,
    title VARCHAR2(150) NOT NULL,
    description VARCHAR2(500) NOT NULL,
    location VARCHAR2(150) NOT NULL,
    item_date DATE NOT NULL,
    image_path VARCHAR2(255),
    status VARCHAR2(20) DEFAULT 'OPEN' NOT NULL,
    CONSTRAINT fk_items_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_items_type CHECK (type IN ('LOST', 'FOUND')),
    CONSTRAINT chk_items_status CHECK (status IN ('OPEN', 'CLAIMED', 'CLOSED', 'RESOLVED'))
);

CREATE TABLE claims (
    claim_id NUMBER PRIMARY KEY,
    item_id NUMBER NOT NULL,
    claimant_id NUMBER NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    CONSTRAINT fk_claims_item FOREIGN KEY (item_id) REFERENCES items(item_id),
    CONSTRAINT fk_claims_user FOREIGN KEY (claimant_id) REFERENCES users(user_id),
    CONSTRAINT chk_claims_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE items_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE claims_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE OR REPLACE TRIGGER users_bir
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF :NEW.user_id IS NULL THEN
        SELECT users_seq.NEXTVAL INTO :NEW.user_id FROM dual;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER items_bir
BEFORE INSERT ON items
FOR EACH ROW
BEGIN
    IF :NEW.item_id IS NULL THEN
        SELECT items_seq.NEXTVAL INTO :NEW.item_id FROM dual;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER claims_bir
BEFORE INSERT ON claims
FOR EACH ROW
BEGIN
    IF :NEW.claim_id IS NULL THEN
        SELECT claims_seq.NEXTVAL INTO :NEW.claim_id FROM dual;
    END IF;
END;
/

INSERT INTO users (name, email, password, role, active)
VALUES ('System Administrator', 'admin@campus.edu', '008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601', 'ADMIN', 1);

INSERT INTO users (name, email, password, role, active)
VALUES ('Campus Student', 'student@campus.edu', '008c70392e3abfbd0fa47bbc2ed96aa99bd49e159727fcba0f2e6abeb3a9d601', 'USER', 1);

COMMIT;
