# 🎬 Compresser les Vidéos pour GitHub (< 10 MB)

## Problème
GitHub limite les vidéos dans les issues à **10 MB maximum**.
Vos vidéos : 30 MB (FR) + 33 MB (EN) = trop volumineuses ❌

## ✅ Solution : Compresser avec FFmpeg

### Option A : Utiliser FFmpeg (Ligne de Commande)

#### 1. Installer FFmpeg (si pas déjà installé)

```bash
# Sur macOS avec Homebrew
brew install ffmpeg

# Vérifier l'installation
ffmpeg -version
```

#### 2. Compresser les Vidéos

```bash
cd /Users/mtoure/dev/maven-flow

# Créer un dossier pour les vidéos compressées
mkdir -p videos-compressed

# Compresser la vidéo française (cible : ~8 MB)
ffmpeg -i Maven_Deploy_Manifest_Plugin_fr.mp4 \
  -vcodec libx264 \
  -crf 28 \
  -preset medium \
  -vf "scale=1280:-2" \
  -movflags +faststart \
  -maxrate 800k \
  -bufsize 1600k \
  videos-compressed/Maven_Deploy_Manifest_Plugin_fr_compressed.mp4

# Compresser la vidéo anglaise (cible : ~8 MB)
ffmpeg -i Maven_Deploy_Manifest_Plugin_eng.mp4 \
  -vcodec libx264 \
  -crf 28 \
  -preset medium \
  -vf "scale=1280:-2" \
  -movflags +faststart \
  -maxrate 800k \
  -bufsize 1600k \
  videos-compressed/Maven_Deploy_Manifest_Plugin_eng_compressed.mp4

# Vérifier la taille des fichiers compressés
ls -lh videos-compressed/
```

#### Explication des Paramètres

- `-crf 28` : Qualité (18-28 = bonne qualité, 28-35 = qualité moyenne)
- `-preset medium` : Vitesse de compression
- `-vf "scale=1280:-2"` : Réduire la résolution à 1280px de largeur
- `-maxrate 800k` : Limiter le bitrate à 800 kbps
- `-bufsize 1600k` : Buffer pour le bitrate
- `-movflags +faststart` : Optimiser pour le streaming web

#### 3. Ajuster si Nécessaire

Si les vidéos sont encore trop grandes :

```bash
# Compression plus agressive (cible : ~5 MB)
ffmpeg -i Maven_Deploy_Manifest_Plugin_fr.mp4 \
  -vcodec libx264 \
  -crf 32 \
  -preset medium \
  -vf "scale=960:-2" \
  -movflags +faststart \
  -maxrate 500k \
  -bufsize 1000k \
  videos-compressed/Maven_Deploy_Manifest_Plugin_fr_small.mp4
```

---

### Option B : Utiliser HandBrake (Interface Graphique)

#### 1. Télécharger HandBrake

- Site : https://handbrake.fr/
- Gratuit et open-source

#### 2. Paramètres Recommandés

1. Ouvrir HandBrake
2. Charger votre vidéo
3. **Preset** : "Web" → "Gmail Medium 5 Minutes 720p30"
4. **Dimensions** : Width = 1280 (ou 960 pour plus petit)
5. **Video** :
   - Codec : H.264
   - Framerate : Same as source
   - Quality : RF 28-32
   - Encoder Preset : Medium
6. **Audio** : AAC, Bitrate 128 kbps
7. Cliquer "Start Encode"

---

### Option C : Utiliser un Service en Ligne

#### CloudConvert (Gratuit)

1. Allez sur : https://cloudconvert.com/mp4-compress
2. Uploadez votre vidéo
3. Paramètres :
   - **Video Codec** : H.264
   - **Resolution** : 1280x720
   - **Video Bitrate** : 800 kbps
   - **Audio Bitrate** : 128 kbps
4. Téléchargez le résultat

---

## 🎯 Objectif de Taille

Pour GitHub issues (max 10 MB) :
- ✅ **Idéal** : 5-8 MB par vidéo
- ⚠️ **Maximum** : 9.5 MB (garder une marge)

---

## 📊 Estimation de Taille

Pour une vidéo de **3 minutes** :

| Résolution | Bitrate | Taille Estimée |
|------------|---------|----------------|
| 1920x1080 | 1500 kbps | ~34 MB ❌ |
| 1280x720 | 800 kbps | ~18 MB ❌ |
| 1280x720 | 500 kbps | ~11 MB ⚠️ |
| 960x540 | 500 kbps | ~8 MB ✅ |
| 960x540 | 400 kbps | ~6 MB ✅ |

---

## 🚀 Commandes Rapides (Copier-Coller)

```bash
# Vérifier si FFmpeg est installé
which ffmpeg || brew install ffmpeg

# Aller dans le dossier des vidéos
cd /Users/mtoure/dev/maven-flow

# Créer dossier de sortie
mkdir -p videos-compressed

# Compresser les 2 vidéos (cible : ~7 MB chacune)
ffmpeg -i Maven_Deploy_Manifest_Plugin_fr.mp4 \
  -vcodec libx264 -crf 30 -preset medium \
  -vf "scale=1280:-2" -movflags +faststart \
  -maxrate 600k -bufsize 1200k \
  -acodec aac -b:a 96k \
  videos-compressed/Maven_Deploy_Manifest_Plugin_fr_compressed.mp4

ffmpeg -i Maven_Deploy_Manifest_Plugin_eng.mp4 \
  -vcodec libx264 -crf 30 -preset medium \
  -vf "scale=1280:-2" -movflags +faststart \
  -maxrate 600k -bufsize 1200k \
  -acodec aac -b:a 96k \
  videos-compressed/Maven_Deploy_Manifest_Plugin_eng_compressed.mp4

# Vérifier les tailles
ls -lh videos-compressed/
```

---

## ✅ Après Compression

1. Vérifiez que les vidéos font < 10 MB
2. Testez la qualité (regardez les vidéos)
3. Si OK, uploadez dans l'issue GitHub
4. Donnez-moi les URLs générées

---

## 🎬 Alternative : Garder YouTube

Si la compression dégrade trop la qualité, vous pouvez :
- **Garder la solution actuelle** (thumbnails YouTube)
- Les utilisateurs cliquent et regardent sur YouTube
- Qualité maximale préservée
- Statistiques YouTube disponibles

---

**Voulez-vous que je vous aide à compresser les vidéos avec FFmpeg ?** 🎥

