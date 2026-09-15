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
- [x] Écrans Compose : catégorisation (bottom sheet de partage), bibliothèque, détail de catégorie
- [x] Tests unitaires de démonstration sur la couche domain (JUnit + MockK + Truth)
- [ ] Gestion des catégories (renommer/supprimer/fusionner) — écran à construire
- [x] Pipeline CI (GitHub Actions) : tests + build APK debug à chaque push sur `main`

## Build local

Nécessite Android Studio (Koala ou plus récent) avec JDK 17.

```
./gradlew assembleDebug
./gradlew test
```

L'APK debug est généré dans `app/build/outputs/apk/debug/`.

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
