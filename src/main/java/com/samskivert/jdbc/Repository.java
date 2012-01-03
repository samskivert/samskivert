//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;

/**
 * The repository class provides basic functionality upon which to build
 * an interface to a repository of information stored in a database (a
 * table or set of tables) that is accessed via JDBC.
 *
 * <p> It is expected that the repository class will encapsulate all
 * database access for a particular table or set of tables. The interface
 * provided to the rest of the application will involve only the
 * application object model. For example:
 *
 * <pre>
 * public class PeopleRepository extends SimpleRepository
 * {
 *     public Person getPerson (int personid);
 *     public Person[] getPeopleByFirstName (String firstName);
 *     public void updatePerson (Person person);
 * }
 * </pre>
 *
 * <p> It is probably also desirable to catch SQL exceptions and wrap them
 * in the <code>PersistenceException</code> class that is provided by this
 * package.
 *
 * <p> The repository comes in a few flavors depending on the needs of the
 * persistence services being developed:
 *
 * <ul>
 * <li>{@link SimpleRepository}: The simple repository is used by services
 * that need access to a single JDBC connection to perform their database
 * operations.</li>
 *
 * <li>{@link JORARepository}: JORA repository is used by services that
 * wish to make use of the JORA Java/RDBMS interoperability package.</li>
 *
 * <li> Because the repository provides a unified interface to a
 * particular persistence service, it is conceivable that it would need to
 * talk to multiple databases to provide those services. Presently, the
 * repository only supports a single connection, but if the need arose,
 * implementing a repository flavor that supported multiple connections
 * would be the proper solution.</li>
 * </ul>
 */
public class Repository
{
    /**
     * Creates and initializes the repository.
     *
     * @param provider the connection provider which will be used to
     * obtain our database connection.
     */
    public Repository (ConnectionProvider provider)
    {
        _provider = provider;
    }

    /**
     * Database operations should be encapsulated in instances of this
     * class and then provided to the repository for invocation. This
     * allows the repository to manage transaction commits for you as well
     * as for it to automatically retry an operation if the connection
     * failed for some transient reason.
     */
    public interface Operation<V>
    {
        /**
         * Invokes code that performs one or more database operations, all
         * of which will be encapsulated in a single transaction (which
         * can be retried in the event of a transient failure).
         *
         * @param conn the database connection on which the operations
         * will be performed.
         * @param liaison a database liaison for the supplied connection
         * which can be used to determine things for which there is no
         * standard way to determine via JDBC.
         *
         * @exception SQLException if thrown, this will be wrapped in a
         * {@link PersistenceException} before being passed up to the
         * operation invoker.
         * @exception PersistenceException can be thrown if something goes
         * awry when executing the operation. Note that the operation will
         * not be retried if a persistence exception is thrown. Such
         * exceptions are assumed to be application specific and not
         * indicative of a basic JDBC failure. The transaction
         * <em>will</em> be rolled back in such cases, however.
         */
        public V invoke (Connection conn, DatabaseLiaison liaison)
            throws SQLException, PersistenceException;
    }

    /** Our database connection provider. */
    protected ConnectionProvider _provider;
}
