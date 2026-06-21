#!/usr/bin/env bash
set -euo pipefail

#   ./run.sh start  
#   ./run.sh stop   
#   ./run.sh restart 
#   ./run.sh status  
#   ./run.sh seed    — тестовые данные (generate_data.py)
#   ./run.sh logs    — логи backend

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CODE_DIR="${ROOT_DIR}/code"
FRONTEND_DIR="${CODE_DIR}/frontend"
BACKEND_DIR="${CODE_DIR}/backend"
COMPOSE_FILE="${CODE_DIR}/docker-compose.yml"
DC="docker compose -f ${COMPOSE_FILE}"
PID_FILE="${CODE_DIR}/.bookstore-backend.pid"
LOG_FILE="${BACKEND_DIR}/logs/bookstore-run.log"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

info()    { echo -e "${CYAN}[INFO]${RESET} $*"; }
success() { echo -e "${GREEN}[OK]${RESET}   $*"; }
warn()    { echo -e "${YELLOW}[WARN]${RESET} $*"; }
error()   { echo -e "${RED}[ERR]${RESET}  $*" >&2; }

require_cmd() {
    local name="$1"
    if ! command -v "$name" &>/dev/null; then
        error "Не найдено: ${name}. Установите и повторите."
        exit 1
    fi
}

require_docker() {
    require_cmd docker
    if ! docker info &>/dev/null; then
        error "Docker не запущен. Запустите Docker Desktop."
        exit 1
    fi
}

wait_healthy() {
    local service="$1"
    local max=60
    local n=0
    info "Ожидание ${service}..."
    while ! $DC ps "$service" 2>/dev/null | grep -q "healthy"; do
        sleep 2
        n=$((n + 2))
        if [ "$n" -ge "$max" ]; then
            error "${service} не стал healthy за ${max}s"
            exit 1
        fi
    done
    success "${service} готов"
}

backend_running() {
    [ -f "$PID_FILE" ] || return 1
    local pid
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null
}

stop_backend() {
    if backend_running; then
        local pid
        pid="$(cat "$PID_FILE")"
        info "Остановка backend (PID ${pid})..."
        kill "$pid" 2>/dev/null || true
        for _ in 1 2 3 4 5; do
            kill -0 "$pid" 2>/dev/null || break
            sleep 1
        done
        if kill -0 "$pid" 2>/dev/null; then
            warn "Принудительное завершение backend"
            kill -9 "$pid" 2>/dev/null || true
        fi
        success "Backend остановлен"
    elif [ -f "$PID_FILE" ]; then
        warn "Устаревший PID-файл — удаляю"
    fi
    rm -f "$PID_FILE"
}

build_frontend() {
    require_cmd npm
    info "Сборка frontend..."
    (cd "$FRONTEND_DIR" && npm run build)
    success "Frontend собран в backend/src/main/resources/static/"
}

build_backend() {
    require_cmd mvn
    require_cmd java
    info "Сборка backend (mvn package)..."
    (cd "$BACKEND_DIR" && mvn -q -DskipTests package)
    if [ ! -d "${BACKEND_DIR}/target/components" ] || [ ! -d "${BACKEND_DIR}/target/lib" ]; then
        error "Не найдены target/components или target/lib после сборки"
        exit 1
    fi
    success "Backend собран"
}

start_backend() {
    if backend_running; then
        warn "Backend уже запущен (PID $(cat "$PID_FILE")). Используйте: ./run.sh restart"
        return 0
    fi

    if lsof -i :8080 -sTCP:LISTEN -t &>/dev/null 2>&1; then
        error "Порт 8080 занят. Освободите его или остановите другой процесс."
        exit 1
    fi

    mkdir -p "${BACKEND_DIR}/logs"
    info "Запуск backend на http://localhost:8080 ..."

    nohup "${BACKEND_DIR}/scripts/run-web.sh" postgres \
        --spring.datasource.url=jdbc:postgresql://localhost:5433/bookstore_db \
        --spring.datasource.username=postgres \
        --spring.datasource.password=postgres \
        --spring.data.redis.host=localhost \
        --spring.data.redis.port=6379 \
        --spring.jpa.hibernate.ddl-auto=none \
        >>"$LOG_FILE" 2>&1 &

    local pid=$!
    echo "$pid" >"$PID_FILE"

    local n=0
    while ! curl -sf -o /dev/null http://localhost:8080/ 2>/dev/null; do
        sleep 1
        n=$((n + 1))
        if ! kill -0 "$pid" 2>/dev/null; then
            error "Backend завершился при старте. Лог: ${LOG_FILE}"
            tail -n 30 "$LOG_FILE" >&2 || true
            rm -f "$PID_FILE"
            exit 1
        fi
        if [ "$n" -ge 90 ]; then
            error "Backend не ответил за 90s. Лог: ${LOG_FILE}"
            exit 1
        fi
    done

    success "Backend запущен (PID ${pid})"
}

