//
// $Id: RDJPrefs.java,v 1.2 2003/05/07 17:27:26 mdb Exp $

package robodj.util;

import java.util.Properties;

import com.samskivert.util.Config;

/**
 * Contains settings and preferences for this RoboDJ installation.
 */
public class RDJPrefs
{
    /** Configuration key for {@link #getRepositoryDirectory}. */
    public static final String REPO_DIR_KEY = "repository.basedir";

    /** Configuration key for {@link #getRepositoryTemp}. */
    public static final String REPO_TMPDIR_KEY = "repository.tmpdir";

    /** Configuration key for {@link #getJDBCConfig}. */
    public static final String JDBC_DRIVER_KEY = "jdbc.default.driver";

    /** Configuration key for {@link #getJDBCConfig}. */
    public static final String JDBC_USERNAME_KEY = "jdbc.default.username";

    /** Configuration key for {@link #getJDBCConfig}. */
    public static final String JDBC_PASSWORD_KEY = "jdbc.default.password";

    /** Configuration key for {@link #getJDBCConfig}. */
    public static final String JDBC_URL_KEY = "jdbc.default.url";

    /** Configuration key for {@link #getMusicDaemonHost}. */
    public static final String MUSICD_HOST_KEY = "musicd.host";

    /** Configuration key for {@link #getMusicDaemonPort}. */
    public static final String MUSICD_PORT_KEY = "musicd.port";

    /** Configuration key for {@link #getCDDBHost}. */
    public static final String CDDB_HOST_KEY = "cddb.host";

    /** Configuration key for {@link #getUser}. */
    public static final String USER_KEY = "user";

    /** Provides access to configuration data. */
    public static Config config = new Config("robodj/config");

    /**
     * Returns the repository directory name.
     */
    public static String getRepositoryDirectory ()
    {
        return config.getValue(REPO_DIR_KEY, "");
    }

    /**
     * Returns a temporary directory into which we can store ripped tracks.
     */
    public static String getRepositoryTemp ()
    {
        return config.getValue(
            REPO_TMPDIR_KEY, System.getProperty("java.io.tmpdir"));
    }

    /**
     * Returns the JDBC configuration for this RoboDJ installation.
     */
    public static Properties getJDBCConfig ()
    {
        return config.getSubProperties("jdbc");
    }

    /**
     * Returns the host by which we connect to the music daemon.
     */
    public static String getMusicDaemonHost ()
    {
        return config.getValue(MUSICD_HOST_KEY, "");
    }

    /**
     * Returns the port by which we connect to the music daemon.
     */
    public static int getMusicDaemonPort ()
    {
        return config.getValue(MUSICD_PORT_KEY, 2500);
    }

    /**
     * Returns the hostname of the CDDB server we use to look up album and
     * track names.
     */
    public static String getCDDBHost ()
    {
        return config.getValue(CDDB_HOST_KEY, "");
    }

    /**
     * Returns our locally configured username.
     */
    public static String getUser ()
    {
        return config.getValue(USER_KEY, "");
    }
}
