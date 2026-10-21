---
layout: screen
lang: pt
base: "/pt"
key: "screens/settings"
screen: settings
title: Ajustes
---
# Ajustes

**O que é.** Tudo o que não é um registro: preferências, fontes de dados, segurança e a pasta. Abre pela engrenagem na barra superior; em telas largas, é uma aba na barra lateral.

**Aviso.** Uma barra amarela no topo indica quando os valores em cache estão desatualizados (atualize quando estiver online) ou quando a avaliação está incompleta porque falta um saldo ou uma cotação. Ela aparece somente aqui.

**Preferências.** Moeda padrão e tema (sistema, claro, escuro). A moeda padrão é usada para avaliar a Visão geral e é sugerida para novas caixinhas e metas; os registros existentes mantêm sua moeda. **Idioma** muda a interface imediatamente; *Padrão do sistema* segue o dispositivo.

**Provedores de dados gratuitos.** Uma linha por tipo de dado, cada uma com o operador em uso: saldos BTC, ETH, TON, TRX; listas de tokens ETH, TON, TRX; preços de cripto; câmbio de moedas fiduciárias; **Inflação** (**World Bank**, **ECB**, **OECD**, **Desativado**) e **Crescimento do mercado** (**World Bank**, **Yahoo Finance · não oficial**, **Desativado**), que alimentam as [metas FIRE]({{ page.base }}/screens/goal#fire) e mantêm os padrões do app quando estão em **Desativado**; **Histórico de taxas** (**Frankfurter + DefiLlama**, **Desativado**) preenche o histórico diário de taxas de cada ativo que você possui, até a data mais antiga dos seus registros, para que os valores passados possam ser calculados. Toque em uma linha para escolher outro operador, deixar a consulta de tokens de uma rede como **Desativado** ou informar uma chave de API opcional. As chaves são armazenadas criptografadas no dispositivo e enviadas apenas ao operador que as emitiu. **Testar fontes / atualizar portfólio** consulta todos os operadores com os ativos que você realmente possui e informa o que falhou. Nenhum operador é substituído em silêncio.

**Corretoras.** As contas de corretora e as credenciais delas têm tela própria: [Corretoras]({{ page.base }}/screens/brokers).

**Atualização automática das taxas.** **Desativado**, **A cada 24 h**, **12 h**, **6 h**, **3 h** ou **1 h**. Quando ativada, uma tarefa em segundo plano obtém, nesse intervalo, a taxa atual de cada ativo que você possui, com os operadores escolhidos acima, e guarda as amostras no histórico de taxas. Ela nunca altera a pasta de dados: na próxima vez que você abrir o app ou atualizar, as amostras são incorporadas às cotações em cache, de modo que os totais partem das taxas mais recentes mesmo offline. Requer conexão de rede e usa um pouco de bateria e dados. A última execução aparece em **Cotações e atualização** como *Última atualização em segundo plano*.

**Cotações e atualização.** Todas as cotações em cache e todas as séries de inflação e de crescimento obtidas, com a fonte e o horário em que foram observadas e obtidas. Cotações desatualizadas continuam utilizáveis e são sinalizadas pelo aviso no topo desta tela.

**Segurança.** **Criptografia** criptografa com uma senha todos os arquivos da pasta; desativá-la descriptografa os arquivos. Com a criptografia ativada, você pode usar **Definir PIN**, ativar **Usar biometria**, escolher **Bloquear após tempo em segundo plano** e **Alterar senha**. Não há recuperação de senha. Dez PINs incorretos removem o PIN; a senha sempre funciona. Veja [Segurança]({{ page.base }}/manual#security).

**Armazenamento.** A pasta atual e a revisão dela. **Reconectar / abrir pasta** abre de novo o seletor de pastas; **Recarregar arquivos locais** lê a pasta de novo, por exemplo depois que sua ferramenta de sincronização trouxe alterações; **Exportar backup** grava um único arquivo portátil (sem criptografia quando a criptografia está desativada, e identificado como tal); **Restaurar backup** valida o arquivo antes de substituir os registros e mantém as versões salvas existentes.

**Fontes / atribuição.** Links para o site de cada operador.

**Legal.** Links para a [Política de Privacidade]({{ page.base }}/privacy), para as declarações de [Segurança dos dados]({{ page.base }}/data-safety) e de [Recursos financeiros]({{ page.base }}/financial-features) neste site, no idioma da interface. A versão e o número de build do app ficam no final.
