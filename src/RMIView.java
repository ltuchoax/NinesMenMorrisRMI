import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

public class RMIView extends JFrame {

    //UI
    private JLabel displayField;
    private ImageIcon image;
    private ImageIcon defaultImage;
    private ImageIcon player1Image;
    private ImageIcon player2Image;

    private JButton[] buttons = new JButton[24];

    private JTextArea chatArea;
    private JButton chatButton;
    private JScrollPane chatScrollPane;
    private JTextField chatTextField;

    public RMIView(Integer playerID) {
        initComponents();
        setUpGUI(playerID);
    }

    private void initComponents() {
        for (int i = 0; i < 24; i++) {
            this.buttons[i] = new JButton();
        }

        this.chatArea = new JTextArea();
        this.chatButton = new JButton();
        this.chatScrollPane = new JScrollPane();
        this.chatTextField = new JTextField();

        this.image = new ImageIcon(this.getClass().getResource("/images/board3.png"));

        this.defaultImage = new ImageIcon(this.getClass().getResource("/images/defaultImage.png"));
        this.player1Image = new ImageIcon(this.getClass().getResource("/images/player1.png"));
        this.player2Image = new ImageIcon(this.getClass().getResource("/images/player2.png"));

        this.displayField = new JLabel();
    }

    private void setUpGUI(Integer playerID) {
        this.setResizable(false);
        this.setBackground(Color.WHITE);
        this.setSize(1150, 700);
        this.setTitle("Nines Men's Morris - Player#" + playerID);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        displayField.setIcon(image);
        this.setContentPane(displayField);

        // Buttons Layout
        setupButtonsLayoutView();

        // Chat Layout
        setupChatLayoutView();

        this.setVisible(true);
    }

    private void setupButtonsLayoutView() {
        setupButton(buttons[0], "0",6, 10);
        setupButton(buttons[1], "1",290, 10);
        setupButton(buttons[2], "2",572, 10);

        setupButton(buttons[3], "3",88, 89);
        setupButton(buttons[4], "4",290, 89);
        setupButton(buttons[5], "5",496, 89);

        setupButton(buttons[6], "6",170, 167);
        setupButton(buttons[7], "7",290, 167);
        setupButton(buttons[8], "8",410, 167);

        setupButton(buttons[9], "9",6, 286);
        setupButton(buttons[10], "10",88, 286);
        setupButton(buttons[11], "11",170, 286);

        setupButton(buttons[12], "12",410, 286);
        setupButton(buttons[13], "13",494, 286);
        setupButton(buttons[14], "14",575, 286);

        setupButton(buttons[15], "15",170, 411);
        setupButton(buttons[16], "16",290, 411);
        setupButton(buttons[17], "17",410, 411);

        setupButton(buttons[18], "18",88, 492);
        setupButton(buttons[19], "19",290, 492);
        setupButton(buttons[20], "20",496, 492);

        setupButton(buttons[21], "21",6, 572);
        setupButton(buttons[22], "22",290, 572);
        setupButton(buttons[23], "23",572, 572);

        for (int i = 0; i < 24; i++) {
            this.add(buttons[i]);
        }
    }

    private void setupChatLayoutView() {
        Font font = new Font("Monospaced", Font.PLAIN, 15);

        chatTextField.setBounds(700, 550, 330, 40);

        chatButton.setFont(font);
        chatButton.setText("Send");
        chatButton.setForeground(Color.WHITE);
        chatButton.setBackground(Color.DARK_GRAY);
        chatButton.setBounds(1030, 550, 70, 39);

        chatArea.setEditable(false);
        chatArea.setColumns(20);
        chatArea.setRows(5);
        chatArea.setWrapStyleWord(true);
        chatArea.setLineWrap(true);

        chatArea.setFont(font);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        chatArea.append("--------------- COMMANDS ---------------");
        chatArea.append("\n1- !surrender");
        chatArea.append("\n2- !draw\n");


        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        chatScrollPane.setViewportView(chatArea);
        chatScrollPane.setBounds(700, 80, 400, 460);

        this.add(chatTextField);
        this.add(chatButton);
        this.add(chatScrollPane);
    }
    public void setupButton(JButton button, String i, Integer x, Integer y) {
        button.setBounds(x, y, 80, 80);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.setIcon(defaultImage);
        button.setDisabledIcon(defaultImage);
        button.setActionCommand(i);
    }

    public Integer getButtonsArrayLength() {
        return buttons.length;
    }

    public void addActionListenerOnButtons(ActionListener al) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].addActionListener(al);
        }
    }

    public void updateButtonStatus(Integer index, Boolean isEnabled) {
        buttons[index].setEnabled(isEnabled);
    }

    public void setDefaultImageForEnabledIcon(Integer index) {
        buttons[index].setIcon(defaultImage);
    }

    public void setPlayer1ImageForDisabledIcon(Integer index) {
        buttons[index].setDisabledIcon(player1Image);
    }

    public void setPlayer2ImageForDisabledIcon(Integer index) {
        buttons[index].setDisabledIcon(player2Image);
    }

    public void setPlayer1ImageForEnabledIcon(Integer index) {
        buttons[index].setIcon(player1Image);
    }

    public void setPlayer2ImageForEnabledIcon(Integer index) {
        buttons[index].setIcon(player2Image);
    }

    public void appendMessage(String message) {
        chatArea.append(message);
    }

    public void setupListenersToSendMessage(ActionListener actionListener, KeyListener keyListener) {
        chatButton.addActionListener(actionListener);
        chatTextField.addKeyListener(keyListener);
    }

    public void setChatTextFieldText() {
        chatTextField.setText("");
    }

    public String getChatTextFieldText() {
        return chatTextField.getText();
    }

    public void updateChatPosition() {
        try {
            chatArea.setCaretPosition(chatArea.getLineStartOffset(chatArea.getLineCount() - 1));
        } catch (BadLocationException e) {
            System.out.println("BadLocationException - updateChatPosition()");
        }
    }
}
