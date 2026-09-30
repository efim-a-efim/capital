---
layout: default
lang: fr
base: "/fr"
key: "financial-features"
title: Déclaration relative aux fonctionnalités financières
class: doc
---
# Déclaration relative aux fonctionnalités financières

<p class="meta">Réponses au formulaire de la Google Play Console (Policy and programs → App content → Financial features ; Règles et programmes → Contenu de l’application → Fonctionnalités financières), avec leur justification. Vérifiées pour la version 2.2.1 le 30 septembre 2026.</p>

## Réponse au formulaire

**Select all of the financial features the app provides** (Sélectionnez toutes les fonctionnalités financières proposées par l’application) : **The app does not provide any financial features** (L’application ne propose aucune fonctionnalité financière).

## Justification

Capital est une application de suivi de l’épargne personnelle. Elle enregistre ce que l’utilisateur possède déjà et montre comment cette épargne se répartit entre les objectifs qu’il s’est lui-même fixés. Pour chaque fonctionnalité du formulaire :

| Fonctionnalité du formulaire | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later (prêteur direct de prêts personnels, intermédiaire de prêts, prêts sur salaire, ligne de crédit, avances sur salaire, microfinance, paiement différé) | Aucun prêt, sous quelque forme que ce soit |
| Banking (Services bancaires) | Aucun compte, aucun dépôt, aucun accès à un compte. Les soldes bancaires sont saisis par l’utilisateur |
| Mobile payments and digital wallets, money transfer and wire services (paiements mobiles et portefeuilles numériques, transferts d’argent et virements) | Ne peut ni envoyer, ni recevoir, ni détenir d’argent. L’affectation aux objectifs est un calcul affiché à l’écran ; elle ne déplace rien |
| Cryptocurrency wallet (Portefeuille de cryptomonnaies) | Lit le solde des adresses publiques que l’utilisateur colle. L’application ne détient jamais de clés privées ni de phrases de récupération et ne peut ni signer ni diffuser de transactions : ce n’est donc pas un portefeuille |
| Cryptocurrency exchange (Plateforme d’échange de cryptomonnaies) | Aucun trading, aucun routage d’ordres, aucune conversion de monnaie fiduciaire en cryptomonnaie |
| Rewards and incentives, crowdfunding and chit funds, prediction markets (récompenses et incitations, financement participatif et tontines, marchés de prédiction) | Aucune |
| Credit monitoring and reporting (Surveillance et rapports de crédit) | Aucune |
| Financial advice (Conseil financier) | Aucun. La projection se contente de calculer à partir des chiffres de l’utilisateur (« Objectif atteint le … grâce aux versements prévus ») ; elle ne recommande aucun produit, aucun actif ni aucune action. Le calculateur de rééquilibrage liste les achats nécessaires pour atteindre les pourcentages que l’utilisateur a lui-même définis |
| Insurance (Assurance) | Aucune |
| Achats intégrés, dons | Aucun n’est traité par l’application. Un écran de dons affiche les adresses publiques de portefeuille crypto du développeur (les mêmes que sur ce site) ; le transfert s’effectue dans l’application de portefeuille de l’utilisateur, ne débloque rien et reste invisible pour l’application |

L’application ne propose par ailleurs aucun achat intégré ni aucune fonctionnalité payante. La version Google Play est compilée sans l’écran de dons.

## Si l’examinateur n’est pas d’accord

Si l’examen Google Play classe malgré tout l’application comme proposant une fonctionnalité financière, l’option la plus proche est **Other** (Autre), avec cette description :

> Read-only personal savings tracker. Users type in their balances or paste public blockchain addresses; the app fetches balances and market prices from third-party data sources and shows how the savings cover the user's own goals. No custody, no keys, no transactions, no lending, no trading, no advice.
>
> (Application de suivi de l’épargne personnelle en lecture seule. Les utilisateurs saisissent leurs soldes ou collent des adresses blockchain publiques ; l’application récupère les soldes et les cours du marché auprès de sources de données tierces et montre comment l’épargne couvre les objectifs de l’utilisateur. Aucune conservation d’actifs, aucune clé, aucune transaction, aucun prêt, aucun trading, aucun conseil.)

Les exigences propres à certains pays pour les applications de prêt personnel, ainsi que les questions relatives aux cryptomonnaies pour les États-Unis, ne s’appliquent pas, car aucune de ces fonctionnalités n’est sélectionnée.

## Autres éléments sur lesquels un examinateur peut s’interroger

- Les données de marché proviennent d’opérateurs tiers choisis par l’utilisateur (voir la [Politique de confidentialité]({{ page.base }}/privacy)). L’application affiche le nom et le site de l’opérateur dans les Paramètres.
- Les requêtes sur les portefeuilles crypto utilisent des API blockchain publiques, en lecture seule.
- L’application fonctionne entièrement sur l’appareil et ne dispose d’aucun serveur exploité par le développeur.
