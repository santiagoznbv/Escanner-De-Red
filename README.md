
# Escáner de Red – Entrega Final

Proyecto en Java con interfaz Swing para escanear un rango de IPs, resolver nombres de host, filtrar/ordenar resultados y exportar a CSV.

## Requisitos
- Java 17 o superior
- Permisos para ejecutar `ping`/`isReachable` y `nslookup`

## Cómo ejecutar
Compilar desde la carpeta del proyecto:

```bash
javac -d bin src/gui/AppWindow.java src/logic/*.java
java -cp bin gui.AppWindow
```

## Funcionalidades
- Escaneo de rango IPv4 (mismo /24)
- Tiempo de respuesta y estado (activo/inactivo)
- Resolución de nombre de host (nslookup)
- Búsqueda en vivo + filtro "Solo activos"
- Orden por columnas (click en encabezado)
- Exportación a CSV
- Copiar fila al portapapeles
