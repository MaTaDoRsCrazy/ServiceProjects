# Описание REST API

Базовый адрес API: `http://localhost:8080/api`.

Для защищенных запросов используется заголовок:

```http
Authorization: Bearer <jwt-token>
```

## Авторизация

### POST `/auth/login`

Вход в систему.

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Ответ:

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "username": "admin",
    "fullName": "Администратор системы",
    "email": "admin@example.com",
    "role": "ADMIN"
  }
}
```

### GET `/auth/me`

Возвращает текущего пользователя.

## Заявки

### GET `/tickets`

Возвращает список заявок. Для сотрудника возвращаются только его заявки.

Параметр фильтрации:

| Параметр | Описание |
| --- | --- |
| `status` | `NEW`, `IN_PROGRESS`, `WAITING_PARTS`, `RESOLVED`, `CLOSED` |

### POST `/tickets`

Создание заявки.

```json
{
  "title": "Не работает принтер",
  "description": "При печати появляется ошибка",
  "priority": "HIGH",
  "equipmentId": 2
}
```

### PUT `/tickets/{id}`

Редактирование заявки. Доступно ролям `ADMIN` и `TECHNICIAN`.

```json
{
  "title": "Не печатает МФУ",
  "description": "Ошибка замятия бумаги",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "equipmentId": 2,
  "assignedToId": 2
}
```

### PATCH `/tickets/{id}/status`

Изменение статуса заявки.

```json
{
  "status": "RESOLVED",
  "comment": "Проблема устранена"
}
```

### GET `/tickets/{id}/comments`

Возвращает комментарии заявки.

### POST `/tickets/{id}/comments`

Добавляет комментарий.

```json
{
  "message": "Проверил оборудование на месте"
}
```

## Оборудование

### GET `/equipment`

Возвращает список оборудования.

### POST `/equipment`

Создание карточки оборудования. Доступно ролям `ADMIN` и `TECHNICIAN`.

```json
{
  "inventoryNumber": "NB-2026-010",
  "title": "Ноутбук Acer Aspire",
  "type": "LAPTOP",
  "status": "IN_SERVICE",
  "location": "Кабинет 210",
  "serialNumber": "ACR-010",
  "assignedToId": 3,
  "departmentId": 2,
  "purchaseDate": "2026-01-15",
  "comment": "Выдан сотруднику"
}
```

### PUT `/equipment/{id}`

Обновление карточки оборудования.

### DELETE `/equipment/{id}`

Удаление карточки оборудования. Доступно роли `ADMIN`.

## Отчеты

### GET `/reports/dashboard`

Возвращает агрегированную статистику.

```json
{
  "totalTickets": 5,
  "openTickets": 3,
  "resolvedTickets": 2,
  "equipmentTotal": 10,
  "equipmentInRepair": 1,
  "ticketsByStatus": {
    "NEW": 2,
    "IN_PROGRESS": 1,
    "WAITING_PARTS": 0,
    "RESOLVED": 1,
    "CLOSED": 1
  },
  "equipmentByStatus": {
    "IN_SERVICE": 8,
    "REPAIR": 1,
    "RESERVED": 1,
    "WRITTEN_OFF": 0
  }
}
```

