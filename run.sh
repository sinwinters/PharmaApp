#!/usr/bin/env bash
# Скрипт запуска на Linux/macOS — проверяет зависимости и запускает приложение

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MISSING=()
WARNINGS=()

# Java 21+
if ! command -v java &>/dev/null; then
  MISSING+=("Java не найдена. Установите JDK 21 (https://adoptium.net/) и добавьте в PATH.")
else
  JAVA_VER=$(java -version 2>&1 | head -1 | sed -n 's/.*version "\([0-9]*\).*/\1/p')
  if [ -z "$JAVA_VER" ] || [ "$JAVA_VER" -lt 21 ]; then
    MISSING+=("Требуется Java 21 или новее. Сейчас: Java $JAVA_VER. Установите JDK 21.")
  fi
fi

# Node.js 18+
if ! command -v node &>/dev/null; then
  MISSING+=("Node.js не найден. Установите с https://nodejs.org и добавьте в PATH.")
else
  NODE_VER=$(node -v | sed 's/v\([0-9]*\).*/\1/')
  if [ "$NODE_VER" -lt 18 ]; then
    MISSING+=("Требуется Node.js 18+. Сейчас: $(node -v). Установите с https://nodejs.org")
  fi
fi

# Docker
DOCKER_AVAILABLE=false
if command -v docker &>/dev/null && docker compose version &>/dev/null; then
  DOCKER_AVAILABLE=true
fi

# Порты
PG_OPEN=0
REDIS_OPEN=0
(command -v nc &>/dev/null && nc -z 127.0.0.1 5432 2>/dev/null) && PG_OPEN=1
(command -v nc &>/dev/null && nc -z 127.0.0.1 6379 2>/dev/null) && REDIS_OPEN=1

if [ "$PG_OPEN" -eq 0 ] || [ "$REDIS_OPEN" -eq 0 ]; then
  if [ "$DOCKER_AVAILABLE" = true ]; then
    WARNINGS+=("PostgreSQL или Redis не запущены. Запускаю через Docker...")
  else
    [ "$PG_OPEN" -eq 0 ] && MISSING+=("PostgreSQL не доступен на localhost:5432. Запустите: docker compose up -d postgres redis (или установите PostgreSQL вручную, БД pharma, пользователь pharma, пароль pharma).")
    [ "$REDIS_OPEN" -eq 0 ] && MISSING+=("Redis не доступен на localhost:6379. Запустите: docker compose up -d postgres redis (или установите Redis вручную).")
  fi
fi

# Gradle / gradlew
GRADLE_CMD=""
if [ -f "backend/gradlew" ]; then
  GRADLE_CMD="./gradlew"
elif command -v gradle &>/dev/null; then
  GRADLE_CMD="gradle"
else
  MISSING+=("Gradle не найден. В каталоге backend выполните: gradle wrapper --gradle-version 8.10 (если установлен Gradle).")
fi

# Вывод отсутствующего
if [ ${#MISSING[@]} -gt 0 ]; then
  echo ""
  echo "ОШИБКА: Не удалось запустить приложение."
  echo ""
  echo "Отсутствует или настроено неверно:"
  for m in "${MISSING[@]}"; do echo "  * $m"; done
  echo ""
  echo "После установки перезапустите: ./run.sh"
  exit 1
fi

for w in "${WARNINGS[@]}"; do echo "$w"; done

# Docker: postgres + redis
if [ "$DOCKER_AVAILABLE" = true ] && { [ "$PG_OPEN" -eq 0 ] || [ "$REDIS_OPEN" -eq 0 ]; }; then
  echo "Запуск PostgreSQL и Redis (Docker)..."
  docker compose up -d postgres redis
  echo "Ожидание готовности БД (до 30 сек)..."
  for i in $(seq 1 30); do
    if nc -z 127.0.0.1 5432 2>/dev/null; then break; fi
    sleep 1
  done
  echo "PostgreSQL и Redis запущены."
fi

# npm install frontend
if [ ! -d "frontend/node_modules" ]; then
  echo "Установка зависимостей frontend..."
  (cd frontend && npm install)
fi

# Backend в фоне
echo "Запуск Backend..."
if [ -n "$GRADLE_CMD" ]; then
  (cd backend && $GRADLE_CMD bootRun) &
  BACKEND_PID=$!
fi
sleep 25

# Frontend
echo "Запуск Frontend..."
echo ""
echo "========================================"
echo "  Frontend:  http://localhost:5173"
echo "  Backend:   http://localhost:8080/api/v1"
echo "  Демо-вход: admin / password"
echo "  Остановка: Ctrl+C (остановит frontend; backend: kill $BACKEND_PID)"
echo "========================================"
(cd frontend && npm run dev)
