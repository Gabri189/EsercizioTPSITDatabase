CREATE DATABASE IF NOT EXISTS eserciziotpsit;
USE eserciziotpsit;
CREATE TABLE IF NOT EXISTS `eserciziotpsit`.`studenti` (
  `id` INT NOT NULL,
  `nome` VARCHAR(45) NOT NULL,
  `cognome` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));
  INSERT INTO Studenti (id, nome, cognome)
  VALUES (1,"Andrea", "Buono"),
  (2,"Stefano", "Caiazzo"),
  (3,"Lorenzo", "Cibecchini"),
  (4,"Dario", "Colzi"),
  (5,"Gabriele", "Tozzi");
