-- Thêm cột provider và provider_id vào bảng users
ALTER TABLE users
ADD COLUMN provider VARCHAR(50) NULL AFTER avatar,
ADD COLUMN provider_id VARCHAR(255) NULL AFTER provider;

