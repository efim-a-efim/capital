---
layout: screen
lang: es
base: "/es"
key: "screens/brokers"
screen: brokers
title: Brókeres
---
# Brókeres

**Qué es.** Sus conexiones de solo lectura con cuentas de valores y de divisas. Una cuenta de bróker es el bróker, el ID de la cuenta o de la consulta y la credencial del bróker; al actualizar, la aplicación lee el valor total de la cuenta en su moneda base. Las cuentas viven en esta pantalla, no dentro de los bolsillos: un bolsillo solo se vincula a una, y los bolsillos siguen siendo el lugar donde se cuentan sus ahorros.

**Qué muestra cada fila.** El nombre de la cuenta, el bróker y el ID, el último valor leído en la moneda de la cuenta y en su moneda predeterminada, cuándo se observó y cuándo se obtuvo, y el bolsillo al que está vinculada: **Vinculada a …** abre ese bolsillo, *Sin vincular a un bolsillo* significa que todavía no lo cuenta ninguno. **Editar** cambia el nombre, el bróker o el ID; **Eliminar** quita la cuenta y, si estaba vinculada, la posición que la vinculaba.

**Añadir una cuenta.** Pulse **+**, introduzca un nombre, elija el bróker e introduzca el ID que usa ese bróker: el Flex Query id en Interactive Brokers, el ID de cuenta en OANDA, el número de cuenta en Trading 212. Para SnapTrade pulse **Conectar un bróker a través de SnapTrade**, vuelva, pulse **Obtener cuentas** y elija una. Guarde. La moneda y el valor aparecen tras la siguiente actualización.

**Ignorar saldos inferiores a.** Márquelo e introduzca un importe en su moneda predeterminada (1 por defecto) para mantener el polvo fuera de sus ahorros: cuando el valor de la cuenta, convertido con los tipos de cambio en caché, es inferior a ese importe, la posición vinculada cuenta como 0 y la fila dice *Cuenta como 0: inferior a …*. El valor real sigue visible en esta pantalla. Sin un tipo de cambio para la moneda de la cuenta no se ignora nada.

**Vincularla a un bolsillo.** Abra el bolsillo, pulse **Añadir posición**, ponga **Seguimiento** en **Cuenta de bróker** y elija la cuenta; deje el nombre en blanco para usar el de la cuenta. Una cuenta puede estar en un solo bolsillo a la vez. **Editar / mover** en la posición la pasa a otro bolsillo; al eliminar la posición se desvincula la cuenta sin eliminarla.

**Credenciales.** El token o la clave de cada bróker compatible (Interactive Brokers, OANDA, Trading 212, SnapTrade); un conjunto por bróker sirve para todas las cuentas de ese bróker. Se cifran con una clave guardada en Android Keystore, nunca se escriben en la carpeta de datos, quedan fuera de las exportaciones y de las copias de seguridad del sistema, y se envían solo al bróker que las emitió. **Guía de configuración de cuentas de bróker** abre [Cuentas de bróker y de divisas]({{ page.base }}/accounts), que enumera los pasos de cada bróker.

**Actualizar.** El icono de actualizar de esta pantalla lee todas las cuentas; el de un bolsillo lee solo las cuentas vinculadas a ese bolsillo. Una cuenta que no se puede leer conserva su último valor y muestra el mensaje del bróker bajo su fila. La aplicación solo lee: nunca da órdenes ni mueve dinero.
