package diarsid.support.javafx.mouse;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;

import static java.lang.String.format;

import static diarsid.support.javafx.mouse.ClickType.DOUBLE_CLICK;
import static diarsid.support.javafx.mouse.ClickType.SEQUENTIAL_CLICK;
import static diarsid.support.javafx.mouse.ClickType.USUAL_CLICK;

public class ClickTypeDurations {

    public static class LogicMismatchException extends RuntimeException {

        public LogicMismatchException(String message) {
            super(message);
        }
    }

    private final LongProperty millisForDoubleClick;
    private final LongProperty millisForSequentialClick;
    private final Lock changeMillis;
    private final Lock getMillis;

    public ClickTypeDurations(LongProperty millisForDoubleClick, LongProperty millisForSequentialClick) {
        this.millisForDoubleClick = millisForDoubleClick;
        this.millisForSequentialClick = millisForSequentialClick;
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.changeMillis = lock.writeLock();
        this.getMillis = lock.readLock();
        this.checkValuesLogic();
    }

    private void checkValuesLogic() {
        long sequentialMs = this.millisForSequentialClick.get();
        long doubleMs = this.millisForDoubleClick.get();

        if ( sequentialMs < 1 ) {
            throw new LogicMismatchException(format("millis for %s cannot be zero or negative!", SEQUENTIAL_CLICK));
        }

        if ( doubleMs < 1 ) {
            throw new LogicMismatchException(format("millis for %s cannot be zero or negative!", DOUBLE_CLICK));
        }

        if ( doubleMs > sequentialMs ) {
            throw new LogicMismatchException(format("millis for %s cannot be higher than for %s!", DOUBLE_CLICK, SEQUENTIAL_CLICK));
        }
    }

    public ClickTypeDurations(long millisForDoubleClick, long millisForSequentialClick) {
        this(new SimpleLongProperty(millisForDoubleClick), new SimpleLongProperty(millisForSequentialClick));
    }

    public ReadOnlyLongProperty millisForDoubleClick() {
        return this.millisForDoubleClick;
    }

    public ReadOnlyLongProperty millisForSequentialClick() {
        return this.millisForSequentialClick;
    }

    public ClickType defineClickType(long timeAfterLastClick) {
        this.getMillis.lock();
        try {
            if ( timeAfterLastClick <= this.millisForDoubleClick.get() ) {
                return DOUBLE_CLICK;
            }
            else if ( timeAfterLastClick <= this.millisForSequentialClick.get() ) {
                return SEQUENTIAL_CLICK;
            }
            else {
                return USUAL_CLICK;
            }
        }
        finally {
            this.getMillis.unlock();
        }
    }

    public boolean setMillisForDoubleClick(long millis) {
        this.changeMillis.lock();
        try {
            if ( millis >= this.millisForSequentialClick.doubleValue() ) {
                return false;
            }
            else {
                this.millisForDoubleClick.set(millis);
                return true;
            }
        }
        finally {
            this.changeMillis.unlock();
        }
    }

    public boolean setMillisForSequentialClick(long millis) {
        this.changeMillis.lock();
        try {
            if ( millis <= this.millisForDoubleClick.doubleValue() ) {
                return false;
            }
            else {
                this.millisForSequentialClick.set(millis);
                return true;
            }
        }
        finally {
            this.changeMillis.unlock();
        }
    }
}
