# Система управления аптекой с модулем автоматического заказа лекарств

Веб-приложение из двух независимых проектов: **Backend** (Spring Boot REST API) и **Frontend** (React SPA).

## Как запустить (рекомендуемый способ)

**Windows:** дважды щёлкните по `run.bat` или в PowerShell выполните:
```powershell
.\run.ps1
```

**Linux / macOS:** в терминале из каталога проекта:
```bash
chmod +x run.sh
./run.sh
```

Скрипт сам:
- **Догружает недостающее:** при отсутствии Java 21, Node.js 18+ или Gradle Wrapper — загружает их в каталог `.pharma-app` (только Windows для Java/Node; на Linux/macOS нужна ручная установка или свой скрипт);
- если установлен **Docker** — поднимает **PostgreSQL** и **Redis**;
- если чего-то не удаётся получить автоматически — выводит, **что именно отсутствует** и что сделать;
- запускает backend и frontend (на Windows — в двух отдельных окнах).

После запуска откройте в браузере **http://localhost:5173**.

---

## Требования (что проверяет скрипт)

| Нужно | Зачем |
|-------|--------|
| **JDK 21** | Backend (Spring Boot) |
| **Node.js 18+** | Frontend (React, Vite) |
| **Docker** (желательно) | PostgreSQL и Redis одной командой; без Docker их нужно установить и запустить вручную |
| **Gradle** или **gradlew** в `backend/` | Сборка backend; скрипт при отсутствии может догрузить Gradle и создать wrapper |

## Запуск вручную (без скрипта)

### 1. База данных и Redis

```bash
docker compose up -d postgres redis
```

(Или установите PostgreSQL 15+ и Redis локально: БД `pharma`, пользователь `pharma`, пароль `pharma`; Redis на порту 6379.)

### 2. Backend

```bash
cd backend
gradle bootRun
# или при наличии wrapper: ./gradlew bootRun
```

Адреса: http://localhost:8080 (приложение), http://localhost:8080/api/v1 (API), http://localhost:8080/api/v1/swagger-ui.html (Swagger).

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Откройте http://localhost:5173.

## Запуск через Docker Compose (backend + frontend + БД)

```bash
# Полный запуск (postgres, redis, backend, frontend)
docker compose --profile full up -d --build
```

Можно создать `.env` на основе `.env.example` и запускать без длинной команды:

```bash
cp .env.example .env
# заполните GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET
docker compose --profile full up -d --build
```

По умолчанию OAuth2 redirect после логина ведёт на `http://localhost`.
При развёртывании на другом домене задайте переменную окружения перед запуском:

```bash
APP_FRONTEND_URL=https://your-domain.example docker compose --profile full up -d --build
```

- Frontend: http://localhost  
- Backend API: http://localhost:8080/api/v1  

### Новые API для аналитики и отчетности
- `GET /api/v1/analytics/drugs?periodDays=30` — агрегированная аналитика по продажам и остаткам.
- `GET /api/v1/analytics/reports/minzdrav-rb?periodDays=30` — шаблонный отчет по контрольным требованиям Минздрава РБ.
- `POST /api/v1/integrations/rb-medical-card/verify` — заглушка интеграции карты медицинского обслуживания РБ.
- `GET /api/v1/sales/benefits/rb` — справочник льгот и скидок РБ для применения при продаже.
- `GET /api/v1/orders/{id}/invoice` — накладная для заказа (для автозаказов формируется автоматически).


Для входа через Google (OAuth2) передайте переменные окружения:

```bash
GOOGLE_CLIENT_ID=your-client-id \
GOOGLE_CLIENT_SECRET=your-client-secret \
APP_FRONTEND_URL=http://localhost \
  docker compose --profile full up -d --build
```

## Структура проекта

```
PharmaApp/
├── backend/                 # Spring Boot REST API
│   ├── src/main/java/com/pharma/
│   │   ├── domain/          # сущности, репозитории, стратегии, наблюдатели
│   │   ├── application/     # DTO, сервисы, порты (OrderBuilder)
│   │   └── infrastructure/  # JPA, Security, REST, интеграции (ExchangeRateClient)
│   └── src/main/resources/db/migration/  # Flyway
├── frontend/                # React SPA
│   └── src/
│       ├── api/             # клиент API
│       ├── store/           # Zustand (auth)
│       ├── components/
│       └── pages/
├── docker-compose.yml
└── README.md
```

