#!/usr/bin/env python3
"""Revisa que un PR cumpla las convenciones del proyecto. Lista todos los problemas antes de fallar."""

import os
import re
import subprocess
import sys

MINIMO_DE_COMMITS = 3
MAXIMO_PRIMERA_LINEA = 72
TIPOS = "feat|fix|refactor|perf|test|build|ci|chore|style|revert"
FORMATO_COMMIT = re.compile(rf"^({TIPOS})(\([a-z-]+\))?!?: \S.*[^.]$")
FORMATO_RAMA = re.compile(rf"^({TIPOS})/[a-z0-9]+(-[a-z0-9]+)*$")
FORMATO_HOTFIX = re.compile(r"^hotfix/[a-z0-9]+(-[a-z0-9]+)*$")
FIRMA_AGREGADA = re.compile(r"^[a-z-]+-by:", re.IGNORECASE | re.MULTILINE)

CARPETAS_DE_LA_RAIZ = {"backend", "frontend", "infra", "pruebas-carga", ".github"}
ARCHIVOS_DE_LA_RAIZ = {"README.md", ".gitignore", ".gitattributes", ".editorconfig"}
NOMBRES_CON_PUNTO = {".github", ".gitignore", ".gitattributes", ".gitkeep", ".editorconfig", ".terraform.lock.hcl"}
EXTENSIONES_SECRETAS = (".pem", ".key", ".tfstate", ".tfvars", ".tfplan")
CARPETAS_GENERADAS = {"node_modules", "dist", "build", "bin", ".terraform"}

CARPETA_MIGRACIONES = "backend/app/src/main/resources/db/migration/"
FORMATO_MIGRACION = re.compile(r"^V(\d{3})__[a-z0-9_]+\.sql$")


def git(*argumentos):
    return subprocess.run(["git", *argumentos], check=True, capture_output=True, text=True).stdout


def revisar_titulo(titulo):
    if not FORMATO_COMMIT.match(titulo):
        yield f"El título del PR no sigue Conventional Commits: {titulo}"
    if len(titulo) > MAXIMO_PRIMERA_LINEA:
        yield f"El título del PR pasa de {MAXIMO_PRIMERA_LINEA} caracteres"


def revisar_rama(rama):
    if not (FORMATO_RAMA.match(rama) or FORMATO_HOTFIX.match(rama)):
        yield f"La rama debe llamarse tipo/descripcion-en-minusculas: {rama}"


def revisar_commits(commits, exigir_minimo):
    if exigir_minimo and len(commits) < MINIMO_DE_COMMITS:
        yield f"El PR tiene {len(commits)} commit(s) y el mínimo es {MINIMO_DE_COMMITS}"
    for identificador, mensaje in commits:
        primera_linea = mensaje.split("\n", 1)[0]
        corto = identificador[:7]
        if not FORMATO_COMMIT.match(primera_linea):
            yield f"{corto} no sigue Conventional Commits: {primera_linea}"
        if len(primera_linea) > MAXIMO_PRIMERA_LINEA:
            yield f"{corto} pasa de {MAXIMO_PRIMERA_LINEA} caracteres en la primera línea"
        if FIRMA_AGREGADA.search(mensaje):
            yield f"{corto} lleva una línea de firma o coautoría, que el proyecto no usa"


def revisar_archivo(ruta):
    partes = ruta.split("/")
    nombre = partes[-1]
    if len(partes) == 1:
        if nombre not in ARCHIVOS_DE_LA_RAIZ:
            yield f"{ruta}: la raíz solo admite {', '.join(sorted(ARCHIVOS_DE_LA_RAIZ))}"
        return
    if partes[0] not in CARPETAS_DE_LA_RAIZ:
        yield f"{ruta}: la raíz solo admite las carpetas {', '.join(sorted(CARPETAS_DE_LA_RAIZ))}"
    if nombre.endswith(".md"):
        yield f"{ruta}: la documentación no va en el repo"
    if nombre.endswith(EXTENSIONES_SECRETAS) or ".tfstate" in nombre:
        yield f"{ruta}: parece un secreto o un estado, no debe subirse"
    for parte in partes:
        if parte in CARPETAS_GENERADAS:
            yield f"{ruta}: es contenido generado"
            break
        if parte.startswith(".") and parte not in NOMBRES_CON_PUNTO:
            yield f"{ruta}: archivo oculto no permitido"
            break


def revisar_migraciones(cambios, migraciones_finales):
    for estado, ruta in cambios:
        if ruta.startswith(CARPETA_MIGRACIONES) and estado != "A":
            yield f"{ruta}: una migración ya integrada no se modifica, se crea otra versión"
    numeros = []
    for nombre in sorted(migraciones_finales):
        formato = FORMATO_MIGRACION.match(nombre)
        if not formato:
            yield f"{nombre}: la migración debe llamarse V001__modulo_accion.sql"
            continue
        numeros.append(int(formato.group(1)))
    if numeros != list(range(1, len(numeros) + 1)):
        yield f"Las migraciones deben numerarse sin saltos ni repetidos: {numeros}"


def leer_commits(base, cabeza):
    salida = git("log", "--no-merges", "--reverse", "--format=%H%x00%B%x01", f"{base}..{cabeza}")
    bloques = [bloque.strip("\n") for bloque in salida.split("\x01") if bloque.strip()]
    return [tuple(bloque.split("\x00", 1)) for bloque in bloques]


def leer_cambios(base, cabeza):
    salida = git("diff", "--no-renames", "--name-status", f"{base}...{cabeza}")
    return [tuple(linea.split("\t", 1)) for linea in salida.splitlines()]


def leer_migraciones(cabeza):
    salida = git("ls-tree", "--name-only", cabeza, CARPETA_MIGRACIONES)
    return [ruta.rsplit("/", 1)[-1] for ruta in salida.splitlines()]


def main():
    titulo = os.environ["TITULO"]
    rama = os.environ["RAMA"]
    base = os.environ["BASE"]
    cabeza = os.environ["CABEZA"]

    es_version = rama == "dev"
    es_hotfix = rama.startswith("hotfix/")

    problemas = []
    if not es_version:
        problemas += revisar_titulo(titulo)
        problemas += revisar_rama(rama)
        problemas += revisar_commits(leer_commits(base, cabeza), exigir_minimo=not es_hotfix)

    cambios = leer_cambios(base, cabeza)
    for estado, ruta in cambios:
        if estado != "D":
            problemas += revisar_archivo(ruta)
    problemas += revisar_migraciones(cambios, leer_migraciones(cabeza))

    for problema in problemas:
        print(f"::error::{problema}")
    if problemas:
        sys.exit(1)
    print("Convenciones correctas")


if __name__ == "__main__":
    main()
