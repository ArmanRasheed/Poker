                          // Backend server logic for Poker games //

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Poker {

    // deck = full deck of Card objects, hand & dealer = three Card hands for player & dealer
    ArrayList<Card> deck, hand, dealer;
    // stores bets and total amount of money won
    int ante, p, totalWin;

    // Default Constructor
    public Poker() {
        deck = new ArrayList<Card>();
        hand = new ArrayList<Card>();
        dealer = new ArrayList<Card>();
        totalWin = 0; ante = 0; p = 0;
    }

    // Instantiates deck with 52 Card objects in new deck order
    public void createDeck() {
        ArrayList<Card> newDeck = new ArrayList<Card>();

        ArrayList<String> ranks = new ArrayList<String>();
        ranks.add("A"); ranks.add("2"); ranks.add("3"); ranks.add("4");
        ranks.add("5"); ranks.add("6"); ranks.add("7"); ranks.add("8");
        ranks.add("9"); ranks.add("10"); ranks.add("J"); ranks.add("Q");
        ranks.add("K");

        ArrayList<String> suits = new ArrayList<String>();
        suits.add("S"); suits.add("D"); suits.add("C"); suits.add("H");

        for (String i : suits)
            for (String j : ranks)
                newDeck.add(new Card(j, i));

        deck = newDeck;
    }

    // randomly shuffles deck
    public void shuffle() {
        ArrayList<Card> shuffled = new ArrayList<Card>();
        Random rand = new Random();
        while(deck.size() > 0) shuffled.add(deck.remove(rand.nextInt(deck.size())));
        deck = shuffled;
    }

    // Puts dealt Cards back into deck, shuffles deck, and instantiates 'hand' and 'dealer' with new hands
    public void deal() {
        while(!hand.isEmpty()) deck.add(hand.remove(0));
        while(!dealer.isEmpty()) deck.add(dealer.remove(0));

        shuffle();
        hand.add(deck.remove(0)); hand.add(deck.remove(0)); hand.add(deck.remove(0));
        dealer.add(deck.remove(0)); dealer.add(deck.remove(0)); dealer.add(deck.remove(0));
    }

    // checks if a hand is a straight
    public boolean hasStraight(ArrayList<String> uns) {
        // ranks defines rank values based on array index (higher is better)
        ArrayList<String> ranks = new ArrayList<String>();
        ranks.add("2"); ranks.add("3"); ranks.add("4");
        ranks.add("5"); ranks.add("6"); ranks.add("7"); ranks.add("8");
        ranks.add("9"); ranks.add("10"); ranks.add("J"); ranks.add("Q");
        ranks.add("K"); ranks.add("A");

        // boundary hand for the one exception to this algorithm == [A, 2, 3]
        ArrayList<Integer> wrap = new ArrayList<Integer>();
        wrap.add(0); wrap.add(1); wrap.add(12);

        // instantiates array of the rank values from ranks indices
        ArrayList<Integer> vals = new ArrayList<Integer>();
        int rMat = 0; // counts pairs in the hand which abide by the format of a straight
        for (String i : uns)
            for (int j = 0; j < ranks.size(); j++)
                if (i.equals(ranks.get(j))) vals.add(j);

        // sorted for easy comparison
        Collections.sort(vals);

        for(int i = 0; i < vals.size()-1; i++) {
            if (vals.equals(wrap)) {rMat = 3; break;} // exception to algorithm
            if (vals.get(i) == (vals.get(i+1).intValue())) break; // if two ranks are the same, break
            if (vals.get(i) == (vals.get(i + 1) - 1)) rMat++;
            if (i == 0) if (vals.get(i) == (vals.get(vals.size() - 1) - 2)) rMat++;
        }

        return (rMat == 3); // returns true if all three out of three pairs abide by straight format
    }

    // returns an array defined as [PP Value, Highest Card Value] for an input hand
    public ArrayList<Integer> evalHand(ArrayList<Card> hand) {
        ArrayList<String> ranks = new ArrayList<String>();
        ranks.add("2"); ranks.add("3"); ranks.add("4");
        ranks.add("5"); ranks.add("6"); ranks.add("7"); ranks.add("8");
        ranks.add("9"); ranks.add("10"); ranks.add("J"); ranks.add("Q");
        ranks.add("K"); ranks.add("A");

        // rs = only hand ranks, suits = only hand suits, vals = return array
        // highRank stores highest card value, 'Mat' vars counts equal matches in Card ranks and suits
        ArrayList<String> rs = new ArrayList<String>();
        ArrayList<String> suits = new ArrayList<String>();
        ArrayList<Integer> vals = new ArrayList<Integer>();
        String r, s;
        boolean straight = false;
        int highRank = -1; int rMat = 0; int sMat = 0;

        // instantiate rs and suits from Card String format
        for (Card i : hand) {
            r = i.getCard().substring(0, i.getCard().indexOf(" "));
            rs.add(r);
            s = i.getCard().substring(i.getCard().indexOf(" ") + 1);;
            suits.add(s);
        }

        straight = hasStraight(rs);

        // rank matches found here
        if(rs.get(0).equals(rs.get(1))) rMat++;
        if(rs.get(0).equals(rs.get(2))) rMat++;
        if(rs.get(1).equals(rs.get(2))) rMat++;

        // highest card value found here
        for (String i : rs)
            for (int j = 0; j < ranks.size(); j++)
                if (i.equals(ranks.get(j)) && (j > highRank)) highRank = j;

        // suit matches found here
        if(suits.get(0).equals(suits.get(1))) sMat++;
        if(suits.get(0).equals(suits.get(2))) sMat++;
        if(suits.get(1).equals(suits.get(2))) sMat++;

        // Player Plus value found and appended to vals
        if (straight && sMat == 3) vals.add(5); // straight flush
        else if (rMat == 3) vals.add(4);        // three of a kind
        else if (straight) vals.add(3);         // straight
        else if (sMat == 3) vals.add(2);        // flush
        else if (rMat == 1) vals.add(1);        // pair
        else vals.add(0);                       // no Player Plus

        vals.add(highRank);

        return vals;
    }

    // returns money made this round from Player Plus wager
    public int calcPP() {
        int pp = evalHand(hand).get(0);

        if (pp == 5) return (40 * p);
        else if (pp == 4) return (30 * p);
        else if (pp == 3) return (6 * p);
        else if (pp == 2) return (3 * p);
        else if (pp == 1) return p;
        else return 0;
    }

    // returns 1 for a win, 0 for no win/loss, and -1 for loss
    public int win() {
        ArrayList<Integer> h = evalHand(hand);
        ArrayList<Integer> d = evalHand(dealer);

        if (d.get(0).equals(0) && !(d.get(1) >= 10)) return 0;
        if (!(h.get(0).equals(d.get(0)))) { if (h.get(0) > d.get(0)) return 1;  else return -1;}
        if (h.get(1) > d.get(1)) return 1;
        else if (h.get(1).equals(d.get(1))) return 0;
        return -1;
    }

    // returns money made from this round for a given bet
    public int earnings() {
        if (win() == 1) return (4 * ante);
        else return 0;
    }

    // updates total winnings from these rounds of poker games, returns money from round
    public int playGame() { totalWin += (earnings() + calcPP() + (-2 * ante) - p); return (earnings() + calcPP() + (-2 * ante) - p); }

    // updates total winnings from after a fold, returns money from round
    public int folded() { totalWin += (calcPP() - ante - p); return (calcPP() - ante - p); }

    // when Dealer doesn't Qualify
    public int justPP() { totalWin += (calcPP() - p); return (calcPP() - p); }

    // ******************* GETTERS & SETTERS *******************
    public void setAnte(int a) { ante = a; }
    public void setPP(int pp) { p = pp; }
    public ArrayList<Card> getHand() { return hand; }
    public ArrayList<Card> getDealer() { return dealer; }
    public ArrayList<Card> getDeck() { return deck; }
    public int getWin() { return totalWin; }
}
