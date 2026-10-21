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

## FIRE {#fire}

Una meta de tipo **FIRE** (independencia financiera, jubilación anticipada) responde a dos preguntas: cuándo puede el dinero empezar a pagar sus gastos de vida y cuánto dura. Es una proyección con sus propias cifras; no recomienda nada. El tipo se elige con **Tipo de meta** (**Meta de ahorro** / **FIRE**) en el editor.

**Campos del editor.** Todos los campos salvo el nombre y la moneda pueden quedar en blanco; la aplicación calcula lo que puede a partir del resto.

- **Fecha de nacimiento · YYYY-MM-DD · opcional**: muestra las edades y fija el horizonte según la esperanza de vida.
- **Fecha FIRE · YYYY-MM-DD · en blanco para calcular**: el día en que deja de ahorrar y empieza a gastar.
- **Puedo invertir · en blanco para calcular**: un importe **por** **semana**, **2 semanas**, **mes**, **año** o **N días** (en ese caso indique **Días**).
- **Crecimiento de la aportación % al año · en blanco = inflación**: cuánto más puede invertir cada año, por ejemplo con un aumento de sueldo anual. En blanco se mantiene el poder adquisitivo de la aportación.
- **Gasto al mes tras FIRE · en blanco para calcular**.
- **Importe necesario tras FIRE · en blanco para calcular**: el importe FIRE, el objetivo de la meta.
- **Supuestos avanzados**: **Inflación % · media / optimista / pesimista**, **Crecimiento % · medio / optimista / pesimista**, **Tasa de retiro %**, **Trabajar hasta los** (65 por defecto) y **Esperanza de vida · años**. Cada etiqueta termina en «· ahora {valor}», el valor vigente cuando el campo está en blanco.

Todos los importes están en dinero de hoy de la moneda de la meta. La pantalla muestra también el valor en la fecha FIRE en dinero futuro. El dinero que la meta tiene hoy procede de sus bolsillos conectados, como en cualquier meta; los [Planes]({{ page.base }}/screens/plans) no entran en el cálculo FIRE.

**Qué se calcula.**

- Fecha FIRE: el primer mes a partir del cual el gasto dura hasta la esperanza de vida (50 años sin fecha de nacimiento) y, si se indica un objetivo, este se alcanza; no más tarde de **Trabajar hasta los**, de lo contrario *Inalcanzable antes del* cumpleaños correspondiente. Sin objetivo, el importe FIRE pasa a ser el importe que necesita ese periodo de vida.
- Aportación: a partir del objetivo y la fecha FIRE.
- Objetivo: a partir del gasto (gasto × 12 ÷ tasa de retiro) cuando se indica la fecha FIRE, o a partir de la fecha FIRE y la aportación.
- Gasto: a partir del objetivo (objetivo × tasa de retiro ÷ 12).

Si se indican la fecha FIRE, la aportación y el objetivo, se mantiene la fecha y la pantalla muestra lo que falta y la fecha más próxima alcanzable. El objetivo y la fecha calculados se escriben en el objetivo y la fecha de vencimiento de la meta, de modo que el orden, la asignación, el Resumen y los Planes funcionan como con cualquier meta. Se actualizan en cada edición y en cada actualización.

**La sección FIRE.** Primero los valores que son iguales en todos los escenarios (lo que usted indicó y la **Tasa de retiro**), uno por línea. Después una fila plegable por escenario, **Medio**, **Optimista** y **Pesimista**, con sus tasas de crecimiento e inflación, la fecha FIRE calculada si la dejó en blanco, y una insignia: **Dinero suficiente** (verde) cuando el dinero dura más allá de la esperanza de vida, **el dinero nunca se acabará 🥳** (verde) cuando nunca se agota, **Dinero insuficiente** (amarillo) cuando la fecha FIRE es inalcanzable o el dinero se acaba antes. Toque la fila para desplegar los valores que difieren según el escenario:

| Fila | Significado |
|---|---|
| **Fecha FIRE** | La fecha, con «N años» cuando se conoce la fecha de nacimiento, o *Inalcanzable antes del <fecha>* |
| **Aportación al mes** | Lo que debe invertirse cada mes |
| **Importe FIRE · dinero de hoy** | El importe necesario, tal como lo indica el editor |
| **Importe FIRE · en la fecha FIRE** | El mismo importe tras la inflación |
| **Gasto al mes · dinero de hoy** | El gasto mensual tras FIRE |
| **Gasto al mes · en la fecha FIRE** | El mismo gasto tras la inflación |
| **El dinero dura** | *hasta el <fecha> · N años*, *N años · más allá de la esperanza de vida*, *el dinero nunca se acabará 🥳* o *faltan …* cuando la fecha no es alcanzable |

Bajo los pliegues, una línea «Tasas: …» indica el proveedor del que proceden las tasas, o *valores predeterminados integrados*. El gráfico de debajo muestra cómo se gasta el dinero: su valor en dinero de hoy desde hoy hasta que se acaba o hasta el horizonte, con tres curvas de colores, una por escenario. La línea discontinua marca la fecha FIRE. La nota de debajo dice *Una proyección con sus propias cifras; no recomienda nada.* En la pestaña [Metas]({{ page.base }}/screens/goals) la meta muestra *FIRE el <fecha> · N años* o *inalcanzable*.

