public class Card {
    private final String rank, suit;
    public Card(String r, String s) { rank = r; suit = s; }
    public Card(String c) { rank = c.substring(0,1); suit = c.substring(c.length()-1); }
    public String getCard() { return rank +" "+ suit; }
}