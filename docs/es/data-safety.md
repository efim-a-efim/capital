---
layout: default
lang: es
base: "/es"
key: "data-safety"
title: Declaración de seguridad de los datos
class: doc
---
# Declaración de seguridad de los datos

<p class="meta">Respuestas para el formulario de Google Play Console (Política y programas → Contenido de la aplicación → Seguridad de los datos), con la justificación de cada una. Revisado con la versión 2.2.1 el 30 de septiembre de 2026. La <a href="{{ page.base }}/privacy">Política de privacidad</a> expone los mismos hechos de cara al usuario.</p>

## Cómo trata los datos la aplicación

Capital no tiene backend. Todo lo que introduce el usuario se queda en una carpeta del dispositivo. Los únicos datos que salen del dispositivo son los que la aplicación envía, siguiendo las instrucciones del usuario, a los operadores de datos externos que el usuario elige en Ajustes: direcciones públicas de billeteras, identificadores de contratos de tokens, códigos de moneda y cualquier clave API que el usuario haya introducido para ese operador. Los operadores responden a la solicitud; la aplicación guarda localmente los saldos y precios recibidos y no conserva ninguna copia de la solicitud. Ningún SDK de la aplicación envía datos por su cuenta: las dependencias son únicamente AndroidX, Kotlin, OkHttp, Bouncy Castle y ZXing (generación de códigos QR, sin conexión). La pantalla de propinas muestra direcciones fijas incluidas en la aplicación y no envía nada.

Google Play considera que los datos se *recogen* (collected) cuando se transmiten fuera del dispositivo, aunque no intervenga ningún servidor del desarrollador y el tratamiento sea efímero, así que la declaración no es «no recoge nada». Se trata de un único tipo de datos, efímero y opcional.

## Respuestas del formulario

### Resumen

| Pregunta | Respuesta |
|---|---|
| ¿Tu aplicación recoge o comparte alguno de los tipos de datos de usuario obligatorios? | **Yes** (Sí) |
| ¿Todos los datos de usuario que recoge tu aplicación se cifran en tránsito? | **Yes** (Sí) — solo HTTPS; el tráfico sin cifrar está desactivado en el manifiesto |
| ¿Ofreces a los usuarios una forma de solicitar que se eliminen sus datos? | **Yes** (Sí) — no se conserva nada una vez completada la solicitud, lo que cumple la regla de «eliminados en un plazo de 90 días desde su recogida» para la insignia. Los usuarios eliminan los datos del dispositivo borrando la carpeta y desinstalando la aplicación; consulte la Política de privacidad. |

### Tipos de datos

Seleccione exactamente un tipo.

| Categoría | Tipo de datos | Recogidos | Compartidos | Efímeros | Obligatorio u opcional | Finalidades |
|---|---|---|---|---|---|---|
| Financial info (Información financiera) | Other financial info (Otra información financiera) | Yes (Sí) | No | **Yes** (Sí) | **Optional** (Opcional) | App functionality (Funcionalidad de la aplicación) |

Qué abarca el tipo: las direcciones públicas de blockchain que sigue el usuario, los contratos de tokens encontrados en ellas y los códigos de moneda de las posiciones del usuario. Se transmiten al operador de datos que eligió el usuario para obtener saldos y precios, se mantienen en memoria durante la solicitud y se descartan.

Por qué **no se comparten**: la transferencia va directamente del dispositivo al operador que eligió el usuario, en una actualización iniciada por el usuario, después de que la aplicación le haya indicado en Ajustes qué operador se consultará y que la solicitud revela la dirección y la IP a ese operador. Es la excepción de «acción iniciada por el usuario en la que este espera razonablemente que los datos se compartan». El desarrollador no recibe nada y no tiene proveedores de servicios.

Por qué son **opcionales**: la aplicación se puede usar por completo solo con posiciones manuales. Las direcciones y las claves API se introducen de forma voluntaria.

Las claves API que introduce el usuario se envían solo al operador que las emitió. Son las credenciales del usuario para el propio servicio de ese operador y no se declaran como un tipo de datos de usuario aparte; si un revisor lo pregunta, descríbalas como se indica aquí.

### Tipos que **no** se recogen

Todas las demás categorías son «No»: ni ubicación, ni información personal, ni contactos, ni mensajes, ni fotos o vídeos, ni archivos y documentos, ni actividad en la aplicación, ni navegación web, ni información y rendimiento de la aplicación (ni registros de fallos, ni diagnósticos), ni identificadores del dispositivo u otros. Las direcciones IP llegan a los operadores como parte de cualquier solicitud HTTPS y la aplicación no las usa para ningún fin.

Los registros financieros del usuario (posiciones, metas, planes) se tratan solo en el dispositivo y quedan fuera del alcance del formulario.

### Prácticas de seguridad

| Elemento | Respuesta |
|---|---|
| Revisión de seguridad independiente (MASA) | No |
| Compromiso de cumplir la política de Familias | No (no es una aplicación para niños) |

## Declaraciones relacionadas en la página Contenido de la aplicación

| Declaración | Respuesta |
|---|---|
| URL de la política de privacidad | `{{ site.url }}/privacy` |
| Anuncios | No, la aplicación no contiene anuncios |
| Acceso a la aplicación | Todas las funciones están disponibles sin acceso especial. Sin inicio de sesión. Las claves API de proveedores son opcionales; cada proveedor tiene una opción predeterminada sin clave. |
| Clasificación de contenido (IARC) | Cuestionario de utilidades / productividad; sin violencia, contenido sexual, juegos de azar, sustancias controladas, interacción entre usuarios ni compartición de ubicación. Resultado esperado: Everyone (Todos) / PEGI 3. |
| Público objetivo y contenido | 18 años o más (herramienta de finanzas personales; no está pensada para niños) |
| Aplicación de noticias | No |
| Rastreo de contactos y estado de COVID-19 | No |
| Seguridad de los datos | Como se indica arriba |
| Aplicación gubernamental | No |
| Funciones financieras | Consulte la [Declaración de funciones financieras]({{ page.base }}/financial-features) |
| Aplicaciones de salud | Sin funciones de salud |

## Qué actualizar cuando cambie la aplicación

Vuelva a revisar esta página cuando una versión añada analíticas, informes de errores, cuentas, un servidor gestionado por el desarrollador, un SDK nuevo con acceso a la red o la posibilidad de compartir datos con otra aplicación en el dispositivo. Cualquiera de estos cambios modifica el formulario.
