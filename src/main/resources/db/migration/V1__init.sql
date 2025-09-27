-- V1__init.sql - estrutura e carga mínima
CREATE TABLE IF NOT EXISTS motos (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    modelo VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
);

-- Inserir dois registros reais
IF NOT EXISTS (SELECT 1 FROM motos WHERE placa = 'ABC1D23')
    INSERT INTO motos (placa, modelo, status) VALUES ('ABC1D23', 'Honda CG 160', 'ATIVA');

IF NOT EXISTS (SELECT 1 FROM motos WHERE placa = 'XYZ4E56')
    INSERT INTO motos (placa, modelo, status) VALUES ('XYZ4E56', 'Yamaha Fazer 250', 'EM_MANUTENCAO');
