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

O Capital se conecta apenas a interfaces cujas credenciais têm vida longa: um token ou uma chave que você cria uma vez e que continua válido até você revogá-lo, até uma validade que você escolheu ou por pelo menos meses (os tokens da T-Invest expiram após três meses sem uso, os da ALOR após um ano). Todas as corretoras abaixo estão disponíveis em qualquer idioma em que o app esteja. Compatíveis hoje:

| Corretora | Interface usada | O que é lido |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (somente obtenção de relatórios) | Patrimônio líquido do último dia útil, na moeda base da conta |
| [OANDA](#oanda) | API REST v20, contas fxTrade reais | Patrimônio líquido no momento da atualização, na moeda da conta |
| [Trading 212](#trading-212) | API pública, contas Invest e Stocks ISA | Valor total da conta no momento da atualização, na moeda principal da conta |
| [SnapTrade](#snaptrade) | SnapTrade Personal, um agregador que cobre muitas corretoras | Valor total da conta conforme a corretora o informa ao SnapTrade, na moeda da conta |
| [Alpaca](#alpaca) | Trading API, contas reais | Patrimônio (caixa mais posições), em dólares americanos |
| [Tradier](#tradier) | Brokerage API | Patrimônio total, em dólares americanos |
| [tastytrade](#tastytrade) | Open API com uma concessão OAuth pessoal | Valor líquido de liquidação, em dólares americanos |
| [Public.com](#public) | Individual API | Valor total da conta, em dólares americanos |
| [eToro](#etoro) | API pública | Saldo da conta escolhida (em uma conta de negociação: caixa mais posições investidas), na moeda dela |
| [Indexa Capital](#indexa-capital) | API REST, token somente leitura | Total da carteira na última data de avaliação, na moeda da conta |
| [T-Invest](#t-invest) | API T-Invest (T-Bank) | Valor total da carteira, em rublos |
| [ALOR](#alor) | ALOR OpenAPI | Avaliação da carteira na Bolsa de Moscou, em rublos |
| [Capital.com](#capital-com) | API pública, contas reais | Saldo incluindo lucros e prejuízos em aberto, na moeda da conta |
| [Akahu](#akahu) | Aplicativo pessoal do Akahu, um agregador da Nova Zelândia | Saldo de uma conta conectada (Sharesies, Hatch, Kernel, KiwiSaver e outras), na moeda dela |

## Antes de começar {#before-you-start}

- **O que sai do dispositivo.** A cada atualização, o app envia seu token de acesso e o ID da conta ou da consulta a essa corretora, por HTTPS. A corretora vê seu endereço IP, como em qualquer solicitação.
- **Onde as credenciais ficam guardadas.** Aba Corretoras → Credenciais. Elas são criptografadas com uma chave mantida no Android Keystore, nunca são gravadas na sua pasta de dados e ficam de fora das exportações e dos backups do sistema. Um conjunto de credenciais por corretora vale para todas as contas que você adicionar dessa corretora.
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

1. **Corretoras → Credenciais → Token de acesso: Interactive Brokers**, cole o token e salve.
2. **Corretoras → +**: informe um nome, defina **Corretora** como Interactive Brokers, informe o **ID da Flex Query** e salve.
3. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, escolha a conta e salve.
4. Pressione **Atualizar**. A primeira execução leva até meio minuto, porque o relatório é gerado sob demanda.

Quando o token expira, a atualização informa *O token expirou; gere um novo no Client Portal*: gere um novo token e cole-o em Credenciais. A Interactive Brokers permite uma solicitação de relatório por segundo e dez por minuto com o mesmo token, limite que uma atualização nunca excede.

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

1. **Corretoras → Credenciais → Token de acesso: OANDA**, cole o token e salve.
2. **Corretoras → +**: informe um nome, defina **Corretora** como OANDA, informe o **ID da conta OANDA** e salve.
3. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, escolha a conta e salve.
4. Pressione **Atualizar**.

Uma conta de margem com NAV negativo é informada como erro, e não contabilizada como economias.

Documentação da OANDA: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

O Capital chama o **resumo da conta** (account summary) da API pública da Trading 212 e armazena o valor total da conta na moeda principal dela. A API cobre contas **Invest** e **Stocks ISA**; um par de chaves pertence a uma conta e o Capital guarda um único par de chaves, então lê uma conta Trading 212.

### 1. Crie a chave de API

1. No app ou no site da Trading 212, abra o menu (**☰**) → **Settings** → **API (Beta)** e aceite o aviso de risco.
2. Pressione **Generate API key**. Dê um nome a ela, mantenha apenas a permissão **Account data** (leitura) e escolha acesso por IP *Unrestricted* (o endereço de um celular muda).
3. Envie. Copie os dois valores: a **API Key** e a **API Secret Key**. O segredo é mostrado uma única vez; se você o perder, exclua a chave e gere um novo par.

### 2. Conecte no Capital

1. **Corretoras → Credenciais → Chave de API: Trading 212** e **Segredo de API: Trading 212**, cole cada valor.
2. **Corretoras → +**: informe um nome, defina **Corretora** como Trading 212, informe o **Número da conta Trading 212** (o ID da conta mostrado no app, somente dígitos) e salve.
3. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, escolha a conta e salve.
4. Pressione **Atualizar**. A Trading 212 permite uma solicitação de resumo a cada 5 segundos.

Documentação da Trading 212: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

O [SnapTrade](https://snaptrade.com) é um agregador: você conecta uma conta de corretora ao SnapTrade uma única vez, e o SnapTrade a lê para você. Ele cobre muitas corretoras que não têm API pública própria. O Capital usa o **SnapTrade Personal**, o plano gratuito para as suas próprias contas, com o seu próprio ID de cliente e a sua própria chave de consumidor. Os dados desse plano são atualizados pelo SnapTrade cerca de uma vez por dia.

O que é enviado: seu ID de cliente e, como assinatura, nada da chave de consumidor em si (as solicitações são assinadas com ela). O SnapTrade, e não o Capital, mantém a conexão com a sua corretora; os termos e a política de privacidade dele se aplicam a essa conexão.

### 1. Crie a chave de API

1. Cadastre-se no [painel do SnapTrade](https://dashboard.snaptrade.com/signup) e escolha o plano **Personal**.
2. No painel, crie uma chave de API. Copie o **ID de cliente** e a **chave de consumidor**; a chave de consumidor é mostrada uma única vez.

### 2. Conecte no Capital

1. **Corretoras → Credenciais → ID de cliente: SnapTrade** e **Chave de consumidor: SnapTrade**, cole cada valor.
2. **Corretoras → +**: informe um nome e defina **Corretora** como SnapTrade.
3. Pressione **Conectar uma corretora pelo SnapTrade**. O Connection Portal do SnapTrade abre no navegador; faça login na sua corretora por lá (o link vale por 5 minutos). Volte ao Capital.
4. Pressione **Buscar contas** e escolha a conta; o ID dela preenche o campo **ID da conta do SnapTrade**. Salve.
5. Abra a caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, escolha a conta e salve; depois pressione **Atualizar**.

Uma conta que o SnapTrade ainda não terminou de sincronizar informa *O SnapTrade ainda não tem valor total para esta conta*; atualize de novo mais tarde.

Documentação do SnapTrade: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Conectar uma conta no Capital {#connect}

As seções abaixo explicam como criar a credencial em cada corretora. No Capital, os passos são os mesmos para todas:

1. **Corretoras → Credenciais**: pressione os botões de credenciais da corretora e cole cada valor.
2. **Corretoras → +**: informe um nome, escolha a **Corretora**, pressione **Buscar contas** e escolha a conta (ou digite o ID dela) e salve.
3. Abra uma caixinha, **Adicionar posição**, defina **Acompanhamento** como **Conta de corretora**, escolha a conta e salve. Pressione **Atualizar**.

## Alpaca {#alpaca}

A Alpaca emite um ID de chave e um segredo por conta; eles continuam válidos até você gerá-los de novo. Apenas contas reais são lidas: as chaves de uma conta paper não funcionam na API real.

1. Faça login no [painel da Alpaca](https://app.alpaca.markets), mude para a sua conta real e, na página inicial, em **API Keys**, pressione **Generate New Keys**.
2. Copie o **API Key ID** e a **Secret Key**; o segredo é exibido uma única vez.
3. No Capital, cole-os como **Chave de API: Alpaca** e **Segredo de API: Alpaca** e siga [Conectar uma conta](#connect). **Buscar contas** mostra o número da conta da chave.

Documentação da Alpaca: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

O token de API das configurações da Tradier nunca expira.

1. Faça login na Tradier e abra [Settings → API Access](https://web.tradier.com/user/api). Copie o **API Access Token** da sua conta de corretora (não o token do sandbox).
2. No Capital, cole-o como **Token de acesso: Tradier** e siga [Conectar uma conta](#connect).

Documentação da Tradier: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

A tastytrade usa uma concessão OAuth pessoal: você cria um aplicativo para si mesmo e uma concessão cujo token de atualização nunca expira. O Capital o troca por um token de acesso de 15 minutos a cada atualização.

1. Em [my.tastytrade.com](https://my.tastytrade.com), abra **Manage → My Profile → API → OAuth Applications** e pressione **+ New OAuth client**. Dê um nome a ele, qualquer URI de redirecionamento HTTPS (por exemplo, `https://capital.fimych.dev`) e apenas o escopo **read**. Salve e copie o **Client Secret**; ele é exibido uma única vez.
2. Pressione **Manage** ao lado do aplicativo, depois **Create Grant**, e copie o **token de atualização** (refresh token).
3. No Capital, cole-os como **Token de atualização: tastytrade** e **Segredo do cliente: tastytrade** e siga [Conectar uma conta](#connect).

Documentação da tastytrade: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

A Individual API da Public foi feita para as suas próprias contas. A chave secreta tem vida longa e pode ser revogada; o Capital a troca por um token de acesso de cinco minutos a cada atualização.

1. No app web da Public, abra a página **API** das suas configurações e gere uma **chave secreta**.
2. No Capital, cole-a como **Chave secreta: Public.com** e siga [Conectar uma conta](#connect).

Documentação da Public: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

As chaves da eToro têm vida longa; você pode dar a elas uma data de expiração e uma lista de IPs, e pode torná-las somente leitura. Sua conta da eToro precisa estar verificada.

1. Na eToro, abra **Settings → Trading → API Key Management** e pressione **Create New Key**. Escolha o ambiente **Real**, a permissão **Read**, nenhuma lista de IPs e, se quiser, uma data de expiração. Confirme com o código por SMS.
2. Copie a **Public API Key** e a **User Key**; a chave do usuário é exibida uma única vez.
3. No Capital, cole-as como **Chave de API pública: eToro** e **Chave do usuário: eToro** e siga [Conectar uma conta](#connect). **Buscar contas** lista suas contas de negociação, de caixa e outras da eToro.

Documentação da eToro: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

O token da área privada da Indexa é somente leitura. Ele está vinculado ao seu e-mail, à sua senha e ao seu dispositivo: depois de trocar a senha, gere-o de novo.

1. Na área privada da Indexa, abra **Configurações do usuário → Aplicativos** (*User settings → Applications*) e copie o token.
2. No Capital, cole-o como **Token de acesso: Indexa Capital** e siga [Conectar uma conta](#connect). As contas de previdência e de investimento são listadas.

A Indexa avalia os fundos uma vez por dia útil; a data de observação é essa data de avaliação.

Documentação da Indexa Capital: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

A API T-Invest do T-Bank aceita um token que você emite nas configurações de investimento. Um token expira três meses após o último uso e precisa ser usado em até sete dias após a emissão; uma atualização semanal o mantém ativo. Escolha um token **somente leitura**.

1. Abra as [configurações do T-Invest](https://www.tbank.ru/invest/settings/) e emita um **token da API T-Invest** para a bolsa com acesso **somente leitura** (todas as contas ou uma). A confirmação de operações por código precisa estar desativada para emiti-lo. Copie o token; ele é exibido uma única vez.
2. No Capital, cole-o como **Token de acesso: T-Invest** e siga [Conectar uma conta](#connect).

O T-Bank serve essa API com a Russian Trusted Root CA, que o Android não inclui. O Capital confia nesse certificado apenas para o endereço da API T-Invest (`invest-public-api.tbank.ru`), e em nenhuma outra conexão.

Documentação da T-Invest: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

A ALOR emite um token de atualização válido por um ano; o Capital o troca por um token de acesso de 30 minutos a cada atualização. A ALOR não oferece token somente leitura: o token poderia negociar, o Capital apenas lê.

1. Entre no [portal de desenvolvedores da ALOR](https://alor.dev), vincule sua conta de negociação, abra **API Access Tokens** e pressione **Create Token**. Copie o token de atualização.
2. No Capital, cole-o como **Token de atualização: ALOR** e siga [Conectar uma conta](#connect). **Buscar contas** lista as carteiras da conta (mercado de ações D…, mercado de câmbio G…, derivativos 7500…); adicione uma por carteira.

Documentação da ALOR: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

As chaves da Capital.com são válidas por um ano por padrão, ou até a data que você escolher. Elas têm permissão de negociação (a Capital.com não tem chaves somente leitura); o Capital apenas lê. Cada chave tem a sua própria senha, que não é a senha da sua conta.

1. Ative a autenticação em dois fatores, depois abra **Settings → API integrations** e pressione **Generate API key**. Dê um rótulo a ela e uma **senha personalizada**, mantenha ou defina a validade e confirme com o código 2FA. Copie a chave; ela é exibida uma única vez.
2. No Capital, cole **Chave de API: Capital.com**, o seu e-mail de login como **E-mail de login: Capital.com** e a senha personalizada como **Senha da chave de API: Capital.com** e siga [Conectar uma conta](#connect). Apenas contas reais são lidas.

Documentação da Capital.com: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

O [Akahu](https://www.akahu.nz) conecta bancos, plataformas de investimento e planos KiwiSaver da Nova Zelândia; um aplicativo pessoal gratuito lê as suas próprias contas. O Akahu atualiza os dados cerca de uma vez por dia.

1. Cadastre-se em [my.akahu.nz](https://my.akahu.nz) e conecte seus provedores (por exemplo, Sharesies, Hatch, Kernel, Simplicity, Milford ou o seu plano KiwiSaver).
2. Abra a página **Developers**, aceite os termos de desenvolvedor e copie o **App ID Token** e o **User Access Token**.
3. No Capital, cole-os como **Token de ID do app: Akahu** e **Token de acesso do usuário: Akahu** e siga [Conectar uma conta](#connect).

Documentação do Akahu: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Corretoras populares por mercado {#by-market}

Como as corretoras mais usadas nos mercados dos idiomas do Capital podem ser conectadas, em outubro de 2026. *Direta* significa uma seção acima; *SnapTrade* significa por meio do [SnapTrade](#snaptrade); nos demais casos, o motivo de não poder ser lida, e o saldo pode ser mantido como uma posição **Manual**.

| Mercado | Corretora | Como |
|---|---|---|
| Estados Unidos | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Direta |
| Estados Unidos | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| Estados Unidos | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Sem API pública |
| Canadá | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Canadá | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Sem API pública |
| Reino Unido e Irlanda | Trading 212, eToro, Interactive Brokers | Direta |
| Reino Unido e Irlanda | AJ Bell | SnapTrade |
| Reino Unido e Irlanda | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Sem API pública |
| Reino Unido e Irlanda | IG | Impossível: toda sessão exige a senha da conta |
| Europa | Indexa Capital (Espanha), eToro, Trading 212, Interactive Brokers | Direta |
| Europa | DEGIRO, BUX | SnapTrade |
| Europa | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Sem API pública para investimentos |
| Europa | XTB | Impossível: a API foi encerrada em março de 2025 |
| Europa | Saxo, comdirect | Impossível: apenas tokens de vida curta ou sessões com TAN |
| Europa | Bitpanda, Freedom24 | Impossível: a API não retorna o valor total da conta |
| Rússia e Cazaquistão | T-Invest, ALOR | Direta |
| Rússia e Cazaquistão | BCS | Impossível: sem valor total, e o token expira após 90 dias |
| Rússia e Cazaquistão | Finam | Ainda não: a moeda do valor da conta não está documentada |
| Rússia e Cazaquistão | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Sem API pública, ou sem valor total nela |
| Índia | Zerodha, Upstox | SnapTrade (as regras da SEBI encerram as sessões de API todo dia, então a conexão precisa de renovação frequente) |
| Índia | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Impossível: as regras da SEBI encerram toda sessão de API diariamente |
| Paquistão e Bangladesh | Todas as corretoras de bolsa | Sem API pública |
| China, Hong Kong e Taiwan | moomoo | SnapTrade |
| China, Hong Kong e Taiwan | Futu, Tiger Brokers, Longbridge | Ainda não: vida útil da chave ou formato de resposta não totalmente documentados, ou as chaves não podem ser limitadas à leitura |
| China, Hong Kong e Taiwan | East Money, Huatai, CITIC, Yuanta, Fubon | Sem API web pública (apenas terminais desktop ou SDKs com certificado) |
| Japão | OANDA Japan (contas que se qualificam para acesso à API) | Direta, como a OANDA |
| Japão | SBI Securities, Rakuten Securities, Monex, Matsui | Sem API pública |
| Austrália e Nova Zelândia | CommSec, Stake | SnapTrade |
| Austrália e Nova Zelândia | Sharesies, Hatch, Kernel, Simplicity, planos KiwiSaver | Akahu (contas da Nova Zelândia) |
| Oriente Médio e África | eToro | Direta |
| Oriente Médio e África | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Sem API pública para pessoas físicas |
| Sudeste Asiático | Stockbit, Ajaib, Bibit, IPOT, VPS | Sem API pública |
| Sudeste Asiático | SSI, TCBS, DNSE | Impossível: tokens de 8 horas com código de uso único, ou apenas saldo em caixa |
| América Latina | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Sem API pública para pessoas físicas, ou logins apenas com senha |
| Forex e CFD | OANDA, Capital.com | Direta |
| Forex e CFD | Corretoras MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | Impossível: sem acesso de leitura por HTTPS |
| Forex e CFD | Corretoras cTrader, FXCM, Forex.com | Impossível: registro de aplicativo, API descontinuada ou logins com senha |

## Outras corretoras {#other-brokers}

O Capital se conecta apenas a interfaces que funcionam a partir de um celular por HTTPS, com um token que você mesmo pode criar e que leem sem conseguir negociar. Isso exclui, por enquanto:

- Contas **MetaTrader 4 e 5**. A senha de investidor dá acesso somente de leitura, mas apenas dentro do terminal MetaTrader; as corretoras não publicam nenhuma interface HTTPS para ele.
- Corretoras cuja API exige um programa rodando em um computador (por exemplo, o gateway da Client Portal Web API da Interactive Brokers; o Capital usa o Flex Web Service no lugar) ou o registro de um aplicativo OAuth.
- Corretoras cuja API emite apenas tokens de vida curta por meio de OAuth, por exemplo o Saxo Bank (os tokens de acesso duram 20 minutos; o token de 24 horas do portal de desenvolvedores vale apenas para o ambiente de simulação).
- Bancos e corretoras sem API pública.

Muitas dessas corretoras são cobertas pelo [SnapTrade](#snaptrade). Caso contrário, informe o saldo como uma posição **Manual** e atualize o número quando conferir seu extrato. Se a sua corretora oferecer um endpoint HTTPS simples, baseado em token, que leia o valor da conta, [abra uma issue]({{ site.repo }}/issues) com um link para a documentação dele. Cada corretora no Capital é um pequeno plugin; desenvolvedores podem adicionar uma seguindo [o guia de plugins]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Mensagens e o que fazer {#messages}

| Mensagem | O que fazer |
|---|---|
| *Interactive Brokers precisa das credenciais na tela Corretoras* / *OANDA precisa das credenciais …* | Cole o token, a chave ou o ID de cliente em Credenciais, na aba Corretoras. |
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
| *tastytrade recusou o token de atualização ou o segredo do cliente; crie uma nova concessão* | Crie uma nova concessão para o aplicativo e cole o token de atualização dela; confira o segredo do cliente. |
| *Capital.com não abriu uma sessão; confira a chave de API, o login e a senha da chave* | A chave, o e-mail ou a senha personalizada da chave está errada, ou a chave expirou. |
| *Conta não encontrada; escolha-a de novo* | A corretora não lista mais esta conta; edite-a e escolha-a em **Buscar contas**. |

O valor anterior continua visível após qualquer uma dessas mensagens, marcado como desatualizado.
