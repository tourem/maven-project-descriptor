# 🎬 Guide : Lecteur Vidéo Natif dans GitHub README

## ❌ Pourquoi YouTube Redirige ?

GitHub **ne permet PAS** :
- Les iframes YouTube (sécurité)
- Les balises `<video>` avec URLs externes
- L'embedding de lecteurs tiers

**Résultat** : Les thumbnails YouTube sont cliquables mais redirigent vers YouTube.

---

## ✅ Solution : GitHub User Attachments (Lecteur Natif)

Pour avoir un **lecteur vidéo natif** directement dans le README GitHub, vous devez :

### Étape 1 : Créer une Issue Temporaire

1. Allez sur : https://github.com/tourem/deploy-manifest-plugin/issues
2. Cliquez sur **"New Issue"**
3. Titre : `[TEMP] Video Upload for README`
4. Ne remplissez rien d'autre pour l'instant

### Étape 2 : Uploader les Vidéos dans l'Issue

1. **Téléchargez vos vidéos depuis YouTube** (si vous ne les avez plus localement)
   - Utilisez un outil comme : https://yt-dlp.org/ ou https://www.y2mate.com/
   - Ou utilisez vos fichiers originaux : `Maven_Deploy_Manifest_Plugin_fr.mp4` et `Maven_Deploy_Manifest_Plugin_eng.mp4`

2. **Dans le corps de l'issue**, glissez-déposez les 2 fichiers MP4
   - Vous verrez "Uploading..." puis GitHub génère automatiquement des URLs

3. **Attendez la fin de l'upload**
   - GitHub affiche automatiquement les vidéos avec un lecteur natif dans l'issue
   - Les URLs générées ressemblent à :
   ```
   https://github.com/user-attachments/assets/12345678-abcd-1234-abcd-123456789abc
   ```

### Étape 3 : Copier les URLs Générées

Dans le corps de l'issue, vous verrez maintenant les vidéos affichées. 

**Cliquez sur "Edit" (crayon)** pour voir le markdown source, vous verrez :

```markdown
https://github.com/user-attachments/assets/XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX

https://github.com/user-attachments/assets/YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY
```

**Copiez ces 2 URLs** (une pour chaque vidéo).

### Étape 4 : Me Donner les URLs

Donnez-moi les 2 URLs et je mettrai à jour le README avec :

```markdown
**🇫🇷 Version Française:**

https://github.com/user-attachments/assets/VOTRE-URL-VIDEO-FR

**🇬🇧 English Version:**

https://github.com/user-attachments/assets/VOTRE-URL-VIDEO-EN
```

### Étape 5 : Fermer l'Issue

Une fois les URLs copiées et le README mis à jour, fermez l'issue temporaire.

---

## 🎬 Résultat Attendu

Avec les URLs `user-attachments`, le README affichera :

- ✅ **Lecteur vidéo natif GitHub** directement dans le README
- ✅ **Contrôles play/pause/volume** intégrés
- ✅ **Barre de progression**
- ✅ **Pas de redirection** vers YouTube
- ✅ **Lecture directe** dans la page GitHub

---

## 📊 Comparaison des Solutions

| Solution | Lecteur Natif | Redirection | Statistiques | Facilité |
|----------|---------------|-------------|--------------|----------|
| **YouTube Thumbnails** (actuel) | ❌ | ✅ Vers YouTube | ✅ Vues YouTube | ⭐⭐⭐ Facile |
| **GitHub User Attachments** | ✅ | ❌ Aucune | ❌ Non | ⭐⭐ Moyen |
| **Git LFS** | ❌ | ✅ Téléchargement | ❌ Non | ⭐ Difficile |

---

## 🎯 Recommandation

**Pour un lecteur natif sans redirection** :
→ Utilisez GitHub User Attachments (suivez les étapes ci-dessus)

**Pour garder les statistiques YouTube** :
→ Gardez la solution actuelle (thumbnails cliquables)

**Compromis** :
→ Mettez les deux ! User Attachments pour le lecteur natif + lien YouTube pour les stats

---

## 📝 Exemple de README Final (avec les deux)

```markdown
### 🎥 Video Demonstrations

**🇫🇷 Version Française:**

https://github.com/user-attachments/assets/VOTRE-URL-FR

> 📺 [Voir aussi sur YouTube](https://youtu.be/CLNUvOquHas) pour commenter et partager

**🇬🇧 English Version:**

https://github.com/user-attachments/assets/VOTRE-URL-EN

> 📺 [Watch also on YouTube](https://youtu.be/4CWSKUi2Ys4) to comment and share
```

---

## ❓ Questions Fréquentes

**Q: Les vidéos user-attachments expirent-elles ?**
R: Non, elles sont permanentes tant que l'issue/PR existe (même fermée).

**Q: Quelle est la taille maximale ?**
R: GitHub accepte jusqu'à 10 MB pour les vidéos dans les issues. Pour des vidéos plus grandes, compressez-les ou gardez YouTube.

**Q: Puis-je supprimer l'issue après ?**
R: Non ! Si vous supprimez l'issue, les URLs user-attachments ne fonctionneront plus. Fermez-la mais ne la supprimez pas.

---

## 🚀 Prochaines Étapes

1. Décidez quelle solution vous préférez :
   - **Option A** : Garder YouTube (actuel) - facile, avec stats
   - **Option B** : GitHub User Attachments - lecteur natif, pas de redirection
   - **Option C** : Les deux - meilleur des deux mondes

2. Si vous choisissez B ou C, suivez les étapes 1-5 ci-dessus

3. Donnez-moi les URLs et je mettrai à jour le README

---

**Dites-moi quelle option vous préférez !** 🎬

