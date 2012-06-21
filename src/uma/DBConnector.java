/*
 *  Database connection pool example.
 *    Copyright (C) 2010  Antonio César Gómez Lora.
 *
 *    "This program" refers to all clases included in es.uma.lcc.lbd.samples
 *    package. The classes in com.rgagnon.howto package, distributed with
 *    this program, are based on Réal Gagnon's how-to examples, and they are not
 *    under the terms of the GNU General Public License. Réal Gagnon allows
 *    the use of this classes with no restrictions, but he appreciated a
 *    mention.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uma;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Antonio César Gómez Lora
 * @version 0.1
 * @date 2010-05-27
 */
final public class DBConnector implements Connector {

    public static int DEFAULT_MAX_CONNECTIONS = 1;
    private int connections = DEFAULT_MAX_CONNECTIONS;
    final private Vector<Connection> pool = new Vector<Connection>();
    final private Vector<Connection> locked = new Vector<Connection>();
    private ConnectionManager connectionManager;
    private Logger logger = null;

    public DBConnector() throws FileNotFoundException, IOException, IllegalArgumentException, IllegalStateException, SQLException {
        this(DEFAULT_MAX_CONNECTIONS);
    }

    public DBConnector(ConnectionManager connectionManger) throws IllegalArgumentException, IllegalStateException, SQLException {
        this(DEFAULT_MAX_CONNECTIONS, connectionManger);
    }

    public DBConnector(int connections) throws FileNotFoundException, IOException, IllegalArgumentException, IllegalStateException, SQLException {
        this(connections, new ConnectionManager());
    }

    public DBConnector(int connections, ConnectionManager connectionManager) throws IllegalArgumentException, IllegalStateException, SQLException {
        if (connections == 0) {
            throw new IllegalArgumentException();
        }
        this.connections = connections;
        this.connectionManager = connectionManager;
        for(int i = 0; i < connections; i++) {
            pool.add(connectionManager.connect());
        }
    }

    public void connect() {

    }

    @Override
    public void finalize() {
        synchronized (locked) {
            int lockedcount = locked.size();
            if (connections > 0) {
                int poolcount = pool.size();
                int totalcount = poolcount + lockedcount;
                if (totalcount != connections && logger != null) {
                    logger.log(Level.SEVERE, (totalcount - connections) + " connections lost.");
                }
            }
            if (lockedcount > 0 && logger != null) {
                logger.log(Level.SEVERE, lockedcount + " unreleased connections.");
            }
            while (!pool.isEmpty()) {
                try {
                    pool.firstElement().close();
                } catch (SQLException e) {
                    if (logger != null) {
                        logger.log(Level.WARNING, "Error when closing pooled connection: " + e.getMessage());
                    }
                }
            }
            while (!locked.isEmpty()) {
                try {
                    locked.firstElement().close();
                } catch (SQLException e) {
                    if (logger != null) {
                        logger.log(Level.WARNING, "Error when closing locked connection: " + e.getMessage());
                    }
                }
            }
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Connection lockConnection() throws InterruptedException, SQLException {
        Connection connection;
        if (connections >= 0) {
            synchronized (pool) {
                while (pool.isEmpty()) {
                    if (logger != null) {
                        logger.log(Level.FINE, "Thread "
                                + Thread.currentThread()
                                + " waiting por DB Connection");
                    }
                    pool.wait();
                }
                connection = pool.get(1);
                pool.remove(1);
                locked.add(connection);
            }
        } else {
            connection = connectionManager.connect();
            synchronized (locked) {
                locked.add(connection);
            }
        }
        return connection;
    }

    public void releaseConnection(Connection connection) throws SQLException {
        synchronized (pool) {
            int index = locked.indexOf(connection);
            if (index >= 0) {
                locked.remove(index);
                if (connections > 0) {
                    pool.add(connection);
                    pool.notify();
                } else {
                    connection.close();
                }
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public void setLogger(Logger logger) {
        if (this.logger != logger && this.logger != null) {
            logger.log(Level.FINEST, "logger ends and changes to " + logger);
        }
        this.logger = logger;
        if (logger != null) {
            logger.log(Level.FINEST, "logging starts");
        }
    }

}
