CREATE TABLE token_renovacion (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 usuario_id BIGINT NOT NULL, token_hash VARCHAR(64) NOT NULL,
 fecha_expiracion TIMESTAMP(6) NOT NULL, revocado BOOLEAN NOT NULL DEFAULT FALSE,
 CONSTRAINT uk_token_hash UNIQUE(token_hash),
 CONSTRAINT fk_token_usuario FOREIGN KEY(usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;
