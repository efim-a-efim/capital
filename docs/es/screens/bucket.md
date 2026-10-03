---
layout: screen
lang: es
base: "/es"
key: "screens/bucket"
screen: bucket
title: Bolsillo
---
# Bolsillo

**Qué es.** Un bolsillo con sus posiciones. Se llega tocando una tarjeta en la pestaña [Bolsillos]({{ page.base }}/screens/buckets); **← Todos los bolsillos** vuelve atrás.

**Encabezado.** El valor del bolsillo y, después, dos cifras que solo tienen sentido juntas: **Asignado**, la parte que reclaman las metas conectadas, y **Disponible**, el resto. **Editar bolsillo** abre el nombre, la moneda y los interruptores de cartera. **Eliminar bolsillo** borra el bolsillo y sus posiciones tras una confirmación.

**Posiciones.** Cada posición muestra su nombre, su valor en la moneda del bolsillo, cómo se sigue (*Manual*, *Billetera* o *Cuenta de bróker*), la cantidad en su moneda original, cuándo se observó el valor y cuándo se obtuvo por última vez. **Editar / mover** la modifica o la mueve a otro bolsillo; **Eliminar** la borra.

**Añadir posición** abre el editor de posiciones:

- **Manual**: un nombre, un código de moneda o de activo y la cantidad. Úselo para todo lo que la aplicación no puede leer.
- **Billetera**: elija la red (BTC, ETH, TON, TRX) y pegue una dirección pública. Al actualizar, la aplicación lee el saldo nativo y, en ETH, TON y TRX, los tokens fungibles de esa dirección. Vuelva a abrir el editor y pulse **Obtener tokens** para verlos y desactivar los que no quiera que se contabilicen.
- **Cuenta de bróker**: elija el bróker (Interactive Brokers, OANDA, Trading 212, SnapTrade) e introduzca el Flex Query id o el ID de cuenta; para SnapTrade, **Obtener cuentas** enumera las cuentas conectadas para elegir una. Al actualizar, la aplicación lee el valor total de la cuenta en la moneda base de la cuenta; el token de acceso se introduce en Ajustes → Cuentas de bróker. **Guía de configuración de cuentas de bróker** abre [Cuentas de bróker y de divisas]({{ page.base }}/accounts), que enumera los pasos de cada bróker.

**Tokens y «no contabilizado».** Un token se identifica por su dirección de contrato. Solo se contabiliza si su fuente de precios incluye exactamente ese contrato; si no, aparece como *Token desconocido · No contabilizado* y queda fuera de los totales. Así es como un falso «USDT» recibido por airdrop se queda fuera de sus ahorros.

**Modo cartera.** Cuando está activado, la pantalla añade una tabla con el valor, la proporción real, el objetivo y la diferencia de cada activo, además de un botón **Reequilibrar** que pide un importe y enumera qué comprar. Las ventas solo aparecen si *Permitir ventas al reequilibrar* está activado. No se realiza ninguna operación.
