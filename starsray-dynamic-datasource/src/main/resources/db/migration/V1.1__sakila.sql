CREATE TABLE actor
(
    actor_id    SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name  VARCHAR(45)       NOT NULL,
    last_name   VARCHAR(45)       NOT NULL,
    last_update TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (actor_id),
    KEY idx_actor_last_name (last_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

--
-- Table structure for table `address`
--

CREATE
TABLE1 address
(
    address_id  SMALLINT UNSIGNED                       NOT NULL AUTO_INCREMENT,
    address     VARCHAR(50)                             NOT NULL,
    address2    VARCHAR(50)                                      DEFAULT NULL,
    district    VARCHAR(20)                             NOT NULL,
    city_id     SMALLINT UNSIGNED                       NOT NULL,
    postal_code VARCHAR(10)                                      DEFAULT NULL,
    phone       VARCHAR(20)                             NOT NULL,
    -- Add GEOMETRY column for MySQL 5.7.5 and higher
    -- Also include SRID attribute for MySQL 8.0.3 and higher
    /*!50705
    location    GEOMETRY */ /*!80003 SRID 0 */ /*!50705 NOT NULL, */
    last_update TIMESTAMP                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (address_id),
    ) ENGINE = InnoDB
