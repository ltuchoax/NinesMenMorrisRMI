import javax.swing.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RMIPlayer {
    private static final long serialVersionUID = 1L;

    // UI
    private RMIView gameView;

    // GAME LOGIC
    private int playerID;
    private int turnsMade;
    private int otherPlayer;

    private Integer[] myPoints;
    private Integer[] enemyPoints;
    private Integer[] allPoints;

    private int selectedPoint;
    private int targetPoint;
    private boolean isMyTurn;
    private boolean madeAMill;
    private boolean canRemove;
    private List<Integer[]> millsMade;

    private boolean buttonsEnabled;

    private int piecesUsed;
    private int enemyPieces;

    private List<Integer[]> segments;

    private boolean winner;
    private boolean draw;
    private int drawCount;

    // CONNECTIONS TO SERVER
    private ServerInterface serverInterface;
    private RMIPlayerConnection playerConnection;

    public RMIPlayer() {
        this.turnsMade = 0;
        this.myPoints = new Integer[9];
        this.enemyPoints = new Integer[9];
        this.allPoints = new Integer[24];

        this.piecesUsed = 0;
        this.enemyPieces = 0;

        this.selectedPoint = -1;
        this.targetPoint = -1;
        this.isMyTurn = false;
        this.madeAMill = false;
        this.canRemove = false;

        this.segments = getSegmentsList();
        this.millsMade = new ArrayList<Integer[]>();

        this.winner = false;
        this.draw = false;
        this.drawCount = 0;

        connectToServer();

        this.gameView = new RMIView(playerID);

        setUpPlayers();

        setUpButtons();
        setUpChat();
    }

    private List<Integer[]> getSegmentsList() {
        List<Integer[]> segments = new ArrayList<Integer[]>();

        Integer[] mill1 = {0, 1, 2};
        Integer[] mill2 = {3, 4, 5};
        Integer[] mill3 = {6, 7, 8};
        Integer[] mill4 = {9, 10, 11};
        Integer[] mill5 = {12, 13, 14};
        Integer[] mill6 = {15, 16, 17};
        Integer[] mill7 = {18, 19, 20};
        Integer[] mill8 = {21, 22, 23};
        Integer[] mill9 = {0, 9, 21};
        Integer[] mill10 = {3, 10, 18};
        Integer[] mill11 = {6, 11, 15};
        Integer[] mill12 = {1, 4, 7 };
        Integer[] mill13 = {16, 19, 22};
        Integer[] mill14 = {8, 12, 17};
        Integer[] mill15 = {5, 13, 20};
        Integer[] mill16 = {2, 14, 23};

        segments.add(mill1);
        segments.add(mill2);
        segments.add(mill3);
        segments.add(mill4);
        segments.add(mill5);
        segments.add(mill6);
        segments.add(mill7);
        segments.add(mill8);
        segments.add(mill9);
        segments.add(mill10);
        segments.add(mill11);
        segments.add(mill12);
        segments.add(mill13);
        segments.add(mill14);
        segments.add(mill15);
        segments.add(mill16);

        return segments;
    }

    private void connectToServer() {
        try {
            serverInterface = (ServerInterface) Naming.lookup("//localhost/ServerRef");

            this.playerID = serverInterface.getPlayerID();

            playerConnection = new RMIPlayerConnection();

            String playerURL = "//localhost/Player" + playerID + "Ref";
            Naming.rebind(playerURL, playerConnection);

            serverInterface.lookupPlayer(playerURL, playerID);
        }
        catch (Exception e) {
            System.out.println("Exception - connectToServer()");
        }
    }

    private void setUpPlayers() {

        if (playerID == 1) {
            gameView.appendMessage("\n----- You are player #1. You go first. -----");
            otherPlayer = 2;
            buttonsEnabled = true;
        }
        else {
            gameView.appendMessage("\n----- You are player #2. Wait for your turn. -----");
            otherPlayer = 1;
            buttonsEnabled = false;
        }

        toggleButtons();
    }

    private void setUpButtons() {
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JButton b = (JButton) ae.getSource();
                int bNum = Integer.parseInt(b.getActionCommand());

                // PRIMEIRA FASE
                if (piecesUsed < 9) {
                    myPoints[piecesUsed] = bNum;
                    piecesUsed++;
                }

                if (turnsMade <= 9 || !isMyTurn) {
                    turnsMade++;
                    canRemove = false;
                    System.out.println("Turns made: " + turnsMade);
                }

                buttonsEnabled = false;

                // SEGUNDA FASE
                if (turnsMade > 9 && !madeAMill) {
                    isMyTurn = true;
                    if (selectedPoint < 0) {
                        selectedPoint = bNum;
                    } else if (selectedPoint > 0 && targetPoint < 0) {
                        targetPoint = bNum;
                    }
                    goToPoint();
                } else if (canRemove) {
                    System.out.println("REMOVA A PEÇA");
                    removeEnemyPiece(bNum);
                } else {
                    toggleButtons();
                    try {
                        serverInterface.sendButtonNum(bNum, playerID);
                    }
                    catch (Exception e) {
                        System.out.println("Exception - setUpButtons()");
                    }
                }
            }
        };

        gameView.addActionListenerOnButtons(al);
    }

    private void setUpChat() {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendChatMessage();
            }
        };

        KeyListener keyListener = new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendChatMessage();
                }
            }
        };

        gameView.setupListenersToSendMessage(actionListener, keyListener);
    }

    private void toggleButtons() {
        for (int i = 0; i < gameView.getButtonsArrayLength(); i++) {
            gameView.updateButtonStatus(i, allPoints[i] == null && buttonsEnabled);
        }

        setButtonColor();
    }

    private void toggleButtonsAfterPiecesPlaced() {

        for (int i = 0; i < allPoints.length; i++) {
            if (allPoints[i] == null) {
                gameView.updateButtonStatus(i, true);
                gameView.setDefaultImageForEnabledIcon(i);
            } else if (allPoints[i] == 1) {
                gameView.updateButtonStatus(i, true);
            } else if (allPoints[i] == 2) {
                gameView.updateButtonStatus(i, false);
            }
        }

        setButtonColor();
    }

    private void setButtonColor() {
        for (int i = 0; i < enemyPoints.length; i++) {
            if (playerID == 1) {
                if (myPoints[i] != null) {
                    gameView.setPlayer1ImageForEnabledIcon(myPoints[i]);
                    gameView.setPlayer1ImageForDisabledIcon(myPoints[i]);
                }

                if (enemyPoints[i] != null) {
                    gameView.setPlayer2ImageForDisabledIcon(enemyPoints[i]);
                }
            }

            if (playerID == 2) {
                if (myPoints[i] != null) {
                    gameView.setPlayer2ImageForEnabledIcon(myPoints[i]);
                    gameView.setPlayer2ImageForDisabledIcon(myPoints[i]);
                }

                if (enemyPoints[i] != null) {
                    gameView.setPlayer1ImageForDisabledIcon(enemyPoints[i]);
                }
            }
        }
    }

    private void goToPoint() {
        boolean isValid = false;

        Integer[] buttonToSwitch = new Integer[2];
        buttonToSwitch[0] = selectedPoint;
        if (targetPoint > 0) {
            if (allPoints[targetPoint] == null) {
                buttonToSwitch[1] = targetPoint;
            }

            for (Integer[] segment : segments) {
                if (Arrays.asList(segment).containsAll(Arrays.asList(buttonToSwitch))) {
                    isValid = true;

                    for (int i = 0; i < myPoints.length; i++) {
                        if (myPoints[i] != null) {
                            if (myPoints[i].equals(selectedPoint)) {
                                myPoints[i] = targetPoint;
                            }
                        }
                    }
                }
            }

            if (isValid) {
                updateAllPointsArray();
                toggleButtonsAfterPiecesPlaced();

                checkIfMadeAMill();
                System.out.println(madeAMill);
                if (isMyTurn && madeAMill) {
                    System.out.println("PODE REMOVER PEÇA");
                    canRemove = true;
                } else {
                    finishTurn(targetPoint);
                }
            }
        }
    }

    private void finishTurn(int point) {
        try {
            serverInterface.sendButtonNum(point, playerID);
            serverInterface.updateAllPoints(myPoints, enemyPoints, playerID);
            targetPoint = -1;
            selectedPoint = -1;
        } catch (Exception e) {
            System.out.println("Exception - finishTurn()");
        }
    }

    private void checkIfMadeAMill() {
        for (Integer[] segment : segments) {
            if (Arrays.asList(myPoints).containsAll(Arrays.asList(segment))) {
                System.out.println("CONTÉM UM SEGMENTO DE MILL");
                if (!millsMade.isEmpty()) {
                    for (Integer[] mills : millsMade) {
                        if (!Arrays.asList(myPoints).containsAll(Arrays.asList(mills))) {
                            madeAMill = true;
                            gameView.appendMessage("\n-> Player#" + playerID + " made a mill. Remove one opponent piece.");
                            gameView.updateChatPosition();
                            toggleEnemyButtons();
                            millsMade.add(segment);
                        }
                    }
                } else {
                    madeAMill = true;
                    gameView.appendMessage("\n-> Player#" + playerID + " made a mill. Remove one opponent piece.");
                    gameView.updateChatPosition();
                    toggleEnemyButtons();
                    millsMade.add(segment);
                }
            }
        }
    }

    private void toggleEnemyButtons() {
        for (int i = 0; i < allPoints.length; i++) {
            if (allPoints[i] == null) {
                gameView.updateButtonStatus(i, false);
            } else if (allPoints[i] == 1) {
                gameView.updateButtonStatus(i, false);
            } else if (allPoints[i] == 2) {
                gameView.updateButtonStatus(i, true);
            }
        }
        setEnemyButtonColor();
    }

    private void setEnemyButtonColor() {
        for (int i = 0; i < enemyPoints.length; i++) {
            if (playerID == 1) {
                if (myPoints[i] != null) {
                    gameView.setPlayer1ImageForEnabledIcon(myPoints[i]);
                    gameView.setPlayer1ImageForDisabledIcon(myPoints[i]);
                }

                if (enemyPoints[i] != null) {
                    gameView.setPlayer2ImageForEnabledIcon(enemyPoints[i]);
                }
            }

            if (playerID == 2) {
                if (myPoints[i] != null) {
                    gameView.setPlayer2ImageForEnabledIcon(myPoints[i]);
                    gameView.setPlayer2ImageForDisabledIcon(myPoints[i]);
                }

                if (enemyPoints[i] != null) {
                    gameView.setPlayer1ImageForEnabledIcon(enemyPoints[i]);
                }
            }
        }
    }

    private void removeEnemyPiece(int piece) {
        enemyPieces--;
        for (Integer i = 0; i < enemyPoints.length; i++) {
            if (enemyPoints[i] == piece) {
                enemyPoints[i] = null;
            }
        }
        allPoints[piece] = null;
        isMyTurn = false;
        madeAMill = false;

        finishTurn(targetPoint);
    }

    private void updateAllPointsArray() {
        allPoints = new Integer[24];

        for (int i = 0; i < myPoints.length; i++) {
            if (myPoints[i] != null) {
                allPoints[myPoints[i]] = 1;
            }

            if (enemyPoints[i] != null) {
                allPoints[enemyPoints[i]] = 2;
            }
        }
    }

    private void checkWinner() {
        if (enemyPoints.length == 2) {
            winner = true;
            gameView.appendMessage("\n----- Player#" + playerID + " WINS! -----");

            try {
                serverInterface.sendMessage("@win@", playerID);
                gameView.updateChatPosition();
            }
            catch (Exception e) {
                System.out.println("Exception - checkWinner()");
            }
        }
    }

    // CHAT
    public void sendChatMessage() {
        String message = gameView.getChatTextFieldText();

        gameView.appendMessage("\nPlayer#" + playerID + ": " + message);

        try {
            serverInterface.sendMessage(message, playerID);
        } catch (Exception e) {
            System.out.println("Exception - sendChatMessage()");
        }

        gameView.setChatTextFieldText();

        if (message.equalsIgnoreCase("!surrender") && !winner) {
            gameView.appendMessage("\n You surrendered. Player#" + otherPlayer + " won!");

            buttonsEnabled = false;
            toggleButtons();

            winner = true;
        }

        if (message.equalsIgnoreCase("!draw") && !winner) {
            if (draw && drawCount > 0) {
                gameView.appendMessage("\n You already requested a draw. Wait for the answer.");
            }

            if (!draw && drawCount == 0) {
                gameView.appendMessage("\n You requested a draw. Wait for your opponent.");
                draw = true;
                drawCount++;
            }

            if (!draw && drawCount > 0) {
                gameView.appendMessage("\n GAME OVER! It's a draw.");

                buttonsEnabled = false;
                toggleButtons();

                winner = true;
                draw = true;
            }
        }

        gameView.updateChatPosition();
    }


    private class RMIPlayerConnection extends UnicastRemoteObject implements PlayerInterface {
        private static final long serialVersionUID = 1L;

        public RMIPlayerConnection() throws RemoteException {
            super();

            System.out.println("----Client----");
            System.out.println("Connected to server as Player #" + playerID + ".");
        }

        @Override
        public void updateTurn(int bNum) throws RemoteException {
            if (turnsMade <= 9 || (!isMyTurn && !madeAMill)) {
                if (bNum != -1) {
                    gameView.appendMessage("\n Player#" + otherPlayer + " clicked on point #" + bNum
                            + ". It's your turn.");
                    gameView.updateChatPosition();

                    if (enemyPieces < 9) {
                        enemyPoints[enemyPieces] = bNum;
                        enemyPieces++;
                    }
                }

                if (turnsMade <= 9) {
                    for (int i = 0; i < myPoints.length; i++) {
                        if (myPoints[i] != null) {
                            allPoints[myPoints[i]] = 1;
                        }
                        if (enemyPoints[i] != null) {
                            allPoints[enemyPoints[i]] = 2;
                        }
                    }
                }

                buttonsEnabled = true;
                if (turnsMade >= 9 && enemyPieces <= 9) {
                    toggleButtonsAfterPiecesPlaced();
                } else {
                    toggleButtons();
                }

                if (!winner) {
                    checkWinner();
                }
            }
        }


        @Override
        public void updateAllPoints(Integer[] myUpdatedPoints, Integer[] enemyUpdatedPoints) throws RemoteException {
            myPoints = myUpdatedPoints;
            enemyPoints = enemyUpdatedPoints;

            updateAllPointsArray();

            buttonsEnabled = true;

            if (turnsMade >= 9 && enemyPieces <= 9) {
                toggleButtonsAfterPiecesPlaced();
            } else {
                toggleButtons();
            }

            if (!winner) {
                checkWinner();
            }
        }

        //CHAT
        @Override
        public void receiveMessage(String message) throws RemoteException {

            if (!message.equalsIgnoreCase("@exit@")) {

                if (!message.equalsIgnoreCase("@win@")) {
                    gameView.appendMessage("\nPlayer #" + otherPlayer + ": " + message);
                }

                if (message.equalsIgnoreCase("@win@") && !winner) {
                    gameView.appendMessage("\n----- Player #" + otherPlayer + " WINS! -----");

                    buttonsEnabled = false;
                    toggleButtons();

                    winner = true;
                }

                if (message.equalsIgnoreCase("!surrender") && !winner) {
                    gameView.appendMessage("\n----- YOU WIN! Player #" + otherPlayer + " surrendered. -----");

                    buttonsEnabled = false;
                    toggleButtons();

                    winner = true;
                }

                if (message.equalsIgnoreCase("!draw") && !winner) {
                    if (draw) {
                        gameView.appendMessage("\n----- GAME OVER! Both players agreed to a draw -----");

                        buttonsEnabled = false;
                        toggleButtons();

                        winner = true;
                        draw = true;
                    }
                    else {
                        gameView.appendMessage("\n----- Player #" + otherPlayer + " requested a draw. Send !draw to accept -----");
                        drawCount++;
                    }
                }
            }
            gameView.updateChatPosition();
        }
    }

    public static void main(String[] args) {
        new RMIPlayer();
    }
}
