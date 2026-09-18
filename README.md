# ClipSort

Application Android qui intercepte le partage natif (TikTok, Instagram, YouTube, Facebook)
pour catégoriser un clip en 2 taps, au lieu de l'envoyer dans une liste WhatsApp sans tri.

## Architecture

Clean Architecture en 3 couches, MVVM côté presentation :

```
domain/          Kotlin pur, aucune dépendance Android. Modèles, interfaces repository, use cases.
data/            Implémentation concrète : Room (entities, DAO), mappers, repositories.
presentation/    Compose + ViewModel + UiState par écran, injectés via Hilt.
```

Règle stricte : la couche `presentation` ne connaît que des modèles `domain`, jamais une
entité Room. La conversion se fait exclusivement dans `data/mapper/EntityMappers.kt`.

## État actuel

- [x] Modèles domain (`Category`, `Clip`) + interfaces repository
- [x] Use cases : détection de source, création de catégorie, sauvegarde de clip, lecture bibliothèque
- [x] Persistance Room (entities, DAO, base de données)
- [x] Repositories concrets + module Hilt
- [x] Quatre écrans Compose : accueil, tous les clips, collection, feuille de partage
- [x] Thèmes clair/sombre, ressources d'interface FR/EN, actions accessibles sans appui long
- [x] Recherche multi-mots dans notes, liens, sources et noms de collections
- [x] Filtres combinés source/statut, tri chronologique, compteurs actualisés par Room
- [x] Ouverture de vidéo, partage du lien, édition de note, statut vu/à voir, suppression confirmée
- [x] Extraction du lien partagé, sessions de partage indépendantes, protection contre les doubles clics
- [x] Tests domain, ViewModel, liens et intégration Room (JUnit, MockK, Robolectric)
- [x] Renommage et suppression des catégories (fusion non implémentée)
- [x] CI GitHub Actions à chaque push : tests, lint, builds debug/release R8, tests UI sur émulateur Android 35
- [x] Release signée avec la même clé privée à chaque build CI, certificat vérifié
- [x] Schéma Room v1 versionné ; absence de migration explicite = erreur, jamais effacement automatique

L'audit et la direction KMP/SwiftUI + Supabase validée sont documentés dans
[docs/production-audit.md](docs/production-audit.md). Ces composants ne sont pas
encore implémentés. La v1 reste une application Android locale.
La direction visuelle, les parcours et les limites sont documentés dans
[docs/interface.md](docs/interface.md). La recherche actuelle filtre en mémoire ;
ce n'est pas encore un index SQLite FTS. Tags, doublons persistants, miniatures vidéo
réelles, IA, rappels, widgets, achats et exports restent à développer.

## Build local

Nécessite JDK 17 et Android SDK 35. Le Wrapper télécharge Gradle 8.9 avec
vérification SHA-256. Android Studio est facultatif pour la ligne de commande.

```
./gradlew testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Avec un émulateur Android 35 démarré : `./gradlew connectedDebugAndroidTest`.
La CI conserve rapports et captures Compose dans `android-ui-verification`.
Les données illustrées sont uniquement des fixtures des tests ; une installation
réelle affiche votre bibliothèque locale, vide au premier lancement.

L'APK debug est généré dans `app/build/outputs/apk/debug/`.
Sous Windows, utiliser `./gradlew.bat`. Configurer `ANDROID_HOME` ou `sdk.dir`
dans un fichier `local.properties` non versionné.

La compilation et les tests de ce chantier sont exécutés sur GitHub Actions.
Les artefacts `verification-reports` contiennent les résultats JUnit, le lint et
les schémas Room ; `clipsort-debug-apk` contient l'APK installable de développement.
Sur push et lancement manuel, `clipsort-release-apk` contient la release R8 signée
avec une clé stable conservée dans GitHub Secrets, son checksum et le rapport de
signature. Les PR vérifient une release non signée. Voir [signature et sauvegarde](docs/signing.md).
Les tests Room utilisent Robolectric avec SQLite. Les tests instrumentés couvrent
aussi l'activité de partage réelle, Hilt et la persistance sur l'émulateur.
Les tests UI doivent réussir avant que le job de signature puisse démarrer.

### Évolution de la base de données

Le schéma v1 dans `app/schemas/` est généré par Room, pas écrit à la main.
Pour modifier la structure : incrémenter la version, ajouter une migration explicite,
tester la conservation des données depuis chaque version distribuée et versionner
le nouveau schéma généré. Ne pas réécrire les anciens schémas. La CI refuse un
schéma généré non versionné. Aucun changement de structure n'est introduit dans
ce premier incrément, donc aucune migration v1 → v2 n'est encore nécessaire.

## Mise en ligne sur GitHub

Depuis le dossier du projet :

```
git init
git branch -M main
git add .
git commit -m "Initial commit: architecture MVVM ClipSort"
```

Puis crée un repo vide sur https://github.com/new (sans README ni .gitignore, pour éviter un conflit),
et relie-le :

```
git remote add origin https://github.com/<ton-compte>/ClipSort.git
git push -u origin main
```

## Licence

Publié sous licence **Apache 2.0** — voir le fichier `LICENSE`. Utilisation, modification
et redistribution libres, y compris commerciale, avec obligation de conserver la notice
de copyright et de mentionner les modifications apportées.

## Distribution open source (F-Droid)

Le Play Store n'est pas adapté à une distribution open source classique. Pour une app
Android FOSS, **F-Droid** est le canal naturel : gratuit, pas de compte développeur, pas
de vérification d'identité — seule la source doit être publique et sous licence libre
(ce qui est le cas ici).

Étapes :
1. Rendre le repo GitHub public
2. Créer un tag de version (`git tag v1.0.0 && git push --tags`) — F-Droid build depuis un tag, pas depuis `main`
3. Vérifier l'absence de dépendances non-libres : ce projet n'utilise que des bibliothèques
   AndroidX/Jetpack (Room, Compose, Hilt), toutes Apache 2.0 — aucun service Google
   propriétaire (pas de Firebase, pas de Play Services), donc conforme aux critères F-Droid
4. Soumettre une demande d'inclusion via **https://gitlab.com/fdroid/rfp/-/issues/new**
   (Requests For Packaging), en indiquant l'URL du repo et la licence
5. L'équipe F-Droid crée ensuite le fichier de métadonnées de build et publie l'app une
   fois la demande validée — délai variable (bénévoles), compter plusieurs semaines

Rien n'empêche de publier en parallèle sur GitHub Releases (APK signé téléchargeable
directement) pendant que la demande F-Droid est en cours.
