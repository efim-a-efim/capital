---
layout: default
lang: es
base: "/es"
key: "manual"
title: Manual de usuario
class: doc
---
# Manual de usuario

<p class="meta">Capital 2.2 · Android 8.0 y posterior</p>

## La idea

Usted guarda dinero en varios sitios: una cuenta de ahorro, efectivo, una cuenta de valores, una billetera de criptomonedas. Capital llama a cada sitio **bolsillo**. Quiere ese dinero para varias cosas: un fondo de emergencia, un viaje, un portátil. Capital llama a cada una **meta**. Usted conecta bolsillos a metas y la aplicación calcula en qué medida está cubierta cada meta con lo que tiene hoy. Añada los ahorros que **planea** hacer y además le dirá la fecha en que se completa cada meta.

Nada en la aplicación mueve dinero. Es un reflejo de lo que usted posee y una calculadora de lo que eso cubre.

## Primer inicio {#first-launch}

1. **Elija una carpeta.** Seleccione una carpeta exclusiva en el dispositivo, por ejemplo `Documents/Capital`. Ahí se escribe cada registro. Una carpeta que ya contiene datos de Capital se abre directamente.
2. **Ajustes → Moneda predeterminada.** Los totales y el Resumen se muestran en esa moneda.
3. Si quiere, configure **claves de proveedores** en Ajustes para los operadores que ofrecen límites más altos con una clave gratuita (Alchemy, TronGrid, TON Center, CoinGecko). Cada red y cada fuente de precios tiene una opción predeterminada que no necesita clave.

## Bolsillos {#buckets}

Pestaña Bolsillos → **+**. Asigne al bolsillo un nombre y una moneda. Ábralo para añadir posiciones:

- **Posición manual**: un nombre, un código de moneda o de activo (EUR, USD, BTC, el ticker de una acción que usted mismo valora…) y una cantidad. Úsela para saldos bancarios, efectivo y todo lo que la aplicación no puede leer.
- **Posición de billetera**: elija la red (BTC, ETH, TON, TRX) y pegue una dirección pública. Al actualizar, la aplicación lee el saldo nativo y, en ETH, TON y TRX, los tokens fungibles de la dirección.

Los importes admiten punto o coma decimal, sin separadores de miles. Cada bolsillo muestra las cantidades en su moneda original y su valor en su moneda predeterminada. Si falta una cotización, el total se marca como incompleto; un valor en caché desactualizado sigue siendo utilizable, con una advertencia.

**Tokens.** Un token se identifica por su dirección de contrato, nunca por su nombre. Solo se contabiliza si la fuente de precios elegida incluye exactamente ese contrato; todo lo demás aparece como *Token desconocido · No contabilizado* y queda fuera de los totales. Abra el editor de una posición de billetera para obtener sus tokens y desactivar los que no quiera.

**Modo cartera** (ajustes del bolsillo) trata un bolsillo como una cartera de inversión: fije un porcentaje objetivo por activo, compare la proporción real con el objetivo y use **Reequilibrar** para obtener una lista de lo que debe comprar con un importe dado. Solo se proponen ventas si *Permitir ventas al reequilibrar* está activado. Es una calculadora; no cambia nada.

## Metas {#goals}

Pestaña Metas → **+**. Una meta tiene un nombre, una moneda, un importe objetivo y una fecha de vencimiento. Abra la meta y pulse **Conectar bolsillo** para indicar qué bolsillos pueden financiarla, opcionalmente con un límite: un importe fijo, un porcentaje del bolsillo o un porcentaje de la meta.

Cómo se asigna el dinero:

- Las metas con una fecha de vencimiento más cercana se cubren primero. Las metas que comparten fecha se cubren en el orden mostrado; arrastre el asa para reordenarlas.
- Un bolsillo conectado a varias metas se reparte entre ellas según los límites y nunca se cuenta dos veces.
- El resultado se muestra como *cubierto / objetivo* y *Falta por cubrir*. En el Resumen verá el total, lo asignado a metas y lo que queda.

