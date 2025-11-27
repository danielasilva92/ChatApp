package se.sprinto.hakan.chatapp;

import se.sprinto.hakan.chatapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sprinto.hakan.chatapp.dao.MessageDatabaseDAO;
import se.sprinto.hakan.chatapp.dao.UserDatabaseDAO;
import se.sprinto.hakan.chatapp.model.Message;
import se.sprinto.hakan.chatapp.util.DatabaseUtil;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * enhetstester för både user och messageDAO
 * använder H2 databasen för att testa mina DAO klasser
 */

public class UserMessageDAOTest {
    /**
     * skapar tabeller i h2
     * körs för varje test
     */
    @BeforeEach
    void setup() throws Exception {
        System.out.println("Testdatabasen H2 fixas...");
        /**
         *   ansluter till h2 databasen
         *  med hjälp av databaseutil för att få connection( test properties)
         */

        // läser properties!

        try (InputStream testProps = getClass().getResourceAsStream("/test.properties")) {
            if (testProps != null) {
                DatabaseUtil.getInstance().loadPropertiesFromStream(testProps);
                System.out.println("✅ Test-properties inlästa");
            } else {
                throw new RuntimeException("❌ Kunde inte hitta test.properties");
            }

        }
        //skapar tabeller
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            //raderar eventuellt gammal tabell
            stmt.executeUpdate("DROP TABLE IF EXISTS messages");
            stmt.executeUpdate("DROP TABLE IF EXISTS users");

            //skapa user tabelen
            stmt.execute("CREATE TABLE users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(60) UNIQUE NOT NULL," +
                    "password VARCHAR(60) NOT NULL)");

            System.out.println("users tabell skapad i H2;P!");

            //skapa message tabellen
            stmt.execute("CREATE TABLE messages (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "user_id INT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "timestamp TIMESTAMP NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");

            System.out.println("Messages tabell skapad i H2;P!");

            System.out.println("Test databasen H2 fixat och REDO!");

        }
    }

/**
        *Ha med metoder till intergration mellan user and messge
 * testa att login fungerar
 * testa att man inte skapar dubbletter av typ användarnamn
 * */
@Test
void testUserAndMessageIntegration() {
    System.out.println("Testar att UserDAO fungerar med UserMessageDAO...");
    System.out.println("~~~~~~~~~~~~~~VG TESTET PÅ GÅÅÅÅÅÅÅÅÅNG~~~~~~~~~~~~~~~~");

    // skapar DAO insatser
    UserDatabaseDAO userDAO = new UserDatabaseDAO();
    MessageDatabaseDAO messageDAO = new MessageDatabaseDAO();

    System.out.println("DAO instansierade!");

    // Registera ny användare
    User newUser = new User("testuser3", "testpassword2");
    User registeredUser = userDAO.register(newUser);

  //verifiera att registering lyckas
    assertNotNull(registeredUser,"Användaren ska finnas i databasen");
    assertNotNull(registeredUser.getId(), "användarens id ska vara satt");
    assertTrue(registeredUser.getId() > 0, "ID ska vara större än 0");
    assertEquals("testuser3", registeredUser.getUsername(),"Användarnamet ska STÄMMA");

    System.out.println("användaren registrerad med ID: " + registeredUser.getId());

    //fångar upp Id
    int userId = registeredUser.getId();
    System.out.println("Användar-ID: " + userId);

    //skspar 2 medd kopplade till användaren
    Message message1 = new Message(userId, "Det FÖRSTA meddelandet", LocalDateTime.now());

    Message message2 = new Message(userId, "Det ANDRA meddelandet", LocalDateTime.now());

    System.out.println("två meddelanden skapade!");

    messageDAO.saveMessage(message1);
    messageDAO.saveMessage(message2);

    System.out.println("2 meddelanden sparade i H2!");

    // hämta ALLLLA meddelande för användarem
    List<Message> userMessages = messageDAO.getMessagesByUserId(userId);
    System.out.println("Meddelanden hämtaden från databasen!");
    System.out.println("antal meddelanden: " + userMessages.size());

    // verifiera att det finns JUST 2 MEDD
    assertEquals(2, userMessages.size(), "antal meddelanden ska vara 2");
    System.out.println("VERIFIERAT, det finns EXAKT 2 meddelanden ;)");

    // extra metod som  verifierar vilka medd
    Message fetchedMessage1 = userMessages.get(0);
    Message fetchedMessage2 = userMessages.get(1);
    assertEquals("Det FÖRSTA meddelandet", fetchedMessage1.getText(), "första meddelandes text ska stämma");
    assertEquals("Det ANDRA meddelandet", fetchedMessage2.getText(), "andra meddelandes text ska stämma");
    assertEquals(userId,fetchedMessage1.getUserId(), "Första medd userID ska stämma");
    assertEquals((userId),fetchedMessage2.getUserId(),"Andra medd userID ska stämma");

    System.out.println("Alla TESTER GOOOODKÄNDA WUHUHUHUHU!!!");
    System.out.println("VG-TEST KLART OCH EXTRA GODKÄÄÄÄNT ;D");

}
}
