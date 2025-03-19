package terrascape.server;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static terrascape.utils.Constants.*;

public final class Server {

    private final ScheduledExecutorService executor;
    private long gameTickStartTime, lastGameTickProcessingTime;

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
        try {
            gameTickStartTime = System.nanoTime();

            ServerLogic.updateGT(EngineManager.getTick());
            EngineManager.incTick();
            EngineManager.setLastGTTime(System.nanoTime());

            long endTime = System.nanoTime();
            lastGameTickProcessingTime = endTime - gameTickStartTime;
            gameTickStartTime = endTime;

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public long getDeltaTime() {
        return System.nanoTime() - gameTickStartTime;
    }

    public long getLastGameTickProcessingTime() {
        return lastGameTickProcessingTime;
    }
}
