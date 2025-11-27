package se.sprinto.hakan.chatapp.util;

import org.mindrot.jbcrypt.BCrypt;

/** utility klass för en säker hantering av lösenord
 * hashar lösenord innan man sparar det i databasen
 * ren hjälpklass därav ligger den i util-paketet
 * den har inget med databasen eller de andra klasserna att göra
 *
 */

public class PasswordUtil {
    /**
     * Hashar lösenordet med BCrypt.gensalt()
     * generarar automatiskt et unikt salt för varje hash
     * @param password lösenord som användaren vill hasha
     * @return en säker hash sparas i databasen istället för klartext
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());

    }

    /**
     * Verifierar lösenordet vid inlogging
     * @param password som användaren skriver in
     * @param hashedPassword ligger sparat i databasen
     * @return true om lösenorden matchar, annars false
     */

    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
