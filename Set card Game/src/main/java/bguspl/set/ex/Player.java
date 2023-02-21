package bguspl.set.ex;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import bguspl.set.Env;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    public Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate
     * key presses).
     */
    Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    protected final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    // OURS
    BlockingQueue<Integer> keyStrokes;

    public AtomicLong freezeUntilMillis;
    private Dealer dealer;
    public AtomicBoolean endFreezeNotified;

    /**
     * The class constructor.
     *
     * @param env    - the game environment object.
     * @param table  -
     *               the table object.
     * @param dealer - the dealer object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided
     *               manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        // this.keyStrokes = new ArrayDeque<Integer>(3);
        this.keyStrokes = new ArrayBlockingQueue<Integer>(3);

        this.dealer = dealer;
        this.freezeUntilMillis = new AtomicLong();
        endFreezeNotified = new AtomicBoolean(true);
    }

    /**
     * The main player thread of each player starts here (main loop for the player
     * thread).
     */
    @Override
    public void run() {
        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");

        playerThread = Thread.currentThread();

        if (!human) {
            createArtificialIntelligence();
            // createSuperDuperSmartArtificialIntelligence();
        }

        while (!terminate) {
            Integer slot;
            try {
                slot = keyStrokes.take();
            } catch (InterruptedException e) {
                terminate();
                break;
            } // interrupted due to termination fits

            dealer.readWriteLock.readLock().lock();
            try {
                if (!table.removeToken(id, slot)) {
                    table.placeToken(id, slot);
                    declareSet();
                } else {
                }
            } catch (IOException e) {
                // fourth token or token on null card
            } finally {
                try {
                    dealer.readWriteLock.readLock().unlock();
                } catch (IllegalMonitorStateException e) {
                    // may have been unlocked in declare set
                }
            }
        }

        if (!human) {
            while (aiThread.isAlive()) { // wait for the AI thread to terminate
                try {
                    aiThread.join();
                } catch (InterruptedException ignored) {
                    // System.out.println("Player thread interrupted at join");
                }
            }
        }
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }

    private void declareSet() {
        Integer[] tokens = new Integer[3];
        for (int i = 0; i < 3; i++) {
            if (table.playersTokens[id][i] == null) {
                return;
            }
            tokens[i] = new Integer(table.playersTokens[id][i]);
        }

        Integer[] cardSet = new Integer[3];
        for (int i = 0; i < 3; i++) {
            if (table.playersTokens[id][i] == null) {
                System.out.println(Arrays.toString(table.playersTokens[id]));
                throw new RuntimeException("null in playersTokens");
            }
            cardSet[i] = table.slotToCard[table.playersTokens[id][i]];
        }

        SetRequest request = new SetRequest(id,
                tokens,
                cardSet);

        try {
            dealer.readWriteLock.readLock().unlock();
            this.table.setRequsts.put(request);
            synchronized (this) {
                do {
                    wait();
                } while (isFrozen());
                this.endFreezeNotified.set(true);
            }

            synchronized (keyStrokes) {
                keyStrokes.clear();
            }
        } catch (InterruptedException e) {
        }
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of
     * this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it
     * is not full.
     */
    private void createArtificialIntelligence() {
        aiThread = new Thread(() -> {
            env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                int slot = (int) (Math.random() * (env.config.tableSize));
                keyPressed(slot);

                try {
                    synchronized (this) {
                        if (this.isFrozen()) { // consider changing to while
                            wait();
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            }
            env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    private void createSuperDuperSmartArtificialIntelligence() {
        aiThread = new Thread(() -> {
            System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
            List<int[]> sets = null;
            List<Integer> slots = new ArrayList<Integer>();
            while (!terminate) {
                dealer.readWriteLock.readLock().lock();
                keyStrokes.clear();

                try {
                    Integer[] myTokens = table.getPlayerTokens(id);
                    for (Integer slot : myTokens) {
                        if (slot != null) {
                            table.removeToken(id, slot);
                        }
                    }

                    if (table.slotToCard != null && Arrays.asList(table.slotToCard) != null) {
                        List<Integer> nonNullList = Arrays.asList(table.slotToCard).stream()
                                .filter(p -> p != null).collect(Collectors.toList());
                        sets = env.util.findSets(nonNullList, 5);
                        // sets = env.util.findSets(Arrays.asList(table.slotToCard), 5);

                        if (sets != null && sets.size() > 0) {
                            int[] set = sets.get((int) (Math.random() * (sets.size())));
                            for (int i = 0; i < set.length; i++) {
                                Integer card = set[i];
                                slots.add(table.cardToSlot[card]);
                                if (table.cardToSlot[card] == null) {
                                    System.out.println("null in set");
                                    slots = null;
                                    break;
                                }
                            }
                        } else {

                        }
                    }

                } finally {
                    dealer.readWriteLock.readLock().unlock();
                }
                if (slots != null) {
                    slots.forEach((slot) -> {
                        if (slot != null) {
                            keyPressed(slot);
                        } else {
                            throw new RuntimeException("null in slots!");
                        }
                    });
                    slots.clear();
                }
            }
            System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     * @post The player thread is interrupted.
     * @post The player ai thread is interrupted (if exists).
     * @post The player terminate flag is true.
     */
    public void terminate() {
        terminate = true;
        if (playerThread != null) {
            playerThread.interrupt();
            if (!human) {
                aiThread.interrupt();
            }
        }
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     * @post if not frozen and queue not full the the slot is added to the queue of key presses
     */
    public void keyPressed(int slot) {
        if (isFrozen()) {
            return;
        }

        try {
            if (human) {
                keyStrokes.add(slot); // Does not block
            } else {
                keyStrokes.put(slot); // Blocks - does not busy wait
            }
        } catch (IllegalStateException Ignored) {
        } // if queue is full - ignore
        catch (InterruptedException Ignored) {
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() { 
        this.score++;
        env.ui.setScore(this.id, score);
        long current = freezeUntilMillis.get();
        long newval = System.currentTimeMillis() + env.config.pointFreezeMillis;
        while (!this.freezeUntilMillis.compareAndSet(current, newval))
            ;

        endFreezeNotified.set(false);
        env.ui.setFreeze(this.id, Math.toIntExact(env.config.pointFreezeMillis / 1000)); // atomic?
    }

    /**
     * Penalize a player and perform other related actions.
     * @post - the player's score is same.
     * @post - the player's freeze is updated in the ui.
     */
    public void penalty() { 
        long current = freezeUntilMillis.get();
        long newval = System.currentTimeMillis() + env.config.penaltyFreezeMillis;
        while (!this.freezeUntilMillis.compareAndSet(current, newval))
            ;
        endFreezeNotified.set(false);
        env.ui.setFreeze(this.id, Math.toIntExact(env.config.penaltyFreezeMillis / 1000)); // atomic?
    }

    public int score() {
        return score;
    }

    public boolean isFrozen() {
        return System.currentTimeMillis() <= this.freezeUntilMillis.get();
    }

}