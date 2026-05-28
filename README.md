# OTP Protection Service

Сервис для защиты операций с помощью временных одноразовых кодов (OTP). Поддерживает отправку кодов через Email, SMS (через SMPP эмулятор), Telegram и сохранение в файл.

## Оглавление

- [Технологии](#технологии)
- [Требования](#требования)
- [Установка и запуск](#установка-и-запуск)
- [Конфигурация](#конфигурация)
- [API Эндпоинты](#api-эндпоинты)
- [Тестирование](#тестирование)
- [Структура проекта](#структура-проекта)
- [Авторы](#авторы)

---

## Технологии

| Компонент      | Технология             |
|----------------|------------------------|
| Язык           | Java 17                |
| Сборка         | Gradle (Kotlin DSL)    |
| HTTP Сервер    | com.sun.net.httpserver |
| База данных    | PostgreSQL 17          |
| Пул соединений | HikariCP               |
| Аутентификация | JWT (JJWT)             |
| Хеширование    | BCrypt                 |
| JSON           | Gson                   |
| Логирование    | Logback + SLF4J        |
| Email          | Jakarta Mail (SMTP)    |
| SMS            | jSMPP (SMPP протокол)  |
| Telegram       | HTTP Client            |

---

## Требования

- Java 17 или выше
- PostgreSQL 17
- Gradle 8.x (или использование wrapper)
- SMPPsim (для SMS эмуляции) - опционально
- Telegram Bot Token (опционально)
- SMTP доступ (опционально)

---

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd otp-protection-service
```

### 2. Настройка базы данных PostgreSQL

```bash
# Создание базы данных
createdb -U postgres otp_service

# Или через psql
psql -U postgres -c "CREATE DATABASE otp_service;"
```

### 3. Настройка конфигурации

Скопируйте и отредактируйте файлы конфигурации:

```bash
# Основная конфигурация (установите пароль БД)
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Email конфигурация (опционально)
cp src/main/resources/email.properties.example src/main/resources/email.properties

# SMS конфигурация (опционально)
cp src/main/resources/sms.properties.example src/main/resources/sms.properties

# Telegram конфигурация (опционально)
cp src/main/resources/telegram.properties.example src/main/resources/telegram.properties
```

Минимальная конфигурация (application.properties):

```properties
db.url=jdbc:postgresql://localhost:5432/otp_service
db.username=postgres
db.password=your_password
server.port=8080
```

### 4. Запуск SMPPsim (для SMS, опционально)

```bash
# Скачать SMPPsim с http://www.smppsim.com/
cd smppsim
java -jar smppsim.jar
# или на Windows: startsmppsim.bat
```

### 5. Создание Telegram бота (опционально)

1. Напишите @BotFather в Telegram
2. Создайте бота командой /newbot
3. Получите токен
4. Начните диалог с ботом
5. Получите chatId:

```text
https://api.telegram.org/bot<YOUR_TOKEN>/getUpdates
```

### 6. Сборка и запуск

```bash
# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew run
```

Ожидаемый вывод:
```text
2025-05-28 12:00:00.000 INFO  [main] ru.nes.otp.OTPApplication - Starting OTP Protection Service...
2025-05-28 12:00:00.100 INFO  [main] ru.nes.otp.config.DatabaseConfig - Database connection pool initialized
2025-05-28 12:00:00.200 INFO  [main] ru.nes.otp.OTPApplication - OTP Service started successfully on port 8080
2025-05-28 12:00:00.200 INFO  [main] ru.nes.otp.service.scheduler.OtpExpiryScheduler - OtpExpiryScheduler started. Interval: 30 seconds
```

## Конфигурация

### application.properties

| Параметр                        | Описание                             | Значение по умолчанию                        |
|---------------------------------|--------------------------------------|----------------------------------------------|
| server.port                     | Порт HTTP сервера                    | 8080                                         |
| server.backlog                  | Размер очереди подключений           | 50                                           |
| db.url                          | URL подключения к PostgreSQL         | jdbc:postgresql://localhost:5432/otp_service |
| db.username                     | Имя пользователя БД                  | postgres                                     |
| db.password                     | Пароль БД                            | postgres                                     |
| db.maxPoolSize                  | Максимальный размер пула соединений  | 10                                           |
| jwt.secret                      | Секретный ключ для JWT               | (обязателен к изменению)                     |
| jwt.expirationMs                | Время жизни JWT в миллисекундах      | 3600000 (1 час)                              |
| scheduler.expiryIntervalSeconds | Интервал проверки просроченных кодов | 30                                           |

### email.properties (для отправки Email)

| Параметр                  | Описание                |
|---------------------------|-------------------------|
| email.username            | Логин для SMTP          |
| email.password            | Пароль для SMTP         |
| email.from                | Адрес отправителя       |
| mail.smtp.host            | SMTP сервер             |
| mail.smtp.port            | SMTP порт               |
| mail.smtp.auth            | Включить аутентификацию |
| mail.smtp.starttls.enable | Включить TLS            |

### sms.properties (для SMPP)

| Параметр         | Описание          | Значение по умолчанию |
|------------------|-------------------|-----------------------|
| smpp.host        | Хост SMPP сервера | localhost             |
| smpp.port        | Порт SMPP сервера | 2775                  |
| smpp.system_id   | System ID         | smppclient1           |
| smpp.password    | Пароль            | password              |
| smpp.source_addr | Адрес отправителя | OTPService            |


### telegram.properties

| Параметр           | Описание             |
|--------------------|----------------------|
| telegram.bot.token | Токен Telegram бота  |
| telegram.chat.id   | ID чата для отправки |

## API Эндпоинты

Базовый URL: http://localhost:8080/api

### Публичные эндпоинты

POST /register - Регистрация пользователя

Тело запроса:

```json
{
    "login": "username",
    "password": "password123"
}
```

Успешный ответ (200):

```json
{
    "status": "success",
    "message": "User registered successfully",
    "timestamp": "2025-05-28T12:00:00"
}
```

* Ошибки:
  * 400 - Неверный формат логина/пароля
  * 409 - Пользователь уже существует

POST /login - Вход в систему

Тело запроса:

```json
{
    "login": "username",
    "password": "password123"
}
```

Успешный ответ (200):

```json
{
    "status": "success",
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9...",
        "login": "username",
        "role": "USER",
        "expiresIn": 3600000
    },
    "timestamp": "2025-05-28T12:00:00"
}
```

* Ошибки:
  * 400 - Неверный формат запроса
  * 401 - Неверный логин или пароль

## Защищенные эндпоинты (требуют JWT токен)

Заголовок авторизации:

```text
Authorization: Bearer <your_jwt_token>
```

POST /otp/generate - Генерация OTP кода

Тело запроса:

```json
{
    "operationId": "order_12345",
    "channel": "EMAIL",
    "destination": "user@example.com"
}
```

* Поддерживаемые каналы:
  * EMAIL - отправка на email
  * SMS - отправка через SMS (требуется SMPPsim)
  * TELEGRAM - отправка в Telegram
  * FILE - сохранение в файл

Успешный ответ (200):

```json
{
    "status": "success",
    "data": {
        "operationId": "order_12345",
        "channel": "EMAIL",
        "destination": "user@example.com",
        "expiresInSeconds": 300,
        "message": "OTP code sent successfully via EMAIL to user@example.com"
    },
    "timestamp": "2025-05-28T12:00:00"
}
```

* Ошибки:
  * 400 - Неверный формат запроса
  * 401 - Отсутствует или невалидный токен
  * 500 - Внутренняя ошибка сервера

POST /otp/validate - Проверка OTP кода

Тело запроса:

```json
{
    "operationId": "order_12345",
    "code": "123456"
}
```

Успешный ответ (200):

```json
{
    "status": "success",
    "data": {
        "valid": true,
        "message": "OTP code validated successfully"
    },
    "timestamp": "2025-05-28T12:00:00"
}
```

Ответ при неверном коде (400):

```json
{
    "status": "error",
    "code": 400,
    "message": "Invalid OTP code",
    "path": "/api/otp/validate",
    "timestamp": "2025-05-28T12:00:00"
}
```

### Административные эндпоинты (требуют роль ADMIN)

GET /admin/users - Список всех пользователей (кроме админов)

Успешный ответ (200):

```json
{
    "status": "success",
    "data": [
        {
            "id": 2,
            "login": "user1",
            "role": "USER",
            "createdAt": "2025-05-28T10:00:00"
        }
    ],
    "timestamp": "2025-05-28T12:00:00"
}
```

DELETE /admin/users/{id} - Удаление пользователя

Успешный ответ (200):

```json
{
    "status": "success",
    "message": "User deleted successfully",
    "timestamp": "2025-05-28T12:00:00"
}
```

GET /admin/config - Получение конфигурации OTP

Успешный ответ (200):

```json
{
    "status": "success",
    "data": {
        "id": 1,
        "lifetimeSeconds": 300,
        "codeLength": 6,
        "updatedAt": "2025-05-28T10:00:00",
        "updatedBy": "admin"
    },
    "timestamp": "2025-05-28T12:00:00"
}
```

PUT /admin/config - Обновление конфигурации OTP

Тело запроса:

```json
{
    "lifetimeSeconds": 300,
    "codeLength": 6
}
```

Успешный ответ (200):

```json
{
    "status": "success",
    "message": "Configuration updated successfully",
    "timestamp": "2025-05-28T12:00:00"
}
```

## Тестирование

### Сценарий 1: Регистрация и логин

```bash
# 1. Регистрация первого пользователя (становится ADMIN)
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"admin123"}'

# 2. Логин
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin","password":"admin123"}'

# Сохраните полученный токен
TOKEN="<YOUR_TOKEN>"
```

### Сценарий 2: Генерация и проверка OTP кода (FILE канал)

```bash
# 1. Генерация кода
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "operationId": "test_operation_001",
    "channel": "FILE",
    "destination": "admin"
  }'

# В ответе придет код (например: 482391)

# 2. Проверка кода
curl -X POST http://localhost:8080/api/otp/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "operationId": "test_operation_001",
    "code": "482391"
  }'
```

### Сценарий 3: Проверка просроченного кода

```bash
# 1. Сгенерируйте код
# 2. Подождите 5+ минут (или измените lifetimeSeconds через админку)
# 3. Попробуйте проверить код - должен вернуться статус EXPIRED
```

### Сценарий 4: Административные операции

```bash
# 1. Создание обычного пользователя
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"login":"testuser","password":"test123"}'

# 2. Получение списка пользователей (только ADMIN)
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer $TOKEN"

# 3. Обновление конфигурации OTP
curl -X PUT http://localhost:8080/api/admin/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"lifetimeSeconds":180,"codeLength":5}'

# 4. Удаление пользователя
curl -X DELETE http://localhost:8080/api/admin/users/2 \
  -H "Authorization: Bearer $TOKEN"
```

### Сценарий 5: Отправка через разные каналы

```bash
# Email (требует настройки SMTP)
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "operationId": "email_test",
    "channel": "EMAIL",
    "destination": "user@example.com"
  }'

# SMS (требует запущенного SMPPsim)
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "operationId": "sms_test",
    "channel": "SMS",
    "destination": "+71234567890"
  }'

# Telegram (требует настройки бота)
curl -X POST http://localhost:8080/api/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "operationId": "tg_test",
    "channel": "TELEGRAM",
    "destination": "123456789"
  }'