## Реализованные требования

- **Архитектура:** модульный монолит, разделение Backend / Frontend, общение по HTTP/JSON, REST.
- **Backend:** Java 21, Spring Boot 3, Clean Architecture, паттерны Strategy (PricingStrategy), Observer (LowStockObserver + AutoOrderService), Builder (OrderBuilder), Facade (SaleService).
- **БД:** PostgreSQL, 10 таблиц в 3НФ, миграции Flyway.
- **Кэш:** Redis для категорий, лекарств (findById), поставщиков; TTL и инвалидация при изменении данных.
- **Фоновые задачи:** Quartz Scheduler — автозаказ по расписанию (cron); задачу можно расширять (SpringBeanJobFactory).
- **API:** пагинация (Pageable), фильтрация и сортировка (query params, JPA Specification для лекарств), JWT + refresh, RBAC (@PreAuthorize).
- **Интеграции:** внешний REST API курсов валют (open.er-api.com), OAuth2 Google (при настройке GOOGLE_CLIENT_ID/SECRET).
- **Автозаказ:** Quartz Job проверяет остатки ниже min_quantity и создаёт заказы по поставщикам.
- **Тесты:** JUnit 5, Mockito; интеграционные тесты с Testcontainers (PostgreSQL); unit-тесты сервисов и OrderBuilder.
- **Статический анализ:** Checkstyle (config в `backend/config/checkstyle`), SpotBugs (исключения в `backend/config/spotbugs/exclude.xml`).
- **CI/CD:** GitHub Actions — сборка backend (gradle build), frontend (npm build), запуск тестов (в т.ч. Testcontainers при наличии Docker).
- **Логирование:** Logback (`logback-spring.xml`) — структурированный вывод (JSON) для профилей prod/docker, обычный паттерн для dev/test.

## Use cases (более 15)

1. Вход по логину/паролю  
2. Обновление токена (refresh)  
3. Вход через Google (OAuth2)  
4. Просмотр и пагинация списка лекарств  
5. Фильтрация лекарств по названию, категории, поставщику  
6. Создание / редактирование / удаление лекарства  
7. Просмотр остатков (в карточке лекарства)  
8. Проведение продажи (списание остатков)  
9. Просмотр истории продаж  
10. Создание заказа поставщику  
11. Просмотр заказов  
12. Изменение статуса заказа  
13. Автоматическое создание заказов при низком остатке (по расписанию)  
14. CRUD категорий  
15. CRUD поставщиков  
16. Получение курса валюты (внешний API)  

## Демо-данные

После первого запуска Flyway применяются миграции, включая `V2__demo_data.sql` с тестовыми записями (админ-пользователь, категории, поставщики, лекарства). Для production рекомендуется заменить или отключить этот seed-набор.
 

### Применение льгот при продаже
В `POST /api/v1/sales` можно передать поле `benefitCode`, например:
`RB_DISABLED_GROUP_1_2`, `RB_CHILD_UNDER_3`, `RB_CHRONIC_DISEASE`.
Сервис применит соответствующую скидку по всей продаже и вернет суммы до/после скидки и ссылку на норму права в ответе.


### Автозаказ: GLN и накладная
Для автозаказов система передает GLN фармсклада (из `suppliers.warehouse_gln`, при отсутствии — `app.auto-order.default-gln`)
и автоматически формирует накладную (`invoiceNumber`, `invoiceGeneratedAt`).


### Отпуск антибиотиков и наркосодержащих препаратов с ЭЦП Avest
Для лекарств с признаком `requiresEdsSignature=true` и типом контроля (`ANTIBIOTIC`/`NARCOTIC`)
при `POST /api/v1/sales` нужно передать:
- `prescriptionNumber`
- `edsSignature`
- `edsProvider=AVEST`

Проверка подписи выполняется через интеграционный gateway-заглушку Avest.
