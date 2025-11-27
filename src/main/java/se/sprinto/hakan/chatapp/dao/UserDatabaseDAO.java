package se.sprinto.hakan.chatapp.dao;
import se.sprinto.hakan.chatapp.model.Message;
import se.sprinto.hakan.chatapp.model.User;
import se.sprinto.hakan.chatapp.util.DatabaseUtil;
import se.sprinto.hakan.chatapp.util.PasswordUtil;

import java.sql.*;


/**
 * Databasimplentation av USERDAO!!
 * Hanterar användarinloggning och registrering mot databasen.
 */

public class UserDatabaseDAO implements UserDAO {
    /**
     * Loggar in användare med username och password.
     *
     * @param username
     * @param password
     * @return user-objekt om inloggning lyckas, null annars.
     */

    @Override
    public User login(String username, String password) {
        String sql = """
                SELECT id, username, password 
                FROM users 
                WHERE username = ?
                """;
        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

           try(ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
                   String storedHash = rs.getString("password");

                   // jämför
                   if (!PasswordUtil.verifyPassword(password, storedHash)) {
                       return null; // FEL LÖSEN!
                   }
                   User user = new User(
                           rs.getInt("id"),
                           rs.getString("username"),
                           storedHash
                   );
                   return user;
               }
           }

        } catch (SQLException e) {
            throw new RuntimeException("❌Kunde inte logga in: " + e.getMessage());

        }
        return null;
    }

    /**
     * Regristrerar en ny användare i databasen
     *
     * @param user User-objekt med användarnamn &lösenord
     * @return user-objekt med genererad id, null annars.
     */

    @Override
    public User register(User user) {
        String sql = """
        INSERT INTO users (username, password)
         VALUES (?, ?)
         """;

        String hashed = PasswordUtil.hashPassword(user.getPassword());

        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashed);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                return null;
            }
                // hämtar auto genererade ID från databasen
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));

                    }
                    return user;
                }

        } catch (SQLException e) {
            //MySQL error kod 1062: username redan finns (UNIK index)
            if (e.getErrorCode() == 1062) {
                return null;

            }

        }
       return null;

    }
        /**
         * För VG -> Login med JOIN som hämtar användaren tsm med meddelanden
         * @param username användarnamn
         * @param password lösenord
         * @return user objekt med meddelanden, null annars.
         */

        public User loginWithMessages (String username, String password){
            String sql = """
                    SELECT 
                    u.id AS user_id,
                    u.username, 
                    u.password AS password_hash,
                    m.id AS message_id,
                    m.user_id AS msg_user_id, 
                    m.message AS msg_text, 
                    m.timestamp 
                    FROM users u 
                    LEFT JOIN messages m ON u.id = m.user_id 
                    WHERE u.username = ?
                    ORDER BY m.timestamp;
                    """;
            try (Connection conn = DatabaseUtil.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    User user = null;
                    while (rs.next()) {
                        if (user == null) {
                            String hashed = rs.getString("password_hash");
                            if (!PasswordUtil.verifyPassword(password, hashed)) {
                                return null;
                            }
                            user = new User(
                                    rs.getInt("user_id"),
                                    rs.getString("username"),
                                    hashed
                            );
                        }
                        //kolla OM DE FINNS meddelanden
                        int messageId = rs.getInt("message_id");
                        if (!rs.wasNull()) {
                            Message message = new Message(
                                    rs.getInt("msg_user_id"),
                                    rs.getString("msg_text"),
                                    rs.getTimestamp("timestamp").toLocalDateTime());
                            user.addMessage(message);
                            message.setId(messageId);

                        }
                    }
                    {
                    }
                    return user;
                }
            } catch (SQLException e) {
                System.err.println("Fel vid inloggning med JOIN " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
