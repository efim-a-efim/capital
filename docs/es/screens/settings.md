---
layout: screen
lang: es
base: "/es"
key: "screens/settings"
screen: settings
title: Ajustes
---
# Ajustes

**Qué es.** Todo lo que no es un registro: preferencias, fuentes de datos, seguridad y la carpeta. Se abre con el engranaje de la barra superior; en pantallas anchas es una pestaña del panel lateral.

**Preferencias.** Moneda predeterminada y tema (sistema, claro, oscuro). La moneda predeterminada se usa para valorar el Resumen y se propone para los bolsillos y metas nuevos; los registros existentes conservan su moneda. **Idioma** cambia la interfaz de inmediato; *Predeterminado del sistema* sigue al dispositivo.

**Proveedores de datos gratuitos.** Una fila por tipo de datos, cada una con el operador en uso: saldos BTC, ETH, TON y TRX; listas de tokens ETH, TON y TRX; precios de criptomonedas; tipos de cambio fiat. Toque una fila para elegir otro operador, poner en **Desactivado** las consultas de tokens de una red o introducir una clave API opcional. Las claves se guardan cifradas en el dispositivo y solo se envían al operador que las emitió. **Probar fuentes / actualizar cartera** consulta a cada operador con los activos que realmente posee e informa de lo que ha fallado. Nunca se sustituye un operador por otro sin avisar.

**Brókeres.** Las cuentas de bróker y sus credenciales tienen su propia pantalla: [Brókeres]({{ page.base }}/screens/brokers).

**Cotizaciones y vigencia.** Cada cotización en caché con la hora en que se observó y se obtuvo. Las cotizaciones desactualizadas siguen siendo utilizables y se marcan en el Resumen.

**Seguridad.** **Cifrado** cifra con una contraseña todos los archivos de la carpeta; al desactivarlo se descifran. Con el cifrado activado puede usar **Configurar PIN**, activar **Usar biometría**, elegir **Bloquear tras un tiempo en segundo plano** y **Cambiar contraseña**. No existe recuperación de contraseña. Diez PIN incorrectos eliminan el PIN; la contraseña siempre funciona. Consulte [Seguridad]({{ page.base }}/manual#security).

**Almacenamiento.** La carpeta actual y su revisión. **Reconectar / abrir carpeta** vuelve a abrir el selector de carpetas; **Recargar archivos locales** vuelve a leer la carpeta, por ejemplo después de que su herramienta de sincronización haya traído cambios; **Exportar copia de seguridad** escribe un único archivo portátil (sin cifrar cuando el cifrado está desactivado, y marcado como tal); **Restaurar copia de seguridad** valida un archivo antes de reemplazar los registros y conserva las instantáneas existentes.

**Fuentes / atribución.** Enlaces al sitio web de cada operador.

**Legal.** Enlaces a la [Política de privacidad]({{ page.base }}/privacy), a la declaración de [Seguridad de los datos]({{ page.base }}/data-safety) y a la de [Funciones financieras]({{ page.base }}/financial-features) de este sitio, en el idioma de la interfaz. La versión de la aplicación y el número de compilación están al final.
