/*
 * Decompiled with CFR 0_113.
 */
package sampling.evaluation.blogger.fi.iki.elonen;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRunner {
    private static final Logger LOG = Logger.getLogger(ServerRunner.class.getName());

    public static void executeInstance(NanoHTTPD server) {
        try {
            server.start();
        }
        catch (IOException ioe2) {
            System.err.println("Couldn't start server:\n" + ioe2);
            System.exit(-1);
        }
        System.out.println("Server started, Hit Enter to stop.\n");
        try {
            System.in.read();
        }
        catch (Throwable ioe2) {
            // empty catch block
        }
        server.stop();
        System.out.println("Server stopped.\n");
    }

    public static <T extends NanoHTTPD> void run(Class<T> serverClass) {
        try {
            ServerRunner.executeInstance((NanoHTTPD)serverClass.newInstance());
        }
        catch (Exception e) {
            LOG.log(Level.SEVERE, "Cound nor create server", e);
        }
    }
}

