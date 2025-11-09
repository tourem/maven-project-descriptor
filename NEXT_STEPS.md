# 🚀 Next Steps - Maven Central Publication

## ✅ What Has Been Done

All necessary changes have been completed to prepare the project for Maven Central publication:

1. ✅ **GroupId Migration:** `com.larbotech` → `io.github.tourem`
2. ✅ **Version Update:** `1.0-SNAPSHOT` → `1.0.0-SNAPSHOT`
3. ✅ **Java Packages Renamed:** All 32 Java files migrated to new package structure
4. ✅ **Maven Central Metadata:** Added licenses, developers, SCM, issue management
5. ✅ **Plugin Configuration:** Configured sources, javadoc, GPG signing, Nexus staging
6. ✅ **GitHub Actions Workflow:** Created automated release workflow
7. ✅ **Documentation:** Updated README, CHANGELOG, USAGE, created setup guides
8. ✅ **Build Verification:** All tests pass (117/117), build successful
9. ✅ **Git Commit:** Changes committed and pushed to GitHub

---

## 📋 What You Need to Do Now

### Step 1: Configure GitHub Secrets (REQUIRED)

Before you can release to Maven Central, you MUST configure 4 GitHub Secrets:

#### 1.1 Generate Sonatype Token

1. Go to https://central.sonatype.com/
2. Login with your GitHub account
3. Click on your profile (top right) → "View Account"
4. Click "Generate User Token"
5. Copy the **username** and **password**

#### 1.2 Generate GPG Key (if you don't have one)

```bash
# Generate GPG key
gpg --gen-key

# Follow prompts:
# - Name: Mamadou Touré
# - Email: touremamadou1990@gmail.com
# - Passphrase: Choose a strong passphrase (save it!)

# List your keys to get KEY_ID
gpg --list-secret-keys --keyid-format=long

# Export private key
gpg --armor --export-secret-keys YOUR_KEY_ID

# Publish public key to keyservers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys YOUR_KEY_ID
```

#### 1.3 Add Secrets to GitHub

Go to: https://github.com/tourem/descriptor-plugin/settings/secrets/actions

Add these 4 secrets:

| Secret Name | Value | Where to Get |
|-------------|-------|--------------|
| `SONATYPE_USERNAME` | Your Sonatype username | From Step 1.1 (username part) |
| `SONATYPE_TOKEN` | Your Sonatype password | From Step 1.1 (password part) |
| `GPG_PRIVATE_KEY` | Your GPG private key | From Step 1.2 (full output including BEGIN/END) |
| `GPG_PASSPHRASE` | Your GPG passphrase | From Step 1.2 (passphrase you chose) |

**📖 Detailed Instructions:** See `.github/SECRETS_SETUP.md`

---

### Step 2: Verify Namespace on Sonatype Central

1. Go to https://central.sonatype.com/
2. Login with your GitHub account
3. Verify that namespace `io.github.tourem` is registered and verified
4. If not, follow the verification process (usually automatic for GitHub users)

---

### Step 3: Trigger Your First Release

Once secrets are configured:

1. Go to GitHub Actions: https://github.com/tourem/descriptor-plugin/actions
2. Click on "Maven Central Release" workflow (left sidebar)
3. Click "Run workflow" button (top right)
4. Enter version: `1.0.0`
5. Click "Run workflow" (green button)

The workflow will:
- ✅ Build the project
- ✅ Run all tests
- ✅ Sign artifacts with GPG
- ✅ Deploy to Maven Central
- ✅ Create Git tag `v1.0.0`
- ✅ Create GitHub Release
- ✅ Bump version to `1.0.1-SNAPSHOT`

**⏱️ Duration:** ~5-10 minutes

---

### Step 4: Verify Publication

After the workflow completes successfully:

#### 4.1 Check Maven Central

Wait 10-30 minutes for Maven Central synchronization, then check:

**Search:** https://central.sonatype.com/search?q=io.github.tourem

**Direct Link:** https://central.sonatype.com/artifact/io.github.tourem/descriptor-plugin

#### 4.2 Test the Published Plugin

```bash
# Download from Maven Central
mvn dependency:get -Dartifact=io.github.tourem:descriptor-plugin:1.0.0

# Use the plugin
mvn io.github.tourem:descriptor-plugin:1.0.0:generate
```

---

## 🎯 Quick Start Checklist

Use this checklist to track your progress:

