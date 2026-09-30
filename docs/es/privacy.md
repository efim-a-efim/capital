---
layout: default
lang: es
base: "/es"
key: "privacy"
title: Política de privacidad
class: doc
---
# Política de privacidad

<p class="meta">Capital para Android (paquete <code>dev.capital</code>) · Desarrollador: {{ site.developer }} · Vigente desde el 30 de septiembre de 2026</p>

## Resumen

- Capital no tiene cuentas de usuario, analíticas, publicidad, informes de errores ni servidores gestionados por el desarrollador. El desarrollador nunca recibe sus datos.
- Sus registros financieros se guardan únicamente en su dispositivo, en una carpeta que usted elige. Puede cifrarlos con una contraseña.
- El único tráfico de red son las solicitudes que la aplicación envía, siguiendo sus instrucciones, a los operadores de datos de precios y de blockchain que usted elige en Ajustes. Esas solicitudes incluyen las direcciones públicas de billeteras, los contratos de tokens y los códigos de moneda que usted sigue, y cualquier clave API que haya introducido para ese operador.

## Qué guarda la aplicación en su dispositivo

**En la carpeta que usted elige.** Bolsillos, posiciones, direcciones de billeteras, metas, conexiones, ahorros planificados, precios en caché y los ajustes que pertenecen a esos datos. Los archivos son texto sin cifrar salvo que active el cifrado (Ajustes → Seguridad). Con el cifrado activado, cada archivo se cifra con AES-256-GCM usando una clave derivada de su contraseña con Argon2id. No existe recuperación de contraseña.

**En el almacenamiento privado de la aplicación** (inaccesible para otras aplicaciones):

| Elemento | Finalidad |
|---|---|
| Permiso de acceso a la carpeta elegida | Volver a abrir la carpeta en el siguiente inicio |
| Claves API de proveedores que usted introdujo | Se envían solo al operador que las emitió; cifradas con una clave guardada en Android Keystore; excluidas de las instantáneas, las exportaciones y las copias de seguridad del sistema |
| Ajustes de bloqueo | Desbloquear la carpeta cifrada sin la contraseña: una copia de la clave de datos, cifrada con una clave derivada de su PIN y vinculada a Android Keystore. El PIN en sí no se guarda |
| Idioma y tema elegidos | Preferencias de la interfaz |

La copia de seguridad de Android y la transferencia entre dispositivos están desactivadas para la aplicación, por lo que el sistema no copia nada de esto a Google ni a otro dispositivo.

## Qué sale de su dispositivo

Capital se comunica solo con los operadores que usted elige en Ajustes, solo mediante HTTPS y solo cuando usted actualiza o prueba las fuentes. Cada solicitud se responde y se descarta; la aplicación guarda en su carpeta los saldos y precios recibidos, no la solicitud.

| Datos enviados | A quién | Para qué |
|---|---|---|
| Direcciones públicas de billeteras que usted añadió | El operador de datos de blockchain elegido para esa red | Leer el saldo y los tokens de la dirección |
| Direcciones de contratos de tokens e identificadores de activos | El operador de precios de criptomonedas que usted eligió | Obtener el precio de los activos |
| Códigos de moneda | El operador de tipos de cambio fiat que usted eligió | Convertir entre monedas |
| La clave API que usted introdujo para un operador | Solo ese operador | Autenticar su propia cuenta en ese operador |

Como en cualquier solicitud por internet, cada operador ve además su dirección IP. Los operadores son independientes del desarrollador y tratan la solicitud según sus propias condiciones y políticas de privacidad, enlazadas en la aplicación desde Ajustes → Fuentes / atribución:

| Datos | Operadores |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum y tokens ERC-20 | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON y jettons | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON y tokens TRC-20 | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Precios de criptomonedas | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Tipos de cambio fiat | [Frankfurter](https://frankfurter.dev), [Banco Central Europeo](https://www.ecb.europa.eu) |

No se envía nada a ningún otro sitio. Ningún dato se vende, se comparte con fines publicitarios ni se usa para crear perfiles. Las consultas a blockchains públicas revelan que alguien con su dirección IP tiene interés en la dirección que usted sigue; use una VPN si eso le importa.

## Qué no hace nunca la aplicación

- Nunca pide, guarda ni transmite claves privadas ni frases semilla. No puede firmar ni enviar transacciones.
- Nunca transfiere dinero. Las asignaciones a metas son cálculos que se le muestran a usted y nada más.
- Nunca se comunica con el desarrollador. No hay telemetría, ni comprobación de actualizaciones dentro de la aplicación, ni notificaciones push.

## Permisos

| Permiso | Uso |
|---|---|
| Internet | Solicitudes a los operadores indicados arriba |
| Acceso a carpetas | Lo concede usted mediante el selector de carpetas de Android para la carpeta que elige; la aplicación no puede leer otras carpetas |
| Biometría | Desbloqueo con huella dactilar o rostro mediante el diálogo propio de Android; la aplicación solo recibe el resultado (éxito o fallo), nunca datos biométricos |

## Sincronización y copias de seguridad

Capital no sincroniza nada por sí misma. Si pone la carpeta bajo una herramienta de sincronización (Syncthing, Nextcloud, Google Drive, …), las condiciones de privacidad de esa herramienta se aplican a las copias que haga. Los archivos son texto sin cifrar salvo que el cifrado esté activado; las copias sin cifrar hechas antes de activar el cifrado siguen siendo legibles para quien las tenga.

**Exportar copia de seguridad** en Ajustes escribe un único archivo en la ubicación que usted elija. Contiene los mismos registros y solo está tan protegido como esa ubicación.

## Eliminar sus datos

Elimine la carpeta que eligió (y las copias que haya hecho su herramienta de sincronización) y desinstale la aplicación. La desinstalación borra el almacenamiento privado de la aplicación, incluidas las claves de proveedores y los ajustes de bloqueo. El desarrollador no tiene nada que eliminar y no puede eliminar nada en su nombre. Los operadores a los que usted consultó pueden conservar registros de las solicitudes según sus propias normas de conservación.

## Menores

Capital es una herramienta de finanzas personales para adultos. No está dirigida a menores de 13 años y no recopila conscientemente datos de ellos.

## Cambios en esta política

La versión vigente está siempre en [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Los cambios importantes se indican en las notas de la versión que los introduce.

## Contacto

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Registro de incidencias]({{ site.repo }}/issues)
