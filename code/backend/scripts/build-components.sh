#!/usr/bin/env bash
# Builds the backend then copies runnable component JARs to dist/.
# Artifact split matches diagrams/src/c4_l3.puml (see maven-jar-plugin classifiers in pom.xml):
#   repositoryinterfaces — domain + repository interfaces (DIP)
#   repository           — JPA/Mongo/redis repository implementations + config
#   authservice          — Identity Services (Auth*, UserService, exceptions)
#   bookservice          — Catalog Services (Book*, Review*, Collection*, Wishlist*)
#   orderservice         — Order Service
#   recommendationservice— Recommendation Services (Recommendation*, Collaborative*, Content*)
#   identityapi          — Identity Controllers (Auth, User) + DTOs
#   catalogapi           — Catalog Controllers (+ Wishlist) + DTOs
#   orderapi             — Order Controllers + DTOs
#   techui               — Spring Boot launcher + bundled web layer + static assets
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${PROJECT_DIR}"

mvn -q clean package -DskipTests

DIST_DIR="${PROJECT_DIR}/dist"
COMPONENTS_DIR="${DIST_DIR}/components"
LIB_DIR="${DIST_DIR}/lib"

rm -rf "${DIST_DIR}"
mkdir -p "${COMPONENTS_DIR}" "${LIB_DIR}"

cp "${PROJECT_DIR}/target"/bookstore-*.jar "${COMPONENTS_DIR}/"
cp "${PROJECT_DIR}/target/lib/"*.jar "${LIB_DIR}/"

