---
layout: default
lang: fr
base: "/fr"
key: "accounts"
title: Comptes de courtage et de forex
class: doc
---
# Comptes de courtage et de forex

Capital peut lire la valeur totale d’un compte de courtage ou de forex comme il lit un portefeuille crypto. Vous ajoutez le compte à une poche sous la forme d’une position de type **Compte de courtage**, et chaque actualisation récupère la valeur nette d’inventaire (net asset value, NAV) du compte dans sa devise de référence. L’application se contente de lire : elle utilise l’interface de rapports du courtier avec un jeton d’accès que vous créez vous-même, ne passe, ne modifie ni n’annule jamais d’ordre, et ne déplace jamais d’argent.

Capital se connecte uniquement à des interfaces dont les identifiants sont de longue durée : un jeton ou une clé que vous créez une fois et qui reste valable jusqu’à ce que vous le révoquiez, jusqu’à une expiration que vous avez choisie, ou pendant des mois au moins (les jetons T-Invest expirent après trois mois sans utilisation, ceux d’ALOR après un an). Tous les courtiers ci-dessous sont disponibles quelle que soit la langue de l’application. Pris en charge actuellement :

| Courtier | Interface utilisée | Ce qui est lu |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (récupération de rapports uniquement) | Valeur nette d’inventaire du dernier jour ouvré, dans la devise de référence du compte |
| [OANDA](#oanda) | API REST v20, comptes fxTrade réels | Valeur nette d’inventaire au moment de l’actualisation, dans la devise du compte |
| [Trading 212](#trading-212) | API publique, comptes Invest et Stocks ISA | Valeur totale du compte au moment de l’actualisation, dans la devise principale du compte |
| [SnapTrade](#snaptrade) | SnapTrade Personal, un agrégateur qui couvre de nombreux courtiers | Valeur totale du compte telle que le courtier la déclare à SnapTrade, dans la devise du compte |
| [Alpaca](#alpaca) | Trading API, comptes réels | Equity (liquidités plus positions), en dollars américains |
| [Tradier](#tradier) | Brokerage API | Equity totale, en dollars américains |
| [tastytrade](#tastytrade) | Open API avec une autorisation OAuth personnelle | Valeur nette de liquidation, en dollars américains |
| [Public.com](#public) | Individual API | Valeur totale du compte, en dollars américains |
| [eToro](#etoro) | API publique | Solde du compte choisi (pour un compte de trading : liquidités plus positions investies), dans sa devise |
| [Indexa Capital](#indexa-capital) | API REST, jeton en lecture seule | Total du portefeuille à la dernière date de valorisation, dans la devise du compte |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | Valeur totale du portefeuille, en roubles |
| [ALOR](#alor) | ALOR OpenAPI | Valorisation du portefeuille à la Bourse de Moscou, en roubles |
| [Capital.com](#capital-com) | API publique, comptes réels | Solde incluant les profits et pertes ouverts, dans la devise du compte |
| [Akahu](#akahu) | Application personnelle Akahu, un agrégateur néo-zélandais | Solde d’un compte connecté (Sharesies, Hatch, Kernel, KiwiSaver et autres), dans sa devise |

## Avant de commencer {#before-you-start}

- **Ce qui quitte l’appareil.** À chaque actualisation, l’application envoie votre jeton d’accès et l’identifiant du compte ou de la requête à ce courtier, en HTTPS. Le courtier voit votre adresse IP, comme pour toute requête.
- **Où les identifiants sont conservés.** Onglet Courtiers → Identifiants. Ils sont chiffrés avec une clé conservée dans Android Keystore, ne sont jamais écrits dans votre dossier de données et sont exclus des exports et des sauvegardes du système. Un seul jeu d’identifiants par courtier couvre tous les comptes que vous ajoutez chez ce courtier.
- **Ce qui est enregistré dans votre dossier.** L’identifiant du compte, la dernière valeur lue et la date de lecture. Rien d’autre en provenance du courtier.
- **Les écrans du courtier peuvent changer.** Les étapes ci-dessous correspondent aux sites Web des courtiers en octobre 2026. Les courtiers renomment des menus et déplacent des réglages de temps à autre ; une étape peut donc sembler un peu différente lorsque vous la suivez. La documentation du courtier, indiquée dans chaque section, fait foi : si une étape ne correspond plus, cherchez le même terme sur la page du courtier.

## Interactive Brokers {#interactive-brokers}

Capital utilise le **Flex Web Service**, l’interface d’Interactive Brokers pour récupérer des rapports préconfigurés. Le jeton d’accès utilisé ne peut que générer et télécharger des rapports ; il ne permet ni de se connecter, ni de passer des ordres, ni de retirer des fonds. Capital demande la section **Net Asset Value (NAV) Summary in Base** d’une Activity Flex Query et prend le total de la date de rapport la plus récente : la valeur est donc la clôture du dernier jour ouvré.

### 1. Créer la Flex Query

1. Connectez-vous à [Client Portal](https://www.interactivebrokers.com/portal) et ouvrez **Performance & Reports → Flex Queries** (selon les comptes, le menu s’appelle *Reporting*).
2. Sous **Activity Flex Query**, appuyez sur **+** (Create, créer). Donnez un nom à la requête, par exemple `Capital`.
3. Dans la liste **Sections**, activez exactement ces deux sections et ces champs (sélectionner tous les champs d’une section fonctionne aussi) :
   - **Account Information** : *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base** : *Report Date*, *Total*.
4. Dans **Delivery Configuration**, réglez **Format** sur `XML` et **Period** sur `Last Business Day`. Les autres options peuvent garder leurs valeurs par défaut.
5. Enregistrez la requête, puis appuyez sur l’icône **i** (information) à côté d’elle et notez le **Query ID**, un nombre.

La requête doit couvrir un seul compte. Si vous avez des comptes liés ou une structure de conseiller, créez une requête par compte et ne sélectionnez que ce compte lors de sa création.

### 2. Activer le Flex Web Service et créer le jeton d’accès

1. Sur la même page **Flex Queries**, ouvrez **Flex Web Service Configuration**.
2. Activez **Flex Web Service Status** et enregistrez. Un jeton d’accès est créé.
3. Pour choisir la durée de validité du jeton d’accès, appuyez sur **Generate New Token** : de 6 heures à 1 an. Laissez **Valid for IP address** vide pour un téléphone, dont l’adresse change. Générer un nouveau jeton invalide le précédent.
4. Copiez le jeton d’accès.

### 3. Le connecter dans Capital

1. **Courtiers → Identifiants → Jeton d’accès : Interactive Brokers**, collez le jeton d’accès et enregistrez.
2. **Courtiers → +** : saisissez un nom, réglez **Courtier** sur Interactive Brokers, saisissez le **Flex Query id** et enregistrez.
3. Ouvrez la poche, **Ajouter une position**, réglez **Suivi** sur **Compte de courtage**, choisissez le compte et enregistrez.
4. Appuyez sur **Actualiser**. La première exécution prend jusqu’à une demi-minute, car le rapport est généré à la demande.

Lorsque le jeton d’accès expire, l’actualisation affiche *Le jeton d’accès a expiré ; générez-en un nouveau dans Client Portal* : générez un nouveau jeton d’accès et collez-le sous Identifiants. Interactive Brokers autorise une demande de rapport par seconde et dix par minute pour un même jeton, ce qu’une actualisation ne dépasse jamais.

Documentation d’Interactive Brokers : [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital appelle le **résumé de compte** (account summary) de l’API REST v20 d’OANDA et enregistre la NAV du compte (solde plus gains ou pertes latents) dans la devise du compte. Seuls les comptes réels **fxTrade** sont pris en charge ; les comptes de démonstration ne sont pas de l’épargne.

**Un jeton d’accès personnel OANDA n’est pas en lecture seule.** Il donne un accès complet à l’API pour tous les sous-comptes de votre identifiant, y compris pour trader. Capital n’appelle jamais que le résumé de compte, mais quiconque obtiendrait le jeton pourrait trader avec. Traitez-le comme un mot de passe : collez-le uniquement dans Capital et révoquez-le dans le portail OANDA si vous perdez votre téléphone.

### 1. Créer le jeton d’accès

1. Connectez-vous au portail de gestion de votre compte OANDA fxTrade.
2. Ouvrez **My Services → Manage API Access** (sur l’ancien portail : *My Account → My Services → Manage API Access*).
3. Acceptez la licence de l’API et appuyez sur **Generate**. Copiez le jeton d’accès ; OANDA ne l’affiche plus ensuite. Si vous le perdez, révoquez-le à cet endroit et générez-en un nouveau.

### 2. Trouver l’identifiant du compte

L’identifiant de compte v20 a la forme `001-001-1234567-001`, avec des traits d’union. Il figure dans le même portail à côté de chaque sous-compte, et sur la plateforme fxTrade dans les détails du compte.

### 3. Le connecter dans Capital

1. **Courtiers → Identifiants → Jeton d’accès : OANDA**, collez le jeton d’accès et enregistrez.
2. **Courtiers → +** : saisissez un nom, réglez **Courtier** sur OANDA, saisissez l’**Identifiant de compte OANDA** et enregistrez.
3. Ouvrez la poche, **Ajouter une position**, réglez **Suivi** sur **Compte de courtage**, choisissez le compte et enregistrez.
4. Appuyez sur **Actualiser**.

Un compte sur marge dont la NAV est négative est signalé comme une erreur au lieu d’être compté comme de l’épargne.

Documentation d’OANDA : [API REST v20](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital appelle le **résumé de compte** (account summary) de l’API publique de Trading 212 et enregistre la valeur totale du compte dans la devise principale du compte. L’API couvre les comptes **Invest** et **Stocks ISA** ; une paire de clés appartient à un seul compte et Capital conserve une seule paire de clés : il lit donc un seul compte Trading 212.

### 1. Créer la clé API

1. Dans l’application ou sur le site de Trading 212, ouvrez le menu (**☰**) → **Settings** → **API (Beta)** et acceptez l’avertissement sur les risques.
2. Appuyez sur **Generate API key**. Donnez-lui un nom, ne gardez que l’autorisation **Account data** (lecture) et choisissez l’accès *Unrestricted* pour les adresses IP (l’adresse d’un téléphone change).
3. Validez. Copiez les deux valeurs : l’**API Key** et l’**API Secret Key**. Le secret n’est affiché qu’une fois ; si vous le perdez, supprimez la clé et générez une nouvelle paire.

### 2. La connecter dans Capital

1. **Courtiers → Identifiants → Clé API : Trading 212** et **Secret API : Trading 212**, collez chaque valeur.
2. **Courtiers → +** : saisissez un nom, réglez **Courtier** sur Trading 212, saisissez le **Numéro de compte Trading 212** (l’identifiant de compte affiché dans l’application, chiffres uniquement) et enregistrez.
3. Ouvrez la poche, **Ajouter une position**, réglez **Suivi** sur **Compte de courtage**, choisissez le compte et enregistrez.
4. Appuyez sur **Actualiser**. Trading 212 autorise une demande de résumé toutes les 5 secondes.

Documentation de Trading 212 : [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) est un agrégateur : vous connectez une seule fois un compte de courtage à SnapTrade, et SnapTrade le lit pour vous. Il couvre de nombreux courtiers qui n’ont pas d’API publique. Capital utilise **SnapTrade Personal**, l’offre gratuite pour vos propres comptes, avec votre propre client id et votre propre consumer key. Sur cette offre, SnapTrade actualise les données environ une fois par jour.

Ce qui est envoyé : votre client id et, en guise de signature, rien du consumer key lui-même (les requêtes sont signées avec). C’est SnapTrade, et non Capital, qui détient la connexion à votre courtier ; ses conditions et sa politique de confidentialité s’appliquent à cette connexion.

### 1. Créer la clé API

1. Inscrivez-vous sur le [tableau de bord SnapTrade](https://dashboard.snaptrade.com/signup) et choisissez l’offre **Personal**.
2. Dans le tableau de bord, créez une clé API. Copiez le **client id** et le **consumer key** ; le consumer key n’est affiché qu’une fois.

### 2. La connecter dans Capital

1. **Courtiers → Identifiants → Identifiant client : SnapTrade** et **Clé consumer : SnapTrade**, collez chaque valeur.
2. **Courtiers → +** : saisissez un nom et réglez **Courtier** sur SnapTrade.
3. Appuyez sur **Connecter un courtier via SnapTrade**. La Connection Portal de SnapTrade s’ouvre dans le navigateur ; connectez-vous à votre courtier à cet endroit (le lien est valable 5 minutes). Revenez dans Capital.
4. Appuyez sur **Récupérer les comptes** et choisissez le compte ; son identifiant remplit le champ **Identifiant de compte SnapTrade**. Enregistrez.
5. Ouvrez la poche, **Ajouter une position**, réglez **Suivi** sur **Compte de courtage**, choisissez le compte et enregistrez, puis **Actualiser**.

Un compte que SnapTrade n’a pas encore fini de synchroniser signale *SnapTrade n’a pas encore de valeur totale pour ce compte* ; actualisez à nouveau plus tard.

Documentation de SnapTrade : [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Connecter un compte dans Capital {#connect}

Les sections ci-dessous indiquent comment créer l’identifiant chez chaque courtier. Dans Capital, les étapes sont les mêmes pour tous :

1. **Courtiers → Identifiants** : appuyez sur les boutons d’identifiants du courtier et collez chaque valeur.
2. **Courtiers → +** : saisissez un nom, choisissez le **Courtier**, puis appuyez sur **Récupérer les comptes**, choisissez le compte (ou saisissez son identifiant) et enregistrez.
3. Ouvrez une poche, **Ajouter une position**, réglez **Suivi** sur **Compte de courtage**, choisissez le compte et enregistrez. Appuyez sur **Actualiser**.

## Alpaca {#alpaca}

Alpaca émet un identifiant de clé et un secret par compte ; ils restent valables jusqu’à ce que vous les régénériez. Seuls les comptes réels sont lus : les clés d’un compte paper ne fonctionnent pas avec l’API réelle.

1. Connectez-vous au [tableau de bord Alpaca](https://app.alpaca.markets), passez à votre compte réel puis, sur la page d’accueil, sous **API Keys**, appuyez sur **Generate New Keys**.
2. Copiez l’**API Key ID** et la **Secret Key** ; le secret n’est affiché qu’une fois.
3. Dans Capital, collez-les comme **Clé API : Alpaca** et **Secret API : Alpaca**, puis suivez [Connecter un compte](#connect). **Récupérer les comptes** affiche le numéro de compte de la clé.

Documentation d’Alpaca : [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

Le jeton d’API de vos réglages Tradier n’expire jamais.

1. Connectez-vous à Tradier et ouvrez [Settings → API Access](https://web.tradier.com/user/api). Copiez l’**API Access Token** de votre compte de courtage (pas le jeton du bac à sable).
2. Dans Capital, collez-le comme **Jeton d’accès : Tradier**, puis suivez [Connecter un compte](#connect).

Documentation de Tradier : [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade utilise une autorisation OAuth personnelle (grant) : vous créez une application pour vous-même et une autorisation dont le jeton d’actualisation n’expire jamais. Capital l’échange contre un jeton d’accès de 15 minutes à chaque actualisation.

1. Sur [my.tastytrade.com](https://my.tastytrade.com), ouvrez **Manage → My Profile → API → OAuth Applications** et appuyez sur **+ New OAuth client**. Donnez-lui un nom, une URI de redirection HTTPS quelconque (par exemple `https://capital.fimych.dev`) et uniquement le périmètre (scope) **read**. Enregistrez et copiez le **Client Secret** ; il n’est affiché qu’une fois.
2. Appuyez sur **Manage** à côté de l’application, puis sur **Create Grant**, et copiez le **jeton d’actualisation** (refresh token).
3. Dans Capital, collez-les comme **Jeton d’actualisation : tastytrade** et **Secret client : tastytrade**, puis suivez [Connecter un compte](#connect).

Documentation de tastytrade : [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

L’Individual API de Public est conçue pour vos propres comptes. La clé secrète est de longue durée et révocable ; Capital l’échange contre un jeton d’accès de cinq minutes à chaque actualisation.

1. Dans l’application Web de Public, ouvrez la page **API** de vos réglages et générez une **clé secrète**.
2. Dans Capital, collez-la comme **Clé secrète : Public.com**, puis suivez [Connecter un compte](#connect).

Documentation de Public : [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

Les clés eToro sont de longue durée ; vous pouvez leur donner une date d’expiration et une liste d’adresses IP, et les rendre en lecture seule. Votre compte eToro doit être vérifié.

1. Dans eToro, ouvrez **Settings → Trading → API Key Management** et appuyez sur **Create New Key**. Choisissez l’environnement **Real**, l’autorisation **Read**, aucune liste d’adresses IP et, si vous le souhaitez, une date d’expiration. Confirmez avec le code reçu par SMS.
2. Copiez la **Public API Key** et la **User Key** ; la clé utilisateur n’est affichée qu’une fois.
3. Dans Capital, collez-les comme **Clé API publique : eToro** et **Clé utilisateur : eToro**, puis suivez [Connecter un compte](#connect). **Récupérer les comptes** liste vos comptes eToro de trading, de liquidités et autres.

Documentation d’eToro : [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

Le jeton de l’espace privé d’Indexa est en lecture seule. Il est lié à votre e-mail, à votre mot de passe et à votre appareil : après un changement de mot de passe, générez-le à nouveau.

1. Dans l’espace privé d’Indexa, ouvrez **Réglages utilisateur → Applications** et copiez le jeton.
2. Dans Capital, collez-le comme **Jeton d’accès : Indexa Capital**, puis suivez [Connecter un compte](#connect). Les comptes de retraite et d’investissement sont tous deux listés.

Indexa valorise les fonds une fois par jour ouvré ; la date d’observation est cette date de valorisation.

Documentation d’Indexa Capital : [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

La T-Invest API de T-Bank accepte un jeton que vous émettez dans les réglages d’investissement. Un jeton expire trois mois après sa dernière utilisation et doit être utilisé dans les sept jours suivant son émission ; une actualisation hebdomadaire le maintient actif. Choisissez un jeton en **lecture seule**.

1. Ouvrez les [réglages T-Invest](https://www.tbank.ru/invest/settings/) et émettez un **jeton d’API T-Invest** pour la bourse avec un accès en **lecture seule** (tous les comptes ou un seul). La confirmation des opérations par code doit être désactivée pour l’émettre. Copiez le jeton ; il n’est affiché qu’une fois.
2. Dans Capital, collez-le comme **Jeton d’accès : T-Invest**, puis suivez [Connecter un compte](#connect).

T-Bank sert cette API sous l’autorité de certification Russian Trusted Root CA, qu’Android n’inclut pas. Capital fait confiance à ce certificat uniquement pour l’adresse de la T-Invest API (`invest-public-api.tbank.ru`), et pour aucune autre connexion.

Documentation de T-Invest : [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR émet un jeton d’actualisation valable un an ; Capital l’échange contre un jeton d’accès de 30 minutes à chaque actualisation. ALOR ne propose pas de jeton en lecture seule : le jeton pourrait passer des ordres, Capital se contente de lire.

1. Connectez-vous au [portail développeur d’ALOR](https://alor.dev), liez votre compte de trading, ouvrez **API Access Tokens** et appuyez sur **Create Token**. Copiez le jeton d’actualisation.
2. Dans Capital, collez-le comme **Jeton d’actualisation : ALOR**, puis suivez [Connecter un compte](#connect). **Récupérer les comptes** liste les portefeuilles du compte (marché actions D…, marché des changes G…, dérivés 7500…) ; ajoutez-en un par portefeuille.

Documentation d’ALOR : [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Les clés Capital.com sont valables un an par défaut, ou jusqu’à la date que vous choisissez. Elles portent des droits de trading (Capital.com n’a pas de clés en lecture seule) ; Capital se contente de lire. Une clé a son propre mot de passe, qui n’est pas celui de votre compte.

1. Activez l’authentification à deux facteurs, puis ouvrez **Settings → API integrations** et appuyez sur **Generate API key**. Donnez-lui un libellé et un **mot de passe personnalisé**, gardez ou réglez l’expiration, et confirmez avec le code 2FA. Copiez la clé ; elle n’est affichée qu’une fois.
2. Dans Capital, collez **Clé API : Capital.com**, votre e-mail de connexion comme **E-mail de connexion : Capital.com** et le mot de passe personnalisé comme **Mot de passe de la clé API : Capital.com**, puis suivez [Connecter un compte](#connect). Seuls les comptes réels sont lus.

Documentation de Capital.com : [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) connecte des banques, des plateformes d’investissement et des régimes KiwiSaver néo-zélandais ; une application personnelle gratuite lit vos propres comptes. Akahu actualise les données environ une fois par jour.

1. Inscrivez-vous sur [my.akahu.nz](https://my.akahu.nz) et connectez vos fournisseurs (par exemple Sharesies, Hatch, Kernel, Simplicity, Milford ou votre régime KiwiSaver).
2. Ouvrez la page **Developers**, acceptez les conditions pour développeurs et copiez l’**App ID Token** et le **User Access Token**.
3. Dans Capital, collez-les comme **Jeton d’ID d’application : Akahu** et **Jeton d’accès utilisateur : Akahu**, puis suivez [Connecter un compte](#connect).

Documentation d’Akahu : [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Courtiers populaires par marché {#by-market}

Comment se connecter aux courtiers les plus utilisés sur les marchés des langues de Capital, en octobre 2026. *Direct* renvoie à une section ci-dessus ; *SnapTrade* signifie via [SnapTrade](#snaptrade) ; sinon, la raison pour laquelle le courtier ne peut pas être lu, et le solde peut être conservé comme position **Manuelle**.

| Marché | Courtier | Comment |
|---|---|---|
| États-Unis | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Direct |
| États-Unis | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| États-Unis | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | Pas d’API publique |
| Canada | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Canada | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | Pas d’API publique |
| Royaume-Uni et Irlande | Trading 212, eToro, Interactive Brokers | Direct |
| Royaume-Uni et Irlande | AJ Bell | SnapTrade |
| Royaume-Uni et Irlande | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | Pas d’API publique |
| Royaume-Uni et Irlande | IG | Impossible : chaque session exige le mot de passe du compte |
| Europe | Indexa Capital (Espagne), eToro, Trading 212, Interactive Brokers | Direct |
| Europe | DEGIRO, BUX | SnapTrade |
| Europe | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | Pas d’API publique pour les investissements |
| Europe | XTB | Impossible : l’API a été fermée en mars 2025 |
| Europe | Saxo, comdirect | Impossible : uniquement des jetons de courte durée ou des sessions TAN |
| Europe | Bitpanda, Freedom24 | Impossible : l’API ne renvoie pas la valeur totale du compte |
| Russie et Kazakhstan | T-Invest, ALOR | Direct |
| Russie et Kazakhstan | BCS | Impossible : pas de valeur totale, et son jeton expire après 90 jours |
| Russie et Kazakhstan | Finam | Pas encore : la devise de la valeur du compte n’est pas documentée |
| Russie et Kazakhstan | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | Pas d’API publique, ou pas de valeur totale dans celle-ci |
| Inde | Zerodha, Upstox | SnapTrade (les règles de la SEBI mettent fin chaque jour aux sessions d’API ; la connexion doit donc être renouvelée souvent) |
| Inde | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Impossible : les règles de la SEBI mettent fin chaque jour à toute session d’API |
| Pakistan et Bangladesh | Tous les courtiers de la bourse | Pas d’API publique |
| Chine, Hong Kong et Taïwan | moomoo | SnapTrade |
| Chine, Hong Kong et Taïwan | Futu, Tiger Brokers, Longbridge | Pas encore : durée de vie des clés ou format de réponse mal documentés, ou clés non limitables à la lecture |
| Chine, Hong Kong et Taïwan | East Money, Huatai, CITIC, Yuanta, Fubon | Pas d’API Web publique (terminaux de bureau ou SDK à certificat uniquement) |
| Japon | OANDA Japan (comptes éligibles à l’accès API) | Direct, comme OANDA |
| Japon | SBI Securities, Rakuten Securities, Monex, Matsui | Pas d’API publique |
| Australie et Nouvelle-Zélande | CommSec, Stake | SnapTrade |
| Australie et Nouvelle-Zélande | Sharesies, Hatch, Kernel, Simplicity, régimes KiwiSaver | Akahu (comptes néo-zélandais) |
| Moyen-Orient et Afrique | eToro | Direct |
| Moyen-Orient et Afrique | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | Pas d’API publique pour les particuliers |
| Asie du Sud-Est | Stockbit, Ajaib, Bibit, IPOT, VPS | Pas d’API publique |
| Asie du Sud-Est | SSI, TCBS, DNSE | Impossible : jetons de 8 heures avec code à usage unique, ou solde de liquidités uniquement |
| Amérique latine | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | Pas d’API publique pour les particuliers, ou connexions par mot de passe uniquement |
| Forex et CFD | OANDA, Capital.com | Direct |
| Forex et CFD | Courtiers MetaTrader (XM, Exness, Pepperstone, IC Markets, Admirals) | Impossible : pas d’accès en lecture HTTPS |
| Forex et CFD | Courtiers cTrader, FXCM, Forex.com | Impossible : enregistrement d’application, API obsolète ou connexions par mot de passe |

## Autres courtiers {#other-brokers}

Capital se connecte uniquement à des interfaces qui fonctionnent depuis un téléphone en HTTPS, avec un jeton d’accès que vous pouvez créer vous-même et qui permettent de lire sans pouvoir trader. Cela exclut, pour l’instant :

- Les comptes **MetaTrader 4 et 5**. Le mot de passe investisseur donne un accès en lecture seule, mais uniquement dans le terminal MetaTrader ; les courtiers ne publient aucune interface HTTPS pour cela.
- Les courtiers dont l’API exige un programme exécuté sur un ordinateur (par exemple la passerelle Client Portal Web API d’Interactive Brokers ; Capital utilise à la place le Flex Web Service) ou l’enregistrement d’une application OAuth.
- Les courtiers dont l’API ne délivre que des jetons de courte durée via OAuth, par exemple Saxo Bank (les jetons d’accès durent 20 minutes ; le jeton de 24 heures du portail développeur ne couvre que l’environnement de simulation).
- Les banques et courtiers sans API publique.

Beaucoup de ces courtiers sont couverts par [SnapTrade](#snaptrade). Sinon, saisissez le solde comme une position **Manuel** et mettez le chiffre à jour lorsque vous consultez votre relevé. Si votre courtier propose un point d’accès HTTPS simple, à jeton, qui lit la valeur du compte, [ouvrez un ticket]({{ site.repo }}/issues) avec un lien vers sa documentation. Chaque courtier dans Capital est un petit plugin ; les développeurs peuvent en ajouter un en suivant [le guide des plugins]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Messages et que faire {#messages}

| Message | Que faire |
|---|---|
| *Interactive Brokers nécessite ses identifiants sur l’écran Courtiers* / *OANDA nécessite ses identifiants …* | Collez le jeton, la clé ou le client id sous Identifiants dans l’onglet Courtiers. |
| *Le jeton d’accès a expiré ; générez-en un nouveau dans Client Portal* | Générez un nouveau jeton d’accès Flex Web Service et collez-le. |
| *Jeton d’accès invalide* | Copiez à nouveau le jeton d’accès ; un nouveau jeton remplace l’ancien. |
| *Le jeton d’accès est limité à une autre adresse IP* | Générez le jeton d’accès sans restriction d’adresse IP. |
| *Flex Query id invalide* | Vérifiez le nombre ; la requête doit être une Activity Flex Query de cet identifiant. |
| *Ajoutez à la Flex Query la section Net Asset Value (NAV) Summary in Base avec Report Date et Total* | Modifiez la requête et ajoutez la section et les champs. |
| *Ajoutez à la Flex Query le champ Currency de Account Information* | Modifiez la requête et ajoutez le champ. |
| *La requête a renvoyé N comptes ; créez une Flex Query par compte* | Créez une requête qui couvre un seul compte. |
| *Le relevé n’est pas encore prêt ; actualisez à nouveau dans une minute* | Interactive Brokers génère encore le rapport ; actualisez à nouveau. |
| *Accès refusé ; vérifiez la clé du fournisseur ou le quota* | Le jeton OANDA est erroné ou révoqué, ou la paire de clés Trading 212 est erronée ou n’a pas l’autorisation Account data. |
| *SnapTrade n’a pas encore de valeur totale pour ce compte ; synchronisez la connexion et réessayez* | SnapTrade n’a pas encore synchronisé le courtier ; actualisez à nouveau plus tard. |
| *Aucun compte connecté pour l’instant. Connectez d’abord un courtier via SnapTrade.* | Ouvrez la Connection Portal depuis l’éditeur et connectez un courtier. |
| *La valeur de compte négative … n’est pas prise en charge* | Le compte est à découvert ; il n’ajoute rien à votre épargne. |
| *tastytrade a rejeté le jeton d’actualisation ou le secret client ; créez une nouvelle autorisation (grant)* | Créez une nouvelle autorisation pour l’application et collez son jeton d’actualisation ; vérifiez le secret client. |
| *Capital.com n’a pas ouvert de session ; vérifiez la clé API, l’e-mail de connexion et le mot de passe de la clé* | La clé, l’e-mail ou le mot de passe personnalisé de la clé est erroné, ou la clé a expiré. |
| *Compte introuvable ; choisissez-le à nouveau* | Le courtier ne liste plus ce compte ; modifiez-le et choisissez-le dans **Récupérer les comptes**. |

La valeur précédente reste affichée après chacun de ces messages, signalée comme obsolète.
