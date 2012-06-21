/*
 *  Database parameter manager and connection class example.
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

import encrypt.CryptoUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Antonio César Gómez Lora
 * @version 0.1
 * @date 2010-05-27
 */
public class ConnectionManager {

    public final static String DEFAULTPROPERTIESFILENAME = "connection.properties";
    public final static String CONNECTION = "connection";
    public final static String DRIVER = "driver";
    public final static String KEYFILE = "keyfile";
    public final static String PASSWORD = "password";
    public final static String PASSWORDFILE = "passwordfile";
    public final static String USER = "user";
    private String connection;
    private String driver;
    private String keyFile;
    private String password;
    private String passwordFile;
    private String propertiesFile;
    private String user;

    public ConnectionManager() throws FileNotFoundException, IOException {
        this(DEFAULTPROPERTIESFILENAME);
    }

    public ConnectionManager(String propertiesFile) throws FileNotFoundException, IOException {
        load(propertiesFile);
    }

    public Connection connect() throws IllegalStateException, SQLException {
        Class cl;
        Constructor co;
        Driver d;
        try {
            cl = Class.forName(driver);
            co = cl.getConstructor((Class[]) null);
            d = (Driver) co.newInstance((Object[]) null);
        } catch (Exception exception) {
            throw new IllegalStateException("Exception creating instance of driver " + driver, exception);
        }
        DriverManager.registerDriver(d);
        return DriverManager.getConnection(connection, user, password);
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getKeyFile() {
        return keyFile;
    }

    void setKeyFile(String keyfile) {
        this.keyFile = keyfile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile) throws FileNotFoundException, IOException {
        this.propertiesFile = propertiesFile;
        load();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void load() throws FileNotFoundException, IOException {
        BufferedReader r = new BufferedReader(new FileReader(this.propertiesFile));
        load(r);
        r.close();
    }

    public void load(Reader r) throws IOException {
        Properties connectionProperties = new Properties();
        connectionProperties.load(r);
        connection = connectionProperties.getProperty(CONNECTION);
        driver = connectionProperties.getProperty(DRIVER);
        keyFile = connectionProperties.getProperty(KEYFILE);
        password = connectionProperties.getProperty(PASSWORD);
        passwordFile = connectionProperties.getProperty(PASSWORDFILE);
        user = connectionProperties.getProperty(USER);
        if (passwordFile != null) {
            BufferedReader passReader = new BufferedReader(new FileReader(passwordFile));
            password = passReader.readLine();
            passReader.close();
        }
        if (keyFile != null) {
            File kf = new File(keyFile);
            byte[] decrypted = null;
            try {
                SecretKeySpec sks = CryptoUtils.getSecretKeySpec(kf);
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, sks);
                decrypted = cipher.doFinal(CryptoUtils.hexStringToByteArray(password));
            } catch (Exception exception) {
                throw new IllegalStateException("Decrypter fails", exception);
            }
            password = new String(decrypted);
        }
    }

    public void load(String propertiesFile) throws FileNotFoundException, IOException {
        this.propertiesFile = propertiesFile;
        load();
    }

    public void store() throws IOException {
        FileWriter fw = new FileWriter(propertiesFile);
        store(fw);
        fw.close();
    }

    public void store(String propertiesFile) throws IOException {
        this.propertiesFile = propertiesFile;
        store();
    }

    public void store(Writer fw) throws IOException {
        Properties connectionProperties = new Properties();
        if (connection != null) {
            connectionProperties.setProperty(CONNECTION, connection);
        }
        if (driver != null) {
            connectionProperties.setProperty(DRIVER, driver);
        }
        if (password != null) {
            if (keyFile != null) {
                try {
                    SecretKeySpec sks = CryptoUtils.getSecretKeySpec(new File(keyFile));
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
                    byte[] encrypted = cipher.doFinal(password.getBytes());
                    password = CryptoUtils.byteArrayToHexString(encrypted);
                    connectionProperties.setProperty(PASSWORD, password);
                    connectionProperties.setProperty(KEYFILE, keyFile);
                } catch (Exception exception) {
                    throw new IllegalStateException("Encrypter fails", exception);
                }
            }
            if (passwordFile != null) {
                PrintWriter passWriter = new PrintWriter(new FileWriter(passwordFile));
                passWriter.print(password);
                passWriter.close();
                connectionProperties.setProperty(PASSWORDFILE, passwordFile);
            } else {
                connectionProperties.setProperty(PASSWORD, password);
            }
        }
        if (user != null) {
            connectionProperties.setProperty(USER, user);
        }
        connectionProperties.store(fw, "Connection Data");
    }

    public void storeSecure() throws IOException {
        FileWriter fw = new FileWriter(propertiesFile);
        storeSecure(fw);
        fw.close();
    }

    public void storeSecure(String propertiesFile) throws IOException {
        this.propertiesFile = propertiesFile;
        storeSecure();
    }

    public void storeSecure(Writer fw) throws IOException {
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (Exception exception) {
            throw new IllegalStateException("Key generation fails", exception);
        }
        keyGen.init(128);
        SecretKey sk = keyGen.generateKey();
        keyFile = "connection.key";
        FileWriter fwk = new FileWriter(new File(keyFile));
        fwk.write(CryptoUtils.byteArrayToHexString(sk.getEncoded()));
        fwk.flush();
        fwk.close();
        store(fw);
    }
}
