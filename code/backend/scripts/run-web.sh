#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

COMPONENTS_DIR="${PROJECT_DIR}/dist/components"
LIB_DIR="${PROJECT_DIR}/dist/lib"

if [[ ! -d "${COMPONENTS_DIR}" || ! -d "${LIB_DIR}" ]]; then
  COMPONENTS_DIR="${PROJECT_DIR}/target/components"
  LIB_DIR="${PROJECT_DIR}/target/lib"
fi

if [[ ! -d "${COMPONENTS_DIR}" || ! -d "${LIB_DIR}" ]]; then
  echo "Не найдены компоненты для запуска."
  echo "Ожидаются папки:"
  echo "  - ${PROJECT_DIR}/dist/components и ${PROJECT_DIR}/dist/lib"
  echo "  или"
  echo "  - ${PROJECT_DIR}/target/components и ${PROJECT_DIR}/target/lib"
  echo
  echo "Сначала соберите backend:"
  echo "  mvn -DskipTests package"
  exit 1
fi

CLASSPATH="${COMPONENTS_DIR}/*:${LIB_DIR}/*"

cd "${PROJECT_DIR}"
DB_PROFILE="${1:-postgres}"
if [[ "${DB_PROFILE}" != "postgres" && "${DB_PROFILE}" != "mongo" ]]; then
  echo "Использование: $0 [postgres|mongo] [доп.аргументы spring]"
  exit 2
fi
shift || true
java -cp "${CLASSPATH}" ru.bookstore.Application --spring.profiles.active=web,"${DB_PROFILE}" "$@"
