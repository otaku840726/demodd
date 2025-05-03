![Unit Test](https://github.com/otaku840726/demodd/actions/workflows/test.yml/badge.svg)
![Build](https://github.com/otaku840726/demodd/actions/workflows/build.yml/badge.svg)

# DemoDD - 註冊登入範例系統

DemoDD 是一個基於 Spring Boot 架構實作的範例專案，支援帳號註冊與登入的多階段驗證流程，並整合 JWT、Redis、郵件驗證等功能，適合作為具有高安全性需求的後端應用模板。

<p align="center">
  <img src="https://img.otaku840726.workers.dev/logo/logo7.png" width="50%"/>
</p>

## 功能特色

- ✅ 基礎註冊與登入機制
- 🔐 多階段二因素驗證（2FA）
- ✉️ Email 驗證碼發送（SMTP / Mailjet 支援）
- 🔁 Redis 快取登入驗證流程
- 🔑 JWT Token 授權與權限控制
- 🌐 OpenAPI / Swagger 文件整合
- 🌍 支援多語系錯誤訊息（國際化 i18n）

## 驗證流程說明

1. 使用者透過 `POST /register` 或 `POST /login` 發起請求，系統會回傳一組暫時 JWT Token。
2. 系統根據 JWT Claims 判斷該 Token 是否為已完成所有驗證的正式 Token：
   - ✅ 若是正式 Token，登入成功。
   - ❌ 若是暫時 Token，表示尚未完成所有二因素驗證。
3. 系統會分析尚需哪些驗證步驟（如 Email 驗證碼等）。
4. 使用者需取得 OTP 並透過 `POST /verify` 提交驗證。
5. 驗證成功後系統會回傳新的 Token，重複判斷直到成為正式 Token。

## 專案結構

```
src/main/java/day/ohya/demodd/
├── api/                        # 控制器層
├── config/                     # 設定檔（Spring Security、Swagger 等）
├── model/                      # DTO 與回應模型
├── notification/               # 郵件發送邏輯（SMTP、Mailjet）
├── redis/                      # Redis 快取實作
├── security/                   # JWT、OAuth、自訂認證邏輯
├── constant/                   # 常數定義
├── locale/                     # 多語系支援
```

## 環境建置

### 前置需求

- Java 17+
- Maven 3.8+
- Redis
- MySQL
- 可選：Mailjet API Key 或 SMTP 郵件帳號

### 啟動專案

```bash
./mvnw spring-boot:run
```

預設會啟動於 `http://localhost:8080`

## Swagger 文件

可透過瀏覽器存取 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 郵件設定

可透過 `application.yml` 設定以下參數：

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your@email.com
    password: your-password
```

## 授權與安全設計

- 使用 Spring Security 設定 Security Filter Chain
- 支援 Bearer JWT Token 驗證
- 多階段驗證流程由 Redis 管理暫存狀態，保證流程彈性與安全性

## License

本專案為學習與展示用途，未授權商業使用。
