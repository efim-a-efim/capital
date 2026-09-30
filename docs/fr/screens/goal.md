---
layout: screen
lang: fr
base: "/fr"
key: "screens/goal"
screen: goal
title: Objectif
---
# Objectif

**Ce que c’est.** Un objectif avec son financement. On y accède en touchant une carte dans l’onglet [Objectifs]({{ page.base }}/screens/goals) ; **← Tous les objectifs** permet de revenir en arrière.

**En-tête.** Le nom et le badge, financé / cible, l’échéance, la ligne de projection et **Reste à financer** : le montant cible moins ce qui est financé aujourd’hui.

**Boutons.** **Modifier l’objectif** change le nom, la devise, le montant cible et l’échéance. **Archiver** conserve l’objectif sans le compter ; un objectif archivé affiche **Activer** à la place. **Supprimer** supprime l’objectif et ses connexions après confirmation.

**Sources de financement.** Les poches qui peuvent financer cet objectif. **Connecter une poche** en ajoute une avec une limite de contribution :

- **Auto — jusqu’au besoin restant** : la poche fournit tout ce dont l’objectif a encore besoin, une fois que les objectifs prioritaires ont pris leur part.
- **Montant fixe dans la devise de l’objectif**.
- **% de la poche** : au plus cette part de la valeur de la poche.
- **% de l’objectif** : au plus cette part du montant cible.

L’aperçu dans l’éditeur montre ce que la connexion apporterait aujourd’hui. Les limites sont des plafonds : l’ordre des objectifs, l’épargne disponible et les autres connexions peuvent réduire la contribution. Chaque source listée indique ce qu’elle apporte actuellement et pourquoi elle n’apporte pas davantage ; **Modifier la connexion** change la limite, **Déconnecter** supprime la connexion.

**Versements prévus.** Les versements prévus qui alimentent cet objectif, chacun avec le montant que la projection lui attribue. Un versement prévu entièrement absorbé par des objectifs prioritaires n’apparaît pas ici.

**Lire la projection.** « Objectif atteint le 20 déc. 2026 grâce aux versements prévus · dans les temps » signifie que le cumul des versements prévus jusqu’à cette date couvre le besoin restant avant l’échéance. « Les versements prévus couvrent jusqu’à … · il manque … » signifie que ce n’est pas le cas ; ajoutez un versement prévu, déplacez l’échéance ou réduisez le montant cible.
