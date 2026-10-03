---
layout: screen
lang: fr
base: "/fr"
key: "screens/bucket"
screen: bucket
title: Poche
---
# Poche

**Ce que c’est.** Une poche avec ses positions. On y accède en touchant une carte dans l’onglet [Poches]({{ page.base }}/screens/buckets) ; **← Toutes les poches** permet de revenir en arrière.

**En-tête.** La valeur de la poche, puis deux chiffres qui n’ont de sens qu’ensemble : **Affecté**, la part réclamée par les objectifs connectés, et **Disponible**, le reste. **Modifier la poche** donne accès au nom, à la devise et aux options du mode portefeuille. **Supprimer la poche** supprime la poche et ses positions après confirmation.

**Positions.** Chaque position affiche son nom, sa valeur dans la devise de la poche, son mode de suivi (*Manuel*, *Portefeuille crypto* ou *Compte de courtage*), la quantité native, la date d’observation de la valeur et la date de sa dernière récupération. **Modifier / déplacer** la modifie ou la déplace vers une autre poche ; **Supprimer** la supprime.

**Ajouter une position** ouvre l’éditeur de position :

- **Manuel** : un nom, un code de devise ou d’actif et la quantité. Utilisez ce mode pour tout ce que l’application ne peut pas lire.
- **Portefeuille crypto** : choisissez la blockchain (BTC, ETH, TON, TRX) et collez une adresse publique. L’application lit le solde natif à l’actualisation et, sur ETH, TON et TRX, les jetons fongibles détenus à cette adresse. Rouvrez l’éditeur et appuyez sur **Récupérer les jetons** pour les voir et désactiver ceux que vous ne voulez pas compter.
- **Compte de courtage** : choisissez l’un des comptes ajoutés sur l’écran [Courtiers]({{ page.base }}/screens/brokers) ; laissez le nom vide pour utiliser celui du compte. À l’actualisation, l’application lit la valeur totale du compte dans la devise de référence du compte. Un compte ne peut être lié qu’à une seule poche ; supprimer la position la délie sans supprimer le compte.

**Jetons et « non compté ».** Un jeton est identifié par l’adresse de son contrat. Il n’est compté que si votre source de cours référence exactement ce contrat ; sinon, il est listé comme *Jeton inconnu · non compté* et reste exclu des totaux. C’est ce qui empêche un faux « USDT » reçu par airdrop de gonfler votre épargne.

**Mode portefeuille.** Lorsqu’il est activé, l’écran ajoute un tableau indiquant pour chaque actif la valeur, la part réelle, la cible et l’écart, ainsi qu’un bouton **Rééquilibrer** qui demande un montant et liste ce qu’il faut acheter. Des ventes n’apparaissent que si *Autoriser les ventes lors du rééquilibrage* est activé. Aucune opération n’est exécutée.