- [ ] **Step 1.1:** Generate Sonatype token at central.sonatype.com
- [ ] **Step 1.2:** Generate GPG key (or use existing)
- [ ] **Step 1.2:** Publish GPG public key to keyservers
- [ ] **Step 1.3:** Add `SONATYPE_USERNAME` to GitHub Secrets
- [ ] **Step 1.3:** Add `SONATYPE_TOKEN` to GitHub Secrets
- [ ] **Step 1.3:** Add `GPG_PRIVATE_KEY` to GitHub Secrets
- [ ] **Step 1.3:** Add `GPG_PASSPHRASE` to GitHub Secrets
- [ ] **Step 2:** Verify namespace `io.github.tourem` on Sonatype
- [ ] **Step 3:** Trigger release workflow with version `1.0.0`
- [ ] **Step 4.1:** Verify artifact appears on Maven Central
- [ ] **Step 4.2:** Test downloading and using the published plugin

---

## 📚 Documentation References

| Document | Purpose |
|----------|---------|
| `MAVEN_CENTRAL_RELEASE.md` | Complete release guide and troubleshooting |
| `.github/SECRETS_SETUP.md` | Detailed GitHub Secrets setup instructions |
| `MIGRATION_TO_MAVEN_CENTRAL.md` | Summary of all migration changes |
| `README.md` | Updated with new Maven coordinates |

---

## 🔧 Useful Commands

### Local Testing

```bash
# Build with all Maven Central plugins
mvn clean install -Prelease

# Test GPG signing locally
mvn clean verify -Prelease -Dgpg.passphrase=YOUR_PASSPHRASE

# Generate descriptor with new coordinates
mvn io.github.tourem:descriptor-plugin:1.0.0-SNAPSHOT:generate
```

### After Release

```bash
# Download from Maven Central
mvn dependency:get -Dartifact=io.github.tourem:descriptor-plugin:1.0.0

# Use in your project
mvn io.github.tourem:descriptor-plugin:1.0.0:generate
```

---

## ⚠️ Important Notes

### Breaking Changes for Users

If anyone was using the old groupId `com.larbotech`, they need to update:

**Old:**
```xml
<groupId>com.larbotech</groupId>
<artifactId>descriptor-plugin</artifactId>
<version>1.0-SNAPSHOT</version>
```

**New:**
```xml
<groupId>io.github.tourem</groupId>
<artifactId>descriptor-plugin</artifactId>
<version>1.0.0</version>
```

### Version Numbering

- **Current:** `1.0.0-SNAPSHOT` (development)
- **First Release:** `1.0.0` (stable)
- **After Release:** `1.0.1-SNAPSHOT` (next development)

Follow Semantic Versioning:
- **MAJOR:** Breaking changes (e.g., `2.0.0`)
- **MINOR:** New features, backward compatible (e.g., `1.1.0`)
- **PATCH:** Bug fixes, backward compatible (e.g., `1.0.1`)

---

## 🆘 Troubleshooting

### "401 Unauthorized" during deployment

**Cause:** Invalid Sonatype credentials

**Solution:**
1. Regenerate token at https://central.sonatype.com/
2. Update `SONATYPE_USERNAME` and `SONATYPE_TOKEN` in GitHub Secrets

### "gpg: signing failed: No secret key"

**Cause:** Invalid GPG private key

**Solution:**
1. Re-export GPG key: `gpg --armor --export-secret-keys YOUR_KEY_ID`
2. Update `GPG_PRIVATE_KEY` in GitHub Secrets (include BEGIN/END lines)

### "Public key not found on keyserver"

**Cause:** GPG public key not published

**Solution:**
```bash
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
```

### Artifact not appearing on Maven Central

**Cause:** Synchronization delay

**Solution:** Wait 10-30 minutes, then check again

---

## 📞 Support

- **Maven Central Guide:** https://central.sonatype.org/publish/publish-guide/
- **GitHub Issues:** https://github.com/tourem/descriptor-plugin/issues
- **Sonatype Support:** https://central.sonatype.org/support/

---

## 🎉 Success Criteria

You'll know everything is working when:

1. ✅ GitHub Actions workflow completes successfully
2. ✅ Artifact appears on https://central.sonatype.com/artifact/io.github.tourem/descriptor-plugin
3. ✅ You can download it: `mvn dependency:get -Dartifact=io.github.tourem:descriptor-plugin:1.0.0`
4. ✅ You can use it: `mvn io.github.tourem:descriptor-plugin:1.0.0:generate`
5. ✅ Git tag `v1.0.0` exists on GitHub
6. ✅ GitHub Release `v1.0.0` is created

---

**Good luck with your first Maven Central release! 🚀**

If you encounter any issues, check the troubleshooting section or consult the detailed guides.

