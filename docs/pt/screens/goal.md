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

**Fontes de recursos.** As caixinhas que podem cobrir esta meta. **Vincular caixinha** adiciona uma com um limite de contribuição:

- **Automático — até o valor que falta**: a caixinha fornece o que a meta ainda precisa, depois que as metas anteriores ficaram com a parte delas.
- **Valor fixo na moeda da meta**.
- **% da caixinha**: no máximo essa parcela do valor da caixinha.
- **% da meta**: no máximo essa parcela do valor-alvo.

A prévia no editor mostra quanto o vínculo contribuiria hoje. Os limites são tetos: a ordem das metas, as economias disponíveis e outros vínculos podem reduzir a contribuição. Cada fonte listada mostra quanto contribui agora e por que não contribui mais; **Editar vínculo** altera o limite, **Desvincular** o remove.

**Economias planejadas.** Os planos que chegam a esta meta, cada um com o valor que a projeção atribui a ela. Um plano que é totalmente consumido por metas anteriores não aparece aqui.

**Como ler a projeção.** "As economias planejadas concluem esta meta em 20 de dez. de 2026 · no prazo" significa que os planos acumulados até essa data cobrem o que falta antes do vencimento. "Cobrem até … · faltam …" significa que não cobrem; adicione um plano, mude a data ou reduza o valor-alvo.
