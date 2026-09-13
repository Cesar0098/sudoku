# Sudoku

Juego de Sudoku clásico para Android, offline y minimalista: sin cuentas, sin
publicidad, sin analíticas ni conexión a internet de ningún tipo.

## Características

- **100% offline**: no pide permisos, no tiene cuentas/login, no muestra
  anuncios ni hace tracking de ningún tipo.
- **Generador propio** de tableros con solución única garantizada, en cuatro
  dificultades (Fácil / Medio / Difícil / Experto). La dificultad se calcula
  según las técnicas lógicas necesarias para resolver el tablero, no solo por
  la cantidad de celdas vacías.
- **Solver propio** para validar movimientos, comprobar unicidad de la
  solución y evaluar la dificultad.
- **Notas estilo lápiz** (candidatos 1-9 por celda), con limpieza automática
  configurable al colocar un número.
- **Resaltado** de fila, columna, bloque y números iguales al seleccionar una
  celda — incluye coincidencias dentro de las notas de otras celdas.
- **Dos modos de error** configurables: manual (hasta 3 errores) o
  auto-check (avisa al instante si el número es incorrecto).
- Sistema de **pistas**, **deshacer**, **pausa** y temporizador persistente
  entre cierres de la app.
- **Guardado automático** completo de la partida en curso (tablero, notas,
  tiempo, errores, pistas, dificultad) para poder continuarla después.
- **Estadísticas** detalladas por dificultad: partidas comenzadas, % de
  victorias, mejor tiempo, tiempo promedio, errores totales, pistas usadas,
  racha de partidas perfectas y victorias perfectas totales.
- Temas claro y oscuro, con un tablero legible en ambos.
- Diseño e iconografía propios (íconos custom estilo Material Symbols,
  tipografía "New Amsterdam" para el título).

## Stack técnico

- Kotlin + [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- [Room](https://developer.android.com/training/data-storage/room) para la persistencia local
- Arquitectura por capas: UI (Compose) → ViewModel → motor de Sudoku
  (generador + solver) → repositorio → base de datos local
- Tests con JUnit, Robolectric y [Roborazzi](https://github.com/takahirom/roborazzi) (screenshot tests)

## Requisitos

- [Android Studio](https://developer.android.com/studio) (versión reciente)
- JDK 17 (el que trae Android Studio ya sirve)
- Android SDK 36 (`compileSdk`); soporta desde Android 7.0 (API 24)

## Cómo compilarlo

1. Cloná el repositorio.
2. Abrilo con Android Studio (**Open** → elegí la carpeta del proyecto).
3. Dejá que Android Studio sincronice Gradle.
4. Ejecutalo en un emulador o dispositivo físico.

La app es 100% local: no necesitás configurar ninguna clave ni servicio
externo para compilarla o correrla. (`.env.example` y el plugin de
"secrets" en `app/build.gradle.kts` son restos de la plantilla de
scaffolding original — la app no llama a ninguna API y no los usa.)

### Firma de builds "release"

El `signingConfig` de `release` busca un keystore en la variable de entorno
`KEYSTORE_PATH` (o en `my-upload-key.jks` en la raíz del proyecto, por
defecto) y las variables `STORE_PASSWORD` / `KEY_PASSWORD`. Para compilar y
probar la app en modo debug no necesitás nada de esto.

## Tests

```
./gradlew test                 # tests locales (JVM / Robolectric)
./gradlew connectedAndroidTest  # tests instrumentados (requieren un dispositivo/emulador)
```

## Licencia

El código de este proyecto se distribuye bajo la licencia que figura en
[LICENSE](LICENSE).

### Fuentes de terceros

El título de la pantalla de inicio usa la fuente [New Amsterdam](https://fonts.google.com/specimen/New+Amsterdam)
(Google Fonts), licenciada bajo la SIL Open Font License 1.1 — ver
[licenses/OFL-NewAmsterdam.txt](licenses/OFL-NewAmsterdam.txt).
