import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    int count; int port;
    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
    ArrayList<Poker> pgames = new ArrayList<Poker>();
    TheServer server;
    private Consumer<Serializable> callback;

    Server(Consumer<Serializable> call, int p){
        callback = call; port = p; count = 0;
        server = new TheServer();
        server.start();
    }

    public void stop() { server.stopServer(); }

    public class TheServer extends Thread{

        private boolean running = true;

        public void stopServer() {
            running = false; // set the "running" flag to false
            interrupt(); // interrupt the Server thread
        }

        public void run() {
            try (ServerSocket mysocket = new ServerSocket(port)) {
                mysocket.setSoTimeout(1000); // set a timeout of 1 second

                System.out.println("Server is waiting for a player on port " + port + "!");

                while (running && !isInterrupted()) {
                    try {
                        Socket clientSocket = mysocket.accept();
                        ClientThread c = new ClientThread(clientSocket, count);
                        callback.accept("A Player has connected to server: " + "Player #" + (count+1));
                        clients.add(c);
                        c.start();
                        count++;
                    } catch (java.net.SocketTimeoutException e) {
                        // ignore and check running flag again
                    }
                }

                System.out.println("Server Stopped!");
            } catch (Exception e) {
                System.out.println("Server No launch catch");
                callback.accept("Server socket did not launch");
            }
        }
    }

    class ClientThread extends Thread {

        Socket connection;
        ObjectInputStream in;
        ObjectOutputStream out;
        int count;
        String hand, dealer, message;

        ClientThread(Socket s, int count) {
            this.connection = s;
            this.count = count;
        }

        public void updateClients(PokerInfo info) {
            for (ClientThread t : clients) {
                try {
                    t.out.writeObject(info);
                } catch (Exception e) {
                }
            }
        }

        public void run() {
            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
            } catch (Exception e) {
                System.out.println("Streams not open");
            }

            updateClients(new PokerInfo("New Player connected", count));
            pgames.add(new Poker());

            while (true) {
                try {
                    PokerInfo info = (PokerInfo) in.readObject();
                    parseInfo(info);
                } catch (Exception e) {
                    callback.accept("Player #" + (count+1) + " disconnected");
                    updateClients(new PokerInfo("Player disconnected", count));
                    clients.remove(this);
                    break;
                }
            }
        }

        void parseInfo(PokerInfo p) {
            String text = p.getMessage();

            if (text.contains("Ante")) {
                Pattern pattern = Pattern.compile("Ante:(\\d+),PP:(\\d+)");
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    pgames.get(p.getClientId()).setAnte(Integer.parseInt(matcher.group(1)));
                    pgames.get(p.getClientId()).setPP(Integer.parseInt(matcher.group(2)));
                }

                pgames.get(p.getClientId()).createDeck();
                pgames.get(p.getClientId()).deal();

                hand = "";
                for (Card i : pgames.get(p.getClientId()).getHand()) {
                    hand += i.getCard() + "   ";
                }

                dealer = "";
                for (Card i : pgames.get(p.getClientId()).getDealer()) {
                    dealer += i.getCard() + "   ";
                }

                message = "Hand:" + hand + ",Dealer:" + dealer;

                callback.accept("Player " + (count+1) + " set ante bet to: $" + matcher.group(1)
                        + " & Player Plus bet to: $" + matcher.group(2));

                updateClients(new PokerInfo(message, p.getClientId()));
                updateClients(new PokerInfo("PP:" + pgames.get(p.getClientId()).calcPP(), p.getClientId()));
            }

            if (text.contains("Fold")) {
                int roundCash = pgames.get(p.getClientId()).folded();
                updateClients(new PokerInfo("PP:" + pgames.get(p.getClientId()).calcPP(), p.getClientId()));
                updateClients(new PokerInfo("TOT:" + pgames.get(p.getClientId()).getWin(), p.getClientId()));
                updateClients(new PokerInfo("ROUND:" + roundCash, p.getClientId()));
                callback.accept("Player #" + (p.getClientId()+1) + " folded");
                if (roundCash >= 0) callback.accept("Player #" + (p.getClientId()+1) + " earned $" + roundCash);
                else callback.accept("Player #" + (p.getClientId()+1) + " lost $" + roundCash);
            }

            if (text.contains("New!")) {
                callback.accept("Player #" + (p.getClientId()+1) + " has started a new game");
            }

            if (text.contains("RESET")) {
                pgames.set(p.getClientId(), new Poker());
                pgames.get(p.getClientId()).createDeck(); pgames.get(p.getClientId()).deal();
                callback.accept("Player #" + (p.getClientId()+1) + " has started a fresh game");
            }

            if (text.contains("Play")) {
                callback.accept("Player #" + (p.getClientId()+1) + " has played their hand");
                updateClients(new PokerInfo("PP:" + pgames.get(p.getClientId()).calcPP(), p.getClientId()));
                updateClients(new PokerInfo("WIN:" + pgames.get(p.getClientId()).win(), p.getClientId()));
                if (pgames.get(p.getClientId()).win() == 0) {
                    int rCash = pgames.get(p.getClientId()).justPP();
                    updateClients(new PokerInfo("ROUND:" + rCash, p.getClientId()));
                    if (rCash >= 0) callback.accept("Player #" + (p.getClientId()+1) + " earned $" + rCash);
                    else callback.accept("Player #" + (p.getClientId()+1) + " lost $" + rCash);
                } else {
                    int rCash = pgames.get(p.getClientId()).playGame();
                    updateClients(new PokerInfo("ROUND:" + rCash, p.getClientId()));
                    if (rCash >= 0) callback.accept("Player #" + (p.getClientId()+1) + " earned $" + rCash);
                    else callback.accept("Player #" + (p.getClientId()+1) + " lost $" + rCash);
                }
                updateClients(new PokerInfo("TOT:" + pgames.get(p.getClientId()).getWin(), p.getClientId()));

            }

        }


    }
}
