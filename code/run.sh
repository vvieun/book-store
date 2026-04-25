#!/usr/bin/env bash
set -euo pipefail


SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"
DC="docker compose -f $COMPOSE_FILE"

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

require_docker() {
    if ! command -v docker &>/dev/null; then
        error "Docker не установлен. Установите Docker Desktop или Docker Engine."
        exit 1
    fi
    if ! docker info &>/dev/null; then
        error "Docker не запущен. Запустите Docker Desktop и попробуйте снова."
        exit 1
    fi
}

wait_healthy() {
    local service="$1"
    local max=60
    local n=0
    info "Ожидание готовности $service..."
    while ! $DC ps "$service" 2>/dev/null | grep -q "healthy"; do
        sleep 2
        n=$((n+2))
        if [ $n -ge $max ]; then
            error "$service не стал healthy за ${max}s. Проверьте: ./run.sh logs $service"
            exit 1
        fi
    done
    success "$service готов."
}

cmd_start() {
    require_docker
    info "Запуск сервисов (сборка образов при необходимости)..."
    $DC up -d --build

    wait_healthy postgres
    wait_healthy redis

    echo ""
    echo -e "${BOLD}========================================${RESET}"
    echo -e "${GREEN}  Bookstore успешно запущен!${RESET}"
    echo -e "${BOLD}========================================${RESET}"
    echo -e "  Frontend  →  ${CYAN}http://localhost:3000${RESET}"
    echo -e "  API       →  ${CYAN}http://localhost:8080${RESET}"
    echo -e "  Swagger   →  ${CYAN}http://localhost:8080/swagger-ui.html${RESET}"
    echo -e "  PostgreSQL →  localhost:5433  (БД: bookstore_db)"
    echo -e "  Redis     →  localhost:6379"
    echo -e "${BOLD}========================================${RESET}"
    echo -e "  Остановить:  ${YELLOW}./run.sh stop${RESET}"
    echo -e "  Логи:        ${YELLOW}./run.sh logs [api|frontend|postgres|redis]${RESET}"
    echo -e "  Данные:      ${YELLOW}./run.sh seed${RESET}"
    echo ""
}

cmd_stop() {
    require_docker
    info "Остановка сервисов..."
    $DC down
    success "Все сервисы остановлены."
}

cmd_restart() {
    local service="${1:-}"
    require_docker
    if [ -n "$service" ]; then
        info "Перезапуск $service..."
        $DC restart "$service"
        success "$service перезапущен."
    else
        info "Перезапуск всех сервисов..."
        $DC down
        $DC up -d --build
        success "Все сервисы перезапущены."
    fi
}

cmd_logs() {
    require_docker
    local service="${1:-}"
    if [ -n "$service" ]; then
        $DC logs -f --tail=100 "$service"
    else
        $DC logs -f --tail=50
    fi
}

cmd_status() {
    require_docker
    echo ""
    $DC ps
    echo ""
}

cmd_seed() {
    require_docker

    if ! $DC ps postgres 2>/dev/null | grep -q "running\|healthy"; then
        error "PostgreSQL не запущен. Выполните сначала: ./run.sh start"
        exit 1
    fi

    info "Загрузка тестовых данных (generate_data.py)..."

    if ! python3 -c "import psycopg2, bcrypt" 2>/dev/null; then
        warn "Устанавливаю зависимости Python..."
        pip3 install psycopg2-binary bcrypt -q
    fi

    python3 "$SCRIPT_DIR/generate_data.py"
    success "Тестовые данные загружены."
}

cmd_sql() {
    require_docker

    if ! $DC ps postgres 2>/dev/null | grep -q "running\|healthy"; then
        error "PostgreSQL не запущен. Выполните сначала: ./run.sh start"
        exit 1
    fi

    local SQL_DIR="$SCRIPT_DIR/sql"
    if [ ! -d "$SQL_DIR" ]; then
        error "Директория $SQL_DIR не найдена."
        exit 1
    fi

    info "Применение SQL-скриптов к базе данных bookstore_db..."
    for f in "$SQL_DIR"/0*.sql; do
        info "  → $(basename "$f")"
        $DC exec -T postgres psql -U postgres -d bookstore_db -f - < "$f"
    done
    success "Все SQL-скрипты применены."
}

cmd_psql() {
    require_docker
    info "Подключение к PostgreSQL (bookstore_db)..."
    $DC exec postgres psql -U postgres -d bookstore_db
}

cmd_clean() {
    require_docker
    warn "Это удалит все контейнеры И тома (данные БД будут потеряны)!"
    read -r -p "Продолжить? [y/N] " confirm
    if [[ "$confirm" =~ ^[yY]$ ]]; then
        $DC down -v --remove-orphans
        success "Контейнеры и тома удалены."
    else
        info "Отменено."
    fi
}

usage() {
    echo ""
    echo -e "${BOLD}Использование:${RESET} ./run.sh <команда> [аргументы]"
    echo ""
    echo -e "${BOLD}Команды:${RESET}"
    echo -e "  ${GREEN}start${RESET}              Собрать и запустить все сервисы"
    echo -e "  ${GREEN}stop${RESET}               Остановить все сервисы"
    echo -e "  ${GREEN}restart${RESET} [сервис]   Перезапустить (все или один сервис)"
    echo -e "  ${GREEN}logs${RESET} [сервис]      Показать логи (api / frontend / postgres / redis)"
    echo -e "  ${GREEN}status${RESET}             Статус контейнеров"
    echo -e "  ${GREEN}seed${RESET}               Загрузить тестовые данные (generate_data.py)"
    echo -e "  ${GREEN}sql${RESET}                Применить SQL-скрипты из папки sql/"
    echo -e "  ${GREEN}psql${RESET}               Открыть psql-консоль (bookstore_db)"
    echo -e "  ${GREEN}clean${RESET}              Удалить контейнеры и тома (необратимо!)"
    echo ""
    echo -e "${BOLD}Примеры:${RESET}"
    echo -e "  ./run.sh start"
    echo -e "  ./run.sh logs api"
    echo -e "  ./run.sh restart frontend"
    echo -e "  ./run.sh seed"
    echo ""
}

case "${1:-}" in
    start)    cmd_start ;;
    stop)     cmd_stop ;;
    restart)  cmd_restart "${2:-}" ;;
    logs)     cmd_logs "${2:-}" ;;
    status)   cmd_status ;;
    seed)     cmd_seed ;;
    sql)      cmd_sql ;;
    psql)     cmd_psql ;;
    clean)    cmd_clean ;;
    help|-h|--help) usage ;;
    "")       usage ;;
    *)
        error "Неизвестная команда: $1"
        usage
        exit 1
        ;;
esac
