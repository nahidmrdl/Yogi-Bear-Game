import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.*;


/**
 * This class is used to create the main game window.
 */

public class YogiBearGUI extends JFrame {
    private static final int WIDTH = 555;
    private static final int HEIGHT = 615;
    private GamePanel gamePanel;
    public JLabel stats;
    private Clip backgroundMusic;
    private boolean isMuted = false;

    /**
     * This is the constructor of the class.
     * It creates a new JFrame object and adds a background image to it.
     * It also adds a GamePanel object to the frame.
     */
    public YogiBearGUI() {
        setTitle("Yogi Bear Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        stats = new JLabel("");

        stats.setFont(new Font(stats.getName(), Font.PLAIN, 10));
        add(stats, BorderLayout.SOUTH);

        gamePanel = new GamePanel(this);
        add(gamePanel, BorderLayout.CENTER);

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("assets/bgmusic.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        playBackgroundMusic();
        createMenuBar();
        setVisible(true);
    }

    /**
     * This method is used to play the background music.
     */
    private void playBackgroundMusic() {
        if (backgroundMusic != null && !isMuted) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * This method is used to stop the background music.
     */
    private void toggleMute() {
        isMuted = !isMuted;
        if (isMuted) {
            backgroundMusic.stop();
        } else {
            playBackgroundMusic();
        }
    }

    /**
     * This method is used to show the high score table.
     */
    private void showHighScoreTable() {
        JFrame highScoreFrame = new JFrame("High Scores");
        highScoreFrame.setSize(500, 400);
        highScoreFrame.setLocationRelativeTo(null);

        JPanel highScorePanel = new JPanel();
        highScorePanel.setLayout(new BorderLayout());

        String[] columnNames = {"ID","Name", "Score", "Time"};
        String[][] data = new String[10][4];

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/highscores", "root", "salam123");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM scores ORDER BY score DESC LIMIT 10");

            int i = 0;
            while (resultSet.next()) {
                data[i][0] = resultSet.getString("id");
                data[i][1] = resultSet.getString("player_name");
                data[i][2] = resultSet.getString("score");
                data[i][3] = resultSet.getString("time");
                i++;
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable highScoreTable = new JTable(data, columnNames);
        highScoreTable.setEnabled(false);
        highScoreTable.setRowHeight(30);
        highScoreTable.setFont(new Font("Arial", Font.PLAIN, 20));
        highScoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 20));
        highScoreTable.getTableHeader().setReorderingAllowed(false);
        highScoreTable.getTableHeader().setResizingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(highScoreTable);
        highScorePanel.add(scrollPane, BorderLayout.CENTER);

        highScoreFrame.add(highScorePanel);
        highScoreFrame.setVisible(true);
    }

    /**
     * This method is used to restart the game.
     */
    public void restartGame() {
        gamePanel.initializeGame();
        System.out.println("Game restarted");
    }

    /**
     * This method is used to create the menu bar.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);

        JMenu levelsMenu = new JMenu("Levels");
        gameMenu.add(levelsMenu);

        for (int i = 1; i <= 10; i++) {
            JMenuItem levelMenuItem = new JMenuItem("Level " + i);
            int level = i;
            levelMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (level <= 10) {
                        gamePanel.setLevelFromMenu(level);
                    }
                }
            });
            levelsMenu.add(levelMenuItem);
        }

        JMenu soundMenu = new JMenu("Sound");
        menuBar.add(soundMenu);

        JCheckBoxMenuItem toggleMuteButton = new JCheckBoxMenuItem("Toggle Music");
        toggleMuteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMute();
                toggleMuteButton.setSelected(isMuted);
            }
        });
        soundMenu.add(toggleMuteButton);

        JMenuItem highScoreMenuItem = new JMenuItem("High Scores");
        highScoreMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHighScoreTable();
            }
        });
        gameMenu.add(highScoreMenuItem);

        JMenuItem restartMenuItem = new JMenuItem("Restart Game");
        restartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame();
            }
        });
        gameMenu.add(restartMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        gameMenu.add(exitMenuItem);

    }

}