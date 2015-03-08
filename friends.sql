CREATE TABLE IF NOT EXISTS `friends` (
  `friendid` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `owner_uuid` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `owner_name` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `friend_uuid` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `friend_name` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `confirmed` int(1) unsigned NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `updated_at` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`friendid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `buxnewname` (
  `uuid` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
  `name` varchar(128) COLLATE utf8_unicode_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
