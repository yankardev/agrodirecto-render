/* =====================================================================
   AgroDirecto - Preparación de SQL Server
   Crea las 3 bases (una por microservicio) y un usuario de aplicación.
   Las TABLAS las crea cada servicio al iniciar (Hibernate, ddl-auto=update)
   y los datos de prueba los cargan las clases DatosIniciales.
   Ejecutar una sola vez con un usuario administrador (sa).
   ===================================================================== */
IF DB_ID('AgroAuthDB') IS NULL CREATE DATABASE AgroAuthDB;
GO
IF DB_ID('AgroCatalogoDB') IS NULL CREATE DATABASE AgroCatalogoDB;
GO
IF DB_ID('AgroPedidoDB') IS NULL CREATE DATABASE AgroPedidoDB;
GO

IF SUSER_ID('agro_user') IS NULL
    CREATE LOGIN agro_user WITH PASSWORD = 'Agro_2026_Pass1', CHECK_POLICY = OFF;
GO

USE AgroAuthDB;
IF USER_ID('agro_user') IS NULL CREATE USER agro_user FOR LOGIN agro_user;
ALTER ROLE db_owner ADD MEMBER agro_user;
GO
USE AgroCatalogoDB;
IF USER_ID('agro_user') IS NULL CREATE USER agro_user FOR LOGIN agro_user;
ALTER ROLE db_owner ADD MEMBER agro_user;
GO
USE AgroPedidoDB;
IF USER_ID('agro_user') IS NULL CREATE USER agro_user FOR LOGIN agro_user;
ALTER ROLE db_owner ADD MEMBER agro_user;
GO
