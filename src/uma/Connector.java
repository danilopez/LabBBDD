/*
 *  Connector interface example.
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

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Antonio César Gómez Lora
 * @version 0.1
 * @date 2010-05-27
 */
public interface Connector {

    public Connection lockConnection() throws SQLException, InterruptedException;

    public void releaseConnection(Connection connection) throws SQLException;
}
