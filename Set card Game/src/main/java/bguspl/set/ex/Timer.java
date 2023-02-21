package bguspl.set.ex;

import java.util.concurrent.atomic.AtomicLong;

import bguspl.set.Env;

public class Timer implements Runnable {

    private final Env env;
    private AtomicLong startMillisTime;
    private int updatesPerSecond;
    private long warningTime;
    private Thread dealerThread;
    private Dealer dealer;
    private boolean didNotifiedDealer = false;

    public Timer(Env env) {
        this.env = env;
        this.startMillisTime = new AtomicLong(0);
        this.startMillisTime.set(System.currentTimeMillis());

        this.updatesPerSecond = 4;

        warningTime = env.config.turnTimeoutWarningMillis;

    }

    public void updateDealerThread(Thread dealerThread, Dealer dealer) {
        this.dealerThread = dealerThread;
        this.dealer = dealer;
    }

    private void updateTimer() {
        while (!Thread.currentThread().isInterrupted()) {
            try {

                int sleepTimeMillis = 100;
                long diffs = env.config.turnTimeoutMillis + 999 - (System.currentTimeMillis() - startMillisTime.get());

                if (diffs <= 0) {
                    env.ui.setCountdown(0, false);
                } else {
                    env.ui.setCountdown(diffs, diffs <= warningTime);
                }

                updateFrozenPlayers();

                if (Math.toIntExact(diffs) <= 0 && !didNotifiedDealer)// For telling dealer time is up!
                {
                    this.didNotifiedDealer = true;
                    this.dealerThread.interrupt();
                }

                Thread.sleep(sleepTimeMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateFrozenPlayers() {
        for (Player player : dealer.getPlayers()) {
            if (player.isFrozen()) {
                env.ui.setFreeze(player.id,
                        (player.freezeUntilMillis.get() + 999 - System.currentTimeMillis())); // atomic?
            } else if (!player.endFreezeNotified.get()) {
                env.ui.setFreeze(player.id, 0);
                synchronized (player) {
                    player.notifyAll();
                }
            }
        }
    }

    public boolean setStartMillisTime(long newTime) {
        this.didNotifiedDealer = false;
        long current = startMillisTime.get();

        return startMillisTime.compareAndSet(current, newTime);
    }

    @Override
    public void run() {
        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException ignored) {

        }
        while (!Thread.currentThread().isInterrupted()) {
            updateTimer();
        }
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }
}