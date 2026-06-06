-- Fix: tabelas faltando no banco da VPS (l2jmobiusessence)
-- Gerado automaticamente - usa CREATE IF NOT EXISTS, nao perde dados existentes

-- === castle ===
CREATE TABLE IF NOT EXISTS `castle` (
  `id` INT NOT NULL DEFAULT 0,
  `name` varchar(25) NOT NULL,
  `side` enum('NEUTRAL','LIGHT','DARK') DEFAULT 'NEUTRAL' NOT NULL,
  `treasury` BIGINT NOT NULL DEFAULT 0,
  `dynamicTreasury` BIGINT NOT NULL DEFAULT 0,
  `siegeDate` bigint(13) unsigned NOT NULL DEFAULT '0',
  `regTimeOver` enum('true','false') DEFAULT 'true' NOT NULL,
  `regTimeEnd` bigint(13) unsigned NOT NULL DEFAULT '0',
  `showNpcCrest` enum('true','false') DEFAULT 'false' NOT NULL,
  `ticketBuyCount` smallint(3) NOT NULL DEFAULT 0,
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT IGNORE INTO `castle` VALUES
-- (1,'Gludio','NEUTRAL',0,0,0,'true',0,'false',0),
-- (2,'Dion','NEUTRAL',0,0,0,'true',0,'false',0),
(3,'Giran','NEUTRAL',0,0,0,'true',0,'false',0),
-- (4,'Oren','NEUTRAL',0,0,0,'true',0,'false',0),
-- (5,'Aden','NEUTRAL',0,0,0,'true',0,'false',0),
-- (6,'Innadril','NEUTRAL',0,0,0,'true',0,'false',0),
(7,'Goddard','NEUTRAL',0,0,0,'true',0,'false',0);
-- (8,'Rune','NEUTRAL',0,0,0,'true',0,'false',0),
-- (9,'Schuttgart','NEUTRAL',0,0,0,'true',0,'false',0);

-- === character_purge ===
CREATE TABLE IF NOT EXISTS `character_purge` (
  `charId` int(10) unsigned NOT NULL DEFAULT 0,
  `category` int(3) UNSIGNED NOT NULL DEFAULT 0,
  `points` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `keys` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `remainingKeys` int(10) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`,`category`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

-- === character_reco_bonus ===
CREATE TABLE IF NOT EXISTS `character_reco_bonus` (
  `charId` int(10) unsigned NOT NULL,
  `rec_have` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `rec_left` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `time_left` bigint(13) unsigned NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

-- === character_variables ===
CREATE TABLE IF NOT EXISTS `character_variables` (
  `charId` int(10) UNSIGNED NOT NULL,
  `var` varchar(255) NOT NULL,
  `val` text NOT NULL
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE INDEX idx_charId ON character_variables (charId);
CREATE INDEX idx_var ON character_variables (var);

-- === character_daily_rewards ===
CREATE TABLE IF NOT EXISTS `character_daily_rewards` (
  `charId`  int(10) UNSIGNED NOT NULL ,
  `rewardId`  int(3) UNSIGNED NOT NULL ,
  `status`  tinyint(1) UNSIGNED NOT NULL DEFAULT 1 ,
  `progress`  int UNSIGNED NOT NULL DEFAULT 0 ,
  `lastCompleted`  bigint UNSIGNED NOT NULL ,
  PRIMARY KEY (`charId`, `rewardId`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

-- === account_gsdata ===
CREATE TABLE IF NOT EXISTS `account_gsdata` (
  `account_name` VARCHAR(45) NOT NULL DEFAULT '',
  `var`  VARCHAR(255) NOT NULL DEFAULT '',
  `value` text NOT NULL,
  PRIMARY KEY (`account_name`,`var`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- === messages ===
CREATE TABLE IF NOT EXISTS `messages` (
  `messageId` INT NOT NULL DEFAULT 0,
  `senderId` INT NOT NULL DEFAULT 0,
  `receiverId` INT NOT NULL DEFAULT 0,
  `subject` TINYTEXT,
  `content` TEXT,
  `expiration` bigint(13) unsigned NOT NULL DEFAULT '0',
  `reqAdena` BIGINT NOT NULL DEFAULT 0,
  `hasAttachments` enum('true','false') DEFAULT 'false' NOT NULL,
  `isUnread` enum('true','false') DEFAULT 'true' NOT NULL,
  `isDeletedBySender` enum('true','false') DEFAULT 'false' NOT NULL,
  `isDeletedByReceiver` enum('true','false') DEFAULT 'false' NOT NULL,
  `isLocked` enum('true','false') DEFAULT 'false' NOT NULL,
  `sendBySystem` tinyint(1) NOT NULL DEFAULT 0,
  `isReturned` enum('true','false') DEFAULT 'false' NOT NULL,
  `itemId` INT(11) NOT NULL DEFAULT '0',
  `enchantLvl` INT(3) NOT NULL DEFAULT '0',
  `elementals` VARCHAR(25),
  PRIMARY KEY (`messageId`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- === punishments ===
CREATE TABLE IF NOT EXISTS `punishments` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `key` varchar(255) NOT NULL,
  `affect` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `expiration`  bigint NOT NULL,
  `reason` TEXT NOT NULL,
  `punishedBy` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- === character_offline_trade ===
CREATE TABLE IF NOT EXISTS `character_offline_trade` (
  `charId` int(10) unsigned NOT NULL,
  `time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `type` tinyint(4) NOT NULL DEFAULT '0',
  `title` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`charId`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
