
package xotest;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class TicTacToeClient {

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("");
    private Square[] board = new Square[9];
    private Square currentSquare;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    public TicTacToeClient(String serverAddress) throws Exception {

        socket = new Socket(serverAddress, 55555);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, "South");

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.BLACK);   
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();   
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e){
                        currentSquare = board[j];
                        out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

    public void play() throws Exception {
        String response;
        String Me = null,Opp = null; 
        try {
            response = in.readLine();
            
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                if(mark=='X'){
                    Me="X";
                    Opp="O";     
                }else{
                    Me="O";
                    Opp="X";
                }
                frame.setTitle("Tic Tac Toe - Player " + mark);
            }
            
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setText(Me);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(Opp);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            
            out.println("QUIT");
        }
        finally {
            socket.close();
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
            "Want to play again?",
            "Tic Tac Toe ",
            JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    static class Square extends JPanel {
        JLabel label = new JLabel();

        public Square() {
            setBackground(Color.white);
            add(label);
        }

        public void setText(String Text) {
            label.setFont(new Font("Tahoma",Font.BOLD,96));
            label.setText(Text);
        }
        
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            String serverAddress = (args.length == 0) ? "127.0.0.1" : args[1];
            
            TicTacToeClient client = new TicTacToeClient(serverAddress);
            
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(500,500);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            
            client.play();
            
            if (!client.wantsToPlayAgain()) {
                break;
            }
            
        }
    }
}
