package diarsid.support.javafx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javafx.application.Platform;

public class PlatformActions {

    private final static CountDownLatch PLATFORM_STARTUP_LOCK = new CountDownLatch(1);

    static {
        Platform.startup(PLATFORM_STARTUP_LOCK::countDown);
    }

    public static void awaitStartup() {
        if ( PLATFORM_STARTUP_LOCK.getCount() > 0 ) {
            try {
                PLATFORM_STARTUP_LOCK.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }
        }
    }

    public static <T> T doGet(Supplier<T> supplier) {
        PlatformActions.awaitStartup();

        AtomicReference<T> tRef = new AtomicReference<>();
        CountDownLatch supplied = new CountDownLatch(1);

        Platform.runLater(() -> {
            tRef.set(supplier.get());
            supplied.countDown();
        });

        try {
            supplied.await();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return tRef.get();
    }
}
