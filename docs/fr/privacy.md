---
layout: default
lang: fr
base: "/fr"
key: "privacy"
title: Politique de confidentialité
class: doc
---
# Politique de confidentialité

<p class="meta">Capital pour Android (package <code>dev.capital</code>) · Développeur : {{ site.developer }} · En vigueur à partir du 30 septembre 2026</p>

## Résumé

- Capital n’a ni comptes utilisateur, ni outils d’analyse, ni publicité, ni rapports de plantage, ni serveurs exploités par le développeur. Le développeur ne reçoit jamais vos données.
- Vos données financières sont enregistrées uniquement sur votre appareil, dans un dossier de votre choix. Vous pouvez les chiffrer avec un mot de passe.
- Le seul trafic réseau est constitué des requêtes que l’application envoie, à votre demande, aux opérateurs de données de cours et de blockchain que vous choisissez dans les Paramètres. Ces requêtes contiennent les adresses publiques de portefeuilles crypto, les contrats de jetons et les codes de devise que vous suivez, ainsi que toute clé API que vous avez saisie pour cet opérateur.

## Ce que l’application enregistre sur votre appareil

**Dans le dossier que vous choisissez.** Poches, positions, adresses de portefeuilles crypto, objectifs, connexions, versements prévus, cours en cache et les paramètres liés à ces données. Les fichiers sont en texte clair, sauf si vous activez le chiffrement (Paramètres → Sécurité). Lorsque le chiffrement est activé, chaque fichier est chiffré en AES-256-GCM avec une clé dérivée de votre mot de passe par Argon2id. Il n’existe aucune récupération du mot de passe.

**Dans le stockage privé de l’application** (inaccessible aux autres applications) :

| Élément | Finalité |
|---|---|
| Autorisation d’accès au dossier sélectionné | Rouvrir le dossier au lancement suivant |
| Clés API de fournisseurs que vous avez saisies | Envoyées uniquement à l’opérateur qui les a émises ; chiffrées avec une clé conservée dans Android Keystore ; exclues des instantanés, des exports et des sauvegardes du système |
| Paramètres de verrouillage | Déverrouiller le dossier chiffré sans le mot de passe : une copie de la clé des données, chiffrée avec une clé dérivée de votre PIN et liée à Android Keystore. Le PIN lui-même n’est pas enregistré |
| Choix de la langue et du thème | Préférences d’interface |

La sauvegarde Android et le transfert d’appareil à appareil sont désactivés pour l’application : le système ne copie donc rien de tout cela chez Google ni sur un autre appareil.

## Ce qui quitte votre appareil

Capital contacte uniquement les opérateurs que vous choisissez dans les Paramètres, uniquement via HTTPS, et uniquement lorsque vous actualisez ou testez les sources. Chaque requête reçoit sa réponse puis est abandonnée ; l’application conserve dans votre dossier les soldes et les cours renvoyés, pas la requête.

| Données envoyées | Destinataire | Motif |
|---|---|---|
| Adresses publiques de portefeuilles crypto que vous avez ajoutées | L’opérateur de données blockchain sélectionné pour cette blockchain | Lire le solde et les jetons détenus à l’adresse |
| Adresses de contrats de jetons et identifiants d’actifs | L’opérateur de cours crypto que vous avez sélectionné | Obtenir le cours des actifs |
| Codes de devise | L’opérateur de taux de change que vous avez sélectionné | Convertir entre devises |
| La clé API que vous avez saisie pour un opérateur | Cet opérateur uniquement | Vous authentifier sur votre propre compte chez lui |

Chaque opérateur voit aussi votre adresse IP, comme pour toute requête sur Internet. Les opérateurs sont indépendants du développeur et traitent la requête selon leurs propres conditions et politiques de confidentialité, accessibles depuis Paramètres → Sources / crédits dans l’application :

| Données | Opérateurs |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum et jetons ERC-20 | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON et jettons | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON et jetons TRC-20 | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Cours des cryptomonnaies | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Taux de change des devises | [Frankfurter](https://frankfurter.dev), [Banque centrale européenne](https://www.ecb.europa.eu) |

Rien n’est envoyé ailleurs. Aucune donnée n’est vendue, partagée à des fins publicitaires ni utilisée pour établir des profils. Les requêtes sur des blockchains publiques révèlent que l’adresse que vous suivez intéresse quelqu’un situé à votre adresse IP ; utilisez un VPN si cela compte pour vous.

## Ce que l’application ne fait jamais

- Elle ne demande, n’enregistre ni ne transmet jamais de clés privées ni de phrases de récupération. Elle ne peut ni signer ni envoyer de transactions.
- Elle ne transfère jamais d’argent. Les affectations aux objectifs sont des calculs qui vous sont affichés, rien de plus.
- Elle ne contacte jamais le développeur. Il n’y a ni télémétrie, ni vérification des mises à jour intégrée à l’application, ni notifications push.

## Autorisations

| Autorisation | Utilisation |
|---|---|
| Internet | Requêtes vers les opérateurs listés ci-dessus |
| Accès au dossier | Accordé par vous, via le sélecteur de dossiers d’Android, pour le dossier que vous sélectionnez ; l’application ne peut pas lire d’autres dossiers |
| Biométrie | Déverrouillage par empreinte digitale ou reconnaissance faciale via la boîte de dialogue d’Android ; l’application reçoit uniquement un succès ou un échec, jamais de données biométriques |

## Synchronisation et sauvegardes

Capital ne synchronise rien lui-même. Si vous placez le dossier sous un outil de synchronisation (Syncthing, Nextcloud, Google Drive…), les conditions de confidentialité de cet outil s’appliquent aux copies qu’il crée. Les fichiers sont en texte clair tant que le chiffrement n’est pas activé ; les copies en clair réalisées avant l’activation du chiffrement restent lisibles par quiconque les détient.

**Exporter une sauvegarde**, dans les Paramètres, écrit un fichier unique à l’emplacement de votre choix. Il contient les mêmes données et n’est protégé que dans la mesure où cet emplacement l’est.

## Suppression de vos données

Supprimez le dossier que vous avez choisi (ainsi que toutes les copies créées par votre outil de synchronisation) et désinstallez l’application. La désinstallation efface le stockage privé de l’application, y compris les clés de fournisseurs et les paramètres de verrouillage. Le développeur ne détient aucune donnée à supprimer et ne peut rien supprimer à votre place. Les opérateurs que vous avez interrogés peuvent conserver des journaux de requêtes selon leurs propres règles de conservation.

## Enfants

Capital est un outil de finances personnelles destiné aux adultes. Il ne s’adresse pas aux enfants de moins de 13 ans et ne collecte sciemment aucune donnée les concernant.

## Modifications de cette politique

La version en vigueur est toujours disponible sur [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Les modifications importantes sont indiquées dans les notes de version de la version qui les introduit.

## Contact

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Suivi des problèmes]({{ site.repo }}/issues)