**Insignias.** *Cubierta* (verde) cuando los ahorros actuales ya cubren la meta. *Se cubrirá a tiempo* (verde) cuando los ahorros planificados la completan en su fecha de vencimiento o antes. *Sin cubrir* (amarillo) en los demás casos. El texto bajo la meta indica cuándo se completa o cuánto le falta.

**Archivar** una meta la conserva sin contabilizarla. Las metas archivadas aparecen al final de la lista.

## Planes {#plans}

Pestaña Planes → **+**. Un ahorro planificado es un importe que usted prevé añadir en una fecha, por ejemplo lo que ahorra de su sueldo a final de cada mes. Los planes no forman parte de sus ahorros; solo amplían la proyección: «los ahorros planificados completan esta meta el 30 de octubre de 2026 · a tiempo».

El dinero de los planes se aplica después de los bolsillos actuales, a las metas por orden de fecha de vencimiento, de modo que solo completa lo que aún falta. Cuando la fecha de un plan ya ha pasado, este pasa a la sección **Archivada** y deja de contabilizarse: o bien ya movió ese dinero a un bolsillo y la aplicación lo ve allí, o bien el plan no se cumplió. Cambie la fecha a una futura para volver a activarlo; elimínelo si ya no sirve.

## Actualizar

El icono de actualizar de la parte superior vuelve a cargar todos los saldos de billeteras y precios. Un bolsillo se puede actualizar por separado. La aplicación actualiza una vez al iniciarse desde cero; al volver desde segundo plano solo recarga los archivos locales. Actualizar requiere conexión a internet; sin ella se conservan los valores anteriores, marcados como desactualizados.

## Seguridad {#security}

Ajustes → Seguridad.

- **Cifrado** cifra con una contraseña todos los archivos de la carpeta, incluidas las revisiones anteriores. Al desactivarlo se descifran. **No existe recuperación de contraseña**: si pierde la contraseña, los datos no se podrán abrir. Las copias de seguridad sin cifrar hechas antes de activar el cifrado siguen siendo legibles; la aplicación le avisa de ellas, pero no puede eliminarlas.
- **PIN** y **biometría** están disponibles mientras el cifrado está activado. *Usar contraseña* siempre está disponible en la pantalla del PIN. Tras 10 PIN incorrectos, el PIN se elimina y solo funciona la contraseña. Los intentos incorrectos nunca borran datos.
- Mientras el cifrado está activado, se bloquean las capturas de pantalla y la vista previa de aplicaciones recientes.

## Sincronización, copia de seguridad, recuperación {#sync-backup-recovery}

Capital escribe en su carpeta archivos de instantáneas con identificadores de revisión y sumas de verificación, y nunca sincroniza por su cuenta. Ponga la carpeta bajo cualquier herramienta de sincronización que ya utilice. Si dos dispositivos editan a la vez, la aplicación muestra una pantalla de conflicto y le permite elegir una versión; ambos originales permanecen en el disco.

- **Exportar copia de seguridad** (Ajustes) escribe un único archivo portátil. **Restaurar** lo valida antes de cambiar nada.
- Si falla un guardado, sus cambios se conservan en memoria con *Reintentar guardado* y *Guardar copia en la carpeta*.
- Si se pierde el permiso de acceso a la carpeta, vuelva a conectar la misma carpeta.
- Una versión anterior de la aplicación rechaza los archivos escritos por una versión más reciente; actualice la aplicación.

## Idioma {#language}

La aplicación se inicia en el idioma del dispositivo si es uno de los 15 admitidos; si no, en inglés. Cámbielo en Ajustes → Idioma.

## Instalación fuera de Google Play

Descargue el APK de la [última versión]({{ site.repo }}/releases/latest) y ábralo; permita la instalación desde ese origen cuando Android lo pida. Todas las versiones están firmadas con la misma clave, así que las nuevas se instalan sobre las anteriores y conservan sus ajustes. Ni una actualización ni una desinstalación tocan nunca la carpeta con sus datos.
