
package se.sprinto.hakan.chatapp;

/**
 * Launcher-klass för ChatClientFX.
 * Denna klass löser JavaFX module-problemet genom att starta
 * applikationen utan att själv ärva från Application.
 */
public class ChatClientFXLauncher {
    public static void main(String[] args) {
        ChatClientFX.main(args);
    }
}