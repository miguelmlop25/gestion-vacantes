#!/bin/bash
# Script para dar permisos al Maven wrapper y construir el proyecto

# Dar permisos de ejecución al wrapper de Maven
chmod +x mvnw

# Ejecutar el build
./mvnw clean install -DskipTests
