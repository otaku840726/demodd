-- 使用者主表
CREATE TABLE `user`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '使用者 ID',
    `code`              VARCHAR(50)  NOT NULL UNIQUE COMMENT '使用者識別碼（不可變、唯一）',
    `email`             VARCHAR(255) NOT NULL COMMENT '電子郵件',
    `nickname`          VARCHAR(100) NOT NULL COMMENT '暱稱',
    `locale`            VARCHAR(16) DEFAULT NULL COMMENT '偏好的語系（如 zh-TW, en-US）',
    `email_verified_at` DATETIME    DEFAULT NULL COMMENT '電子郵件 驗證時間',
    `is_enabled`        BOOLEAN     DEFAULT TRUE COMMENT '帳戶可用',
    `last_login_at`     DATETIME    DEFAULT NOW() COMMENT '最後一次登入時間',
    `created_at`        DATETIME    DEFAULT NOW() COMMENT '建立時間',
    INDEX `idx_u_email` (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='使用者主表';

-- 使用者身份表（支援多種登入方式）
CREATE TABLE `user_identity`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '身份 ID',
    `user_code`     VARCHAR(50)  NOT NULL COMMENT '對應 user.code',
    `identity_type` VARCHAR(50)  NOT NULL COMMENT '身份類型（如 PASSWORD, GOOGLE）',
    `identifier`    VARCHAR(255) NOT NULL COMMENT '登入識別值（如 email 或 sub）',
    `credentials`   TEXT COMMENT '認證憑證（如密碼 hash 或 access_token）',
    `created_at`    DATETIME DEFAULT NOW() COMMENT '建立時間',
    UNIQUE KEY `uk_ui_it_i` (`identity_type`, `identifier`),
    INDEX `idx_ui_uc` (`user_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='使用者身份（帳號/社群登入等）';

ALTER TABLE `user_identity`
    ADD CONSTRAINT `fk_ui_uc`
        FOREIGN KEY (`user_code`) REFERENCES `user` (`code`)
            ON DELETE CASCADE;

-- 用戶操作日誌表，用於記錄所有用戶請求行為
CREATE TABLE user_operation_log
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主鍵，自增',
    user_code   VARCHAR(50)            NULL COMMENT '用戶代碼，可為 NULL（匿名或未通過認證）',
    ip          VARCHAR(45)            NOT NULL COMMENT '客戶端 IP 地址',
    device      TEXT COMMENT 'User-Agent 標頭，描述裝置或瀏覽器',
    http_method VARCHAR(10)            NOT NULL COMMENT 'HTTP 方法，如 GET、POST',
    http_path   TEXT                   NOT NULL COMMENT '請求的 URI 路徑',
    http_status INT                    NOT NULL COMMENT 'HTTP 回應狀態碼',
    duration_ms INT                    NOT NULL COMMENT '請求處理耗時，單位為毫秒',
    created_at  DATETIME DEFAULT NOW() NOT NULL COMMENT '請求創建時間',

    INDEX `idx_uol_uc` (`user_code`),
    INDEX `idx_uol_ip` (`ip`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用戶操作日誌表，用於記錄所有用戶請求行為';
