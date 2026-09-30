---
layout: default
lang: fr
base: "/fr"
key: "data-safety"
title: Déclaration relative à la sécurité des données
class: doc
---
# Déclaration relative à la sécurité des données

<p class="meta">Réponses au formulaire de la Google Play Console (Policy and programs → App content → Data safety ; Règles et programmes → Contenu de l’application → Sécurité des données), avec la justification de chacune. Vérifiées pour la version 2.2.1 le 30 septembre 2026. La <a href="{{ page.base }}/privacy">Politique de confidentialité</a> présente les mêmes faits à l’intention des utilisateurs.</p>

## Traitement des données par l’application

Capital n’a pas de backend. Tout ce que l’utilisateur saisit reste dans un dossier sur l’appareil. Les seules données qui quittent l’appareil sont celles que l’application envoie, à la demande de l’utilisateur, aux opérateurs de données tiers qu’il sélectionne dans les Paramètres : adresses publiques de portefeuilles crypto, identifiants de contrats de jetons, codes de devise et toute clé API que l’utilisateur a saisie pour cet opérateur. Les opérateurs répondent à la requête ; l’application enregistre localement les soldes et les cours renvoyés et ne conserve aucune copie de la requête. Aucun SDK de l’application n’envoie de données à son éditeur : les dépendances se limitent à AndroidX, Kotlin, OkHttp et Bouncy Castle.

Google Play considère des données comme *collectées* dès qu’elles sont transmises hors de l’appareil, même sans serveur du développeur et même si le traitement est éphémère ; la déclaration n’est donc pas « aucune donnée collectée ». Elle porte sur un seul type de données, éphémère et facultatif.

## Réponses au formulaire

### Vue d’ensemble

| Question | Réponse |
|---|---|
| Does your app collect or share any of the required user data types? (Votre application collecte-t-elle ou partage-t-elle l’un des types de données utilisateur requis ?) | **Yes** (Oui) |
| Is all of the user data collected by your app encrypted in transit? (Toutes les données utilisateur collectées par votre application sont-elles chiffrées lors de leur transfert ?) | **Yes** (Oui) — HTTPS uniquement ; le trafic en clair est désactivé dans le manifeste |
| Do you provide a way for users to request that their data is deleted? (Proposez-vous aux utilisateurs un moyen de demander la suppression de leurs données ?) | **Yes** (Oui) — rien n’est conservé une fois la requête terminée, ce qui satisfait la règle « deleted within 90 days of collection » (suppression dans les 90 jours suivant la collecte) exigée pour le badge. Les utilisateurs suppriment les données présentes sur l’appareil en supprimant le dossier et en désinstallant l’application ; voir la Politique de confidentialité. |

### Types de données

Sélectionnez exactement un type.

| Catégorie | Type de données | Collectées | Partagées | Éphémère | Obligatoire ou facultatif | Finalités |
|---|---|---|---|---|---|---|
| Financial info (Informations financières) | Other financial info (Autres informations financières) | Yes (Oui) | No (Non) | **Yes** (Oui) | **Optional** (Facultatif) | App functionality (Fonctionnement de l’application) |

Ce que couvre ce type : les adresses blockchain publiques que l’utilisateur suit, les contrats de jetons trouvés à ces adresses et les codes de devise des positions de l’utilisateur. Ils sont transmis à l’opérateur de données choisi par l’utilisateur afin d’obtenir les soldes et les cours, conservés en mémoire le temps de la requête, puis supprimés.

Pourquoi **non partagées** : le transfert va directement de l’appareil à l’opérateur choisi par l’utilisateur, lors d’une actualisation que l’utilisateur a lui-même lancée, après que l’application lui a indiqué dans les Paramètres quel opérateur sera interrogé et que la requête révèle l’adresse et l’adresse IP à cet opérateur. C’est l’exception « user-initiated action where the user reasonably expects the data to be shared » (action initiée par l’utilisateur, qui s’attend raisonnablement à ce que les données soient partagées). Le développeur ne reçoit rien et n’a recours à aucun prestataire de services.

Pourquoi **facultatif** : l’application est entièrement utilisable avec des positions manuelles uniquement. Les adresses et les clés API sont saisies par choix.

Les clés API saisies par l’utilisateur sont envoyées uniquement à l’opérateur qui les a émises. Ce sont les identifiants de l’utilisateur pour le service propre à cet opérateur ; elles ne sont pas déclarées comme un type de données utilisateur distinct. Si un examinateur pose la question, décrivez-les comme ci-dessus.

### Types **non** collectés

Toutes les autres catégories sont à « No » (Non) : aucune position géographique, aucune information personnelle, aucun contact, aucun message, aucune photo ni vidéo, aucun fichier ni document, aucune activité dans l’application, aucune navigation Web, aucune information ni performance de l’application (pas de journaux de plantage, pas de diagnostics), aucun identifiant d’appareil ni autre identifiant. Les adresses IP parviennent aux opérateurs dans le cadre de toute requête HTTPS et ne sont utilisées par l’application à aucune fin.

Les données financières de l’utilisateur (positions, objectifs, versements prévus) sont traitées uniquement sur l’appareil et n’entrent pas dans le champ du formulaire.

### Pratiques de sécurité

| Élément | Réponse |
|---|---|
| Independent security review (MASA) (Examen de sécurité indépendant) | No (Non) |
| Committed to follow the Families policy (Engagement à respecter le règlement relatif aux familles) | No (Non) — ce n’est pas une application pour enfants |

## Déclarations associées sur la page App content (Contenu de l’application)

| Déclaration | Réponse |
|---|---|
| Privacy policy URL (URL des règles de confidentialité) | `{{ site.url }}/privacy` |
| Ads (Annonces) | Non, l’application ne contient aucune publicité |
| App access (Accès à l’application) | Toutes les fonctionnalités sont disponibles sans accès spécial. Pas de connexion. Les clés API des fournisseurs sont facultatives ; chaque fournisseur dispose d’une option par défaut sans clé. |
| Content rating (IARC) (Classification du contenu) | Questionnaire Utility / productivity (Utilitaires / productivité) ; pas de violence, de contenu sexuel, de jeux d’argent, de substances réglementées, d’interaction entre utilisateurs ni de partage de position. Résultat attendu : Everyone (Tout public) / PEGI 3. |
| Target audience and content (Public cible et contenu) | 18 ans et plus (outil de finances personnelles ; non conçu pour les enfants) |
| News app (Application d’actualités) | Non |
| COVID-19 contact tracing and status (Recherche des contacts et statut COVID-19) | Non |
| Data safety (Sécurité des données) | Voir ci-dessus |
| Government app (Application gouvernementale) | Non |
| Financial features (Fonctionnalités financières) | Voir la [Déclaration relative aux fonctionnalités financières]({{ page.base }}/financial-features) |
| Health apps (Applications de santé) | Aucune fonctionnalité de santé |

## Ce qu’il faut mettre à jour quand l’application évolue

Revérifiez cette page lorsqu’une version ajoute des outils d’analyse, des rapports de plantage, des comptes, un serveur exploité par le développeur, un nouveau SDK disposant d’un accès réseau, ou un partage sur l’appareil avec une autre application. Chacun de ces changements modifie le formulaire.
