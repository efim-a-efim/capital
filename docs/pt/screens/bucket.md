---
layout: screen
lang: pt
base: "/pt"
key: "screens/bucket"
screen: bucket
title: Caixinha
---
# Caixinha

**O que é.** Uma caixinha com suas posições. Acessada tocando em um cartão na aba [Caixinhas]({{ page.base }}/screens/buckets); **← Todas as caixinhas** volta.

**Cabeçalho.** O valor da caixinha e, em seguida, dois números que só fazem sentido juntos: **Alocado**, a parte reivindicada pelas metas vinculadas, e **Disponível**, o restante. **Editar caixinha** abre o nome, a moeda e as chaves do portfólio. **Excluir caixinha** remove a caixinha e suas posições após uma confirmação.

**Posições.** Cada posição mostra o nome, o valor na moeda da caixinha, como é acompanhada (*Manual*, *Carteira* ou *Conta de corretora*), a quantidade nativa, quando o valor foi observado e quando foi obtido pela última vez. **Editar / mover** altera a posição ou a move para outra caixinha; **Excluir** a remove.

**Adicionar posição** abre o editor de posição:

- **Manual**: um nome, um código de moeda ou de ativo e a quantidade. Use para tudo o que o app não consegue ler.
- **Carteira**: escolha a rede (BTC, ETH, TON, TRX) e cole um endereço público. Ao atualizar, o app lê o saldo nativo e, em ETH, TON e TRX, os tokens fungíveis desse endereço. Abra o editor de novo e toque em **Buscar tokens** para vê-los e desativar os que você não quer contabilizar.
- **Conta de corretora**: escolha uma das contas adicionadas na tela [Corretoras]({{ page.base }}/screens/brokers); deixe o nome em branco para usar o nome da conta. Ao atualizar, o app lê o valor total da conta na moeda base dela. Uma conta pode ser vinculada a uma só caixinha; excluir a posição a desvincula sem excluir a conta.

**Tokens e "não contabilizado".** Um token é identificado pelo endereço do contrato. Ele só é contabilizado quando a sua fonte de preços lista exatamente aquele contrato; caso contrário, aparece como *Token desconhecido · não contabilizado* e fica fora dos totais. É isso que impede que um "USDT" falso recebido por airdrop entre nas suas economias.

**PnL do investimento.** Ative em **Editar caixinha** (caixa **PnL do investimento**). A partir daí o app registra, a cada atualização, a quantidade de cada posição e os aportes e saques das suas contas de corretora, e a tela ganha a seção **Investimento**:

- Cinco linhas, **Todo o período**, **Último ano**, **Último mês**, **Última semana** e **Último dia**, cada uma com o lucro ou prejuízo na moeda da caixinha e o retorno ponderado pelo tempo em percentual, verde se positivo e vermelho se negativo. A linha mostra *desde <data>* quando o histórico começa depois do início do período.
- Um gráfico **Valor** com o valor da caixinha e um gráfico **Lucros e perdas** com o resultado acumulado, ambos na moeda da caixinha. Os botões **Tudo**, **1Y**, **1M**, **1W** e **1D** ajustam os dois gráficos ao período; a curva de PnL recomeça do zero no início do período.
- *Desde <data> · em <moeda>* indica onde o histórico começa. *Alguns dias não têm taxa; esses valores usam a última conhecida* aparece quando o histórico de taxas tem lacunas.

**Como é calculado.** Lucro ou prejuízo = variação do valor menos aportes mais saques. O percentual é ponderado pelo tempo: o retorno de cada dia é encadeado, então o momento dos seus aportes não o altera. Aportes e saques vêm da corretora nas contas vinculadas ([quais corretoras]({{ page.base }}/accounts#pnl)); em posições manuais e carteiras, uma mudança de quantidade conta como aporte ou saque pela taxa do dia, nunca como lucro. Os valores passados usam o histórico diário de taxas (**Ajustes → Histórico de taxas**). Ao ativar a opção, o histórico é montado a partir dos snapshots já presentes na sua pasta e das corretoras (*Montando o histórico de PnL…* e depois *Histórico de PnL pronto: N dias*); até a primeira atualização a seção mostra *Ainda não há histórico*.

**Modo portfólio.** Quando está ativado, a tela ganha uma tabela com valor, participação real, alvo e diferença por ativo, além de um botão **Rebalancear** que pede um valor e lista o que comprar. Vendas só aparecem quando *Permitir vendas no rebalanceamento* está ativado. Nada é negociado.
