package se.sprinto.hakan.chatapp.util;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton class för databasanslutning.
 * läser från properties-fil och ansluter till databasen
 * databasanslutningar till resten av applikationen.
 * grundkod gjort av Håkan Gleissman ;D
 */

public class DatabaseUtil {
    private static DatabaseUtil instance;
    private Properties properties;

    // privat konstuktor för singelton mönstret

    private DatabaseUtil() {
        properties = new Properties();
        try {
            String testProp = "/test.properties";
            InputStream testStream = getClass().getResourceAsStream(testProp);

            if (testStream != null) {
                properties.load(testStream);
                System.out.println("✅ Laddar H2 test-properties från " + testProp);

            } else {
                // ladda vanliga props när testprops inte gör de
                String orgProp = "/application.properties";
                InputStream orgStream = getClass().getResourceAsStream(orgProp);
                if (orgStream == null) {
                    throw new RuntimeException("❌ Kunde inte hitta application.properties");
                }
                properties.load(orgStream);
                System.out.println("✅ Laddar produktions-properties från " + orgProp);
            }
        } catch (IOException e) {
            throw new RuntimeException("❌ Kunde inte läsa properties-fil", e);
        }
    }

    // returnerar den ENDA instansen av databaseutil
    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }


    /**
     * returnerar en connection till databasen
     * @return connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, username, password);

    }

    /**
     * hjälpmetod för att stänga av databasanslutningen säkert
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
        /**
         * gör det möjligt att overrida properties
         */
       public void loadPropertiesFromStream(InputStream input) throws IOException{
           properties.load(input);

        }
    }

