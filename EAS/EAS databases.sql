-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Dec 16, 2025 at 10:44 AM
-- Server version: 8.0.21
-- PHP Version: 7.3.21

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `ecomerce_automation_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
CREATE TABLE IF NOT EXISTS `cart` (
  `CartID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `UserID` bigint UNSIGNED NOT NULL,
  `ProductID` bigint UNSIGNED NOT NULL,
  `Quantity` int NOT NULL DEFAULT '1',
  `AddedDate` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CartID`),
  UNIQUE KEY `uq_user_product` (`UserID`,`ProductID`),
  KEY `ProductID` (`ProductID`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `cart`
--

INSERT INTO `cart` (`CartID`, `UserID`, `ProductID`, `Quantity`, `AddedDate`) VALUES
(1, 3, 9, 6, '2025-11-07 10:46:59'),
(2, 3, 8, 12, '2025-11-07 10:47:17');

-- --------------------------------------------------------

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
CREATE TABLE IF NOT EXISTS `category` (
  `CategoryID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `Name` varchar(100) NOT NULL,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CategoryID`),
  KEY `idx_name` (`Name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`CategoryID`, `Name`, `CreatedAt`) VALUES
(1, 'Electronics', '2025-11-04 10:44:54'),
(2, 'Clothing', '2025-11-04 10:44:54'),
(3, 'Books', '2025-11-04 10:44:54'),
(4, 'Home & Garden', '2025-11-04 10:44:54');