cmd_start() {
    require_docker
    require_cmd npm
    require_cmd mvn
    require_cmd java
    require_cmd curl

    info "Запуск PostgreSQL и Redis..."
    $DC up -d postgres redis
    wait_healthy postgres
    wait_healthy redis

    build_frontend
    build_backend
    start_backend

    echo ""
    echo -e "${BOLD}========================================${RESET}"
    echo -e "${GREEN}  Bookstore запущен (актуальная сборка)${RESET}"
    echo -e "${BOLD}========================================${RESET}"
    echo -e "  Приложение  →  ${CYAN}http://localhost:8080${RESET}"
    echo -e "  PostgreSQL  →  localhost:5433  (bookstore_db)"
    echo -e "  Redis       →  localhost:6379"
    echo -e "  Лог backend →  ${LOG_FILE}"
    echo -e "${BOLD}========================================${RESET}"
    echo -e "  Тест: admin / admin123"
    echo -e "  Данные: ${YELLOW}./run.sh seed${RESET}"
    echo -e "  Стоп:   ${YELLOW}./run.sh stop${RESET}"
    echo ""
}

cmd_stop() {
    stop_backend
    if [ -f "$COMPOSE_FILE" ]; then
        require_docker
        info "Остановка PostgreSQL и Redis..."
        $DC stop postgres redis 2>/dev/null || $DC stop 2>/dev/null || true
        success "Инфраструктура остановлена"
    fi
}

cmd_restart() {
    cmd_stop
    cmd_start
}

cmd_status() {
    echo ""
    if [ -f "$COMPOSE_FILE" ]; then
        echo -e "${BOLD}Docker:${RESET}"
        $DC ps postgres redis 2>/dev/null || warn "Docker Compose недоступен"
    fi
    echo ""
    if backend_running; then
        echo -e "${GREEN}Backend:${RESET} запущен (PID $(cat "$PID_FILE")), http://localhost:8080"
    else
        echo -e "${YELLOW}Backend:${RESET} не запущен"
    fi
    echo ""
}

cmd_seed() {
    require_docker
    if ! $DC ps postgres 2>/dev/null | grep -q "running\|healthy"; then
        error "PostgreSQL не запущен. Сначала: ./run.sh start"
        exit 1
    fi
    if ! python3 -c "import psycopg2" 2>/dev/null; then
        warn "Устанавливаю psycopg2-binary..."
        pip3 install psycopg2-binary -q
    fi
    info "Загрузка тестовых данных..."
    python3 "${CODE_DIR}/scripts/generate_data.py"
    success "Данные загружены"
}

cmd_logs() {
    if [ -f "$LOG_FILE" ]; then
        tail -f "$LOG_FILE"
    else
        error "Лог не найден: ${LOG_FILE}. Сначала: ./run.sh start"
        exit 1
    fi
}

usage() {
    echo ""
    echo -e "${BOLD}Использование:${RESET} ./run.sh <команда>"
    echo ""
    echo -e "  ${GREEN}start${RESET}     Собрать и запустить проект"
    echo -e "  ${GREEN}stop${RESET}      Остановить backend и postgres/redis"
    echo -e "  ${GREEN}restart${RESET}   Перезапуск"
    echo -e "  ${GREEN}status${RESET}    Статус"
    echo -e "  ${GREEN}seed${RESET}      Тестовые данные"
    echo -e "  ${GREEN}logs${RESET}      Лог backend (tail -f)"
    echo ""
}

case "${1:-}" in
    start)   cmd_start ;;
    stop)    cmd_stop ;;
    restart) cmd_restart ;;
    status)  cmd_status ;;
    seed)    cmd_seed ;;
    logs)    cmd_logs ;;
    help|-h|--help) usage ;;
    "") usage ;;
    *)
        error "Неизвестная команда: $1"
        usage
        exit 1
        ;;
esac
