-- Book Management (J2.S.P0113) — SQL Server schema
IF DB_ID(N'BookManagement') IS NULL
BEGIN
    CREATE DATABASE BookManagement;
END
GO

USE BookManagement;
GO

IF OBJECT_ID(N'dbo.Books', N'U') IS NOT NULL
    DROP TABLE dbo.Books;
GO

CREATE TABLE dbo.Books (
    BookId       INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    BookCode     NVARCHAR(20)  NOT NULL UNIQUE,
    BookName     NVARCHAR(200) NOT NULL,
    Author       NVARCHAR(150) NOT NULL,
    Publisher    NVARCHAR(150) NOT NULL,
    PublishYear  INT           NOT NULL,
    ForRent      BIT           NOT NULL CONSTRAINT DF_Books_ForRent DEFAULT (0)
);
GO

-- Sample data (optional)
INSERT INTO dbo.Books (BookCode, BookName, Author, Publisher, PublishYear, ForRent) VALUES
(N'DBI202', N'C# .Net', N'Bill Gates', N'Microsoft', 2016, 0),
(N'JAVA01', N'Core Java 01', N'Oracle', N'Oracle Press', 2018, 1),
(N'JAVA02', N'Core Java 02', N'Oracle', N'Oracle Press', 2019, 0);
GO
