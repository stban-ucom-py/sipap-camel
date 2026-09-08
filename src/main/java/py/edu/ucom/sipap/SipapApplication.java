package py.edu.ucom.sipap;

import org.apache.camel.main.Main;
import py.edu.ucom.sipap.routes.SipapRoutes;

public final class SipapApplication {
    private SipapApplication() {
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new SipapRoutes(true));
        main.run(args);
    }
}