```

## Структура проекта

otp-protection-service/
├── build.gradle.kts                 # Сборка Gradle
├── settings.gradle
├── README.md
├── src/main/
│   ├── java/ru/nes/otp/
│   │   ├── OTPApplication.java      # Точка входа
│   │   ├── config/                  # Конфигурация
│   │   │   ├── DatabaseConfig.java
│   │   │   └── ServerConfig.java
│   │   ├── dao/                     # Data Access Layer
│   │   │   ├── BaseDAO.java
│   │   │   ├── UserDAO.java
│   │   │   ├── OtpCodeDAO.java
│   │   │   └── OtpConfigDAO.java
│   │   ├── service/                 # Business Logic
│   │   │   ├── AuthService.java
│   │   │   ├── OtpService.java
│   │   │   ├── AdminService.java
│   │   │   ├── notification/        # Каналы рассылки
│   │   │   └── scheduler/           # Планировщик
│   │   ├── handler/                 # HTTP Handlers
│   │   │   ├── BaseHandler.java
│   │   │   ├── AuthHandler.java
│   │   │   ├── UserHandler.java
│   │   │   ├── AdminHandler.java
│   │   │   └── NotFoundHandler.java
│   │   ├── security/                # Безопасность
│   │   │   ├── JwtUtil.java
│   │   │   ├── PasswordUtil.java
│   │   │   ├── OtpCodeUtil.java
│   │   │   └── AuthFilter.java
│   │   ├── model/                   # DTO и Entity
│   │   └── util/                    # Утилиты
│   └── resources/
│       ├── application.properties
│       ├── db/init.sql              # Схема БД
│       └── logback.xml              # Логирование
├── logs/                            # Логи приложения
└── otp-files/                       # Сохраненные OTP коды

## Логирование

* Логи пишутся в:
  * Консоль - цветное логирование
  * logs/otp-service.log - все логи
  * logs/otp-errors.log - только ошибки

* Уровни логирования:
  * INFO - основные операции (запросы, ответы)
  * DEBUG - детали SQL запросов, тела запросов
  * WARN - предупреждения (неверные попытки)
  * ERROR - ошибки (с полным stack trace)

## Устранение неполадок

Ошибка подключения к БД

```bash
# Проверьте, запущен ли PostgreSQL
sudo systemctl status postgresql

# Проверьте правильность пароля в application.properties
```

Ошибка 401 Unauthorized
    Убедитесь, что токен передан в заголовке Authorization: Bearer <token>
    Проверьте, не истек ли срок действия токена

Ошибка 403 Forbidden
    Требуется роль ADMIN для административных эндпоинтов
    Первый зарегистрированный пользователь получает роль ADMIN

SMS не отправляется
    Убедитесь, что SMPPsim запущен
    Проверьте порт в sms.properties (по умолчанию 2775)

Email не отправляется
    Проверьте настройки SMTP в email.properties
    Для Gmail используйте пароль приложения

## Автор
Проект разработан в рамках учебного курса Евгением Никоновым

