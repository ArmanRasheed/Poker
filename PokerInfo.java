import java.io.Serializable;
class PokerInfo implements Serializable {
    private String message;
    private int clientId;

    public PokerInfo(String message, int clientId) {
        this.message = message;
        this.clientId = clientId;
    }

    public String getMessage() { return message; }

    public int getClientId() { return clientId; }
}

