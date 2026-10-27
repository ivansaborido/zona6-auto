# Zona 6 Auto

App de Android Auto para ver en la pantalla del coche los centros de Zona 6 con su semáforo, una nota corta, y botones para navegar hasta el centro o llamar al responsable.

## Descargar

Cada cambio en el código compila la app automáticamente. El instalable está siempre en:

**Releases → "Zona 6 Auto - última versión" → `Zona6Auto.apk`**

## Instalar en el móvil

1. Descarga `Zona6Auto.apk` y ábrelo. Si el móvil lo pide, permite instalar apps de este origen.
2. Abre la app y rellena los centros (dirección, responsable, teléfono, semáforo y nota), o importa un CSV.

## Activarla en Android Auto (solo la primera vez)

1. Ajustes del móvil → Android Auto.
2. Toca 10 veces seguidas en "Versión" y acepta el aviso.
3. Menú ⋮ → Ajustes para desarrolladores → activa **Fuentes desconocidas**.
4. Conecta el USB al coche: aparece el icono "Zona 6 Auto".

## Formato del CSV

```
nombre;direccion;responsable;telefono;estado;nota
Málaga;Calle Ejemplo 1, Málaga;Nombre;600000000;rojo;Saturación 92%
```

`estado`: verde, ambar o rojo.
