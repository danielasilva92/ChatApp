package se.sprinto.hakan.chatapp.dao;


import se.sprinto.hakan.chatapp.model.Message;
import se.sprinto.hakan.chatapp.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database implementation av MessageDAO!!
 * hanterar lagring och hämtning av meddelanden från databasen.
 */

public class MessageDatabaseDAO implements MessageDAO{
    /**
     * Sparar ett meddelande till databasen.
     * @param message Message objelt att spara
     */
    @Override
    public void saveMessage(Message message) {
        String sql = "INSERT INTO messages (user_id, message, timestamp) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1,message.getUserId());
            stmt.setString(2,message.getText());
            //konvertera LocalDateTime till java.sql,Timestamp
            stmt.setTimestamp(3, Timestamp.valueOf(message.getTimestamp()));
             stmt.executeUpdate();

        } catch (SQLException e) {
           throw new RuntimeException("❌ Fel vid sparande av meddelande: " + e.getMessage());

        }

    }

    /**
     * hämta alla meddelanden för en specifik användare
     * @param userId användarens id
     * @return en lista med Message-objekt
     */
    @Override
    public List<Message> getMessagesByUserId(int userId) {
        String sql = """
                SELECT id, user_id, message, timestamp 
                FROM messages
                WHERE user_id = ?
                ORDER BY timestamp
                """;
        List<Message>  messages = new ArrayList<>();
            try(Connection conn = DatabaseUtil.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, userId);

                try(ResultSet rs = stmt.executeQuery()) {
                    while(rs.next()) {
                        //skapar message med konstruktor (id,userid,text och timstamp)
                        Message message = new Message(
                                rs.getInt("user_id"),
                                rs.getString("message"),
                                // konventerar till LocalDate
                                rs.getTimestamp("timestamp").toLocalDateTime()
                        );
                        message.setId(rs.getInt("id"));
                        messages.add(message);

                    }


            }
        } catch (SQLException e) {
               throw new RuntimeException("❌Fel vid hämtning av meddelanden: " + e.getMessage());

            }
        return messages;
    }
}
