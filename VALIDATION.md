# Verificación de las mejoras

La suite JVM se ejecutó con Kotlin 2.2.10, JUnit 4.13.2, coroutines 1.10.2,
Lifecycle Android 2.8.7 y las anotaciones reales de Room 2.7.0. Incluye motor,
modelo, repositorio con FakeSudokuDao y ViewModel. No sustituye una compilación
Android ni valida la generación de código de Room o el renderizado de Compose.

En un entorno con JDK 17 y Android SDK 36.1:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat connectedAndroidTest
```

Revisión manual pendiente en dispositivo/emulador:

- Actualizar desde v1/v2/v3 y comprobar partida, notas y estadísticas.
- Gesto Atrás, segundo plano y cancelación durante generación.
- Cierre del proceso, restauración y deshacer/rehacer.
- TalkBack, texto ampliado, horizontal y pantallas pequeñas.
- Validar las capturas visuales tras agregar Rehacer y el selector de entrada.

El generador se comprueba con 16 semillas por dificultad y verifica unicidad y
nivel; el desafío diario también se comprueba con la misma semilla dos veces.

Resultado local: 26 pruebas JVM aprobadas. Se comprobó también con SQLite la conservación de datos al agregar las columnas v4. El wrapper coincide con su SHA-256 oficial. Gradle testDebugUnitTest se detuvo durante configuración con 'No Java compiler found': el entorno disponible es JRE 17, no JDK. Tampoco se encontró Android SDK. No se generó un APK ni se verificó la UI en dispositivo.
