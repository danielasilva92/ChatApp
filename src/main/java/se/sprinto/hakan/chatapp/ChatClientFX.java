package se.sprinto.hakan.chatapp;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX-baserad chatt-klient med grafiskt användargränssnitt.
 * Ansluter till ChatServer och tillhandahåller
 * en animerad UI för inloggning, registrering och chatting.
 *
 * Designen är ett "Midnight Neon"-tema med rosa och lila nyanser.
 */
public class ChatClientFX extends Application {
    // Nätverkskomponenter för serveranslutning
    private Socket socket;              // Socket-anslutning till servern
    private PrintWriter out;            // För att skicka meddelanden till servern
    private BufferedReader in;          // För att läsa meddelanden från servern

    // UI-komponenter
    private VBox chatContainer;
    private ScrollPane chatScrollPane;
    private TextArea chatArea;          // Visar chatthistoriken
    private TextField messageField;     // Textfält för att skriva meddelanden
    private Label statusLabel;          // Visar anslutningsstatus
    private String currentUser;         // Nuvarande inloggad användare

    // Färgpalett: Midnight Neon-tema
    private static final String MIDNIGHT_BG = "#0A0A23";        // Mörkblå bakgrund
    private static final String PANEL_BG = "#1E1E3F";           // Panelbakgrund
    private static final String NEON_PINK = "#FF2E88";          // Neonrosa accent
    private static final String NEON_PURPLE = "#A970FF";        // Neonlila accent
    private static final String TEXT_WHITE = "#F4F4F8";         // Vit text
    private static final String SUCCESS_GREEN = "#3EF28C";      // Grön för framgång
    private static final String ERROR_RED = "#FF5572";          // Röd för fel
    private static final String SOFT_GRAY = "#9AA0B3";          // Grå för subtila element
    private static final String INPUT_BG = "#101028";// Mörk bakgrund för inmatning


    /**
     * Startpunkt för JavaFX-applikationen ;D
     * huvudfönstret och visar inloggningsskärmen.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("💬 Danielas Premium Chat 💬");
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(650);
        showLoginScreen(primaryStage);
    }

    /**
     * Visar inloggnings- och registreringsskärmen med animationer.
     * Hanterar användarens val mellan inloggning och ny kontoregistrering.
     */
    private void showLoginScreen(Stage stage) {
        // Rot-container med gradient-bakgrund
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, " + MIDNIGHT_BG + ", #0E1030);");

        // Underliggande blur-effekt för djup
        Pane underlay = new Pane();
        underlay.setPrefSize(1200, 900);
        underlay.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(169,112,255,0.06), rgba(255,46,136,0.04));");
        underlay.setEffect(new GaussianBlur(30));
        root.getChildren().add(underlay);

