package se.sprinto.hakan.chatapp.test;

import se.sprinto.hakan.chatapp.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Detta är till för att testa databasanslutningen.
 */

public class DatabaseTest {
    public static void main(String[] args) {
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next()) {
                System.out.println("✅ Databas är kopplad! Testresultat: " + rs.getInt(1));
            }
        } catch (Exception e) {
            System.err.println("❌ Kunde inte ansluta till databasen:");
            e.printStackTrace();
        }
    }
}
