-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jul 04, 2025 at 05:59 PM
-- Server version: 8.4.3
-- PHP Version: 8.3.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `personal_expense_tracker`
--

-- --------------------------------------------------------

--
-- Table structure for table `closing_history`
--

CREATE TABLE `closing_history` (
  `id` int NOT NULL,
  `closing_date` date DEFAULT NULL,
  `period_start` date DEFAULT NULL,
  `period_end` date DEFAULT NULL,
  `total_income` decimal(5,2) DEFAULT NULL,
  `total_expense` decimal(5,2) DEFAULT NULL,
  `net_amount` decimal(5,2) DEFAULT NULL,
  `export_filename` text,
  `budget_id` int DEFAULT NULL,
  `transaction_id` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `closing_history`
--

INSERT INTO `closing_history` (`id`, `closing_date`, `period_start`, `period_end`, `total_income`, `total_expense`, `net_amount`, `export_filename`, `budget_id`, `transaction_id`) VALUES
(1, NULL, '2025-05-01', '2025-05-10', NULL, NULL, NULL, NULL, NULL, NULL),
(2, NULL, '2025-07-04', '2025-07-05', NULL, NULL, NULL, NULL, NULL, NULL),
(3, NULL, '2025-05-01', '2025-07-05', NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `master_budgeting`
--

CREATE TABLE `master_budgeting` (
  `budget_id` int NOT NULL,
  `expense` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `expense_description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `amount` double NOT NULL,
  `created_date` date DEFAULT (curdate())
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `master_budgeting`
--

INSERT INTO `master_budgeting` (`budget_id`, `expense`, `expense_description`, `amount`, `created_date`) VALUES
(61, 'Tarik Uang BRI', 'Tarik Uang BRI', 0, '2025-07-04'),
(63, 'Tarik Uang BCA', 'Tarik Uang BCA', 0, '2025-07-04'),
(64, 'F&B Weekdays', 'Food', 0, '2025-07-05');

-- --------------------------------------------------------

--
-- Table structure for table `master_income`
--

CREATE TABLE `master_income` (
  `income_id` int NOT NULL,
  `income_category` varchar(255) DEFAULT NULL,
  `created_date` date NOT NULL DEFAULT (curdate())
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `master_income`
--

INSERT INTO `master_income` (`income_id`, `income_category`, `created_date`) VALUES
(16, 'Year Salary', '2025-07-04'),
(17, 'Day Salary', '2025-07-04'),
(18, 'Month Salary', '2025-07-05');

-- --------------------------------------------------------

--
-- Table structure for table `transaction`
--

CREATE TABLE `transaction` (
  `transaction_id` int NOT NULL,
  `transaction_date` date NOT NULL,
  `type` varchar(25) NOT NULL,
  `budget_id` int DEFAULT NULL,
  `income_id` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `amount` double NOT NULL,
  `new_budget` double NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `closing_history`
--
ALTER TABLE `closing_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_budget_to_closing` (`budget_id`),
  ADD KEY `fk_transaction` (`transaction_id`);

--
-- Indexes for table `master_budgeting`
--
ALTER TABLE `master_budgeting`
  ADD PRIMARY KEY (`budget_id`);

--
-- Indexes for table `master_income`
--
ALTER TABLE `master_income`
  ADD PRIMARY KEY (`income_id`);

--
-- Indexes for table `transaction`
--
ALTER TABLE `transaction`
  ADD PRIMARY KEY (`transaction_id`),
  ADD KEY `fk_budget_id` (`budget_id`),
  ADD KEY `fk_income_id` (`income_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `closing_history`
--
ALTER TABLE `closing_history`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `master_budgeting`
--
ALTER TABLE `master_budgeting`
  MODIFY `budget_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=65;

--
-- AUTO_INCREMENT for table `master_income`
--
ALTER TABLE `master_income`
  MODIFY `income_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `transaction`
--
ALTER TABLE `transaction`
  MODIFY `transaction_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `closing_history`
--
ALTER TABLE `closing_history`
  ADD CONSTRAINT `fk_budget_to_closing` FOREIGN KEY (`budget_id`) REFERENCES `master_budgeting` (`budget_id`),
  ADD CONSTRAINT `fk_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `transaction` (`transaction_id`);

--
-- Constraints for table `transaction`
--
ALTER TABLE `transaction`
  ADD CONSTRAINT `fk_budget_id` FOREIGN KEY (`budget_id`) REFERENCES `master_budgeting` (`budget_id`),
  ADD CONSTRAINT `fk_income_id` FOREIGN KEY (`income_id`) REFERENCES `master_income` (`income_id`),
  ADD CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`budget_id`) REFERENCES `master_budgeting` (`budget_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
