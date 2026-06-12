# Corporate ServiceDesk

Информационная система учета заявок и оборудования организации.

## Состав проекта

- `backend` - Java Spring Boot REST API, JWT, PostgreSQL, JPA/Hibernate.
- `frontend-admin` - web-админка на HTML/CSS/JavaScript.
- `desktop-admin` - desktop-клиент администратора на Kotlin/JVM и Swing.
- `android-app` - мобильное приложение Android на Kotlin.
- `docs` - техническое задание, API, схема БД, UML, тест-план и отчет.
- `docker-compose.yml` - запуск PostgreSQL, backend, frontend и pgAdmin.

## Требования к окружению

- Docker Desktop - для полного запуска через `docker compose`.
- Java 17+ и Maven - для локальной сборки backend без Docker.
- Gradle или IntelliJ IDEA - для запуска desktop-клиента.
- Android Studio - для запуска мобильного приложения.

## Быстрый старт через Docker

```powershell
docker compose up --build
```

После запуска:

- Backend API: `http://localhost:8080/api`
- Web-админка: `http://localhost:3000`
- pgAdmin: `http://localhost:5050`

## Основные сценарии

1. Пользователь входит в систему и создает заявку на обслуживание.
2. Администратор назначает приоритет, статус и ответственную роль.
3. Техник меняет статус заявки и добавляет комментарии.
4. Администратор ведет карточки оборудования и смотрит отчетность.
5. Мобильный клиент позволяет технику быстро просматривать заявки.

## Локальный запуск backend

```powershell
cd backend
mvn spring-boot:run
```

Для локального запуска без Docker нужен PostgreSQL и переменные окружения:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/servicedesk"
$env:DB_USER="servicedesk"
$env:DB_PASSWORD="servicedesk"
$env:JWT_SECRET="change-me-to-a-long-secret-key-for-hmac-sha256"
```

## Сборка

```powershell
cd backend
mvn test
mvn package
```

```powershell
cd desktop-admin
gradle run
```

Android-проект открывается в Android Studio из папки `android-app`.
