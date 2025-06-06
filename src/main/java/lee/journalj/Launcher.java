package lee.journalj;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Launcher {
    private static final Logger logger = Logger.getLogger(Launcher.class.getName());

    public static void main(String[] args) {
        try {
            Main.main(args);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Application failed to start", e);
            System.exit(1);
        }
    }
}
