package terrascape.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static terrascape.utils.Constants.*;

public final class Server {

    private final ScheduledExecutorService executor;
    private long gameTickTime;

    public Server() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executor.scheduleAtFixedRate(this::executeGT, 0, MILLISECONDS_PER_SECOND / TARGET_TPS, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdownNow();
        try {
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeGT() {
        long gameTickTime = System.nanoTime();
        ServerLogic.updateGT(EngineManager.getTick());
        EngineManager.incTick();
        EngineManager.setLastGTTime(System.nanoTime());
        this.gameTickTime = System.nanoTime() - gameTickTime;
    }

    public long getDeltaTime() {
        return gameTickTime;
    }
}
