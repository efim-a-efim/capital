---
layout: default
lang: fr
base: "/fr"
key: "manual"
title: Manuel d’utilisation
class: doc
---
# Manuel d’utilisation

<p class="meta">Capital 2.2 · Android 8.0 et versions ultérieures</p>

## Le principe

Votre argent est réparti à plusieurs endroits : un livret d’épargne, des espèces, un compte-titres, un portefeuille crypto. Capital appelle chacun de ces endroits une **poche**. Cet argent doit servir à plusieurs choses : une épargne de précaution, un voyage, un ordinateur portable. Capital appelle chacune d’elles un **objectif**. Vous connectez des poches à des objectifs, et l’application calcule dans quelle mesure chaque objectif est financé par ce que vous possédez aujourd’hui. Ajoutez les versements que vous **prévoyez** d’effectuer, et elle vous indique aussi la date à laquelle chaque objectif sera atteint.

Rien dans l’application ne déplace d’argent. C’est un reflet de ce que vous possédez et un calculateur de ce que cela couvre.

## Premier lancement {#first-launch}

1. **Choisissez un dossier.** Sélectionnez un dossier dédié sur l’appareil, par exemple `Documents/Capital`. C’est là que toutes les données sont écrites. Un dossier qui contient déjà des données Capital s’ouvre directement.
2. **Paramètres → Devise par défaut.** Les totaux et la Synthèse sont affichés dans cette devise.
3. Si vous le souhaitez, saisissez dans les Paramètres des **clés de fournisseurs** pour les opérateurs qui offrent des limites plus élevées avec une clé gratuite (Alchemy, TronGrid, TON Center, CoinGecko). Chaque blockchain et chaque source de cours dispose d’une option par défaut sans clé. Les comptes de courtage nécessitent un **jeton d’accès** du courtier ; voir [Comptes de courtage et de forex]({{ page.base }}/accounts).

## Poches {#buckets}

Onglet Poches → **+**. Donnez à la poche un nom et une devise. Ouvrez-la pour ajouter des positions :

- **Position manuelle** (*Manuel*) : un nom, un code de devise ou d’actif (EUR, USD, BTC, un symbole boursier que vous valorisez vous-même…) et une quantité. Utilisez-la pour les soldes bancaires, les espèces et tout ce que l’application ne peut pas lire.
- **Position de portefeuille crypto** (*Portefeuille crypto*) : choisissez la blockchain (BTC, ETH, TON, TRX) et collez une adresse publique. À l’actualisation, l’application lit le solde natif et, pour ETH, TON et TRX, les jetons fongibles détenus à cette adresse.
- **Compte de courtage** : choisissez le courtier (Interactive Brokers, OANDA, Trading 212 ou SnapTrade) et saisissez l’identifiant du compte ou de la requête. À l’actualisation, l’application lit la valeur totale du compte dans sa devise de référence. Le jeton d’accès se saisit une seule fois dans Paramètres → Comptes de courtage. Les étapes de configuration, avec des liens vers la documentation des courtiers, figurent sur [Comptes de courtage et de forex]({{ page.base }}/accounts).

Les montants acceptent le point ou la virgule comme séparateur décimal, sans séparateur de milliers. Chaque poche affiche les quantités natives et leur valeur dans votre devise par défaut. S’il manque un cours, le total est signalé comme incomplet ; une valeur en cache obsolète reste utilisable, avec un avertissement.

**Jetons.** Un jeton est identifié par l’adresse de son contrat, jamais par son nom. Il n’est compté que si la source de cours que vous avez choisie référence exactement ce contrat ; tous les autres apparaissent comme *Jeton inconnu · non compté* et restent exclus des totaux. Ouvrez l’éditeur d’une position de portefeuille crypto pour récupérer ses jetons et désactiver ceux que vous ne voulez pas.

Le **Mode portefeuille** (paramètres de la poche) traite une poche comme un portefeuille d’investissement : définissez un pourcentage cible par actif, comparez la part réelle à la cible et utilisez **Rééquilibrer** pour obtenir la liste de ce qu’il faut acheter pour un montant donné. Des ventes ne sont proposées que si *Autoriser les ventes lors du rééquilibrage* est activé. C’est un calculateur ; il ne modifie rien.

## Objectifs {#goals}

Onglet Objectifs → **+**. Un objectif a un nom, une devise, un montant cible et une échéance. Ouvrez l’objectif et utilisez **Connecter une poche** pour indiquer quelles poches peuvent le financer, avec une limite facultative : un montant fixe, un pourcentage de la poche ou un pourcentage de l’objectif.

Comment l’argent est affecté :

