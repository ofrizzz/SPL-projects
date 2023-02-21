package bguspl.set.ex;

public class SetRequest {

    protected int playerID;

    protected Integer[] cards;

    protected Integer[] slots;

    SetRequest(int playerID, Integer[] _slots, Integer[] _cards) {
        this.cards = _cards;
        this.slots = _slots;
        this.playerID = playerID;
    }

    protected int[] getIntCards() {
        int[] copy = new int[cards.length];
        for (int i = 0; i < cards.length; i++) {
            copy[i] = cards[i];
        }
        return copy;
    }

}
