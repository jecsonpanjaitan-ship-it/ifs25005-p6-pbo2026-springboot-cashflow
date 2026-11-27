# ifs25005-p6-pbo2026-springboot-cashflow

Aplikasi web pencatat keuangan pribadi (Cashflow): Spring Boot 4.1.2, Vaadin 25.3.4, JDBC PostgreSQL,
Clean Architecture, dan integrasi AI (OpenAI-compatible).
Package: `org.delcom.ifs25005springbootcashflow`.

## Menjalankan

1. Buat database (sekali saja): `db/create-database.sql` -> `CREATE DATABASE pbo2026_cashflow;`
2. Salin `.env.example` menjadi `.env`, lalu isi `DB_*`, `ROOT_*`, `SESSION_SECRET`, dan `OPENAI_ENDPOINT` / `OPENAI_KEY` / `OPENAI_MODEL`.
3. Jalankan (JDK 25, Maven):
   ```
   mvn spring-boot:run
   ```
   Buka `http://localhost:8080` (sesuai `APP_PORT`). Login admin: `ROOT_USERNAME` / `ROOT_PASSWORD` di `.env`.
   Bila memakai Maven Wrapper: `mvn -N wrapper:wrapper` sekali untuk membuat `mvnw` / `mvnw.cmd`.

## Build JAR (wajib profil `production`)

```
mvn clean package -Pproduction
java -jar target/ifs25005-p6-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

## Uji & coverage

```
mvn clean verify
```
Unit test memakai InMemory / H2 / stub AI (tanpa memanggil AI nyata). Laporan: `target/site/jacoco/index.html`.
Jacoco mewajibkan 100% untuk kode yang diukur; `framework/view`, `framework/auth`, dan `App` dikecualikan.

## Struktur

```
domain/      entity, exception, repository (port), PasswordHasher
usecase/     AuthUseCase, FinanceUseCase, FinanceAiUseCase, UserUseCase
adapter/     ai, presenter, repository (InMemory + JDBC), storage
framework/   auth (AuthSession), view (Vaadin)
config/      AppConfig (composition root)
```

## Rute

| Rute | Akses |
|---|---|
| `/login`, `/register` | Publik (sudah login -> `/`) |
| `/` (FinanceView), `/profile` | USER & ADMIN (belum login -> `/login`) |
| `/users`, `/user-transactions?userId=` | ADMIN saja (USER -> `/`) |
| `/uploads/**` | Publik (file statis) |
