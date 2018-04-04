package org.radarcns.auth.authorization;

import org.radarcns.auth.exception.NotAuthorizedException;
import org.radarcns.auth.token.RadarToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorization helper class for RADAR. This class checks if the authenticated user is allowed to
 * access the protected resources of a given subject based on the authorities and project
 * affiliations.
 */
public class RadarAuthorization {

    private static final Logger log = LoggerFactory.getLogger(RadarAuthorization.class);

    /**
     * Similar to {@link RadarToken#hasPermission(Permission)}, but this method throws an
     * exception rather than returning a boolean. Useful in combination with e.g. Spring's
     * controllers and exception translators.
     * @param token The token of the authenticated client
     * @param permission The permission to check
     * @throws NotAuthorizedException if the supplied token does not have the permission
     */
    public static void checkPermission(RadarToken token, Permission permission)
            throws NotAuthorizedException {
        log.debug("Checking permission {} for user {}", permission.toString(),
                token.getSubject());
        if (!token.hasPermission(permission)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s", token.getSubject(), permission.toString()));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnProject(Permission, String)}, but this method
     * throws an exception rather than returning a boolean. Useful in combination with e.g. Spring's
     * controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param projectName The project for which to check the permission
     * @throws NotAuthorizedException if the supplied token does not have the permission in the
     *     given project
     */
    public static void checkPermissionOnProject(RadarToken token, Permission permission,
            String projectName) throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} in project {}", permission.toString(),
                token.getSubject(), projectName);
        if (!token.hasPermissionOnProject(permission, projectName)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s in project %s", token.getSubject(), permission.toString(),
                    projectName));
        }
    }

    /**
     * Similar to {@link RadarToken#hasPermissionOnSubject(Permission, String, String)}, but this
     * method throws an exception rather than returning a boolean. Useful in combination with e.g.
     * Spring's controllers and exception translators.
     * @param token The token of the logged in user
     * @param permission The permission to check
     * @param projectName The project for which to check the permission
     * @param subjectName The name of the subject to check
     * @throws NotAuthorizedException if the supplied token does not have the permission in the
     *     given project for the given subject
     */
    public static void checkPermissionOnSubject(RadarToken token, Permission permission,
            String projectName, String subjectName) throws NotAuthorizedException {
        log.debug("Checking permission {} for user {} on subject {} in project {}",
                permission.toString(), token.getSubject(), subjectName, projectName);
        if (!token.hasPermissionOnSubject(permission, projectName, subjectName)) {
            throw new NotAuthorizedException(String.format("Client %s does not have "
                    + "permission %s on subject %s in project %s", token.getSubject(),
                    permission.toString(), subjectName, projectName));
        }
    }
}
