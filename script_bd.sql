-- script_bd.sql - DDL da tabela principal (SQL Server)
IF OBJECT_ID('dbo.motos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.motos (
        id BIGINT IDENTITY(1,1) CONSTRAINT PK_motos PRIMARY KEY,
        placa VARCHAR(10) NOT NULL UNIQUE,
        modelo VARCHAR(80) NOT NULL,
        status VARCHAR(30) NOT NULL,
        created_at DATETIMEOFFSET NOT NULL CONSTRAINT DF_motos_created DEFAULT SYSDATETIMEOFFSET()
    );
END;

-- Comentários (extended properties)
EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Tabela de motos usadas na solução', 
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'motos';

EXEC sys.sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Placa da moto (única)', 
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'motos',
    @level2type = N'COLUMN', @level2name = 'placa';
