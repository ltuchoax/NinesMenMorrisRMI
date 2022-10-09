import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PlayerInterface extends Remote {
    public void updateTurn(int bNum) throws RemoteException;
    public void updateAllPoints(Integer[] myUpdatedPoints, Integer[] enemyUpdatedPoints) throws  RemoteException;
    public void receiveMessage(String msg) throws RemoteException;
}
