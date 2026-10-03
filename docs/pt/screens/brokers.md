---
layout: screen
lang: pt
base: "/pt"
key: "screens/brokers"
screen: brokers
title: Corretoras
---
# Corretoras

**O que é.** Suas conexões somente leitura com contas de corretora e de forex. Uma conta de corretora é a corretora, o ID da conta ou da consulta e a credencial da corretora; ao atualizar, o app lê o valor total da conta na moeda base dela. As contas ficam nesta tela, não dentro das caixinhas: a caixinha apenas se vincula a uma conta, e as caixinhas continuam sendo o lugar onde suas economias são contabilizadas.

**O que cada linha mostra.** O nome da conta, a corretora e o ID, o último valor lido na moeda da conta e na sua moeda padrão, quando foi observado e quando foi obtido, e a caixinha à qual está vinculada: **Vinculada a …** abre essa caixinha, *Não vinculada a uma caixinha* significa que nada a contabiliza ainda. **Editar** altera o nome, a corretora ou o ID; **Excluir** remove a conta e, se ela estava vinculada, a posição que a vinculava.

**Adicionar uma conta.** Pressione **+**, informe um nome, escolha a corretora e informe o ID que ela usa: o ID da Flex Query na Interactive Brokers, o ID da conta na OANDA, o número da conta na Trading 212. No SnapTrade, pressione **Conectar uma corretora pelo SnapTrade**, volte, pressione **Buscar contas** e escolha uma. Salve. A moeda e o valor aparecem depois da próxima atualização.

**Ignorar saldos menores que.** Marque a opção e informe um valor na sua moeda padrão (1 por padrão) para manter pequenos resíduos fora das suas economias: quando o valor da conta, convertido com as cotações em cache, fica abaixo desse valor, a posição vinculada conta como 0 e a linha mostra *Contado como 0: abaixo de …*. O valor real continua visível nesta tela. Sem cotação para a moeda da conta, nada é ignorado.

**Vincular a uma caixinha.** Abra a caixinha, pressione **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora** e escolha a conta; deixe o nome em branco para usar o nome da conta. Uma conta pode estar em uma só caixinha por vez. **Editar / mover** na posição a leva para outra caixinha; excluir a posição desvincula a conta sem excluí-la.

**Credenciais.** O token ou a chave de cada corretora compatível (Interactive Brokers, OANDA, Trading 212, SnapTrade); um conjunto por corretora vale para todas as contas dessa corretora. Elas são criptografadas com uma chave mantida no Android Keystore, nunca são gravadas na pasta de dados, ficam de fora das exportações e dos backups do sistema e são enviadas apenas à corretora que as emitiu. **Guia de configuração das contas de corretora** abre [Contas de corretora e de forex]({{ page.base }}/accounts), que lista os passos de cada corretora.

**Atualização.** O ícone de atualizar nesta tela lê todas as contas; a atualização de uma caixinha lê apenas as contas vinculadas a ela. Uma conta que não pode ser lida mantém o último valor e mostra a mensagem da corretora sob a linha dela. O app apenas lê: nunca envia ordens nem move dinheiro.