        // Huvudcontainer för inloggningsformuläret (glassmorfism-effekt)
        VBox loginBox = new VBox(18);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(36));
        loginBox.setMaxWidth(520);
        loginBox.setStyle(
                "-fx-background-color: rgba(30,30,63,0.75);" +  // Halvtransparent panel
                        "-fx-background-radius: 20;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-color: rgba(255,255,255,0.06);" +
                        "-fx-border-width: 1;"
        );
        loginBox.setEffect(new GaussianBlur(1.2));  // lite blur för glaseffekt

        // Titel med pulserande glöd-effekt
        StackPane titleStack = new StackPane();
        Label titleLabel = new Label("✨ DANIS CHAT ✨");
        titleLabel.setFont(Font.font("Inter", FontWeight.EXTRA_BOLD, 42));
        titleLabel.setTextFill(Color.web(TEXT_WHITE));

        // Skapa och animera glöd-effekt
        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web(NEON_PINK));
        titleGlow.setSpread(0.25);
        titleGlow.setRadius(8);
        titleLabel.setEffect(titleGlow);

        // Animation som får glöden att pulsera
        Timeline glowPulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(titleGlow.radiusProperty(), 6)),
                new KeyFrame(Duration.seconds(1.6), new KeyValue(titleGlow.radiusProperty(), 20))
        );
        glowPulse.setAutoReverse(true);
        glowPulse.setCycleCount(Animation.INDEFINITE);
        glowPulse.play();

        titleStack.getChildren().add(titleLabel);

        // Välkomsttext
        Label welcomeLabel = new Label("Välkommen — wuuuhhuuu logga in & chatta");
        welcomeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        welcomeLabel.setTextFill(Color.web(SOFT_GRAY));

        // Visuell separator
        Separator separator = new Separator();
        separator.setMaxWidth(320);
        separator.setStyle("-fx-background-color: rgba(169,112,255,0.12);");

        // === ANVÄNDARNAMN-FÄLT ===
        VBox usernameBox = new VBox(6);
        Label userLabel = new Label("Användarnamn");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        userLabel.setTextFill(Color.web(SOFT_GRAY));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Ditt användarnamn");
        usernameField.setPrefHeight(44);
        styleTextFieldModern(usernameField);  // Applicera custom styling

        usernameBox.getChildren().addAll(userLabel, usernameField);

        // === LÖSENORDS-FÄLT ===
        VBox passwordBox = new VBox(6);
        Label passLabel = new Label("Lösenord");
        passLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        passLabel.setTextFill(Color.web(SOFT_GRAY));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Lösenord");
        passwordField.setPrefHeight(44);
        styleTextFieldModern(passwordField);

        passwordBox.getChildren().addAll(passLabel, passwordField);

        // === KNAPP-RAD (Logga in & Skapa konto) ===
        HBox buttonRow = new HBox(12);
        buttonRow.setAlignment(Pos.CENTER);

        Button loginButton = createMegaButton("🔓 LOGGA IN");
        Button registerButton = createMegaButton("⭐ SKAPA KONTO");

        buttonRow.getChildren().addAll(loginButton, registerButton);

        // === STATUS-LABEL (för felmeddelanden och bekräftelser) ===
        Label loginStatus = new Label();
        loginStatus.setFont(Font.font("System", FontWeight.BOLD, 13));
        loginStatus.setTextFill(Color.web(SOFT_GRAY));
        loginStatus.setWrapText(true);
        loginStatus.setMaxWidth(420);

        // === EVENT HANDLER: LOGGA IN ===
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            // Validera input FÖRE animation
            if (username.isEmpty() || password.isEmpty()) {
                showTemporaryStatus(loginStatus, "⚠️ Fyll i både användarnamn och lösenord!", ERROR_RED);
                shakeNode(loginBox);  // Skaka boxen för att indikera fel
                return;
            }

            // Inaktivera knapp och visa laddningsindikator
            loginButton.setDisable(true);
            loginButton.setText("🔄 ANSLUTER...");

            // Rotera knappen för att visa aktivitet
            RotateTransition rot = new RotateTransition(Duration.seconds(0.9), loginButton);
            rot.setByAngle(360);
            rot.setCycleCount(1);
            rot.setOnFinished(ev -> loginButton.setRotate(0));
            rot.play();

            // Anslut till servern i en separat tråd för att inte frysa UI
            new Thread(() -> {
                boolean success = connectToServer(username, password, true);  // true = inloggning

                // Uppdatera UI på JavaFX Application Thread
                Platform.runLater(() -> {
                    if (success) {
                        showTemporaryStatus(loginStatus, "✅ Inloggning lyckades!", SUCCESS_GREEN);

                        // Fade till chattskärmen
                        FadeTransition ft = new FadeTransition(Duration.seconds(0.35), loginBox);
                        ft.setFromValue(1);
                        ft.setToValue(0);
                        ft.setOnFinished(ev -> showChatScreen(stage));
                        ft.play();
                    } else {
                        showTemporaryStatus(loginStatus, "❌ Fel användarnamn eller lösenord!", ERROR_RED);
                        shakeNode(loginBox);
                        loginButton.setDisable(false);
                        loginButton.setText("🔓 LOGGA IN");
                    }
                });
            }).start();
        });

        // === EVENT HANDLER: SKAPA KONTO ===
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            // Validera input FÖRE animation
            if (username.isEmpty() || password.isEmpty()) {
                showTemporaryStatus(loginStatus, "⚠️ Fyll i både användarnamn och lösenord!", ERROR_RED);
                shakeNode(loginBox);
                return;
            }
            if (password.length() < 3) {
                showTemporaryStatus(loginStatus, "⚠️ Lösenordet måste vara minst 3 tecken!", ERROR_RED);
                shakeNode(passwordField);
                return;
            }

            // Inaktivera knapp och visa laddning
            registerButton.setDisable(true);
            registerButton.setText("🔄 SKAPAR...");

            // Skala knappen för visuell feedback
            ScaleTransition st = new ScaleTransition(Duration.millis(220), registerButton);
            st.setFromX(1);
            st.setFromY(1);
            st.setToX(1.08);
            st.setToY(1.08);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();

            // Anslut och registrera i separat tråd
            new Thread(() -> {
                boolean success = connectToServer(username, password, false);  // false = registrering

                Platform.runLater(() -> {
                    if (success) {
                        showTemporaryStatus(loginStatus, "✅ Konto skapat! Välkommen!", SUCCESS_GREEN);

                        // Vänta lite innan övergång till chat
                        PauseTransition p = new PauseTransition(Duration.seconds(0.8));
                        p.setOnFinished(ev -> {
                            FadeTransition ft = new FadeTransition(Duration.seconds(0.35), loginBox);
                            ft.setFromValue(1);
                            ft.setToValue(0);
                            ft.setOnFinished(ev2 -> showChatScreen(stage));
                            ft.play();
                        });
                        p.play();
                    } else {
                        showTemporaryStatus(loginStatus, "❌ Användarnamnet är upptaget! ", ERROR_RED);
                        shakeNode(loginBox);
                        registerButton.setDisable(false);
                        registerButton.setText("⭐ SKAPA KONTO");
                    }
                });
            }).start();
        });

        // Tillåt Enter-tangent för att logga in
        passwordField.setOnAction(e -> loginButton.fire());

        // Lägg till alla komponenter i loginBox
        loginBox.getChildren().addAll(titleStack, welcomeLabel, separator, usernameBox, passwordBox, buttonRow, loginStatus);
        root.getChildren().add(loginBox);

        // === INGÅNGS-ANIMATION ===
        loginBox.setOpacity(0);
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.6), loginBox);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.6), loginBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition entrance = new ParallelTransition(slideUp, fadeIn);
        entrance.play();

        // Skapa och visa scenen
        Scene scene = new Scene(root, 900, 720);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Visar chattskärmen efter lyckad inloggning/registrering.
     * Innehåller chattområde, meddelandefält och kontroller.
     */
    private void showChatScreen(Stage stage) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom right, " + MIDNIGHT_BG + ", #08081A);");

        // === TOPPRAD (Header med logga ut-knapp) ===
        HBox topBar = createEnhancedTopBar();
        mainLayout.setTop(topBar);

        // === CENTER: CHATTOMRÅDE ===
        VBox centerBox = new VBox(14);
        centerBox.setPadding(new Insets(18));


        Label chatTitle = new Label("\uD83D\uDCACChat");
        chatTitle.setFont(Font.font("System", FontWeight.BOLD, 26));
        chatTitle.setTextFill(Color.web(TEXT_WHITE));

        // --- Chat-yta ---
        chatContainer = new VBox(12);
        chatContainer.setPadding(new Insets(14));
        chatContainer.setStyle("-fx-background-color: " + PANEL_BG + ";");


        chatScrollPane = new ScrollPane(chatContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle(
                "-fx-background: " + PANEL_BG + "; + " +
                        "-fx-border-color: rgba(169, 112,255,0.12);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );


        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);
        centerBox.getChildren().addAll(chatTitle, chatScrollPane);
        mainLayout.setCenter(centerBox);



        // === BOTTEN: MEDDELANDEINPUT OCH KONTROLLER ===
        VBox bottomBox = new VBox(8);
        bottomBox.setPadding(new Insets(14));
        bottomBox.setStyle("-fx-background-color: linear-gradient(to right, rgba(169,112,255,0.06), rgba(255,46,136,0.03));");

        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        // Ikon för meddelande
        Label msgIcon = new Label("✉️");
        msgIcon.setFont(Font.font(22));


        // Emoji Picker knapp
        Button emojiButton = new Button("😊");
        emojiButton.setPrefSize(44, 44);
        emojiButton.setStyle(
                "-fx-background-color: "+ NEON_PINK + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: 18px;"
        );

        emojiButton.setOnAction(e -> showEmojiPicker());

        // Textfält för att skriva meddelanden
        messageField = new TextField();
        messageField.setPromptText("Skriv ditt meddelande här...");
        messageField.setPrefHeight(46);
        HBox.setHgrow(messageField, Priority.ALWAYS);  // Textfältet växer för att fylla utrymmet
        styleTextFieldModern(messageField);

        // Skicka-knapp
        Button sendButton = createSendButton();
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());  // Enter-tangent skickar också


        // Knapp för att hämta sparade meddelanden
        Button myMsgsButton = new Button("📜 MINA MSG");
        myMsgsButton.setPrefHeight(44);
        myMsgsButton.setPrefWidth(120);
        myMsgsButton.setStyle(
                "-fx-background-color: linear-gradient(to right, " + NEON_PURPLE + ", " + NEON_PINK + ");" +
                        "-fx-text-fill: " + TEXT_WHITE + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );

        // Handler för att hämta sparade meddelanden
        myMsgsButton.setOnAction(e -> {
            if (out != null) {
                out.println("/mymsgs");  // Skicka kommando till servern
            }
        });
        // Blixt-ikon för skicka-animation (initialt dold)
        Label flash = new Label("⚡");
        flash.setFont(Font.font(20));
        flash.setVisible(false);
        flash.setOpacity(0);

        inputRow.getChildren().addAll(msgIcon, emojiButton, messageField, sendButton, myMsgsButton, flash);

        // === STATUS-RAD (Visar anslutningsstatus) ===
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(6, 0, 0, 4));

        Label statusIcon = new Label("🟢");  // Grön cirkel = ansluten
        statusIcon.setFont(Font.font(14));

        statusLabel = new Label((currentUser != null ? "Ansluten som " + currentUser : "Inte ansluten"));
        statusLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        statusLabel.setTextFill(Color.web(NEON_PINK));

        statusRow.getChildren().addAll(statusIcon, statusLabel);

        bottomBox.getChildren().addAll(inputRow, statusRow);
        mainLayout.setBottom(bottomBox);

        // Skapa och visa scenen
        Scene scene = new Scene(mainLayout, 1000, 720);
        stage.setScene(scene);

        // Hantera fönsterstängning: koppla från servern
        stage.setOnCloseRequest(e -> disconnect());

        // Fade-in animation för chattskärmen
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.45), mainLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Starta tråd som lyssnar på meddelanden från servern
       startMessageListener(); // ALERTAAAAA
       addSystemMessage("💖 Välkommen till Danis Rosa Chat, " + currentUser + "! 💖");
        stage.show();
    }

    /**
     * Visar emoji picker popup med text-baserade emojis som fungerar överallt
     */
    private void showEmojiPicker() {
        Stage emojiStage = new Stage();
        emojiStage.setTitle("Välj Emoji");
        emojiStage.initOwner(messageField.getScene().getWindow());

        VBox emojiBox = new VBox(12);
        emojiBox.setPadding(new Insets(20));
        emojiBox.setStyle("-fx-background-color: rgba(30,30,63,0.75);" +  // Halvtransparent panel
                "-fx-background-radius: 20;" +
                "-fx-border-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-width: 1;"
        );


        Label title = new Label("✨ Välj en Emoji ✨");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.web(TEXT_WHITE));
        title.setAlignment(Pos.CENTER);

        // Emoji-kategorier med text och unicode
        String[][] emojiData = {
                // [Display Text, Unicode Symbol]
                {"😀 Grin", "😀"}, {"😁 Leende", "😁"}, {"😂 Gråt", "😂"}, {"🤣 Skratt", "🤣"},
                {"😃 Glad", "😃"}, {"😄 Le", "😄"}, {"😅 Svettas", "😅"}, {"😆 Flin", "😆"},
                {"😉 Blinka", "😉"}, {"😊 Lycklig", "😊"}, {"😋 Slicka", "😋"}, {"😎 Cool", "😎"},
                {"😍 Älska", "😍"}, {"😘 Puss", "😘"}, {"🥰 Kär", "🥰"}, {"😗 Kyss", "😗"},
                {"🤔 Tänka", "🤔"}, {"🤨 Tvek", "🤨"}, {"😐 Neutral", "😐"}, {"😑 Uttråk", "😑"},
                {"😮 Wow", "😮"}, {"😯 Chock", "😯"}, {"😪 Trött", "😪"}, {"😫 Utmat", "😫"},
                {"😴 Sova", "😴"}, {"😌 Nöjd", "😌"}, {"😛 Tunga", "😛"}, {"😜 Blink", "😜"},
                {"😒 Missnöjd", "😒"}, {"😓 Kallsvett", "😓"}, {"😔 Ledsen", "😔"}, {"😕 Förvirr", "😕"},
                {"😤 Arg", "😤"}, {"😢 Gråt", "😢"}, {"😭 Sörj", "😭"}, {"😦 Oro", "😦"},
                {"😩 Panik", "😩"}, {"🤯 Mind", "🤯"}, {"😬 Grin", "😬"}, {"😰 Rädd", "😰"},
                {"❤️ Hjärta", "❤️"}, {"💕 Kärlek", "💕"}, {"💖 Glitter", "💖"}, {"💗 Pulser", "💗"},
                {"💙 Blått", "💙"}, {"💚 Grönt", "💚"}, {"💛 Gult", "💛"}, {"🧡 Orange", "🧡"},
                {"💜 Lila", "💜"}, {"🖤 Svart", "🖤"}, {"👍 Tumme", "👍"}, {"👎 Ner", "👎"},
                {"👌 OK", "👌"}, {"✌️ Peace", "✌️"}, {"🤞 Kors", "🤞"}, {"🤟 Kärlek", "🤟"},
                {"👋 Vinka", "👋"}, {"💪 Stark", "💪"}, {"🎉 Party", "🎉"}, {"🎊 Fest", "🎊"},
                {"🎈 Ballong", "🎈"}, {"🎁 Present", "🎁"}, {"🏆 Trofe", "🏆"}, {"🥇 Guld", "🥇"},
                {"⭐ Stjärna", "⭐"}, {"🌟 Glow", "🌟"}, {"✨ Sparkle", "✨"}, {"💫 Dizzy", "💫"},
                {"🔥 Eld", "🔥"}, {"💥 Boom", "💥"}, {"💯 100", "💯"}, {"✅ Check", "✅"},
                {"❌ X", "❌"}, {"🚀 Raket", "🚀"}, {"☕ Kaffe", "☕"}, {"🍕 Pizza", "🍕"},
                {"🍔 Burger", "🍔"}, {"🍰 Tårta", "🍰"}, {"🎂 Fest", "🎂"}, {"🍾 Champ", "🍾"}
        };

        GridPane emojiGrid = new GridPane();
        emojiGrid.setHgap(6);
        emojiGrid.setVgap(6);
        emojiGrid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
        for (String[] emoji : emojiData) {
            String displayText = emoji[0];
            String unicodeSymbol = emoji[1];

            Button emojiBtn = new Button(displayText);
            emojiBtn.setFont(Font.font("System", FontWeight.NORMAL, 12));
            emojiBtn.setPrefSize(90, 45);
            emojiBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + NEON_PURPLE + ", " + NEON_PINK + ");" +
                            "-fx-text-fill: " + TEXT_WHITE + ";" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;"
            );

            emojiBtn.setOnMouseEntered(e -> {
                emojiBtn.setStyle(
                        "-fx-background-color: " + NEON_PURPLE + ";" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-text-fill: " + TEXT_WHITE + ";"
                );
                ScaleTransition st = new ScaleTransition(Duration.millis(100), emojiBtn);
                st.setToX(1.05);
                st.setToY(1.05);
                st.play();
            });

            emojiBtn.setOnMouseExited(e -> {
                emojiBtn.setStyle(
                        "-fx-background-color: rgba(169,112,255,0.1);" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-text-fill: " + TEXT_WHITE + ";"
                );
                ScaleTransition st = new ScaleTransition(Duration.millis(100), emojiBtn);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            emojiBtn.setOnAction(e -> {
                messageField.setText(messageField.getText() + unicodeSymbol + " ");
                messageField.requestFocus();
                messageField.positionCaret(messageField.getText().length());
                emojiStage.close();
            });

            emojiGrid.add(emojiBtn, col, row);
            col++;
            if (col >= 6) {
                col = 0;
                row++;
            }
        }

        ScrollPane emojiScroll = new ScrollPane(emojiGrid);
        emojiScroll.setFitToWidth(true);
        emojiScroll.setPrefHeight(450);
        emojiScroll.setStyle(
                "-fx-background: " + PANEL_BG + ";" +
                        "-fx-border-color: transparent;"
        );

        emojiBox.getChildren().addAll(title, emojiScroll);

        Scene emojiScene = new Scene(emojiBox, 600, 550);
        emojiStage.setScene(emojiScene);
        emojiStage.show();
    }


    /**
     * Skapar en chat-bubbla
     */

    private HBox addChatBubble(String sender,String message, boolean isOwn) {
        //FIIIX
        if(message.startsWith("[") ||message.startsWith("]")) {
            message = message.substring((sender + "[" + "]").length()).trim();
        }

        HBox container = new HBox(10);
        container.setPadding(new Insets(12,14,12,14));
        container.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Avatar
        Label avatar = new Label(isOwn ? "👤" : "👥");
        avatar.setFont(Font.font(25));

        //bubble cointainer
        VBox bubbleContent = new VBox(4);
        bubbleContent.setMaxWidth(500);

        //användarnamn och tid
        HBox meta = new HBox(12);
        meta.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label userLabel = new Label(sender);
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        userLabel.setTextFill(Color.web(SOFT_GRAY));

        Label timeLabel = new Label(getCurrentTime());
        timeLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));
        timeLabel.setTextFill(Color.web(SOFT_GRAY,0.7));

        meta.getChildren().addAll(userLabel, timeLabel);

        //Msgs texten i bubblan
        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        messageLabel.setTextFill(Color.web(TEXT_WHITE));
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(10,14,10,14));
        messageLabel.setMaxWidth(480);

        if(isOwn) {
            messageLabel.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + NEON_PINK + ", " + NEON_PURPLE + ");" +
                            "-fx-background-radius: 16 16 4 16;" +
                            "-fx-effect: dropshadow(gaussian, rgba(255,46,136,0.3), 8,0,0,2);"
            );
        }else{
            messageLabel.setStyle(
                    "-fx-background-color: " + PANEL_BG + ";" +
                            "-fx-border-color: " + NEON_PURPLE + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-background-radius: 16 16 16 4;" +
                            "-fx-border-radius: 16 16 16 4;" +
                            "-fx-effect: dropshadow(gaussian, rgba(169,112,255,0.25), 6, 0, 0, 2);"
            );
        }

        bubbleContent.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubbleContent.getChildren().addAll(avatar, meta, messageLabel);

        if (isOwn) {
            container.getChildren().addAll(bubbleContent,avatar);
        } else {
            container.getChildren().addAll(avatar,bubbleContent);
        }

        chatContainer.getChildren().add(container);

        // Slide & Fade animation
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), container);
        slide.setFromX(isOwn?50:-50);
        slide.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.millis(300), container);
        fade.setFromValue(0); fade.setToValue(1);
        new ParallelTransition(slide,fade).play();

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));

        return container;
    }

    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /**
     * Lägger till ett systemmeddelande (grå text, centrerad)
     */
    private void addSystemMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(6, 10, 6, 10));

        Label systemLabel = new Label(message);
        systemLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        systemLabel.setTextFill(Color.web(SOFT_GRAY));
        systemLabel.setWrapText(true);
        systemLabel.setMaxWidth(700);
        systemLabel.setStyle("-fx-opacity: 0.85; -fx-padding: 5;");

        messageBox.getChildren().add(systemLabel);
        chatContainer.getChildren().add(messageBox);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }


    /**
     * Skapar en förbättrad topprad med logotyp, användarnamn och logga ut-knapp.
     * @return HBox med toppradskomponenter
     */
    private HBox createEnhancedTopBar() {
        HBox topBar = new HBox(16);
        topBar.setPadding(new Insets(14));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, " + NEON_PINK + ", " + NEON_PURPLE + ");");

        // Logotyp-ikon
        Label titleIcon = new Label("💕");
        titleIcon.setFont(Font.font(26));

        // Applikationsnamn
        Label titleLabel = new Label("DANIS ROSA CHAT");
        titleLabel.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 20));
        titleLabel.setTextFill(Color.web(TEXT_WHITE));

        // Subtil glöd på texten
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web(NEON_PINK, 0.2));
        ds.setRadius(8);
        titleLabel.setEffect(ds);

        // Spacer som skjuter användarinfo och knapp till höger
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Användar-ikon och namn
        Label userIcon = new Label("👤");
        userIcon.setFont(Font.font(18));
        Label userLabel = new Label(currentUser != null ? currentUser : "Guest");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        userLabel.setTextFill(Color.web(TEXT_WHITE));

        // Logga ut-knapp
        Button disconnectBtn = new Button("🚪 Logga ut");
        disconnectBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12);" +
                        "-fx-text-fill: " + TEXT_WHITE + ";" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 8 14 8 14;"
        );

        disconnectBtn.setOnAction(e -> {
            disconnect();
            Platform.exit();  // Stäng applikationen
        });

        topBar.getChildren().addAll(titleIcon, titleLabel, spacer, userIcon, userLabel, disconnectBtn);
        return topBar;
    }

    /**
     * Skapar en stiliserad knapp med gradient och animationer.
     * @param text Knappens text
     * @return Stiliserad Button
     */
    private Button createMegaButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(48);

        // Gradient-bakgrund med skugga
        String baseStyle =
                "-fx-background-color: linear-gradient(to right, " + NEON_PINK + ", " + NEON_PURPLE + ");" +
                        "-fx-text-fill: " + TEXT_WHITE + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;";
        button.setStyle(baseStyle);

        // Skugg-effekt
        DropShadow bs = new DropShadow();
        bs.setColor(Color.web(NEON_PINK, 0.25));
        bs.setRadius(12);
        button.setEffect(bs);

        // Hover-effekt
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();

            Timeline bright = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(bs.radiusProperty(), 8)),
                    new KeyFrame(Duration.millis(200), new KeyValue(bs.radiusProperty(), 20))
            );
            bright.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return button;
    }

    /**
     * Skapar en skicka-knapp med special-styling.
     * @return Stiliserad skicka-knapp
     */
    private Button createSendButton() {
        Button button = new Button("📤 SKICKA");
        button.setPrefHeight(44);
        button.setPrefWidth(120);

        String baseStyle =
                "-fx-background-color: linear-gradient(to right, " + NEON_PINK + ", " + NEON_PURPLE + ");" +
                        "-fx-text-fill: " + TEXT_WHITE + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;";
        button.setStyle(baseStyle);

        DropShadow ds = new DropShadow();
        ds.setColor(Color.web(NEON_PINK, 0.22));
        ds.setRadius(8);
        button.setEffect(ds);

        // Hover-animation
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), button);
            st.setToX(1.06);
            st.setToY(1.06);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return button;
    }

    /**
     * Applicerar modern styling på textfält med fokus-effekter.
     * @param field TextFält att stila
     */
    private void styleTextFieldModern(TextField field) {
        String baseStyle =
                "-fx-background-color: " + INPUT_BG + ";" +
                        "-fx-text-fill: " + TEXT_WHITE + ";" +
                        "-fx-prompt-text-fill: rgba(244,244,248,0.45);" +
                        "-fx-border-color: rgba(169,112,255,0.08);" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-font-size: 13px;";
        field.setStyle(baseStyle);

        // Ändra utseende när fältet får fokus
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Ljusare bakgrund och rosa kant vid fokus
                field.setStyle(
                        "-fx-background-color: rgba(30,30,63,0.92);" +
                                "-fx-text-fill: " + TEXT_WHITE + ";" +
                                "-fx-prompt-text-fill: rgba(244,244,248,0.45);" +
                                "-fx-border-color: " + NEON_PINK + ";" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10;" +
                                "-fx-font-size: 13px;" +
                                "-fx-effect: dropshadow(gaussian, rgba(255,46,136,0.12), 8, 0, 0, 2);"
                );
            } else {
                field.setStyle(baseStyle);
            }
        });
    }

    /**
     * Skakar en nod horisontellt för att indikera ett fel.
     * @param node Nod att skaka
     */
    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(40), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    /**
     * Visar en tillgällig statustext med animerad fade-in.
     * @param label Label att uppdatera
     * @param text Texten att visa
     * @param colorHex Färg i hex-format
     */
    private void showTemporaryStatus(Label label, String text, String colorHex) {
        label.setText(text);
        label.setTextFill(Color.web(colorHex));
        label.setStyle("-fx-font-weight: bold;");

        FadeTransition ft = new FadeTransition(Duration.seconds(0.35), label);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Ansluter till chatservern och hanterar inloggning/registrering.
     * Läser serverns meddelanden i korrekt ordning enligt serverprotokollet.
     *
     * VIKTIGT!!!!!! : Måste läsa ALLA meddelanden från servern i rätt ordning
     * eftersom ClientHandler skickar flera rader efter varandra.
     * Detta var en utmanningen för mig då jag INTE VISSTE det :')
     *
     * @param username Användarnamn
     * @param password Lösenord
     * @param isLogin true för inloggning, false för registrering
     * @return true om anslutning lyckades, annars false
     */
    private boolean connectToServer(String username, String password, boolean isLogin) {
        try {
            // Skapa socket-anslutning till servern
            socket = new Socket("localhost", 5555);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // STEG 1: Läs välkomstmeddelande "Välkommen! Har du redan ett konto? (ja/nej)"
            String welcome = in.readLine();
            System.out.println("Server: " + welcome);

            // STEG 2: Svara på frågan om befintligt konto
            out.println(isLogin ? "ja" : "nej");

            if (isLogin) {
                // === INLOGGNINGSFLÖDE ===

                // STEG 3a: Läs "Ange användarnamn:"
                String prompt1 = in.readLine();
                System.out.println("Server: " + prompt1);
                out.println(username);

                // STEG 4a: Läs "Ange lösenord:"
                String prompt2 = in.readLine();
                System.out.println("Server: " + prompt2);
                out.println(password);

                // STEG 5a: Läs resultat (välkomstmeddelande eller felmeddelande)
                String result = in.readLine();
                System.out.println("Server: " + result);

                // Kontrollera om inloggning misslyckades
                if (result.contains("Fel användarnamn") || result.contains("❌")) {
                    return false;
                }

                // STEG 6a: Läs "Vill du hämta dina meddelanden direkt (ja/nej)"
                String fetchPrompt = in.readLine();
                System.out.println("Server: " + fetchPrompt);

                // Svara nej (kan ändras till ja om man vill ladda meddelanden vid inloggning)
                out.println("nej");

                // STEG 7a: Läs serverns svar på meddelande-frågan
                String msgResponse = in.readLine();
                System.out.println("Server: " + msgResponse);

            } else {
                // === REGISTRERINGSFLÖDE ===

                // STEG 3b: Läs "Skapa nytt konto. Ange användarnamn:"
                String prompt1 = in.readLine();
                System.out.println("Server: " + prompt1);
                out.println(username);

                // STEG 4b: Läs "Ange lösenord:"
                String prompt2 = in.readLine();
                System.out.println("Server: " + prompt2);
                out.println(password);

                // STEG 5b: Läs resultat (bekräftelse eller felmeddelande)
                String result = in.readLine();
                System.out.println("Server: " + result);

                // Kontrollera om registrering misslyckades (användarnamn upptaget)
                if (result.contains("redan taget") || result.contains("❌")) {
                    return false;
                }


            // STEG 8: Läs alla status-meddelanden efter lyckad inloggning/registrering
            // Servern skickar 5 rader:
            // 1. "✅ Du är inloggad som: [username]"
            // 2. "Nu kan du börja skriva meddelanden 💬"
            // 3. "Skriv /quit för att avsluta 💗"
            // 4. "Skriv /mymsgs för att se dina sparade meddelanden 📜"
            // 5. "[username] anslöt."
          //  for (int i = 0; i < 5; i++) {
                String infoLine = in.readLine();
                System.out.println("Server: " + infoLine);
            }

            // Spara användarnamnet och returnera framgång
            currentUser = username;
            return true;

        } catch (IOException e) {
            System.err.println("Anslutningsfel: " + e.getMessage());
            return false;
        }
    }

    /**
     * Startar en separat tråd som lyssnar på inkommande meddelanden från servern.
     * Alla meddelanden läggs till i chatArea på JavaFX Application Thread.
     */
    private void startMessageListener() {
        Thread listener = new Thread(() -> {
            try {
                String message;
                // Läs kontinuerligt meddelanden tills anslutningen bryts
                while ((message = in.readLine()) != null) {
                    final String msg = message;

                    // Uppdatera UI på JavaFX-tråden (GUI måste alltid uppdateras på denna tråd)
                    Platform.runLater(() -> {
                            // SYSTEMMEDDELANDEN (vanlig text, ingen bubbla)
                            if (msg.startsWith("✅") || msg.startsWith("Du är inloggad") ||
                                    msg.startsWith("Nu kan du") || msg.startsWith("Skriv /") ||
                                    msg.contains("anslöt") || msg.contains("sparade meddelanden") ||
                                    msg.startsWith("📜") || msg.startsWith("💬") || msg.startsWith("[")) {
                                addSystemMessage(msg);
                                // Meddelanden från /mymsgs addSystemMessage(msg);
                            }


                        // CHAT-MEDDELANDEN MED BUBBLA
                        // Format från servern: "användarnamn: meddelande"
                        else if (msg.contains(": ")) {
                            int colonIndex = msg.indexOf(": ");
                            String sender = msg.substring(0, colonIndex).trim();
                            String content = msg.substring(colonIndex + 2).trim();

                            boolean isOwn = sender.equalsIgnoreCase(currentUser);
                            addChatBubble(sender, content, isOwn);

                            // Blink-effekt för andras meddelanden
                            if (!isOwn) {
                                String origStyle = chatContainer.getStyle();
                                Timeline blink = new Timeline(
                                        new KeyFrame(Duration.ZERO, ev -> chatContainer.setStyle(
                                                origStyle + "-fx-border-color: " + NEON_PURPLE + "; -fx-border-width: 2;")),
                                        new KeyFrame(Duration.seconds(0.15), ev -> chatContainer.setStyle(origStyle))
                                );
                                blink.play();
                            }
                        }
                        // Allt annat som system-text
                        else {
                            addSystemMessage(msg);
                        }

                    });
                }
            } catch (IOException e) {
                // Om anslutningen bryts, uppdatera status
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("🔴 Anslutning bruten");
                        statusLabel.setTextFill(Color.web(ERROR_RED));
                    }
                    addSystemMessage("❌Anslutningen till serven bröts");
                });
            }
        });

        // Sätt som daemon-tråd så den avslutas automatiskt när programmet stängs
        listener.setDaemon(true);
        listener.start();
    }


    /**
     * Skickar ett meddelande till servern och visar visuell feedback.
     * Inkl animationer för att indikera att meddelandet skickades.
     */
    private void sendMessage() {
        String message = messageField.getText().trim();

        // Validera att meddelandet inte är tomt och att anslutningen finns
        if (message.isEmpty() || out == null) return;
        out.println(message);

       if(!message.startsWith("/")){

          addChatBubble(currentUser, message, true);
       }

        messageField.clear();

        // === VISUELL FEEDBACK: BLIXT-ANIMATION ===
        HBox parent = (HBox) messageField.getParent();
        if (parent != null) {
            for (javafx.scene.Node n : parent.getChildren()) {
                if (n instanceof Label && "⚡".equals(((Label) n).getText())) {
                    Label flash = (Label) n;
                    flash.setVisible(true);
                    flash.setOpacity(1);

                    // Animera blixten att flyga åt sidan
                    TranslateTransition tt = new TranslateTransition(Duration.millis(300), flash);
                    tt.setFromX(-4);
                    tt.setToX(12);

                    // Fade ut samtidigt
                    FadeTransition ft = new FadeTransition(Duration.millis(350), flash);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(ev -> flash.setVisible(false));

                    new ParallelTransition(tt, ft).play();
                    break;
                }
            }
        }
        // --- BLINK-EFFEKT PÅ CHAT ---
        String origStyle = chatContainer.getStyle();
        Timeline blink = new Timeline(
                new KeyFrame(Duration.ZERO, ev -> chatContainer.setStyle(origStyle + "-fx-border-color: " + NEON_PINK + "; -fx-border-width: 2;")),
                new KeyFrame(Duration.seconds(0.18), ev -> chatContainer.setStyle(origStyle))
        );
        blink.play();
    }


    /**
     * Kopplar från servern på ett korrekt sätt.
     * Skickar quit-kommando och stänger socket.
     */
    private void disconnect() {
        try {
            // Skicka quit-kommando till servern (VIKTIGT: utan '/' eftersom servern förväntar "quit")
            if (out != null) out.println("quit");

            // Stäng socket-anslutningen
            if (socket != null && !socket.isClosed()) socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Huvudingång för JavaFX-applikationen.
     * @param args Kommandoradsargument
     */
    public static void main(String[] args) {
        launch(args);
    }
}