- Les objectifs dont l’échéance est la plus proche sont financés en premier. Les objectifs de même échéance sont financés dans l’ordre affiché ; faites glisser la poignée pour les réordonner.
- Une poche connectée à plusieurs objectifs est répartie entre eux selon les limites, et n’est jamais comptée deux fois.
- Le résultat s’affiche sous la forme *financé / cible* et *Reste à financer*. La Synthèse montre le total, la part affectée aux objectifs et ce qui reste.

**Badges.** *Financé* (vert) lorsque votre épargne actuelle couvre déjà l’objectif. *Sera financé à temps* (vert) lorsque les versements prévus l’atteignent au plus tard à son échéance. *Non financé* (jaune) dans les autres cas. Le texte sous l’objectif indique quand il sera atteint ou combien il manque.

**Archiver** un objectif permet de le conserver sans le compter. Les objectifs archivés sont listés en bas.

## Prévisions {#plans}

Onglet Prévisions → **+**. Un versement prévu est un montant que vous comptez ajouter à une date donnée, par exemple la part de votre salaire que vous épargnez à la fin de chaque mois. Les versements prévus ne font pas partie de votre épargne ; ils prolongent seulement la projection : « Objectif atteint le 30 oct. 2026 grâce aux versements prévus · dans les temps ».

L’argent des versements prévus est appliqué après les poches actuelles, aux objectifs par ordre d’échéance : il ne complète donc que ce qui reste à financer. Lorsque la date d’un versement prévu est passée, il passe dans la section **Archivé** et n’est plus compté : soit vous avez déjà versé l’argent dans une poche et l’application l’y voit, soit le versement n’a pas eu lieu. Reportez sa date dans le futur pour le réactiver ; supprimez-le s’il n’a plus lieu d’être.

## Actualisation

L’icône d’actualisation en haut recharge tous les soldes de portefeuilles crypto, toutes les valeurs de comptes de courtage et tous les cours. Une poche peut être actualisée seule. L’application s’actualise une fois au démarrage à froid ; au retour depuis l’arrière-plan, elle recharge seulement les fichiers locaux. L’actualisation nécessite une connexion Internet ; sans connexion, les valeurs précédentes sont conservées et signalées comme obsolètes.

## Sécurité {#security}

Paramètres → Sécurité.

- Le **Chiffrement** chiffre tous les fichiers du dossier, y compris les anciennes révisions, avec un mot de passe. Le désactiver les déchiffre. Il n’existe **aucune récupération du mot de passe** : un mot de passe perdu signifie que les données ne pourront plus jamais être ouvertes. Les sauvegardes en clair réalisées avant l’activation du chiffrement restent lisibles ; l’application vous avertit de leur existence mais ne peut pas les supprimer.
- Le **PIN** et la **biométrie** sont disponibles lorsque le chiffrement est activé. *Utiliser le mot de passe* reste toujours accessible sur l’écran du PIN. Après 10 PIN erronés, le PIN est supprimé et seul le mot de passe fonctionne. Les saisies erronées ne suppriment jamais de données.
- Lorsque le chiffrement est activé, les captures d’écran et l’aperçu dans les applications récentes sont bloqués.

## Synchronisation, sauvegarde, récupération {#sync-backup-recovery}

Capital écrit dans votre dossier des fichiers d’instantanés avec identifiants de révision et sommes de contrôle, et n’effectue jamais de synchronisation lui-même. Placez le dossier sous l’outil de synchronisation que vous utilisez déjà. Si deux appareils modifient les données en même temps, l’application affiche un écran de conflit et vous laisse choisir une version ; les deux originaux restent sur le disque.

- **Exporter une sauvegarde** (Paramètres) écrit un seul fichier portable. **Restaurer** le valide avant toute modification.
- En cas d’échec de l’enregistrement, vos modifications restent en mémoire, avec *Réessayer l’enregistrement* et *Enregistrer une copie dans le dossier*.
- Si l’autorisation d’accès au dossier est perdue, reconnectez le même dossier.
- Un fichier écrit par une version plus récente de l’application est refusé par une version plus ancienne ; mettez l’application à jour.

## Langue {#language}

L’application démarre dans la langue de l’appareil si elle fait partie des 15 langues prises en charge, sinon en anglais. Vous pouvez la changer dans Paramètres → Langue.

## Installation en dehors de Google Play

Téléchargez l’APK depuis la [dernière version]({{ site.repo }}/releases/latest) et ouvrez-le ; autorisez l’installation depuis cette source lorsque Android le demande. Toutes les versions sont signées avec la même clé : les nouvelles versions s’installent donc par-dessus les anciennes et conservent vos paramètres. Le dossier contenant vos données n’est jamais modifié par une mise à jour ni par une désinstallation.
