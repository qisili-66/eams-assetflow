-- EAMS authentication foundation
-- MySQL 8.0+
-- This script is safe to execute repeatedly. It does not delete existing data.

CREATE DATABASE IF NOT EXISTS eams
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE eams;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_department (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Department ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT 'Parent department; 0 means root',
    name VARCHAR(100) NOT NULL COMMENT 'Department name',
    sort_no INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_parent_name (parent_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Organization departments';

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User ID',
    username VARCHAR(50) NOT NULL COMMENT 'Login username',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt password hash',
    nickname VARCHAR(50) NOT NULL COMMENT 'Display name',
    department_id BIGINT NOT NULL COMMENT 'Department ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED or DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_department_id (department_id),
    KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System users';

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
    role_code VARCHAR(30) NOT NULL COMMENT 'Role code',
    role_name VARCHAR(50) NOT NULL COMMENT 'Role name',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Relation ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_user_id (user_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User role relations';

INSERT IGNORE INTO sys_department (id, parent_id, name, sort_no)
VALUES (1, 0, 'Company Headquarters', 1),
       (2, 1, 'Technology Department', 1),
       (3, 1, 'Administration Department', 2);

INSERT IGNORE INTO sys_role (id, role_code, role_name)
VALUES (1, 'ADMIN', 'Administrator'),
       (2, 'USER', 'Employee');

