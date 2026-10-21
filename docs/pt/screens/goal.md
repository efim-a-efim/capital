---
layout: screen
lang: pt
base: "/pt"
key: "screens/goal"
screen: goal
title: Meta
---
# Meta

**O que é.** Uma meta com seus recursos. Acessada tocando em um cartão na aba [Metas]({{ page.base }}/screens/goals); **← Todas as metas** volta.

**Cabeçalho.** Nome e selo, coberto / valor-alvo, data de vencimento, a linha de projeção e **Ainda falta**: o valor-alvo menos o que está coberto hoje.

**Botões.** **Editar meta** altera nome, moeda, valor-alvo e data de vencimento. **Arquivar** mantém a meta sem contabilizá-la; uma meta arquivada mostra **Ativar** no lugar. **Excluir** remove a meta e seus vínculos após uma confirmação.

## FIRE {#fire}

Uma meta do tipo **FIRE** (independência financeira, aposentadoria antecipada) responde a duas perguntas: quando o dinheiro pode começar a pagar o seu custo de vida e por quanto tempo ele dura. É uma projeção com os seus próprios números; não recomenda nada. Defina o tipo em **Tipo de meta** (**Meta de economia** / **FIRE**) no editor.

**Campos do editor.** Todos os campos, exceto nome e moeda, podem ficar vazios; o app calcula o que for possível a partir dos demais.

- **Data de nascimento · YYYY-MM-DD · opcional**: mostra as idades e define o horizonte pela expectativa de vida.
- **Data FIRE · YYYY-MM-DD · vazio para calcular**: o dia em que você deixa de poupar e começa a gastar.
- **Posso investir · vazio para calcular**: um valor por **semana**, **2 semanas**, **mês**, **ano** ou **N dias** (nesse caso, informe **Dias**).
- **Crescimento do aporte % por ano · vazio = inflação**: quanto a mais você pode investir a cada ano, por exemplo um aumento salarial anual. Vazio mantém o poder de compra do aporte.
- **Gasto por mês após a FIRE · vazio para calcular**.
- **Valor a ter após a FIRE · vazio para calcular**: o montante FIRE, o valor-alvo da meta.
- **Premissas avançadas**: **Inflação % · média / otimista / pessimista**, **Crescimento % · média / otimista / pessimista**, **Taxa de retirada %** e **Expectativa de vida · anos**. Cada rótulo termina com "· agora {valor}", o valor em vigor quando o campo está vazio.

Todos os valores estão em valores de hoje, na moeda da meta. A tela também mostra o valor na data FIRE, em dinheiro futuro. O dinheiro que a meta tem hoje vem das caixinhas vinculadas, como em qualquer meta; os [Planos]({{ page.base }}/screens/plans) não entram no cálculo FIRE.

**O que é calculado.**

- Data FIRE: a partir do valor-alvo e do aporte.
- Aporte: a partir do valor-alvo e da data FIRE.
- Valor-alvo: a partir do gasto (gasto × 12 ÷ taxa de retirada), ou a partir da data FIRE e do aporte.
- Gasto: a partir do valor-alvo (valor-alvo × taxa de retirada ÷ 12).

Se a data FIRE, o aporte e o valor-alvo forem todos informados, a data é mantida e a tela mostra o que falta e a data mais próxima que é alcançável. O valor-alvo e a data calculados são gravados no valor-alvo e na data de vencimento da meta; assim, a ordem, a alocação, a Visão geral e os Planos funcionam como em qualquer meta. Eles são atualizados a cada edição e a cada atualização.

**A seção FIRE.** Primeiro vêm os valores iguais em todos os cenários (o que você informou e a **Taxa de retirada**), um por linha. Depois, uma linha expansível por cenário, **Média**, **Otimista** e **Pessimista**, com suas taxas de crescimento e de inflação e um selo: **Dinheiro suficiente** (verde) quando o dinheiro dura além da expectativa de vida, **o dinheiro nunca acaba 🥳** (verde) quando ele nunca se esgota, **Dinheiro insuficiente** (amarelo) quando a data FIRE é inalcançável ou o dinheiro acaba antes. Toque na linha para expandir os valores que mudam conforme o cenário:

| Linha | Significado |
|---|---|
| **Data FIRE** | A data, com "N anos" quando a data de nascimento é conhecida, ou *Inalcançável antes de <data>* |
| **Aporte por mês** | O que precisa ser investido a cada mês |
| **Montante FIRE · valores de hoje** | O valor a ter, como o editor o informa |
| **Montante FIRE · na data FIRE** | O mesmo valor depois da inflação |
| **Gasto por mês · valores de hoje** | O gasto mensal após a FIRE |
| **Gasto por mês · na data FIRE** | O mesmo gasto depois da inflação |
| **O dinheiro dura** | *até <data> · N anos*, *N anos · além da expectativa de vida*, *o dinheiro nunca acaba 🥳* ou *faltam …* quando a data não é alcançável |

