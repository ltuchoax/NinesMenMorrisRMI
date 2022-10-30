import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIPlayerConnection extends UnicastRemoteObject implements PlayerInterface {
    private RMIPlayer player;

    private static final long serialVersionUID = 1L;

    public RMIPlayerConnection(RMIPlayer player) throws RemoteException {
        super();
        this.player = player;
       
        System.out.println("----Client----");
        System.out.println("Connected to server as Player #" + player.getPlayerID() + ".");
    }

    @Override
    public void updateTurn(int bNum) throws RemoteException {
        player.updatePlayerTurn(bNum);
    }


    @Override
    public void updateAllPoints(Integer[] myUpdatedPoints, Integer[] enemyUpdatedPoints) throws RemoteException {
        player.updatePlayerPoints(myUpdatedPoints, enemyUpdatedPoints);
    }

    //CHAT
    @Override
    public void receiveMessage(String message) throws RemoteException {
        player.receivePlayerMessage(message);
    }

}
