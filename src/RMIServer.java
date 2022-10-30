import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements ServerInterface {
    private static final long serialVersionUID = 1L;

    private int numPlayers;
    private PlayerInterface player1;
    private PlayerInterface player2;

    public RMIServer() throws RemoteException {
        super();

        System.out.println("----Starting Server----");
        numPlayers = 0;
    }

    @Override
    public int getPlayerID() {
        if (numPlayers < 2) {
            numPlayers++;

            System.out.println("Player #" + numPlayers + " has connected!");

            if (numPlayers == 2) {
                System.out.println("There are 2 players connected. No more connections accepted.");
            }
        }

        return numPlayers;
    }
    


    @Override
    public void lookupPlayer(String playerURL, int playerID) throws RemoteException {
        try {
            if (playerID == 1) {
                player1 = (PlayerInterface) Naming.lookup(playerURL);
            }

            if (playerID == 2) {
                player2 = (PlayerInterface) Naming.lookup(playerURL);
            }
        }
        catch (Exception e) {
            System.out.println("Exception - lookupPlayer()");
        }
    }

    @Override
    public void sendButtonNum(int bNum, int playerID) throws RemoteException {
        if (playerID == 1) {
            player2.updateTurn(bNum);
        }

        if (playerID == 2) {
            player1.updateTurn(bNum);
        }
    }
    @Override
    public void updateAllPoints(Integer[] myPoints, Integer[] enemyPoints, int playerID) throws RemoteException {
        if (playerID == 1) {
            player2.updateAllPoints(enemyPoints, myPoints);
            player1.updateAllPoints(myPoints, enemyPoints);
        }

        if (playerID == 2) {
            player1.updateAllPoints(enemyPoints, myPoints);
            player2.updateAllPoints(myPoints, enemyPoints);
        }
    }

    @Override
    public void sendMessage(String msg, int playerID) throws RemoteException {
        if (playerID == 1) {
            player2.receiveMessage(msg);
        }

        if (playerID == 2) {
            player1.receiveMessage(msg);
        }
    }

    public static void main(String[] args) {

        try {
            LocateRegistry.createRegistry(1099);
            RMIServer server = new RMIServer();
            Naming.rebind("//localhost/ServerRef", server);

            System.out.println("Waiting Connections...");
        }
        catch (Exception e) {
            System.out.println(e);
        }

    }
}