Abaixo das linhas expansíveis, uma linha "Taxas: …" indica o provedor de onde vêm as taxas, ou *padrões do app*. O gráfico abaixo mostra o dinheiro diminuindo: o valor do dinheiro em valores de hoje, de agora até o fim do dinheiro ou do horizonte, com três curvas coloridas, uma por cenário. A linha tracejada marca a data FIRE. A nota abaixo diz *Uma projeção com os seus próprios números; não recomenda nada.* Na aba [Metas]({{ page.base }}/screens/goals), a meta mostra *FIRE em <data> · N anos*, ou *inalcançável*.

**Como é calculado.** Em passos mensais. Até a data FIRE, o aporte cresce conforme **Crescimento do aporte % por ano** (com a inflação quando vazio, mantendo assim o poder de compra), e o dinheiro cresce à taxa do cenário. Depois da data FIRE, o gasto cresce com a inflação e o que sobra continua crescendo à taxa do cenário. O horizonte é a data de nascimento mais a expectativa de vida (95 por padrão), ou 50 anos após a data FIRE sem data de nascimento. *O dinheiro nunca acaba* significa que o dinheiro não se esgota em 200 anos e que, no fim, vale em valores de hoje pelo menos tanto quanto na data FIRE.

**Padrões e fontes.** Sem valor próprio nem taxas obtidas, o app usa crescimento de 7 % (otimista 10 %, pessimista 4 %), inflação de 3 % (2 %, 5 %) e taxa de retirada de 4 %. Eles vêm do UBS Global Investment Returns Yearbook 2026 (mundo, 1900–2024: ações 9,8 % ao ano nominal, títulos 4,6 %, inflação 2,9 %; 7 % é cerca de uma carteira 60/40), das metas de inflação de 2 % dos bancos centrais e de Bengen (1994) e do estudo Trinity (1998) para a regra dos 4 %. Veja também [FIRE]({{ page.base }}/manual#fire) no manual.

**Taxas ao vivo.** **Atualizar** obtém a inflação e o crescimento do mercado para a moeda de cada meta FIRE, a partir dos provedores escolhidos em [Ajustes]({{ page.base }}/screens/settings). A ordem é: o valor próprio em **Premissas avançadas**, depois o valor obtido, depois o padrão. Se a obtenção falhar, a série anterior é mantida e o erro é exibido. As duas fontes de crescimento são índices de preços sem dividendos, então subestimam o retorno total.

**Mensagens.**

| Mensagem | O que fazer |
|---|---|
| *Informe um valor-alvo, um gasto mensal, ou a data FIRE e um aporte* | Preencha um dos três; sem eles nada pode ser calculado. |
| *Informe uma data FIRE ou um aporte* | O valor-alvo é conhecido, mas a data e o aporte não; informe um dos dois. |
| *A data FIRE está no passado* | Escolha uma data futura. |
| *A data de nascimento está no futuro* | Corrija a data de nascimento. |
| *A taxa de retirada deve ser maior que zero* | Informe uma taxa de retirada acima de 0 em **Premissas avançadas**, ou apague o campo para usar o padrão. |
| *Inalcançável antes de <data>* | Com este aporte e estas taxas, o valor-alvo não é atingido antes dessa data (o fim do horizonte); aumente o aporte, reduza o valor-alvo ou o gasto, ou escolha outro cenário. |
| *Sem dados de taxa para <moeda>* | O provedor de inflação ou de crescimento não tem série para esta moeda; escolha outro provedor ou informe as taxas em **Premissas avançadas**. |
| *O ECB não cobre <moeda>* | O ECB fornece apenas a inflação da área do euro; escolha outro provedor de **Inflação** em Ajustes. |
| *A OECD não cobre <moeda>* | A OECD não tem série para esta moeda (por exemplo, RUB); escolha outro provedor de **Inflação** em Ajustes. |

**Fontes de recursos.** As caixinhas que podem cobrir esta meta. **Vincular caixinha** adiciona uma com um limite de contribuição:

- **Automático — até o valor que falta**: a caixinha fornece o que a meta ainda precisa, depois que as metas anteriores ficaram com a parte delas.
- **Valor fixo na moeda da meta**.
- **% da caixinha**: no máximo essa parcela do valor da caixinha.
- **% da meta**: no máximo essa parcela do valor-alvo.

A prévia no editor mostra quanto o vínculo contribuiria hoje. Os limites são tetos: a ordem das metas, as economias disponíveis e outros vínculos podem reduzir a contribuição. Cada fonte listada mostra quanto contribui agora e por que não contribui mais; **Editar vínculo** altera o limite, **Desvincular** o remove.

**Economias planejadas.** Os planos que chegam a esta meta, cada um com o valor que a projeção atribui a ela. Um plano que é totalmente consumido por metas anteriores não aparece aqui.

**Como ler a projeção.** "As economias planejadas concluem esta meta em 20 de dez. de 2026 · no prazo" significa que os planos acumulados até essa data cobrem o que falta antes do vencimento. "Cobrem até … · faltam …" significa que não cobrem; adicione um plano, mude a data ou reduza o valor-alvo.
