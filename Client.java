import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread{

    Socket socketClient;
    ObjectOutputStream out;
    ObjectInputStream in;
    private Consumer<Serializable> callback;
    private String host, hand, dealer;
    int port, id, pp, total, roundWin, res;
    private volatile boolean isRunning = true;
    Object latestData;

    Client(Consumer<Serializable> call, String h, int p){
        host = h; port = p;
        callback = call; id = -1;
    }

    public void run() {
        try {
            socketClient= new Socket(host, port);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        } catch(Exception e) {}

        while(isRunning) {
            try {
                PokerInfo info = (PokerInfo) in.readObject();
                if(id == -1) parseInfo(info);
                else if (info.getClientId() == id) parseInfo(info);
                //callback.accept(info);
            } catch(Exception e) {}
        }
    }

    public void send(PokerInfo data) {
        try {
            out.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopClient() {
        isRunning = false;
        interrupt();
        try {
            if(socketClient != null) {
                socketClient.close();
            }
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    void parseInfo(PokerInfo p) {
        String text = p.getMessage();

        if(text.equals("New Player connected")) { id = p.getClientId(); }

        if(text.contains("Hand")) {
            String[] parts = text.split(",", 2);
            hand = parts[0].substring(5);
            dealer = parts[1].substring(7);
        }

        if (text.contains("PP")) { pp = Integer.parseInt(text.substring(3));}

        if (text.contains("TOT")) { total = Integer.parseInt(text.substring(4)); }

        if (text.contains("ROUND")) { roundWin = Integer.parseInt(text.substring(6)); }

        if (text.contains("WIN")) { res = Integer.parseInt(text.substring(4)); }

    }

    public void updateLatestData(Object data) {
        latestData = data;
    }

    public Object getLatestData() {
        return latestData;
    }

    int getID() { return id; }
    int getPP() { return pp; }
    int getTotal() { return total; }
    int getRoundWin() { return roundWin; }
    int getRes() { return res; }
    String getHand() { return hand; }
    String getDealer() { return dealer; }
}
