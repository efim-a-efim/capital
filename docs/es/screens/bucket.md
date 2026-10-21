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
- **Cuenta de bróker**: elija una de las cuentas añadidas en la pantalla [Brókeres]({{ page.base }}/screens/brokers); deje el nombre en blanco para usar el de la cuenta. Al actualizar, la aplicación lee el valor total de la cuenta en la moneda base de la cuenta. Una cuenta puede estar vinculada a un solo bolsillo; al eliminar la posición se desvincula sin eliminar la cuenta.

**Tokens y «no contabilizado».** Un token se identifica por su dirección de contrato. Solo se contabiliza si su fuente de precios incluye exactamente ese contrato; si no, aparece como *Token desconocido · No contabilizado* y queda fuera de los totales. Así es como un falso «USDT» recibido por airdrop se queda fuera de sus ahorros.

**PnL de inversión.** Actívelo en **Editar bolsillo** (casilla **PnL de inversión**). A partir de entonces la aplicación registra, en cada actualización, la cantidad de cada posición y los depósitos y retiros de sus cuentas de bróker, y la pantalla añade una sección **Inversión**:

- Cinco filas, **Todo el tiempo**, **Último año**, **Último mes**, **Última semana** y **Último día**, cada una con la ganancia o pérdida en la moneda del bolsillo y la rentabilidad ponderada por el tiempo en porcentaje, en verde si es positiva y en rojo si es negativa. Una fila muestra en su lugar *desde <fecha>* cuando el historial empieza después de que el periodo comenzara.
- Un gráfico de **Valor** del bolsillo y un gráfico de **Ganancias y pérdidas** del resultado acumulado, ambos en la moneda del bolsillo. Los chips **Todo**, **1Y**, **1M**, **1W** y **1D** centran ambos gráficos en ese marco temporal; la curva de PnL vuelve a cero al inicio del marco.
- *Desde <fecha> · en <moneda>* indica dónde empieza el historial. *Faltan tasas en algunos días; esos valores usan la última conocida* aparece cuando el historial de tasas tiene huecos.

**Cómo se calcula.** Ganancia o pérdida = variación del valor menos depósitos más retiros. El porcentaje está ponderado por el tiempo: se encadena la rentabilidad de cada día, de modo que el momento de sus depósitos no lo altera. Los depósitos y retiros provienen del bróker en las cuentas vinculadas ([qué brókeres]({{ page.base }}/accounts#pnl)); en las posiciones manuales y las billeteras, un cambio de cantidad cuenta como depósito o retiro a la tasa de ese día, nunca como ganancia. Los valores pasados usan el historial diario de tasas (**Ajustes → Historial de tasas**). Al activar el interruptor se crea el historial a partir de las instantáneas que ya están en su carpeta y de los brókeres (*Creando el historial de PnL…*, y después *Historial de PnL listo: N días*); hasta la primera actualización la sección indica *Aún no hay historial*.

**Modo cartera.** Cuando está activado, la pantalla añade una tabla con el valor, la proporción real, el objetivo y la diferencia de cada activo, además de un botón **Reequilibrar** que pide un importe y enumera qué comprar. Las ventas solo aparecen si *Permitir ventas al reequilibrar* está activado. No se realiza ninguna operación.
