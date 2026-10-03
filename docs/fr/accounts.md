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

Capital se connecte uniquement à des interfaces dont les identifiants sont de longue durée : un jeton ou une clé que vous créez une fois et qui reste valable jusqu’à ce que vous le révoquiez (ou, pour Interactive Brokers, jusqu’à l’expiration que vous avez choisie, d’un an au maximum). Pris en charge actuellement :

| Courtier | Interface utilisée | Ce qui est lu |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (récupération de rapports uniquement) | Valeur nette d’inventaire du dernier jour ouvré, dans la devise de référence du compte |
| [OANDA](#oanda) | API REST v20, comptes fxTrade réels | Valeur nette d’inventaire au moment de l’actualisation, dans la devise du compte |
| [Trading 212](#trading-212) | API publique, comptes Invest et Stocks ISA | Valeur totale du compte au moment de l’actualisation, dans la devise principale du compte |
| [SnapTrade](#snaptrade) | SnapTrade Personal, un agrégateur qui couvre de nombreux courtiers | Valeur totale du compte telle que le courtier la déclare à SnapTrade, dans la devise du compte |

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

## Autres courtiers {#other-brokers}

Capital se connecte uniquement à des interfaces qui fonctionnent depuis un téléphone en HTTPS, avec un jeton d’accès que vous pouvez créer vous-même et qui permettent de lire sans pouvoir trader. Cela exclut, pour l’instant :

- Les comptes **MetaTrader 4 et 5**. Le mot de passe investisseur donne un accès en lecture seule, mais uniquement dans le terminal MetaTrader ; les courtiers ne publient aucune interface HTTPS pour cela.
- Les courtiers dont l’API exige un programme exécuté sur un ordinateur (par exemple la passerelle Client Portal Web API d’Interactive Brokers ; Capital utilise à la place le Flex Web Service) ou l’enregistrement d’une application OAuth.
- Les courtiers dont l’API ne délivre que des jetons de courte durée via OAuth, par exemple Saxo Bank (les jetons d’accès durent 20 minutes ; le jeton de 24 heures du portail développeur ne couvre que l’environnement de simulation).
- Les banques et courtiers sans API publique.

Beaucoup de ces courtiers sont couverts par [SnapTrade](#snaptrade). Sinon, saisissez le solde comme une position **Manuel** et mettez le chiffre à jour lorsque vous consultez votre relevé. Si votre courtier propose un point d’accès HTTPS simple, à jeton, qui lit la valeur du compte, [ouvrez un ticket]({{ site.repo }}/issues) avec un lien vers sa documentation.

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

La valeur précédente reste affichée après chacun de ces messages, signalée comme obsolète.
