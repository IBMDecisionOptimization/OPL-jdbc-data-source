--
-- Create database
-- Comment the following lines if you already have a database for testing
USE master;
GO
If DB_ID(N'custom_data_source') IS NOT NULL
	DROP DATABASE custom_data_source;
CREATE DATABASE custom_data_source;
	
-- If you have a database for testing, please edit the following line:
USE custom_data_source;

--
-- OilData table
--
If DB_ID(N'OilData') IS NOT NULL
	DROP TABLE OilData;
	
CREATE TABLE OilData (
	name		VARCHAR(30) PRIMARY KEY,
	capacity	INT,
	price		FLOAT,
	octane		FLOAT,
	lead		FLOAT,
	CONSTRAINT unique_name_oil UNIQUE (name)
);
INSERT INTO OilData (name, capacity, price, octane, lead) VALUES
	('Crude1', 5000, 45, 12, 0.5),
	('Crude2', 5000, 35, 6, 2),
	('Crude3', 5000, 25, 8, 3);


--
-- GasData table
--
If DB_ID(N'GasData') IS NOT NULL
	DROP TABLE GasData;

CREATE TABLE GasData (
	name		VARCHAR(30) PRIMARY KEY,
	demand 		INT,
	price		FLOAT,
	octane		FLOAT,
	lead		FLOAT,
	CONSTRAINT unique_name_gas UNIQUE(name)
);

INSERT INTO GasData (name, demand, price, octane, lead) VALUES
	('Super', 3000, 70, 10, 1),
	('Regular', 2000, 60, 8, 2),
	('Diesel', 1000, 50, 6, 1);
	
--
-- Result table
--