-- --------------------------------------------------------

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
CREATE TABLE IF NOT EXISTS `inventory` (
  `InventoryID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `ProductID` bigint UNSIGNED NOT NULL,
  `Quantity` int NOT NULL DEFAULT '0',
  `Reserved` int NOT NULL DEFAULT '0',
  `Available` int NOT NULL DEFAULT '0',
  `LastUpdated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`InventoryID`),
  UNIQUE KEY `uq_product` (`ProductID`),
  KEY `idx_available` (`Available`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `inventory`
--

INSERT INTO `inventory` (`InventoryID`, `ProductID`, `Quantity`, `Reserved`, `Available`) VALUES
(1, 1, 120, 0, 120),
(2, 2, 200, 0, 200),
(3, 3, 300, 0, 300),
(4, 4, 150, 0, 150),
(5, 5, 80, 0, 80),
(6, 6, 1, 0, 1),
(7, 7, 0, 0, 100),
(8, 8, 0, 0, 12),
(9, 9, 0, 0, 99),
(10, 10, 1, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `inventorylog`
--

DROP TABLE IF EXISTS `inventorylog`;
CREATE TABLE IF NOT EXISTS `inventorylog` (
  `LogID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `InventoryID` bigint UNSIGNED NOT NULL,
  `ProductID` bigint UNSIGNED NOT NULL,
  `ChangeType` enum('Purchase','Sale','Adjustment','Return','Damage') NOT NULL,
  `QuantityChange` int NOT NULL,
  `PreviousQty` int NOT NULL,
  `NewQty` int NOT NULL,
  `ReferenceID` bigint UNSIGNED DEFAULT NULL,
  `ReferenceType` varchar(50) DEFAULT NULL,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`LogID`),
  KEY `idx_product` (`ProductID`),
  KEY `idx_type` (`ChangeType`),
  KEY `idx_ref` (`ReferenceID`,`ReferenceType`),
  KEY `InventoryID` (`InventoryID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `inventorylog`
--

INSERT INTO `inventorylog` (`LogID`, `InventoryID`, `ProductID`, `ChangeType`, `QuantityChange`, `PreviousQty`, `NewQty`, `ReferenceID`, `ReferenceType`, `CreatedAt`) VALUES
(1, 1, 1, 'Sale', 50, 50, 100, NULL, 'Manual Adjustment', '2025-11-04 12:45:03'),
(2, 1, 1, 'Return', 20, 100, 120, NULL, 'Manual Adjustment', '2025-11-04 12:45:32'),
(3, 6, 6, 'Purchase', 31, 0, 31, NULL, 'Manual Adjustment', '2025-11-04 14:37:32'),
(4, 6, 6, 'Purchase', -30, 31, 1, NULL, 'Manual Adjustment', '2025-11-04 14:50:58'),
(5, 10, 10, 'Purchase', 1, 0, 1, NULL, 'Manual Adjustment', '2025-11-05 19:17:23');

-- --------------------------------------------------------

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
CREATE TABLE IF NOT EXISTS `order` (
  `OrderID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `OrderNumber` varchar(50) NOT NULL,
  `UserID` bigint UNSIGNED NOT NULL,
  `Date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Status` enum('Pending','Confirmed','Shipped','Delivered','Cancelled') NOT NULL DEFAULT 'Pending',
  `TotalAmount` decimal(12,2) NOT NULL,
  `PaymentMethod` varchar(50) DEFAULT NULL,
  `Notes` text,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`OrderID`),
  UNIQUE KEY `OrderNumber` (`OrderNumber`),
  KEY `idx_user` (`UserID`),
  KEY `idx_status` (`Status`),
  KEY `idx_number` (`OrderNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `order`
--

INSERT INTO `order` (`OrderID`, `OrderNumber`, `UserID`, `Date`, `Status`, `TotalAmount`, `PaymentMethod`, `Notes`, `CreatedAt`) VALUES
(1, 'ORD-2025-001', 3, '2025-11-04 10:44:54', 'Confirmed', '1359.97', 'CreditCard', 'Express delivery', '2025-11-04 10:44:54'),
(2, 'ORD-2025-002', 4, '2025-11-04 10:44:54', 'Pending', '79.98', NULL, NULL, '2025-11-04 10:44:54'),
(3, 'ORD-2025-003', 3, '2025-11-04 10:44:54', 'Shipped', '89.99', 'PayPal', NULL, '2025-11-04 10:44:54'),
(5, 'ORD-1762261987701', 3, '2025-11-04 15:13:07', 'Cancelled', '12999.90', 'BankTransfer', NULL, '2025-11-04 15:13:07'),
(6, 'ORD-1762262537196', 3, '2025-11-04 15:22:17', 'Pending', '89.97', 'PayPal', NULL, '2025-11-04 15:22:17'),
(7, 'ORD-1762358231097', 3, '2025-11-05 17:57:11', 'Confirmed', '120000.00', NULL, NULL, '2025-11-05 17:57:11'),
(8, 'ORD-1762363136894', 3, '2025-11-05 19:18:56', 'Pending', '24000.00', NULL, NULL, '2025-11-05 19:18:56');

-- --------------------------------------------------------

--
-- Table structure for table `orderitem`
--

DROP TABLE IF EXISTS `orderitem`;
CREATE TABLE IF NOT EXISTS `orderitem` (
  `OrderItemID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `OrderID` bigint UNSIGNED NOT NULL,
  `ProductID` bigint UNSIGNED NOT NULL,
  `Quantity` int NOT NULL,
  `UnitPrice` decimal(12,2) NOT NULL,
  `TotalPrice` decimal(12,2) NOT NULL,
  PRIMARY KEY (`OrderItemID`),
  KEY `idx_order` (`OrderID`),
  KEY `idx_product` (`ProductID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `orderitem`
--

INSERT INTO `orderitem` (`OrderItemID`, `OrderID`, `ProductID`, `Quantity`, `UnitPrice`, `TotalPrice`) VALUES
(1, 1, 1, 1, '1299.99', '1299.99'),
(2, 1, 2, 1, '39.99', '39.99'),
(3, 1, 3, 1, '19.99', '19.99'),
(4, 2, 2, 2, '39.99', '79.98'),
(5, 3, 5, 1, '89.99', '89.99'),
(6, 5, 1, 10, '1299.99', '12999.90'),
(7, 6, 4, 3, '29.99', '89.97'),
(8, 7, 9, 1, '120000.00', '120000.00'),
(9, 8, 10, 2, '12000.00', '24000.00');

-- --------------------------------------------------------

--
-- Table structure for table `orderpayment`
--

DROP TABLE IF EXISTS `orderpayment`;
CREATE TABLE IF NOT EXISTS `orderpayment` (
  `OrderID` bigint UNSIGNED NOT NULL,
  `PaymentID` bigint UNSIGNED NOT NULL,
  `Amount` decimal(12,2) NOT NULL,
  PRIMARY KEY (`OrderID`,`PaymentID`),
  KEY `PaymentID` (`PaymentID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `orderpayment`
--

INSERT INTO `orderpayment` (`OrderID`, `PaymentID`, `Amount`) VALUES
(1, 1, '1359.97'),
(3, 3, '89.99'),
(6, 4, '89.97'),
(7, 5, '120000.00');

-- --------------------------------------------------------

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
CREATE TABLE IF NOT EXISTS `payment` (
  `PaymentID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `Amount` decimal(12,2) NOT NULL,
  `Date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Type` enum('CreditCard','PayPal','BankTransfer','Cash') NOT NULL,
  `Reference` varchar(100) DEFAULT NULL,
  `Status` enum('Pending','Completed','Failed') NOT NULL DEFAULT 'Pending',
  PRIMARY KEY (`PaymentID`),
  KEY `idx_status` (`Status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `payment`
--

INSERT INTO `payment` (`PaymentID`, `Amount`, `Date`, `Type`, `Reference`, `Status`) VALUES
(1, '1359.97', '2025-11-04 10:44:54', 'CreditCard', 'PAY-REF-1001', 'Completed'),
(2, '79.98', '2025-11-04 10:44:54', 'PayPal', NULL, 'Completed'),
(3, '89.99', '2025-11-04 10:44:54', 'PayPal', 'PAY-REF-1003', 'Completed'),
(4, '89.97', '2025-11-05 12:51:43', 'PayPal', 'PAY-1762339903655', 'Completed'),
(5, '120000.00', '2025-11-05 18:10:12', 'Cash', 'PAY-1762359012899', 'Completed');

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
CREATE TABLE IF NOT EXISTS `product` (
  `ProductID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `Name` varchar(200) NOT NULL,
  `Description` text,
  `CategoryID` bigint UNSIGNED NOT NULL,
  `PriceOrValue` decimal(12,2) NOT NULL,
  `Status` enum('Active','Inactive') NOT NULL DEFAULT 'Active',
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastUpdated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ProductID`),
  KEY `idx_category` (`CategoryID`),
  KEY `idx_status` (`Status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `product`
--

INSERT INTO `product` (`ProductID`, `Name`, `Description`, `CategoryID`, `PriceOrValue`, `Status`, `CreatedAt`) VALUES
(1, 'Laptop Pro 15', 'High-performance laptop', 1, '1299.99', 'Inactive', '2025-11-04 10:44:54'),
(2, 'Wireless Mouse', 'Ergonomic mouse', 1, '39.99', 'Inactive', '2025-11-04 10:44:54'),
(3, 'T-Shirt Basic', '100% cotton t-shirt', 1, '21.00', 'Inactive', '2025-11-04 10:44:54'),
(4, 'Programming Book', 'Learn SQL in 21 days', 3, '29.99', 'Active', '2025-11-04 10:44:54'),
(5, 'Garden Tool Set', 'Complete gardening kit', 4, '89.99', 'Inactive', '2025-11-04 10:44:54'),
(6, 'Hands camera', 'high resolution', 1, '200000.00', 'Inactive', '2025-11-04 13:06:33'),
(7, 'pixel', '', 1, '140000.00', 'Active', '2025-11-05 16:18:46'),
(8, 'Sonny', '', 1, '120000.00', 'Active', '2025-11-05 16:19:52'),
(9, 'Hots 7', '', 1, '120000.00', 'Active', '2025-11-05 17:51:49'),
(10, 'Lyrics', '', 3, '12000.00', 'Active', '2025-11-05 19:16:19');

-- --------------------------------------------------------

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
CREATE TABLE IF NOT EXISTS `review` (
  `reviewid` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `productid` bigint UNSIGNED NOT NULL,
  `userid` bigint UNSIGNED NOT NULL,
  `rating` tinyint NOT NULL,
  `comment` text,
  `isapproved` tinyint(1) NOT NULL DEFAULT '0',
  `createdat` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedat` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`reviewid`),
  UNIQUE KEY `uq_user_product` (`userid`,`productid`),
  KEY `idx_product` (`productid`),
  KEY `idx_user` (`userid`),
  KEY `idx_rating` (`rating`),
  KEY `idx_approved` (`isapproved`)
) ;

--
-- Dumping data for table `review`
--

INSERT INTO `review` (`reviewid`, `productid`, `userid`, `rating`, `comment`, `isapproved`, `createdat`, `updatedat`) VALUES
(1, 1, 3, 5, 'Best laptop ever! Fast and reliable.', 1, '2025-11-04 12:31:32', NULL),
(2, 1, 4, 4, 'Great, but fan is loud.', 1, '2025-11-04 12:31:32', NULL),
(3, 2, 3, 3, 'Mouse works, but scroll is sticky.', 0, '2025-11-04 12:31:32', NULL),
(4, 3, 4, 5, 'Super soft and comfy.', 1, '2025-11-04 12:31:32', NULL),
(5, 4, 3, 5, 'Perfect for learning SQL.', 1, '2025-11-04 12:31:32', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `shipment`
--

DROP TABLE IF EXISTS `shipment`;
CREATE TABLE IF NOT EXISTS `shipment` (
  `ShipmentID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `OrderID` bigint UNSIGNED NOT NULL,
  `TrackingNumber` varchar(100) DEFAULT NULL,
  `Carrier` varchar(100) DEFAULT NULL,
  `Status` enum('Pending','Shipped','Delivered','Failed') NOT NULL DEFAULT 'Pending',
  `ShippedAt` datetime DEFAULT NULL,
  `DeliveredAt` datetime DEFAULT NULL,
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ShipmentID`),
  KEY `idx_order` (`OrderID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `shipment`
--

INSERT INTO `shipment` (`ShipmentID`, `OrderID`, `TrackingNumber`, `Carrier`, `Status`, `ShippedAt`, `DeliveredAt`, `CreatedAt`) VALUES
(1, 1, 'TRACK-2025-001', 'FedEx', 'Shipped', '2025-04-06 10:00:00', NULL, '2025-11-04 10:44:56'),
(2, 3, 'TRACK-2025-003', 'UPS', 'Delivered', '2025-04-07 14:30:00', NULL, '2025-11-04 10:44:56');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `UserID` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `Username` varchar(50) NOT NULL,
  `PasswordHash` varchar(255) NOT NULL,
  `Email` varchar(255) NOT NULL,
  `FullName` varchar(100) NOT NULL,
  `Role` enum('Admin','Seller','Buyer','Supplier') NOT NULL DEFAULT 'Buyer',
  `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `LastLogin` datetime DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Username` (`Username`),
  UNIQUE KEY `Email` (`Email`),
  KEY `idx_email` (`Email`),
  KEY `idx_role` (`Role`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`UserID`, `Username`, `PasswordHash`, `Email`, `FullName`, `Role`, `CreatedAt`, `LastLogin`) VALUES
(1, 'admin', 'admin', 'admin@shop.com', 'Admin User', 'Admin', '2025-11-04 10:44:54', '2025-12-15 20:59:32'),
(2, 'seller1', 'seller1', 'seller1@shop.com', 'John Seller', 'Seller', '2025-11-04 10:44:54', '2025-11-20 13:15:09'),
(3, 'buyer1', 'buyer1', 'buyer1@shop.com', 'Jane Buyer', 'Buyer', '2025-11-04 10:44:54', '2025-11-20 11:36:23'),
(4, 'buyer2', 'buyer2', 'buyer2@shop.com', 'Bob Buyer', 'Buyer', '2025-11-04 10:44:54', NULL),
(5, 'supplier', 'supplier', 's@EAS.com', 'supplier Yve', 'Seller', '2025-11-04 13:09:50', '2025-11-05 16:36:05');

-- --------------------------------------------------------

--
-- Table structure for table `userproductaccess`
--

DROP TABLE IF EXISTS `userproductaccess`;
CREATE TABLE IF NOT EXISTS `userproductaccess` (
  `UserID` bigint UNSIGNED NOT NULL,
  `ProductID` bigint UNSIGNED NOT NULL,
  `AccessRole` enum('Owner','Editor','Viewer') NOT NULL DEFAULT 'Viewer',
  `GrantedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserID`,`ProductID`),
  KEY `ProductID` (`ProductID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `userproductaccess`
--

INSERT INTO `userproductaccess` (`UserID`, `ProductID`, `AccessRole`, `GrantedAt`) VALUES
(2, 1, 'Owner', '2025-11-04 10:44:54'),
(2, 2, 'Owner', '2025-11-04 10:44:54'),
(2, 3, 'Owner', '2025-11-04 10:44:54'),
(2, 6, 'Owner', '2025-11-04 13:06:33');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `inventory`
--
ALTER TABLE `inventory`
  ADD CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`ProductID`) REFERENCES `product` (`ProductID`) ON DELETE CASCADE;

--
-- Constraints for table `inventorylog`
--
ALTER TABLE `inventorylog`
  ADD CONSTRAINT `inventorylog_ibfk_1` FOREIGN KEY (`InventoryID`) REFERENCES `inventory` (`InventoryID`) ON DELETE RESTRICT,
  ADD CONSTRAINT `inventorylog_ibfk_2` FOREIGN KEY (`ProductID`) REFERENCES `product` (`ProductID`) ON DELETE CASCADE;

--
-- Constraints for table `order`
--
ALTER TABLE `order`
  ADD CONSTRAINT `order_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`) ON DELETE RESTRICT;

--
-- Constraints for table `orderitem`
--
ALTER TABLE `orderitem`
  ADD CONSTRAINT `orderitem_ibfk_1` FOREIGN KEY (`OrderID`) REFERENCES `order` (`OrderID`) ON DELETE CASCADE,
  ADD CONSTRAINT `orderitem_ibfk_2` FOREIGN KEY (`ProductID`) REFERENCES `product` (`ProductID`) ON DELETE RESTRICT;

--
-- Constraints for table `orderpayment`
--
ALTER TABLE `orderpayment`
  ADD CONSTRAINT `orderpayment_ibfk_1` FOREIGN KEY (`OrderID`) REFERENCES `order` (`OrderID`) ON DELETE CASCADE,
  ADD CONSTRAINT `orderpayment_ibfk_2` FOREIGN KEY (`PaymentID`) REFERENCES `payment` (`PaymentID`) ON DELETE RESTRICT;

--
-- Constraints for table `product`
--
ALTER TABLE `product`
  ADD CONSTRAINT `product_ibfk_1` FOREIGN KEY (`CategoryID`) REFERENCES `category` (`CategoryID`) ON DELETE RESTRICT;

--
-- Constraints for table `review`
--
ALTER TABLE `review`
  ADD CONSTRAINT `review_ibfk_1` FOREIGN KEY (`productid`) REFERENCES `product` (`ProductID`) ON DELETE CASCADE,
  ADD CONSTRAINT `review_ibfk_2` FOREIGN KEY (`userid`) REFERENCES `user` (`UserID`) ON DELETE CASCADE;

--
-- Constraints for table `shipment`
--
ALTER TABLE `shipment`
  ADD CONSTRAINT `shipment_ibfk_1` FOREIGN KEY (`OrderID`) REFERENCES `order` (`OrderID`) ON DELETE CASCADE;

--
-- Constraints for table `userproductaccess`
--
ALTER TABLE `userproductaccess`
  ADD CONSTRAINT `userproductaccess_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `user` (`UserID`) ON DELETE CASCADE,
  ADD CONSTRAINT `userproductaccess_ibfk_2` FOREIGN KEY (`ProductID`) REFERENCES `product` (`ProductID`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
