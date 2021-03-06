package com.bolyuba.nexus.plugin.npm.pkg;

import com.bolyuba.nexus.plugin.npm.NpmRepository;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

import javax.annotation.Nonnull;

/**
 * Url type as per http://wiki.commonjs.org/wiki/Packages/Registry#URLs
 *
 * @author <a href="mailto:georgy@bolyuba.com">Georgy Bolyuba</a>
 */
class PackageCoordinates {

    public static enum Type {

        REGISTRY_ROOT,
        PACKAGE_ROOT,
        PACKAGE_VERSION,
        REGISTRY_SPECIAL
    }

    private PackageCoordinates() {}

    private Type type;

    private String packageName;

    private String packageVersion;

    private String path;

    public String getPackageName() {
        return packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public String getPath() {
        return path;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PackageCoordinates{" +
                "type=" + type +
                ", packageName='" + packageName + '\'' +
                ", packageVersion='" + packageVersion + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    public static PackageCoordinates coordinatesFromUrl(@Nonnull String requestPath) throws InvalidPackageRequestException {
        PackageCoordinates coordinates = new PackageCoordinates();
        final String normalizedPath = requestPath.toLowerCase();
        coordinates.path = normalizedPath;

        if (RepositoryItemUid.PATH_SEPARATOR.equals(normalizedPath)) {
            coordinates.type = Type.REGISTRY_ROOT;
            return coordinates;
        }

        if (normalizedPath.startsWith(RepositoryItemUid.PATH_SEPARATOR + NpmRepository.NPM_REGISTRY_SPECIAL + RepositoryItemUid.PATH_SEPARATOR)) {
            coordinates.type = Type.REGISTRY_SPECIAL;
            return coordinates;
        }

        String correctedPath =
                normalizedPath.startsWith(RepositoryItemUid.PATH_SEPARATOR) ?
                        normalizedPath.substring(1, normalizedPath.length()) :
                        normalizedPath;
        String[] explodedPath = correctedPath.split(RepositoryItemUid.PATH_SEPARATOR);

        if (explodedPath.length == 2) {
            coordinates.type = Type.PACKAGE_VERSION;
            coordinates.packageName = validate(explodedPath[0], "Invalid package name: ");
            coordinates.packageVersion = validate(explodedPath[1], "Invalid package version: ");
            return coordinates;
        }
        if (explodedPath.length == 1) {
            coordinates.type = Type.PACKAGE_ROOT;
            coordinates.packageName = validate(explodedPath[0], "Invalid package name: ");
            return coordinates;
        }

        throw new InvalidPackageRequestException("Path " + requestPath + " cannot be turned into PackageCoordinates");
    }

    /**
     * See http://wiki.commonjs.org/wiki/Packages/Registry#Changes_to_Packages_Spec
     */
    private static String validate(@Nonnull String nameOrVersion, String errorPrefix) throws InvalidPackageRequestException {
        if (nameOrVersion.startsWith(NpmRepository.NPM_REGISTRY_SPECIAL)) {
            throw new InvalidPackageRequestException(errorPrefix + nameOrVersion);
        }
        if (nameOrVersion.equals(".")) {
            throw new InvalidPackageRequestException(errorPrefix + nameOrVersion);
        }
        if (nameOrVersion.equals("..")) {
            throw new InvalidPackageRequestException(errorPrefix + nameOrVersion);
        }
        return nameOrVersion;
    }
}
