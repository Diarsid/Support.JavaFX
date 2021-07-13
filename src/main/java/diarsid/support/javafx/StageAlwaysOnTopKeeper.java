package diarsid.support.javafx;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;

import diarsid.support.concurrency.stateful.workers.AbstractStatefulPausableDestroyableWorker;
import diarsid.support.concurrency.threads.NamedThreadFactory;
import diarsid.support.objects.references.Possible;

import static java.util.Objects.nonNull;

import static diarsid.support.concurrency.threads.ThreadsUtil.shutdownAndWait;
import static diarsid.support.objects.references.References.simplePossibleButEmpty;

public class StageAlwaysOnTopKeeper extends AbstractStatefulPausableDestroyableWorker {

    private final Stage stage;
    private final ScheduledExecutorService async;
    private final Possible<ScheduledFuture> asyncJob;
    private final long time;
    private final TimeUnit unit;

    public StageAlwaysOnTopKeeper(String name, Stage stage, NamedThreadFactory namedThreadFactory, long time, TimeUnit unit) {
        super(name);
        this.stage = stage;
        this.async = new ScheduledThreadPoolExecutor(1, namedThreadFactory);
        this.asyncJob = simplePossibleButEmpty();
        this.time = time;
        this.unit = unit;
    }

    private void onTop() {
        Platform.runLater(() -> this.stage.setAlwaysOnTop(true));
    }

    @Override
    protected boolean doSynchronizedStartWork() {
        ScheduledFuture newSchedule = this.async.scheduleAtFixedRate(this::onTop, 0, this.time, this.unit);
        ScheduledFuture oldSchedule = this.asyncJob.resetTo(newSchedule);

        if ( nonNull(oldSchedule) ) {
            oldSchedule.cancel(true);
        }

        return true;
    }

    @Override
    protected boolean doSynchronizedPauseWork() {
        ScheduledFuture schedule = this.asyncJob.orThrow();
        schedule.cancel(true);
        return true;
    }

    @Override
    protected boolean doSynchronizedDestroy() {
        shutdownAndWait(this.async);
        return true;
    }
}
