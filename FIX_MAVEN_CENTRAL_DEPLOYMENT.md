# Fix: Maven Central Deployment Error (404 Not Found)

## 🐛 Problem

The GitHub Actions release workflow was failing with this error:

```
Failed to execute goal org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13:deploy 
(injected-nexus-deploy) on project descriptor-parent: 
Execution injected-nexus-deploy of goal org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13:deploy failed: 
Nexus connection problem to URL [https://central.sonatype.com/]: 404 - Not Found
```

## 🔍 Root Cause

The `nexus-staging-maven-plugin` is designed for the **legacy Sonatype OSSRH** (OSS Repository Hosting) system, which uses different URLs and APIs.

**Sonatype Central Portal** (the new system at `central.sonatype.com`) requires a different plugin: **`central-publishing-maven-plugin`**.

### Key Differences

| Feature | Legacy OSSRH | New Central Portal |
|---------|--------------|-------------------|
| **URL** | `https://oss.sonatype.org/` | `https://central.sonatype.com/` |
| **Plugin** | `nexus-staging-maven-plugin` | `central-publishing-maven-plugin` |
| **Authentication** | Username/Password | Token-based (username/password from token) |
| **Distribution Management** | Required | Not required |
| **Staging** | Manual or auto-release | Auto-publish |

## ✅ Solution

### 1. Replace Plugin in POM

**Before:**
```xml
<plugin>
  <groupId>org.sonatype.plugins</groupId>
  <artifactId>nexus-staging-maven-plugin</artifactId>
  <version>1.6.13</version>
  <extensions>true</extensions>
  <configuration>
    <serverId>central</serverId>
    <nexusUrl>https://central.sonatype.com/</nexusUrl>
    <autoReleaseAfterClose>true</autoReleaseAfterClose>
  </configuration>
</plugin>
```

**After:**
```xml
<plugin>
  <groupId>org.sonatype.central</groupId>
  <artifactId>central-publishing-maven-plugin</artifactId>
  <version>0.4.0</version>
  <extensions>true</extensions>
  <configuration>
    <publishingServerId>central</publishingServerId>
    <tokenAuth>true</tokenAuth>
    <autoPublish>true</autoPublish>
  </configuration>
</plugin>
```

### 2. Remove distributionManagement Section

The new plugin doesn't require `distributionManagement` section in POM.

**Removed:**
```xml
<distributionManagement>
  <snapshotRepository>
    <id>central</id>
    <url>https://central.sonatype.com/</url>
  </snapshotRepository>
  <repository>
    <id>central</id>
    <url>https://central.sonatype.com/</url>
  </repository>
</distributionManagement>
```

### 3. Update Property Name

**Before:**
```xml
<nexus-staging-plugin.version>1.6.13</nexus-staging-plugin.version>
```

**After:**
```xml
<central-publishing-plugin.version>0.4.0</central-publishing-plugin.version>
```

## 📋 Changes Made

### Files Modified

1. **`pom.xml`**
   - Replaced `nexus-staging-maven-plugin` with `central-publishing-maven-plugin`
   - Updated plugin version property
   - Updated plugin configuration
   - Removed `distributionManagement` section

2. **`MIGRATION_TO_MAVEN_CENTRAL.md`**
   - Updated documentation to reflect new plugin

## 🧪 Verification

### Local Build Test

```bash
mvn clean install -DskipTests -B
```

**Result:** ✅ SUCCESS

### Deploy Test (Dry Run)

```bash
mvn clean deploy -Prelease -DskipTests -DaltDeploymentRepository=local::file:./target/staging-deploy
```

This will simulate deployment without actually publishing to Maven Central.

## 🚀 Next Steps

The fix has been committed and pushed to GitHub. You can now:

1. **Trigger the release workflow again** from GitHub Actions
2. The deployment should now succeed with the new plugin

### How to Trigger Release

1. Go to: https://github.com/tourem/descriptor-plugin/actions
2. Click on "Release to Maven Central" workflow
3. Click "Run workflow"
4. Enter version (e.g., `1.0.0`)
5. Click "Run workflow" button

## 📚 References

- **Central Publishing Maven Plugin Documentation:**
  - https://central.sonatype.org/publish/publish-portal-maven/
  - https://github.com/sonatype/central-publishing-maven-plugin

- **Migration Guide:**
  - https://central.sonatype.org/publish/publish-portal-migration/

- **Sonatype Central Portal:**
  - https://central.sonatype.com/

## 🔧 Configuration Details

### Plugin Configuration Explained

```xml
<configuration>
  <!-- Server ID must match the one in GitHub Actions setup-java -->
  <publishingServerId>central</publishingServerId>
  
  <!-- Use token-based authentication (username/password from Sonatype token) -->
  <tokenAuth>true</tokenAuth>
  
  <!-- Automatically publish after validation (no manual approval needed) -->
  <autoPublish>true</autoPublish>
</configuration>
```

### GitHub Actions Integration

The GitHub Actions workflow uses `setup-java@v4` which automatically configures Maven settings:

```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: 'maven'
    server-id: central                              # Matches publishingServerId
    server-username: MAVEN_USERNAME                 # Environment variable
    server-password: MAVEN_PASSWORD                 # Environment variable
    gpg-private-key: ${{ secrets.GPG_PRIVATE_KEY }}
    gpg-passphrase: MAVEN_GPG_PASSPHRASE
```

This creates a `~/.m2/settings.xml` with:

```xml
<servers>
  <server>
    <id>central</id>
    <username>${env.MAVEN_USERNAME}</username>
    <password>${env.MAVEN_PASSWORD}</password>
  </server>
</servers>
```

## ⚠️ Important Notes

### Authentication

The new plugin uses **token-based authentication**:

1. Generate token at https://central.sonatype.com/
2. Token provides both username and password
3. Add both to GitHub Secrets:
   - `SONATYPE_USERNAME` - Token username
   - `SONATYPE_TOKEN` - Token password

### Auto-Publish

With `autoPublish: true`, artifacts are automatically published to Maven Central after validation. No manual approval needed.

If you prefer manual approval:
```xml
<autoPublish>false</autoPublish>
```

Then manually publish from https://central.sonatype.com/publishing

### Deployment Timeline

After successful deployment:
1. **Immediate:** Artifacts appear in Sonatype Central Portal
2. **~10-30 minutes:** Artifacts sync to Maven Central
3. **~2 hours:** Full propagation to all Maven Central mirrors

## 🎯 Success Criteria

After the fix, the release workflow should:

1. ✅ Build successfully
2. ✅ Run all tests
3. ✅ Sign artifacts with GPG
4. ✅ Deploy to Sonatype Central Portal (no 404 error)
5. ✅ Auto-publish to Maven Central
6. ✅ Create Git tag
7. ✅ Create GitHub Release

## 🆘 Troubleshooting

### Still getting 404 error?

**Check:**
- Sonatype token is valid (regenerate if needed)
- `SONATYPE_USERNAME` and `SONATYPE_TOKEN` are correctly set in GitHub Secrets
- Namespace `io.github.tourem` is verified in Sonatype Central Portal

### "Unauthorized" error?

**Solution:**
- Regenerate Sonatype token at https://central.sonatype.com/
- Update GitHub Secrets with new token

### "Namespace not verified" error?

**Solution:**
- Login to https://central.sonatype.com/
- Verify namespace `io.github.tourem` (usually automatic for GitHub users)

---

**The fix is now deployed and ready for testing! 🚀**

