-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : dim. 15 fév. 2026 à 19:34
-- Version du serveur : 8.3.0
-- Version de PHP : 8.2.18

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `mindcare`
--

-- --------------------------------------------------------

--
-- Structure de la table `availabilities`
--

DROP TABLE IF EXISTS `availabilities`;
CREATE TABLE IF NOT EXISTS `availabilities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') COLLATE utf8mb4_general_ci NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `is_available` tinyint(1) DEFAULT '1',
  `therapist_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `event`
--

DROP TABLE IF EXISTS `event`;
CREATE TABLE IF NOT EXISTS `event` (
  `id_event` int NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `type` enum('online','physique','hybride') COLLATE utf8mb4_general_ci DEFAULT NULL,
  `date_start` datetime NOT NULL,
  `date_end` datetime DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `max_participants` int DEFAULT NULL,
  `status` enum('draft','published','cancelled') COLLATE utf8mb4_general_ci DEFAULT 'draft',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `organizer_id` int DEFAULT NULL,
  `image_url` mediumtext COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id_event`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `event`
--

INSERT INTO `event` (`id_event`, `title`, `description`, `type`, `date_start`, `date_end`, `location`, `max_participants`, `status`, `created_at`, `organizer_id`, `image_url`) VALUES
(3, 'gggg', 'desc', 'online', '2026-02-27 21:22:00', '2026-03-01 21:23:00', 'laouina', 2, 'draft', '2026-02-12 10:52:05', NULL, 'https://saccotrend.co.ke/wp-content/uploads/2025/07/Johnny-Sins-nude.png');

-- --------------------------------------------------------

--
-- Structure de la table `question`
--

DROP TABLE IF EXISTS `question`;
CREATE TABLE IF NOT EXISTS `question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question_text` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `required` tinyint(1) DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `image_path` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `question`
--

INSERT INTO `question` (`id`, `question_text`, `required`, `created_at`, `image_path`) VALUES
(5, 'hows life today', 1, '2026-02-15 12:23:33', 'imagepath'),
(6, 'rsdtrfyghjlkml', 1, '2026-02-15 13:03:39', 'hjjhjh'),
(7, 'how are you feeling', 1, '2026-02-15 13:04:00', 'cxgfgn'),
(8, 'ujyngvrf', 1, '2026-02-15 13:41:29', 'vfd');

-- --------------------------------------------------------

--
-- Structure de la table `quiz`
--

DROP TABLE IF EXISTS `quiz`;
CREATE TABLE IF NOT EXISTS `quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `category` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `total_questions` int DEFAULT '0',
  `active` tinyint(1) DEFAULT '1',
  `min_score` int DEFAULT '0',
  `max_score` int DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `quiz`
--

INSERT INTO `quiz` (`id`, `title`, `description`, `category`, `total_questions`, `active`, `min_score`, `max_score`, `created_at`, `updated_at`) VALUES
(5, 'ng fvd', 'bgvrfc', 'DEPRESSION', 2, 1, 0, 10, '2026-02-15 12:30:14', '2026-02-15 12:30:14'),
(6, ',ujnyhtbg', ' jhbgf', 'ANXIETY', 1, 1, 0, 10, '2026-02-15 12:31:35', '2026-02-15 12:31:35'),
(7, ',ujnyhbtgv', 'njyhtbgvrfec', 'BURNOUT', 2, 1, 0, 10, '2026-02-15 12:32:05', '2026-02-15 12:32:05'),
(8, 'jnhbg', 'ytrgvfecd', 'DEPRESSION', 1, 1, 0, 10, '2026-02-15 12:34:58', '2026-02-15 12:34:58'),
(9, 'nytbhv', 'bgvfrc', 'ANXIETY', 1, 1, 0, 10, '2026-02-15 12:35:15', '2026-02-15 12:35:15');

-- --------------------------------------------------------

--
-- Structure de la table `quiz_question`
--

DROP TABLE IF EXISTS `quiz_question`;
CREATE TABLE IF NOT EXISTS `quiz_question` (
  `quiz_id` bigint NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`quiz_id`,`question_id`),
  KEY `fk_junction_quiz` (`quiz_id`),
  KEY `fk_junction_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `quiz_question`
--

INSERT INTO `quiz_question` (`quiz_id`, `question_id`) VALUES
(5, 6),
(5, 7),
(6, 7),
(7, 5),
(7, 6),
(8, 7),
(9, 7);

-- --------------------------------------------------------

--
-- Structure de la table `registrations`
--

DROP TABLE IF EXISTS `registrations`;
CREATE TABLE IF NOT EXISTS `registrations` (
  `id_registration` int NOT NULL,
  `user_id` int NOT NULL,
  `event_id` int NOT NULL,
  `status` enum('registered','cancelled','attended') COLLATE utf8mb4_general_ci DEFAULT 'registered',
  `registration_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `qr_code` text COLLATE utf8mb4_general_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `review`
--

DROP TABLE IF EXISTS `review`;
CREATE TABLE IF NOT EXISTS `review` (
  `id_review` int NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `id_client` int NOT NULL,
  PRIMARY KEY (`id_review`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `review_reply`
--

DROP TABLE IF EXISTS `review_reply`;
CREATE TABLE IF NOT EXISTS `review_reply` (
  `id_reply` int NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `id_review` int NOT NULL,
  `id_usr` int NOT NULL,
  PRIMARY KEY (`id_reply`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `therapists`
--

DROP TABLE IF EXISTS `therapists`;
CREATE TABLE IF NOT EXISTS `therapists` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `phone_number` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `specialization` varchar(150) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `consultation_type` enum('ONLINE','IN_PERSON','BOTH') COLLATE utf8mb4_general_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_general_ci DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `therapists`
--

INSERT INTO `therapists` (`id`, `first_name`, `last_name`, `email`, `password`, `phone_number`, `specialization`, `description`, `consultation_type`, `status`, `created_at`, `updated_at`) VALUES
(1, 'sansa', 'adem', 'adem.sansa7@gmail.com', '$2a$12$ZR.J8ojF/RoG1vK53uYMVuFUXUQotqp.15ss4sLgQKK0cLD9DQ2Ju', '29474515', 'eainamologue', 'i am profissianal therapist', 'IN_PERSON', 'ACTIVE', '2026-02-12 14:04:42', '2026-02-12 14:04:42');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('patient','admin') COLLATE utf8mb4_general_ci DEFAULT 'patient',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `first_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `email`, `password`, `role`, `created_at`, `first_name`, `last_name`) VALUES
(4, 'adem.sansa7@gmail.com', '$2a$12$ZR.J8ojF/RoG1vK53uYMVuFUXUQotqp.15ss4sLgQKK0cLD9DQ2Ju', 'patient', '2026-01-23 18:29:47', 'Adem', 'Sansa'),
(6, 'adem.sansa123@gmail.com', 'ademadem', 'patient', '2026-02-06 10:52:04', 'sansasansa', 'updated'),
(7, 'adem.sansa@gmail.com', 'adem2003', 'patient', '2026-02-08 18:18:32', 'adem', NULL);

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `quiz_question`
--
ALTER TABLE `quiz_question`
  ADD CONSTRAINT `fk_junction_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_junction_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
