---
layout: default
lang: es
base: "/es"
key: "financial-features"
title: Declaración de funciones financieras
class: doc
---
# Declaración de funciones financieras

<p class="meta">Respuestas para el formulario de Google Play Console (Política y programas → Contenido de la aplicación → Funciones financieras), con su justificación. Revisado con la versión 2.2.1 el 30 de septiembre de 2026.</p>

## Respuesta del formulario

**Select all of the financial features the app provides** (Selecciona todas las funciones financieras que ofrece la aplicación)**:** **The app does not provide any financial features** (La aplicación no ofrece ninguna función financiera)**.**

## Por qué

Capital es un registro personal de ahorros. Anota lo que el usuario ya posee y muestra cómo esos ahorros se reparten entre las metas del propio usuario. Frente a cada función del formulario:

| Función del formulario | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later (prestamista directo de préstamos personales, intermediario de préstamos, préstamos de día de pago, línea de crédito, anticipos de salario devengado, microfinanzas, compra ahora y paga después) | Ningún tipo de préstamo |
| Banking (banca) | Sin cuentas, depósitos ni acceso a cuentas. El usuario escribe a mano los saldos bancarios. Los valores de las cuentas de valores y de divisas se leen a través de la propia interfaz de informes del bróker con un token que crea el usuario; la aplicación no puede dar órdenes, transferir ni retirar fondos |
| Mobile payments and digital wallets, money transfer and wire services (pagos móviles y billeteras digitales, servicios de transferencia y envío de dinero) | No puede enviar, recibir ni custodiar dinero. La asignación a metas es un cálculo que se muestra en pantalla; no mueve nada |
| Cryptocurrency wallet (billetera de criptomonedas) | Lee el saldo de las direcciones públicas que pega el usuario. Nunca custodia claves privadas ni frases semilla y no puede firmar ni difundir transacciones, así que no es una billetera |
| Cryptocurrency exchange (exchange de criptomonedas) | Sin negociación, sin enrutamiento de órdenes, sin rampa de entrada fiat |
| Rewards and incentives, crowdfunding and chit funds, prediction markets (recompensas e incentivos, micromecenazgo y fondos rotativos, mercados de predicción) | Ninguna |
| Credit monitoring and reporting (supervisión e informes de crédito) | Ninguna |
| Financial advice (asesoramiento financiero) | Ninguno. La proyección presenta operaciones aritméticas sobre los números del propio usuario («los ahorros planificados completan esta meta el …»); no recomienda ningún producto, activo ni acción. La calculadora de reequilibrio enumera las compras necesarias para ajustarse a los porcentajes que el propio usuario fijó |
| Insurance (seguros) | Ninguno |
| In-app purchases, donations (compras en la aplicación, donaciones) | La aplicación no procesa ninguna. Una pantalla de propinas muestra las direcciones públicas de billetera del desarrollador (las mismas que en este sitio); la transferencia se hace en la aplicación de billetera del propio usuario, no desbloquea nada y la aplicación no la ve |

La aplicación tampoco ofrece compras dentro de la aplicación ni funciones de pago.

## Si el revisor no está de acuerdo

Si la revisión de Play clasifica de todos modos la aplicación como proveedora de una función financiera, la opción más cercana es **Other** (Otra) con esta descripción:

> Registro personal de ahorros de solo lectura. Los usuarios introducen sus saldos, pegan direcciones públicas de blockchain o conectan una cuenta de valores con un token de informes; la aplicación obtiene los saldos, los valores de las cuentas y los precios de mercado de fuentes de datos externas y muestra cómo los ahorros cubren las metas del propio usuario. Sin custodia, sin claves, sin transacciones, sin préstamos, sin negociación, sin asesoramiento.

Los requisitos específicos por país para aplicaciones de préstamos personales y las preguntas sobre criptomonedas de Estados Unidos no se aplican, porque no se ha seleccionado ninguna de esas funciones.

## Otros datos que un revisor puede preguntar

- Los datos de mercado proceden de operadores externos elegidos por el usuario (consulte la [Política de privacidad]({{ page.base }}/privacy)). La aplicación muestra en Ajustes el nombre y el sitio web de cada operador.
- Las consultas de billeteras usan API públicas de blockchain de solo lectura.
- Las cuentas de bróker (Interactive Brokers, OANDA, Trading 212, SnapTrade, Alpaca, Tradier, tastytrade, Public.com, eToro, Indexa Capital, T-Invest, ALOR, Capital.com, Akahu) se leen con un token o una clave que el usuario crea en el propio portal del bróker; la aplicación llama solo a puntos de acceso de informes y no puede dar órdenes ni mover dinero. La configuración está documentada en [Cuentas de bróker y de divisas]({{ page.base }}/accounts).
- La aplicación funciona íntegramente en el dispositivo y no tiene ningún servidor gestionado por el desarrollador.
