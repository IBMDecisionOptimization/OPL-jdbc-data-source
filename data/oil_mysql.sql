--
-- Create database
-- Comment the following lines if you already have a database for testing
CREATE DATABASE IF NOT EXISTS custom_data_source;
-- If you have a database for testing, please edit the following line:
USE custom_data_source;

--
-- OilData table
--
DROP TABLE IF EXISTS `OilData`;
CREATE TABLE IF NOT EXISTS `OilData`(
	name		VARCHAR(30) NOT NULL,
	capacity	INT(6),
	price		FLOAT(6,2),
	octane		FLOAT(6,2),
	lead		FLOAT(6,2),
	UNIQUE(`name`),
	PRIMARY KEY (`name`)
);
INSERT INTO `OilData` (`name`, `capacity`, `price`, `octane`, `lead`) VALUES
	('Crude1', 5000, 45, 12, 0.5),
	('Crude2', 5000, 35, 6, 2),
	('Crude3', 5000, 25, 8, 3);


--
-- GasData table
--
DROP TABLE IF EXISTS `GasData`;
CREATE TABLE IF NOT EXISTS `GasData` (
	name		VARCHAR(30) NOT NULL,
	demand 		INT(6),
	price		FLOAT(6,2),
	octane		FLOAT(6,2),
	lead		FLOAT(6,2),
	UNIQUE(`name`),
	PRIMARY KEY (`name`)
);

INSERT INTO `GasData` (`name`, `demand`, `price`, `octane`, `lead`) VALUES
	('Super', 3000, 70, 10, 1),
	('Regular', 2000, 60, 8, 2),
	('Diesel', 1000, 50, 6, 1);
	
--
-- Result table
--