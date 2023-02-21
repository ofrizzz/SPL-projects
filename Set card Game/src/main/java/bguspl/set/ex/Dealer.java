package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.ThreadLogger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    protected final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer countdown times out (at which point he must collect
     * the cards and reshuffle the deck).
     */
    private long countdownUntil;

    private volatile boolean xButtonPressed = false;

    // OURS
    Thread timerThread;
    Timer timer;

    public final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());

        // OURS
        this.timer = new Timer(env);
        this.timerThread = new Thread(this.timer, "timer");
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        // env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");

        this.timer.updateDealerThread(Thread.currentThread(), this);
        this.timerThread.start();
        readWriteLock.writeLock().lock();

        for (Player player : this.players) {
            Thread playerThread = new Thread(player, "player " + player.id);
            playerThread.start();
        }

        while (!shouldFinish()) {
            placeCardsOnTable();
            countdownLoop();
            removeAllCardsFromTable();
        }

        // for (Player player : this.players) {
        // try{
        // player.playerThread.interrupt();
        // player.playerThread.join();
        // }
        // catch(InterruptedException e){
        // System.out.println("Interrupted while waiting for player to finish");
        // }
        // }
        this.readWriteLock.writeLock().unlock();
        if (!xButtonPressed) {
            announceWinners();
        }
        terminate();
        // env.logger.info("Thread " + Thread.currentThread().getName() + "
        // terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did
     * not time out.
     */
    private void countdownLoop() {
        resetCountdown();

        while (!terminate && System.currentTimeMillis() < countdownUntil) {
            updateCountdown();

            readWriteLock.writeLock().unlock();
            sleepUntilWokenOrTimeout();
            readWriteLock.writeLock().lock();
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some
     * purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        boolean isInterrupted = false;

        while (!isInterrupted && !this.terminate) {
            try {
                Object temp = table.setRequsts.poll(100, TimeUnit.MILLISECONDS); // Will block
                if (temp == null) {
                    continue;
                }
                SetRequest request = (SetRequest) temp;
                handleRequest(request);
            } catch (InterruptedException e) {
                isInterrupted = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        this.terminate = true;
        if (!Thread.currentThread().getName().equals("dealer")) {
            xButtonPressed = true;
            return;
        }
        for (Player p : players) {
            p.terminate();
            try {
                p.playerThread.join();
            }

            catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for player to finish");
            }
        }
        timerThread.interrupt();
        try {
            timerThread.join();
        } catch (InterruptedException e) {

        }
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     * 
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks if any cards should be removed from the table.
     */
    private void removeCardsFromTable() {
        // no need in our solution
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        for (int i = 0; i < env.config.tableSize; i++) {
            if (this.table.slotToCard[i] == null && this.deck.size() > 0) {
                table.placeCard(draw(), i);
            }
        }
    }

    /**
     * removes and returns a random card from the deck
     * 
     * @pre deck.size() > 0
     * @post deck.size() == old(deck.size()) - 1 or null if deck is empty
     * @return a random card from the deck
     */
    Integer draw() {
        if (deck.size() > 0) {
            Integer cardIndex = (int) ((Math.random()) * (deck.size() - 1));
            Integer card = deck.get(cardIndex);
            deck.remove((int) cardIndex);
            return card;
        }
        return null;
    }

    private void handleRequest(SetRequest request) {
        if (table.assertCards(request)) {
            readWriteLock.writeLock().lock();
            try {
                if (env.util.testSet(request.getIntCards())) {
                    for (int i = 0; i < 3; i++) {
                        removeAllPlayersTokens(request.slots[i]);
                        table.removeCard(request.slots[i]);
                    }
                    for (int i = 0; i < 3; i++) {
                        table.placeCard(draw(), request.slots[i]);
                    }
                    players[request.playerID].point();
                    resetCountdown();
                } else {
                    players[request.playerID].penalty();
                }
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } else {
            readWriteLock.writeLock().lock();

            try {
                players[request.playerID].freezeUntilMillis.set(System.currentTimeMillis() - 2000);
                players[request.playerID].endFreezeNotified.set(false);
            } finally {
                readWriteLock.writeLock().unlock();
            }
            // release player from freeze with the rest of his tokens and ignore request
        }
        // synchronized (this.players[request.playerID]) {
        // this.players[request.playerID].notifyAll();
        // }
    }

    private void removeAllPlayersTokens(Integer slot) {
        env.ui.removeTokens(slot);

        for (int i = 0; i < table.playersTokens.length; i++) {
            for (int j = 0; j < table.playersTokens[i].length; j++) {
                if (slot.equals(table.playersTokens[i][j])) {
                    table.playersTokens[i][j] = null;
                }
            }
        }
    }

    /**
     * Update the countdown display.
     */
    private void updateCountdown() {
        while (!timer.setStartMillisTime(countdownUntil - env.config.turnTimeoutMillis))
            ;

        synchronized (timer) {
            timer.notifyAll();
        }
    }

    /**
     * Reset the countdown timer and update the countdown display.
     */
    private void resetCountdown() {
        if (env.config.turnTimeoutMillis > 0) {
            countdownUntil = System.currentTimeMillis() + env.config.turnTimeoutMillis;
            updateCountdown();
            if (env.config.hints) {
                this.table.hints();
            }
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     * 
     * @pre none
     * @post deck.size() == old(deck.size()) + table.size()
     * @post table.size() == 0
     */
    void removeAllCardsFromTable() {
        for (int i = 0; i < env.config.tableSize; i++) {
            Integer curr = table.slotToCard[i];
            if (curr != null) {
                removeAllPlayersTokens(i);
                table.removeCard(i);
                deck.add(curr);
            }
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        int[] winners = new int[env.config.players];
        int maxScore = 0;
        int numWinners = 0;
        for (Player player : players) {
            if (player.score() > maxScore) {
                maxScore = player.score();
                winners[0] = player.id;
                numWinners = 1;
            } else if (player.score() == maxScore) {
                winners[numWinners] = player.id;
                numWinners++;
            }
        }
        env.ui.announceWinner(Arrays.copyOf(winners, numWinners));
    }

    public Player[] getPlayers() {
        return players;
    }
}
