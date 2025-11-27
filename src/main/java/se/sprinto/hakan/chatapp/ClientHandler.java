package se.sprinto.hakan.chatapp;

import se.sprinto.hakan.chatapp.dao.*;
import se.sprinto.hakan.chatapp.model.Message;
import se.sprinto.hakan.chatapp.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private User user;

    private final UserDAO userDAO = new UserDatabaseDAO();
    private final MessageDAO messageDAO = new MessageDatabaseDAO();

    ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)

        ) {
            this.out = writer;

            writer.println("\uD83C\uDF38✨Välkommen! Har du redan ett konto? (ja/nej)\uD83C\uDF38✨");
            String answer;
            while (true) {
                answer = in.readLine();
                if (answer == null)
                    return;
                if (answer.equalsIgnoreCase("ja") || answer.equalsIgnoreCase("nej")) {
                    break;
                } else {
                    writer.println("Skriv 'ja' eller 'nej' tack! 💗");
                }
            }


            //==============INLOGGNING====================
                if ("ja".equalsIgnoreCase(answer)) {
                    user = null;
                    while (user == null) {
                        writer.println("Ange användarnamn:");
                        String username = in.readLine();
                        if (username == null) return;

                        writer.println("Ange lösenord:");
                        String password = in.readLine();
                        if (password == null) return;

                       user = userDAO.login(username, password);
                       // user =((UserDatabaseDAO) userDAO).loginWithMessages(username, password);

                        if (user == null) {
                            writer.println("❌💔 Fel användarnamn eller lösenord.");
                            writer.println("Avslutar sessionen. Försök igen! 🌸");

                        } else {
                            //tillagd av mig och return;
                            writer.println("Välkommen tillbaka, " + user.getUsername() + "!");
                            // ger användaer valmöjlighet (VG baserat)
                            writer.println("Vill du hämta dina meddelanden direkt (ja/nej)");
                            String fetchMsgs = in.readLine();
                            boolean loadMessages = fetchMsgs != null && fetchMsgs.equalsIgnoreCase("ja");

                            if (loadMessages) {
                                User fullUser = ((UserDatabaseDAO) userDAO)
                                        .loginWithMessages(user.getUsername(), password);
                                if (fullUser != null) {
                                    user = fullUser;
                                }
                                List<Message> messages = user.getMessages();
                                if (!messages.isEmpty()) {
                                    out.println("\uD83D\uDCDC Dina sparade meddelanden: ");
                                    for (Message m : messages) {
                                        out.println("[" + m.getTimestamp() + "] " + m.getText());
                                    }
                                } else {
                                    out.println("\uD83D\uDCEDInga sparade meddelanden.");
                                }
                            } else {
                                out.println("Du valde att logga in utan att hämta meddelanden.");
                            }
                        }
                    }


                    //===================REGISTRERING========================
                } else if ("nej".equalsIgnoreCase(answer)) {
                    user = null;
                    while (user == null) {

                        writer.println("Skapa nytt konto. Ange användarnamn:");
                        String username = in.readLine();
                        writer.println("Ange lösenord:");
                        String password = in.readLine();

                        user = userDAO.register(new User(username, password));
                        if (user == null) {
                            writer.println("❌💔Användarnamett är redan taget! försök igen");

                        }
                    }

                    writer.println("✨🎀 Konto skapat. Välkommen, " + user.getUsername() + "! 🎀✨");
                }

                //Chat info
                writer.println("✅ Du är inloggad som: " + user.getUsername());
                writer.println("Nu kan du börja skriva meddelanden 💬");
                writer.println("Skriv /quit för att avsluta 💗");
                writer.println("Skriv /mymsgs för att se dina sparade meddelanden 📜");

                out.println(user.getUsername() + " anslöt.");

                //huvudloop för chatwn
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        writer.println("👋✨ Hejdå!");
                        break;
                    }
                    if (message.equalsIgnoreCase("/mymsgs")) {
                        // Hämta meddelanden för denna användare
                        List<Message> messages = messageDAO.getMessagesByUserId(user.getId());
                        if (messages.isEmpty()) {
                            out.println("\uD83D\uDCEDInga sparade meddelanden.");
                        } else {
                            out.println("\uD83D\uDCDCDina meddelanden:");
                            for (Message m : messages) {
                                out.println("[" + m.getTimestamp() + "] " + m.getText());
                            }
                        }
                        continue;
                    }
                    // broadkasta till andra användare
                    server.broadcast("[" + user.getUsername() + "] " + message, this);
                    // spara meddelande till databasen
                    messageDAO.saveMessage(new Message(user.getId(), message, java.time.LocalDateTime.now()));
                }


            } catch(IOException e){
                System.out.println("Problem med klient: " + e.getMessage());
            } finally{
                server.removeClient(this);
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        void sendMessage (String msg){
            if (out != null) out.println(msg);
        }
    }



