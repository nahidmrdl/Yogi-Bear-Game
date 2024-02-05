import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class is used to create the start game menu.
 */

public class StartGameMenu extends JFrame {

    /**
     * This is the constructor of the class.
     * It creates a new JFrame object and adds a background image to it.
     * It also adds two buttons to the frame, one to start the game and one to exit the game.
     */
    public StartGameMenu() {
        setTitle("Yogi Bear Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel() {
            private Image backgroundImage = new ImageIcon("assets/HomeScreen.jpg").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(new BorderLayout());
        setContentPane(panel);

        JButton startGameButton = new JButton("Start Game");
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                YogiBearGUI game = new YogiBearGUI();
                game.setVisible(true);
                dispose();
            }
        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startGameButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }
}
