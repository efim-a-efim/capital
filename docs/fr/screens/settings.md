---
layout: screen
lang: fr
base: "/fr"
key: "screens/settings"
screen: settings
title: Paramètres
---
# Paramètres

**Ce que c’est.** Tout ce qui n’est pas une donnée : préférences, sources de données, sécurité et dossier. S’ouvre avec l’engrenage de la barre supérieure ; sur les grands écrans, c’est un onglet du rail de navigation.

**Préférences.** Devise par défaut et thème (Système, Clair, Sombre). La devise par défaut sert à valoriser la Synthèse et est proposée pour les nouvelles poches et les nouveaux objectifs ; les données existantes conservent leur devise. **Langue** change immédiatement la langue de l’interface ; *Langue du système* suit celle de l’appareil.

**Fournisseurs de données gratuits.** Une ligne par type de données, chacune avec l’opérateur utilisé : soldes BTC, ETH, TON, TRX ; listes de jetons ETH, TON, TRX ; cours des cryptomonnaies ; taux de change des devises. Touchez une ligne pour choisir un autre opérateur, mettre la recherche de jetons sur **Désactivé** pour une blockchain, ou saisir une clé API facultative. Les clés sont enregistrées chiffrées sur l’appareil et envoyées uniquement à l’opérateur qui les a émises. **Tester les sources / actualiser le portefeuille** interroge chaque opérateur avec les actifs que vous détenez réellement et signale ce qui a échoué. Aucun opérateur n’est jamais remplacé à votre insu.

**Courtiers.** Les comptes de courtage et leurs identifiants ont leur propre écran : [Courtiers]({{ page.base }}/screens/brokers).

**Cours et fraîcheur des données.** Chaque cours en cache avec sa date d’observation et de récupération. Les cours obsolètes restent utilisables et sont signalés dans la Synthèse.

**Sécurité.** Le **Chiffrement** chiffre tous les fichiers du dossier avec un mot de passe ; le désactiver les déchiffre. Lorsque le chiffrement est activé, vous pouvez **Définir un PIN**, activer **Utiliser la biométrie**, choisir **Verrouiller après un délai en arrière-plan** et **Changer le mot de passe**. Il n’existe aucune récupération du mot de passe. Dix PIN erronés suppriment le PIN ; le mot de passe fonctionne toujours. Voir [Sécurité]({{ page.base }}/manual#security).

**Stockage.** Le dossier actuel et sa révision. **Reconnecter / ouvrir un dossier** relance le sélecteur de dossiers ; **Recharger les fichiers locaux** relit le dossier, par exemple après que votre outil de synchronisation a apporté des modifications ; **Exporter une sauvegarde** écrit un seul fichier portable (en clair lorsque le chiffrement est désactivé, et signalé comme tel) ; **Restaurer une sauvegarde** valide un fichier avant de remplacer les données et conserve les instantanés existants.

**Sources / crédits.** Des liens vers le site de chaque opérateur.

**Mentions légales.** Des liens vers la [Politique de confidentialité]({{ page.base }}/privacy), vers les déclarations « [Sécurité des données]({{ page.base }}/data-safety) » et « [Fonctionnalités financières]({{ page.base }}/financial-features) » de ce site, dans la langue de l’interface. La version de l’application et le numéro de build figurent en bas de page.
