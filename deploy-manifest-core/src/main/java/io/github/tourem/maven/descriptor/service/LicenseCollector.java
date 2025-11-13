package io.github.tourem.maven.descriptor.service;

import io.github.tourem.maven.descriptor.model.LicenseCompliance;
import io.github.tourem.maven.descriptor.model.LicenseDetail;
import io.github.tourem.maven.descriptor.model.LicenseInfo;
import io.github.tourem.maven.descriptor.model.LicenseOptions;
import io.github.tourem.maven.descriptor.model.LicenseSummary;
import io.github.tourem.maven.descriptor.model.LicenseWarning;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Collects license information for a module's dependencies.
 * Note: First iteration only computes direct dependencies and marks licenses as "unknown".
 * Future iterations may resolve transitive dependencies and fetch real license metadata.
 */
@Slf4j
public class LicenseCollector {

    private static final Set<String> DEFAULT_SCOPES = Set.of("compile", "runtime");

    public LicenseInfo collect(Model model, Path modulePath, LicenseOptions options) {
        if (model == null || options == null || !options.isInclude()) return null;

        List<LicenseDetail> details = new ArrayList<>();
        Map<String, Integer> byType = new HashMap<>();
        int total = 0;
        int identified = 0;
        int unknown = 0;
        List<LicenseWarning> warnings = new ArrayList<>();

        Set<String> allowedScopes = new HashSet<>(DEFAULT_SCOPES);

        if (model.getDependencies() != null) {
            for (Dependency d : model.getDependencies()) {
                String scope = normalizeScope(d.getScope());
                if (!allowedScopes.contains(scope)) continue; // filter to compile/runtime

                total++;
                String license = "unknown";
                String licenseUrl = null;
                boolean multi = false;

                details.add(LicenseDetail.builder()
                        .groupId(d.getGroupId())
                        .artifactId(d.getArtifactId())
                        .version(d.getVersion())
                        .scope(scope)
                        .license(license)
                        .licenseUrl(licenseUrl)
                        .multiLicense(multi)
                        .depth(1)
                        .build());

                if ("unknown".equalsIgnoreCase(license)) {
                    unknown++;
                    byType.merge("unknown", 1, Integer::sum);
                    if (options.isLicenseWarnings()) {
                        warnings.add(LicenseWarning.builder()
                                .severity("MEDIUM")
                                .artifact(d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion())
                                .license("unknown")
                                .reason("License information not found in POM")
                                .recommendation("Add <licenses> section to dependency POM or replace with clearly licensed alternative")
                                .build());
                    }
                } else {
                    identified++;
                    byType.merge(license, 1, Integer::sum);
                }
            }
        }

        LicenseSummary summary = LicenseSummary.builder()
                .total(total)
                .identified(identified)
                .unknown(unknown)
                .byType(byType.isEmpty() ? null : byType)
                .build();

        boolean hasIncompat = false;
        int incompatCount = 0;
        // In this first iteration we do not detect incompatible licenses yet.

        LicenseCompliance compliance = LicenseCompliance.builder()
                .hasIncompatibleLicenses(hasIncompat)
                .incompatibleCount(incompatCount)
                .unknownCount(unknown)
                .commerciallyViable(!hasIncompat)
                .requiresAttribution(identified > 0)
                .build();

        return LicenseInfo.builder()
                .summary(summary)
                .details(details.isEmpty() ? null : details)
                .warnings(warnings.isEmpty() ? null : warnings)
                .compliance(compliance)
                .build();
    }

    private static String normalizeScope(String s) {
        if (s == null || s.isBlank()) return "compile"; // Maven default
        return s.trim().toLowerCase(Locale.ROOT);
    }
}

