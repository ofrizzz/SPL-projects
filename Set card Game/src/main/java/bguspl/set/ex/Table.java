package bguspl.set.ex;

import bguspl.set.Env;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    /**
     * 
     */
    protected Integer[][] playersTokens;

    /**
     * player's set requests for the dealer
     */
    protected BlockingQueue<SetRequest> setRequsts;

    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if
     *                   none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if
     *                   none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;

        setRequsts = new ArrayBlockingQueue<SetRequest>(env.config.players + 1);
        playersTokens = new Integer[env.config.players][3];
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the
     * table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted()
                    .collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(
                    sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * 
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     * @pre slotToCard[slot] == null
     * @pre cardToSlot[card] == null
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(Integer card, int slot) {
        if (card == null) {
            return;
        }
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {
        }
        slotToCard[slot] = card;

        cardToSlot[card] = slot;
        env.ui.placeCard(card, slot);
    }

    /**
     * Removes a card from a grid slot on the table.
     * 
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {
        }

        env.ui.removeCard(slot);

        cardToSlot[slotToCard[slot]] = null;
        slotToCard[slot] = null;
    }

    /**
     * Places a player token on a grid slot.
     * @pre slotToCard[slot] == null
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     * @return - true iff this is the third token.
     * @post playersTokens[player][freeTokenIndex] = slot;
     */
    public boolean placeToken(int player, int slot) throws IOException { // boolean- our change
        if (slotToCard[slot] == null)
            throw new IOException("No card in this slot!");
        boolean flag = false;
        for (int i = 0; i < 3; i++) {
            if (playersTokens[player][i] == null) {
                if (!flag) {
                    playersTokens[player][i] = slot;
                    flag = true;
                    env.ui.placeToken(player, slot);
                } else
                    return false;
            }
        }
        if (!flag) {
            throw new IOException("Remove some token first!");
        }
        return true;
    }

    /**
     * Removes a token of a player from a grid slot.
     * 
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        for (int i = 0; i < 3; i++) {
            if (((Integer) slot).equals(playersTokens[player][i])) {
                playersTokens[player][i] = null;
                env.ui.removeToken(player, slot);
                return true;
            }
        }
        return false;
    }

    public boolean assertCards(SetRequest request) { // dealer calling and only reading so no need for synchronize
        return (slotToCard[request.slots[0]] == request.cards[0])
                && (slotToCard[request.slots[1]] == request.cards[1])
                && (slotToCard[request.slots[2]] == request.cards[2]);
    }

    public Integer[] getPlayerTokens(int playerID) {
        return playersTokens[playerID];
    }

    public int getNumTokens(int playerID) {
        int ctr = 0;
        for (Integer slot : playersTokens[playerID]) {
            if (slot != null)
                ctr++;
        }
        return ctr;
    }

}
