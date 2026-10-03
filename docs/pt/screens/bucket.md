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

**Modo portfólio.** Quando está ativado, a tela ganha uma tabela com valor, participação real, alvo e diferença por ativo, além de um botão **Rebalancear** que pede um valor e lista o que comprar. Vendas só aparecem quando *Permitir vendas no rebalanceamento* está ativado. Nada é negociado.
