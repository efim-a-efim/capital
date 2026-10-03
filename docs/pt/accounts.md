---
layout: default
lang: pt
base: "/pt"
key: "accounts"
title: Contas de corretora e de forex
class: doc
---
# Contas de corretora e de forex

O Capital consegue ler o valor total de uma conta de corretora ou de forex da mesma forma que lê uma carteira de cripto. Você adiciona a conta a uma caixinha como uma posição do tipo **Conta de corretora**, e cada atualização busca o patrimônio líquido (net asset value) da conta na moeda base dela. O app apenas lê: ele usa a interface de relatórios da corretora com um token que você mesmo cria, nunca envia, altera nem cancela uma ordem e nunca movimenta dinheiro.

O Capital se conecta apenas a interfaces cujas credenciais têm vida longa: um token ou uma chave que você cria uma vez e que continua válido até você revogá-lo (ou, no caso da Interactive Brokers, até a validade que você escolheu, de até um ano). Compatíveis hoje:

| Corretora | Interface usada | O que é lido |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (somente obtenção de relatórios) | Patrimônio líquido do último dia útil, na moeda base da conta |
| [OANDA](#oanda) | API REST v20, contas fxTrade reais | Patrimônio líquido no momento da atualização, na moeda da conta |
| [Trading 212](#trading-212) | API pública, contas Invest e Stocks ISA | Valor total da conta no momento da atualização, na moeda principal da conta |
| [SnapTrade](#snaptrade) | SnapTrade Personal, um agregador que cobre muitas corretoras | Valor total da conta conforme a corretora o informa ao SnapTrade, na moeda da conta |

## Antes de começar {#before-you-start}

- **O que sai do dispositivo.** A cada atualização, o app envia seu token de acesso e o ID da conta ou da consulta a essa corretora, por HTTPS. A corretora vê seu endereço IP, como em qualquer solicitação.
- **Onde as credenciais ficam guardadas.** Ajustes → Contas de corretora. Elas são criptografadas com uma chave mantida no Android Keystore, nunca são gravadas na sua pasta de dados e ficam de fora das exportações e dos backups do sistema. Um conjunto de credenciais por corretora vale para todas as contas que você adicionar dessa corretora.
- **O que fica armazenado na sua pasta.** O ID da conta, o último valor lido e quando ele foi lido. Nada mais da corretora.
- **As telas da corretora podem mudar.** Os passos abaixo correspondem aos sites das corretoras em outubro de 2026. As corretoras renomeiam menus e mudam configurações de lugar de tempos em tempos, então um passo pode parecer um pouco diferente quando você o seguir. A documentação da própria corretora, com link em cada seção, é a fonte oficial: se um passo daqui não corresponder mais, procure o mesmo termo na página da corretora.

## Interactive Brokers {#interactive-brokers}

O Capital usa o **Flex Web Service**, a interface da Interactive Brokers para obter relatórios pré-configurados. O token usado só consegue gerar e baixar relatórios; ele não consegue fazer login, negociar nem sacar. O Capital pede o **Net Asset Value (NAV) Summary in Base** (resumo do patrimônio líquido na moeda base) de uma Activity Flex Query e usa o total da data de relatório mais recente, de modo que o valor é o fechamento do último dia útil.

### 1. Crie a Flex Query

1. Faça login no [Client Portal](https://www.interactivebrokers.com/portal) e abra **Performance & Reports → Flex Queries** (em algumas contas o menu se chama *Reporting*).
2. Em **Activity Flex Query**, pressione **+** (Create). Dê um nome à consulta, por exemplo `Capital`.
3. Na lista **Sections**, ative exatamente estas duas seções e campos (selecionar todos os campos de uma seção também funciona):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. Em **Delivery Configuration**, defina **Format** como `XML` e **Period** como `Last Business Day`. As outras opções podem ficar com os valores padrão.
5. Salve a consulta, pressione o ícone **i** (informação) ao lado dela e anote o **Query ID**, um número.

A consulta deve abranger uma única conta. Se você tem contas vinculadas ou uma estrutura de assessor, crie uma consulta por conta e selecione apenas essa conta ao criá-la.

### 2. Ative o Flex Web Service e crie o token

1. Na mesma página **Flex Queries**, abra **Flex Web Service Configuration**.
2. Ative o **Flex Web Service Status** e salve. Um token é criado.
3. Para escolher por quanto tempo o token fica válido, pressione **Generate New Token**: de 6 horas a 1 ano. Deixe **Valid for IP address** vazio em um celular, cujo endereço muda. Gerar um novo token invalida o anterior.
4. Copie o token.

### 3. Conecte no Capital

1. **Ajustes → Contas de corretora → Token de acesso: Interactive Brokers**, cole o token e salve.
2. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, **Corretora** como Interactive Brokers, informe o **ID da Flex Query** e salve.
3. Pressione **Atualizar**. A primeira execução leva até meio minuto, porque o relatório é gerado sob demanda.

Quando o token expira, a atualização informa *O token expirou; gere um novo no Client Portal*: gere um novo token e cole-o em Ajustes. A Interactive Brokers permite uma solicitação de relatório por segundo e dez por minuto com o mesmo token, limite que uma atualização nunca excede.

Documentação da Interactive Brokers: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

O Capital chama o **resumo da conta** (account summary) da API REST v20 da OANDA e armazena o NAV da conta (saldo mais lucro ou prejuízo não realizado) na moeda da conta. Apenas contas reais **fxTrade** são compatíveis; contas de prática não são economias.

**Um token de acesso pessoal da OANDA não é somente de leitura.** Ele concede acesso total à API a todas as subcontas do seu login, inclusive para negociar. O Capital chama apenas o resumo da conta, mas qualquer pessoa que obtenha o token poderia negociar com ele. Trate-o como uma senha: cole-o apenas no Capital e revogue-o no portal da OANDA se perder o celular.

### 1. Crie o token

1. Faça login no portal de gerenciamento de contas fxTrade da OANDA.
2. Abra **My Services → Manage API Access** (no portal antigo: *My Account → My Services → Manage API Access*).
3. Aceite a licença da API e pressione **Generate**. Copie o token; a OANDA não o mostra de novo. Se você o perder, revogue-o lá e gere um novo.

### 2. Encontre o ID da conta

O ID de conta da v20 tem o formato `001-001-1234567-001`, com hifens. Ele aparece no mesmo portal ao lado de cada subconta e, na plataforma fxTrade, nos detalhes da conta.

### 3. Conecte no Capital

1. **Ajustes → Contas de corretora → Token de acesso: OANDA**, cole o token e salve.
2. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, **Corretora** como OANDA, informe o **ID da conta OANDA** e salve.
3. Pressione **Atualizar**.

Uma conta de margem com NAV negativo é informada como erro, e não contabilizada como economias.

Documentação da OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

O Capital chama o **resumo da conta** (account summary) da API pública da Trading 212 e armazena o valor total da conta na moeda principal dela. A API cobre contas **Invest** e **Stocks ISA**; um par de chaves pertence a uma conta e o Capital guarda um único par de chaves, então lê uma conta Trading 212.

### 1. Crie a chave de API

1. No app ou no site da Trading 212, abra o menu (**☰**) → **Settings** → **API (Beta)** e aceite o aviso de risco.
2. Pressione **Generate API key**. Dê um nome a ela, mantenha apenas a permissão **Account data** (leitura) e escolha acesso por IP *Unrestricted* (o endereço de um celular muda).
3. Envie. Copie os dois valores: a **API Key** e a **API Secret Key**. O segredo é mostrado uma única vez; se você o perder, exclua a chave e gere um novo par.

### 2. Conecte no Capital

1. **Ajustes → Contas de corretora → Chave de API: Trading 212** e **Segredo de API: Trading 212**, cole cada valor.
2. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, **Corretora** como Trading 212, informe o **Número da conta Trading 212** (o ID da conta mostrado no app, somente dígitos) e salve.
3. Pressione **Atualizar**. A Trading 212 permite uma solicitação de resumo a cada 5 segundos.

Documentação da Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

O [SnapTrade](https://snaptrade.com) é um agregador: você conecta uma conta de corretora ao SnapTrade uma única vez, e o SnapTrade a lê para você. Ele cobre muitas corretoras que não têm API pública própria. O Capital usa o **SnapTrade Personal**, o plano gratuito para as suas próprias contas, com o seu próprio ID de cliente e a sua própria chave de consumidor. Os dados desse plano são atualizados pelo SnapTrade cerca de uma vez por dia.

O que é enviado: seu ID de cliente e, como assinatura, nada da chave de consumidor em si (as solicitações são assinadas com ela). O SnapTrade, e não o Capital, mantém a conexão com a sua corretora; os termos e a política de privacidade dele se aplicam a essa conexão.

### 1. Crie a chave de API

1. Cadastre-se no [painel do SnapTrade](https://dashboard.snaptrade.com/signup) e escolha o plano **Personal**.
2. No painel, crie uma chave de API. Copie o **ID de cliente** e a **chave de consumidor**; a chave de consumidor é mostrada uma única vez.

### 2. Conecte no Capital

1. **Ajustes → Contas de corretora → ID de cliente: SnapTrade** e **Chave de consumidor: SnapTrade**, cole cada valor.
2. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora** e **Corretora** como SnapTrade.
3. Pressione **Conectar uma corretora pelo SnapTrade**. O Connection Portal do SnapTrade abre no navegador; faça login na sua corretora por lá (o link vale por 5 minutos). Volte ao Capital.
4. Pressione **Buscar contas** e escolha a conta; o ID dela preenche o campo **ID da conta do SnapTrade**. Salve e depois pressione **Atualizar**.

Uma conta que o SnapTrade ainda não terminou de sincronizar informa *O SnapTrade ainda não tem valor total para esta conta*; atualize de novo mais tarde.

Documentação do SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Outras corretoras {#other-brokers}

O Capital se conecta apenas a interfaces que funcionam a partir de um celular por HTTPS, com um token que você mesmo pode criar e que leem sem conseguir negociar. Isso exclui, por enquanto:

- Contas **MetaTrader 4 e 5**. A senha de investidor dá acesso somente de leitura, mas apenas dentro do terminal MetaTrader; as corretoras não publicam nenhuma interface HTTPS para ele.
- Corretoras cuja API exige um programa rodando em um computador (por exemplo, o gateway da Client Portal Web API da Interactive Brokers; o Capital usa o Flex Web Service no lugar) ou o registro de um aplicativo OAuth.
- Corretoras cuja API emite apenas tokens de vida curta por meio de OAuth, por exemplo o Saxo Bank (os tokens de acesso duram 20 minutos; o token de 24 horas do portal de desenvolvedores vale apenas para o ambiente de simulação).
- Bancos e corretoras sem API pública.

Muitas dessas corretoras são cobertas pelo [SnapTrade](#snaptrade). Caso contrário, informe o saldo como uma posição **Manual** e atualize o número quando conferir seu extrato. Se a sua corretora oferecer um endpoint HTTPS simples, baseado em token, que leia o valor da conta, [abra uma issue]({{ site.repo }}/issues) com um link para a documentação dele.

## Mensagens e o que fazer {#messages}

| Mensagem | O que fazer |
|---|---|
| *Interactive Brokers precisa do seu token de acesso em Ajustes → Contas de corretora* / *OANDA precisa do seu token de acesso …* | Cole o token, a chave ou o ID de cliente em Ajustes → Contas de corretora. |
| *O token expirou; gere um novo no Client Portal* | Gere um novo token do Flex Web Service e cole-o. |
| *Token inválido* | Copie o token de novo; um novo token substitui o antigo. |
| *O token é restrito a outro endereço IP* | Gere o token sem restrição de IP. |
| *ID da Flex Query inválido* | Confira o número; a consulta deve ser uma Activity Flex Query deste login. |
| *Adicione à Flex Query a seção Net Asset Value (NAV) Summary in Base com Report Date e Total* | Edite a consulta e adicione a seção e os campos. |
| *Adicione à Flex Query o campo Currency de Account Information* | Edite a consulta e adicione o campo. |
| *A consulta retornou N contas; crie uma Flex Query por conta* | Crie uma consulta que abranja uma única conta. |
| *O extrato ainda não está pronto; atualize de novo em um minuto* | A Interactive Brokers ainda está gerando o relatório; atualize de novo. |
| *Acesso negado; verifique a chave ou a cota do provedor* | O token da OANDA está errado ou foi revogado, ou o par de chaves da Trading 212 está errado ou não tem a permissão Account data. |
| *O SnapTrade ainda não tem valor total para esta conta; sincronize a conexão e tente de novo* | O SnapTrade ainda não sincronizou a corretora; atualize de novo mais tarde. |
| *Nenhuma conta conectada ainda. Conecte primeiro uma corretora pelo SnapTrade.* | Abra o Connection Portal pelo editor e conecte uma corretora. |
| *Valor negativo da conta (…) não é compatível* | A conta está no vermelho; ela não acrescenta nada às suas economias. |

O valor anterior continua visível após qualquer uma dessas mensagens, marcado como desatualizado.
