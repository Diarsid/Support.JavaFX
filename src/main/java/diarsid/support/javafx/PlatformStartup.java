package diarsid.support.javafx;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;

public class PlatformStartup {

    private final static CountDownLatch PLATFORM_STARTUP_LOCK = new CountDownLatch(1);

    static {
        Platform.startup(PLATFORM_STARTUP_LOCK::countDown);
    }

    public static void await() {
        if ( PLATFORM_STARTUP_LOCK.getCount() > 0 ) {
            try {
                PLATFORM_STARTUP_LOCK.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }
    }
}
