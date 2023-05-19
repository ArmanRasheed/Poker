public class rando {
    public static void main(String[] args) {
        Poker test = new Poker();
        test.createDeck();

        test.deal();
        System.out.println(":::::PLAYER:::::");
        for (Card i : test.getHand()) {
            System.out.print(i.getCard() + "  ");
        }
        System.out.println("\n:::::DEALER:::::");
        for (Card i : test.getDealer()) {
            System.out.print(i.getCard() + "  ");
        }

        System.out.println("\n");
        System.out.println("Player HV = " + test.evalHand(test.getHand()));
        System.out.println("Dealer HV = " + test.evalHand(test.getDealer()));
        int win = test.win();
        if(win == 1) System.out.println("Player Win!!!");
        else if (win == -1) System.out.println("Dealer Won :(");
        else System.out.println("Anticlimactic Result 0_0");
    }
}
