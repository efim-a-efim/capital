---
layout: screen
lang: es
base: "/es"
key: "screens/goals"
screen: goals
title: Metas
---
# Metas

**Qué es.** Todo aquello para lo que está ahorrando, agrupado por fecha de vencimiento, en el orden en que se asigna el dinero.

**Qué muestra cada tarjeta.**

- El nombre y una insignia: **Cubierta** (verde) cuando los ahorros actuales cubren el objetivo, **Se cubrirá a tiempo** (verde) cuando los ahorros planificados completan la meta antes de su fecha de vencimiento, **Sin cubrir** (amarillo) en los demás casos.
- **Cubierto / objetivo** en la moneda de la meta, y la fecha de vencimiento.
- Una barra de progreso. La parte sólida es lo cubierto hoy; la parte clara es lo que añadirán los planes más adelante.
- Una línea bajo la barra: *Ya cubierta*, o la fecha en que los ahorros planificados completan la meta, o cuánto falta todavía y para cuándo.

**Orden y prioridad.** Las metas con una fecha de vencimiento más cercana se cubren primero. Las metas que comparten fecha se cubren en el orden mostrado; arrastre el asa **≡** para cambiarlo. Las metas archivadas quedan al final y no reciben nada.

**Añadir una meta.** Pulse **+**. Una meta tiene un nombre, una moneda, un importe objetivo y una fecha de vencimiento. La meta no recibe dinero hasta que la abra y conecte al menos un bolsillo; consulte [Meta]({{ page.base }}/screens/goal).

**Cómo funciona la asignación.** Los bolsillos se reparten entre las metas a las que están conectados, dentro de los límites de cada conexión, empezando por las metas más cercanas. Cuando un bolsillo está conectado a varias metas, la aplicación redistribuye el dinero entre ellas para que ninguna meta se quede corta mientras otra recibe de más. Los ahorros planificados solo se aplican después de todos los bolsillos reales, así que nunca sustituyen al dinero que ya tiene.
