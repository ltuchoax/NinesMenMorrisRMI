import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    public int getPlayerID() throws RemoteException;
    public void lookupPlayer(String playerURL, int playerID) throws RemoteException;
    public void sendButtonNum(int bNum, int playerID) throws RemoteException;
    public void sendMessage(String msg, int playerID) throws RemoteException;
    public void updateAllPoints(Integer[] myPoints, Integer[] enemyPoints, int playerID) throws RemoteException;
}
