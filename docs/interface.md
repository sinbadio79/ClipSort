# Interface ClipSort

Direction retenue : une bibliothèque personnelle calme, avec fond ivoire, vert
profond, accents corail et titres à empattements. Le mode sombre utilise une palette
dédiée. « Collection » est le libellé produit de la catégorie existante ; le modèle
`Category` et la structure Room sont conservés.

## Écrans

- Accueil : statistiques de la bibliothèque réelle, accès aux clips, collections
  et commandes visibles de création/renommage/suppression.
- Tous les clips : recherche locale, filtres combinés source/statut et ordre
  chronologique ; lecture externe et gestion des clips.
- Collection : même parcours de consultation, limité à une catégorie.
- Partage : choix de collection, note et enregistrement depuis le partage Android.
  Les collections sont disposées sur plusieurs lignes ; le formulaire défile avec
  le clavier. Le bouton reste désactivé sans lien et sans sélection explicite.
  Un nouveau partage reçu par l'activité singleTask crée une session indépendante
  pour éviter la réutilisation d'une ancienne note ou d'une sélection.

La refonte est développée et vérifiée par incréments. Les rapports de CI déterminent
quels écrans et interactions sont validés ; cette description ne remplace pas les tests.

## Réactivité et limites

Room reste la source de vérité. Ses flux actualisent listes et compteurs ; aucun
rafraîchissement manuel n'est nécessaire. La recherche locale du premier incrément
est un filtrage multi-mots insensible à la casse, et non un index full-text SQLite.
Une migration FTS et la pagination seront nécessaires si le volume le justifie.

Le contenu des captures est une fixture de test, jamais injectée dans les données
de l'application. Les visuels de plateforme sont des repères de source et ne sont
pas présentés comme des miniatures vidéo récupérées. Aucune collecte réseau de
métadonnées ni suggestion IA fictive n'est ajoutée à cette refonte.

## Vérification

Les tests instrumentés exécutent les écrans Compose sur un émulateur Android 35
Pixel 6. Ils vérifient les actions et enregistrent des PNG du rendu Android dans
l'artefact `android-ui-verification`. Les sources de ces tests se trouvent dans
`app/src/androidTest/kotlin/com/clipsort/app/presentation`.
Les PNG capturent la racine Compose (sans les barres système), afin de ne pas
photographier un dialogue du launcher de l'émulateur. Le panneau de partage est
capturé isolément avec les mêmes composants que la feuille modale de production.
Deux tests complémentaires ouvrent la véritable ShareReceiverActivity avec Hilt
et Room : persistance du lien extrait et de la note, puis réception d'un deuxième
partage sans conservation de l'ancienne saisie. Les doubles clics d'enregistrement
et de création de collection sont couverts par les tests de ViewModel.
Un test de MainActivity parcourt accueil → collection → tous les clips → accueil,
puis vérifie la conservation du filtre après recréation de l'activité.

Les contrôles UI bloquent la signature release. Les contrôles domain/data, lint,
debug, release R8 et vérification de schéma restent actifs. Les captures automatisées
ne remplacent pas une recette TalkBack, grande police et appareils physiques.