**Cómo se calcula.** En pasos mensuales. Hasta la fecha FIRE la aportación crece según **Crecimiento de la aportación % al año · en blanco = inflación** (según la inflación si está en blanco, de modo que mantiene su poder adquisitivo), y el dinero crece a la tasa del escenario. Después de la fecha FIRE el gasto crece con la inflación y lo que queda sigue creciendo a la tasa del escenario. El horizonte es la fecha de nacimiento más la esperanza de vida (95 por defecto), o 50 años después de la fecha FIRE si no hay fecha de nacimiento. *El dinero nunca se acabará* significa que el dinero no se agota en 200 años y que al final vale, en dinero de hoy, al menos tanto como en la fecha FIRE.

**Valores predeterminados y fuentes.** Sin un valor propio ni tasas obtenidas, la aplicación usa un crecimiento del 7 % (optimista 10 %, pesimista 4 %), una inflación del 3 % (2 %, 5 %) y una tasa de retiro del 4 %. Proceden del UBS Global Investment Returns Yearbook 2026 (mundo, 1900–2024: renta variable 9,8 % anual nominal, bonos 4,6 %, inflación 2,9 %; el 7 % equivale aproximadamente a una mezcla 60/40), de los objetivos de inflación del 2 % de los bancos centrales y de Bengen (1994) y el estudio Trinity (1998) para la regla del 4 %. Véase también [FIRE]({{ page.base }}/manual#fire) en el manual.

**Tasas en vivo.** **Actualizar** obtiene la inflación y el crecimiento del mercado para la moneda de cada meta FIRE, de los proveedores elegidos en [Ajustes]({{ page.base }}/screens/settings). El orden es: su valor en **Supuestos avanzados**, después el valor obtenido y por último el predeterminado. Si una consulta falla, se conserva la serie anterior y se muestra el error. Las dos fuentes de crecimiento son índices de precios sin dividendos, por lo que subestiman el rendimiento total.

**Mensajes.**

| Mensaje | Qué hacer |
|---|---|
| *Indique un objetivo, un gasto mensual, o bien una fecha FIRE y una aportación* | Rellene uno de los tres; sin ellos no se puede calcular nada. |
| *Indique una fecha FIRE o una aportación* | Se conoce el objetivo pero ni la fecha ni la aportación; indique una de las dos. |
| *La fecha FIRE ya pasó* | Elija una fecha futura. |
| *La fecha de nacimiento es futura* | Corrija la fecha de nacimiento. |
| *La tasa de retiro debe ser mayor que cero* | Introduzca una tasa de retiro superior a 0 en **Supuestos avanzados**, o bórrela para usar la predeterminada. |
| *Inalcanzable antes del <fecha>* | Con esta aportación y estas tasas el objetivo no se alcanza antes de esa fecha (el final del horizonte); aumente la aportación, reduzca el objetivo o el gasto, o elija otro escenario. |
| *Sin datos de tasas para <moneda>* | El proveedor de inflación o de crecimiento no tiene serie para esta moneda; elija otro proveedor o introduzca las tasas en **Supuestos avanzados**. |
| *ECB no cubre <moneda>* | El ECB solo ofrece la inflación de la zona del euro; elija otro proveedor de **Inflación** en Ajustes. |
| *OECD no cubre <moneda>* | La OECD no tiene serie para esta moneda (por ejemplo RUB); elija otro proveedor de **Inflación** en Ajustes. |

**Fuentes de financiación.** Los bolsillos que pueden financiar esta meta. **Conectar bolsillo** añade uno con un límite de contribución:

- **Automático: hasta lo que falte**: el bolsillo aporta todo lo que la meta aún necesita, después de que las metas anteriores hayan tomado su parte.
- **Importe fijo en la moneda de la meta**.
- **% del bolsillo**: como máximo esa proporción del valor del bolsillo.
- **% de la meta**: como máximo esa proporción del objetivo.

La vista previa del editor muestra lo que la conexión aportaría hoy. Los límites son topes: el orden de las metas, los ahorros disponibles y otras conexiones pueden reducir la contribución. Cada fuente de la lista muestra lo que aporta ahora y por qué no aporta más; **Editar conexión** cambia el límite, **Desconectar** la elimina.

**Ahorros planificados.** Los planes que llegan a esta meta, cada uno con el importe que la proyección le asigna. Un plan que agotan por completo las metas anteriores no aparece aquí.

**Cómo leer la proyección.** «Los ahorros planificados completan esta meta el 20 de diciembre de 2026 · a tiempo» significa que los planes acumulados hasta esa fecha cubren lo que falta antes de la fecha de vencimiento. «Los ahorros planificados cubren hasta … · faltan …» significa que no lo cubren; añada un plan, cambie la fecha o reduzca el objetivo.
