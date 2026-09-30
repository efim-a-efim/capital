---
layout: screen
lang: es
base: "/es"
key: "screens/goal"
screen: goal
title: Meta
---
# Meta

**Qué es.** Una meta con su financiación. Se llega tocando una tarjeta en la pestaña [Metas]({{ page.base }}/screens/goals); **← Todas las metas** vuelve atrás.

**Encabezado.** Nombre e insignia, cubierto / objetivo, fecha de vencimiento, la línea de proyección y **Falta por cubrir**: el objetivo menos lo cubierto hoy.

**Botones.** **Editar meta** cambia el nombre, la moneda, el objetivo y la fecha de vencimiento. **Archivar** conserva la meta sin contabilizarla; una meta archivada muestra **Activar** en su lugar. **Eliminar** borra la meta y sus conexiones tras una confirmación.

**Fuentes de financiación.** Los bolsillos que pueden financiar esta meta. **Conectar bolsillo** añade uno con un límite de contribución:

- **Automático: hasta lo que falte**: el bolsillo aporta todo lo que la meta aún necesita, después de que las metas anteriores hayan tomado su parte.
- **Importe fijo en la moneda de la meta**.
- **% del bolsillo**: como máximo esa proporción del valor del bolsillo.
- **% de la meta**: como máximo esa proporción del objetivo.

La vista previa del editor muestra lo que la conexión aportaría hoy. Los límites son topes: el orden de las metas, los ahorros disponibles y otras conexiones pueden reducir la contribución. Cada fuente de la lista muestra lo que aporta ahora y por qué no aporta más; **Editar conexión** cambia el límite, **Desconectar** la elimina.

**Ahorros planificados.** Los planes que llegan a esta meta, cada uno con el importe que la proyección le asigna. Un plan que agotan por completo las metas anteriores no aparece aquí.

**Cómo leer la proyección.** «Los ahorros planificados completan esta meta el 20 de diciembre de 2026 · a tiempo» significa que los planes acumulados hasta esa fecha cubren lo que falta antes de la fecha de vencimiento. «Los ahorros planificados cubren hasta … · faltan …» significa que no lo cubren; añada un plan, cambie la fecha o reduzca el objetivo.
