import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;



/**
 * This class is used to create the game panel.
 */

class GamePanel extends JPanel implements KeyListener {
    public JLabel stats;
    private volatile boolean allowRangerMovement;
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 27;
    private static final int YOGI_SIZE = 24;
    private int NUM_BASKETS;
    private int[] basketRows;
    private int[] basketCols;
    private int yogiRow = 0;
    private int yogiCol = 0;
    private int currentLevel = 1;
    private int collectedBaskets = 0;
    private int numRangers;
    private int numTrees;
    private int numMountains;
    private int[] rangerRows;
    private int[] rangerCols;
    private int[] treeRows;
    private int[] treeCols;
    private int[] mountainRows;
    private int[] mountainCols;
    private boolean[][] obstacleGrid;
    private Random random = new Random();
    private int yogiLives = 3;
    private int totalCollectedBaskets = 0;
    private static final int NO_SPAWN_REGION_SIZE = 3;
    private ImageIcon backgroundImage;
    private Timer timer;
    private int elapsedTime;
    private Timer rangerMovementTimer;

    /**
     * This is the constructor of the class.
     * It creates a new JPanel object and adds a background image to it.
     * It also adds a KeyListener to the panel.
     *
     * @param gui The YogiBearGUI object that is used to access the stats JLabel.
     */
    public GamePanel(YogiBearGUI gui) {
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        backgroundImage = new ImageIcon("assets/yellowst1.jpg");
        stats = gui.stats;
        initializeGame();
    }

    /**
     * The ActionListener for the game timer.
     * This listener is responsible for updating the elapsed time and game statistics
     * each time the timer fires an action event.
     */
    private ActionListener timerAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            elapsedTime++;
            updateStats();
        }
    };

    /**
     * This method is used to save the score to the database.
     *
     * @param playerName The name of the player.
     * @param score      The score of the player.
     * @param time       The time taken by the player to complete the game.
     */
    private void saveScoreToDatabase(String playerName, int score, int time) {
        String url = "jdbc:mysql://localhost:3306/highscores";
        String user = "root";
        String password = "salam123";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO scores (player_name, score, time) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, playerName);
                preparedStatement.setInt(2, score);
                preparedStatement.setInt(3, time);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Score saved to the database successfully.");
                } else {
                    System.out.println("Failed to save score to the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to update the game statistics.
     */
    public void updateStats() {
        stats.setText("Collected Baskets: " + collectedBaskets + " | Total Collected Baskets: " +
                totalCollectedBaskets + " | Level: " + currentLevel + " | Rangers: " +
                numRangers + " | Lives: " + yogiLives + " | Time: " + elapsedTime + " seconds");
    }

    /**
     * This method is used to check if the level is complete.
     * If the level is complete, it will stop the timer and show a message to the user.
     * If the user has completed all levels, it will show a message to the user and restart the game.
     * Additionally, it will save the score to the database.
     */
    private void isLevelComplete() {
        if (collectedBaskets == NUM_BASKETS) {
            allowRangerMovement = false;
            timer.stop();
            if (currentLevel != 10) {
                JOptionPane.showMessageDialog(this, "Level " + currentLevel + " Complete!\nPlease click \"OK\" to procede to Level " + (currentLevel + 1) + "...");
            }
            totalCollectedBaskets += collectedBaskets;
            collectedBaskets = 0;
            updateStats();
            if (currentLevel < 10) {
                currentLevel++;
                initializeLevel();
                timer.start();
            } else {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Congratulations! You've completed all levels! "
                        + "\nTotal Collected Baskets: " + totalCollectedBaskets
                        + "\nRemaining Lives: " + yogiLives
                        + "\nElapsed Time: " + elapsedTime + " seconds"
                        + "\nPlease click \"OK\" to restart the game.");
                saveScoreToDatabase();
                initializeGame();
            }
        }
    }

    /**
     * This method is used to save the score to the database.
     * It will ask the user to enter their name and save the score to the database.
     */
    private void saveScoreToDatabase() {
        String playerName = null;
        while (playerName == null || playerName.trim().isEmpty() || playerName.trim().length() < 3) {
            playerName = JOptionPane.showInputDialog(this, "Enter your name:");
            if (playerName == null) {
                // if user clicks cancel
                break;
            }
            if (playerName.trim().isEmpty() || playerName.trim().length() < 3) {
                JOptionPane.showMessageDialog(this, "Please enter a name with at least 3 characters.");
            }
        }
        // Save score to the database
        if (playerName != null) {
            saveScoreToDatabase(playerName, totalCollectedBaskets, elapsedTime);
        }
    }


    /**
     * This method is used to set the level from the menu.
     *
     * @param level The level that the user has selected from the menu.
     */
    public void setLevelFromMenu(int level) {
        currentLevel = level;
        totalCollectedBaskets = 0;
        collectedBaskets = 0;
        yogiLives = 3;
        elapsedTime = 0;
        updateStats();
        initializeLevel();
    }


    /**
     * This method is used to initialize the level.
     * It will initialize the number of rangers, trees, mountains, and baskets.
     * It will also initialize the ranger, tree, mountain, and basket positions.
     * It will also initialize the obstacle grid.
     * It will also initialize the yogi position.
     */
    private void initializeLevel() {
        numRangers = currentLevel;
        numTrees = currentLevel;
        numMountains = currentLevel;
        NUM_BASKETS = currentLevel * 2;

        rangerRows = new int[numRangers];
        rangerCols = new int[numRangers];

        treeRows = new int[numTrees];
        treeCols = new int[numTrees];

        mountainRows = new int[numMountains];
        mountainCols = new int[numMountains];

        obstacleGrid = new boolean[GRID_SIZE][GRID_SIZE];
        allowRangerMovement = true;

        spawnRangers();
        spawnObstacles(numTrees, treeRows, treeCols);
        spawnObstacles(numMountains, mountainRows, mountainCols);
        spawnBaskets();

        yogiRow = 0;
        yogiCol = 0;

        updateStats();
    }

    /**
     * This method is used to spawn the baskets.
     */
    private void spawnBaskets() {
        basketRows = new int[NUM_BASKETS];
        basketCols = new int[NUM_BASKETS];

        for (int i = 0; i < NUM_BASKETS; i++) {
            int basketRow, basketCol;
            do {
                basketRow = random.nextInt(GRID_SIZE);
                basketCol = random.nextInt(GRID_SIZE);
            } while (obstacleGrid[basketRow][basketCol] || isRangerPosition(basketRow, basketCol) || isBasketPosition(basketRow, basketCol));

            basketRows[i] = basketRow;
            basketCols[i] = basketCol;
        }
    }

    /**
     * This method is used to check if the position is a basket position.
     *
     * @param row The row of the position.
     * @param col The column of the position.
     * @return True if the position is a basket position, false otherwise.
     */
    private boolean isBasketPosition(int row, int col) {
        for (int i = 0; i < NUM_BASKETS; i++) {
            if (basketRows[i] == row && basketCols[i] == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to check if the position is a ranger position.
     *
     * @param row The row of the position.
     * @param col The column of the position.
     * @return True if the position is a ranger position, false otherwise.
     */
    private boolean isRangerPosition(int row, int col) {
        for (int i = 0; i < numRangers; i++) {
            if (rangerRows[i] == row && rangerCols[i] == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to spawn the rangers.
     */
    private void spawnRangers() {
        for (int i = 0; i < numRangers; i++) {
            int rangerRow, rangerCol;
            do {
                rangerRow = random.nextInt(GRID_SIZE);
                rangerCol = random.nextInt(GRID_SIZE);
            } while (obstacleGrid[rangerRow][rangerCol] || isRangerPosition(rangerRow, rangerCol) || isInNoSpawnRegion(rangerRow, rangerCol));
            rangerRows[i] = rangerRow;
            rangerCols[i] = rangerCol;
        }
    }

    /**
     * This method is used to spawn the obstacles.
     *
     * @param numObstacles The number of obstacles to spawn.
     * @param rows         The rows of the obstacles.
     * @param cols         The columns of the obstacles.
     */
    private void spawnObstacles(int numObstacles, int[] rows, int[] cols) {
        for (int i = 0; i < numObstacles; i++) {
            int obstacleRow, obstacleCol;
            do {
                obstacleRow = random.nextInt(GRID_SIZE);
                obstacleCol = random.nextInt(GRID_SIZE);
            } while (obstacleGrid[obstacleRow][obstacleCol] || isInNoSpawnRegion(obstacleRow, obstacleCol));
            rows[i] = obstacleRow;
            cols[i] = obstacleCol;
            obstacleGrid[obstacleRow][obstacleCol] = true;
        }
    }

    /**
     * This method is used to check if the position is in the no spawn region.
     *
     * @param row The row of the position.
     * @param col The column of the position.
     * @return True if the position is in the no spawn region, false otherwise.
     */
    private boolean isInNoSpawnRegion(int row, int col) {
        return (row < NO_SPAWN_REGION_SIZE && col < NO_SPAWN_REGION_SIZE);
    }

    /**
     * This method is used to start the ranger movement.
     */
    private void startRangerMovement() {
        if (rangerMovementTimer != null) {
            rangerMovementTimer.stop();
        }

        rangerMovementTimer = new Timer(1000, e -> {
            if (allowRangerMovement) {
                for (int i = 0; i < numRangers; i++) {
                    int direction = random.nextInt(4);
                    int newRangerRow = rangerRows[i];
                    int newRangerCol = rangerCols[i];

                    switch (direction) {
                        case 0:
                            newRangerRow = Math.max(0, newRangerRow - 1);
                            break;
                        case 1:
                            newRangerRow = Math.min(GRID_SIZE - 1, newRangerRow + 1);
                            break;
                        case 2:
                            newRangerCol = Math.max(0, newRangerCol - 1);
                            break;
                        case 3:
                            newRangerCol = Math.min(GRID_SIZE - 1, newRangerCol + 1);
                            break;
                    }

                    if (!obstacleGrid[newRangerRow][newRangerCol]) {
                        rangerRows[i] = newRangerRow;
                        rangerCols[i] = newRangerCol;
                        repaint();

                        if (checkYogiCollision(newRangerRow, newRangerCol)) {
                            allowRangerMovement = false;
                            yogiLives--;
                            collectedBaskets = 0;
                            timer.stop();
                            showLoseLifeMessage();
                            updateStats();
                            if (yogiLives <= 0) {
                                endGame();
                            } else {
                                timer.start();
                                initializeLevel();
                            }
                        } else {
                            rangerRows[i] = newRangerRow;
                            rangerCols[i] = newRangerCol;
                        }
                    }
                }
                repaint();
            }
        });
        rangerMovementTimer.start();
    }

    /**
     * This method is used to paint the components of the panel.
     *
     * @param g The Graphics object.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);

        for (int i = 0; i <= GRID_SIZE; i++) {
            int x = i * CELL_SIZE;
            g.drawLine(x, 0, x, getHeight());
        }

        for (int i = 0; i <= GRID_SIZE; i++) {
            int y = i * CELL_SIZE;
            g.drawLine(0, y, getWidth(), y);
        }

        drawYogi(g, yogiRow, yogiCol);

        for (int i = 0; i < numRangers; i++) {
            drawRanger(g, rangerRows[i], rangerCols[i]);
        }

        for (int i = 0; i < numTrees; i++) {
            drawTree(g, treeRows[i], treeCols[i]);
        }

        for (int i = 0; i < numMountains; i++) {
            drawMountain(g, mountainRows[i], mountainCols[i]);
        }

        for (int i = 0; i < NUM_BASKETS; i++) {
            drawBasket(g, basketRows[i], basketCols[i]);
        }
    }

    /**
     * This method is used to draw the yogi.
     *
     * @param g   The Graphics object.
     * @param row The row of the yogi.
     * @param col The column of the yogi.
     */
    private void drawYogi(Graphics g, int row, int col) {
        ImageIcon yogiIcon = new ImageIcon("assets/yogi.png");
        Image yogiImage = yogiIcon.getImage();

        int x = col * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        int y = row * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        g.drawImage(yogiImage, x, y, YOGI_SIZE, YOGI_SIZE, this);
    }

    /**
     * This method is used to draw the ranger.
     *
     * @param g   The Graphics object.
     * @param row The row of the ranger.
     * @param col The column of the ranger.
     */
    private void drawRanger(Graphics g, int row, int col) {
        ImageIcon rangerIcon = new ImageIcon("assets/SpongeRanger.png");
        Image rangerImage = rangerIcon.getImage();

        int x = col * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        int y = row * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        g.drawImage(rangerImage, x, y, YOGI_SIZE, YOGI_SIZE, this);
    }

    /**
     * This method is used to draw the tree.
     *
     * @param g   The Graphics object.
     * @param row The row of the tree.
     * @param col The column of the tree.
     */
    private void drawTree(Graphics g, int row, int col) {
        ImageIcon treeIcon = new ImageIcon("assets/tree.png");
        Image treeImage = treeIcon.getImage();

        int x = col * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        int y = row * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        g.drawImage(treeImage, x, y, YOGI_SIZE, YOGI_SIZE, this);
    }

    /**
     * This method is used to draw the mountain.
     *
     * @param g   The Graphics object.
     * @param row The row of the mountain.
     * @param col The column of the mountain.
     */
    private void drawMountain(Graphics g, int row, int col) {
        ImageIcon mountainIcon = new ImageIcon("assets/mountain.png");
        Image mountainImage = mountainIcon.getImage();

        int x = col * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        int y = row * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        g.drawImage(mountainImage, x, y, YOGI_SIZE, YOGI_SIZE, this);
    }

    /**
     * This method is used to draw the basket.
     *
     * @param g   The Graphics object.
     * @param row The row of the basket.
     * @param col The column of the basket.
     */
    private void drawBasket(Graphics g, int row, int col) {
        ImageIcon basketIcon = new ImageIcon("assets/honeyPot.png");
        Image basketImage = basketIcon.getImage();

        int x = col * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        int y = row * CELL_SIZE + (CELL_SIZE - YOGI_SIZE) / 2;
        g.drawImage(basketImage, x, y, YOGI_SIZE, YOGI_SIZE, this);
    }

    /**
     * This method is used to show the lose life message.
     */
    private void showLoseLifeMessage() {
        JOptionPane.showMessageDialog(this, "Yogi Bear lost a life! Remaining lives: " + yogiLives);
    }


    /**
     * This method is used to handle the key typed event.
     *
     * @param e The KeyEvent object.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * This method is used to handle the key pressed event.
     *
     * @param e The KeyEvent object.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int newYogiRow = yogiRow;
        int newYogiCol = yogiCol;

        if (yogiLives > 0) {
            switch (keyCode) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    newYogiRow = Math.max(0, newYogiRow - 1);
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    newYogiRow = Math.min(GRID_SIZE - 1, newYogiRow + 1);
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    newYogiCol = Math.max(0, newYogiCol - 1);
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    newYogiCol = Math.min(GRID_SIZE - 1, newYogiCol + 1);
                    break;
            }


            if (!obstacleGrid[newYogiRow][newYogiCol]) {
                yogiRow = newYogiRow;
                yogiCol = newYogiCol;
                repaint();

                if (checkRangerCollision(newYogiRow, newYogiCol)) {
                    allowRangerMovement = false;
                    yogiLives--;
                    collectedBaskets = 0;
                    timer.stop();
                    showLoseLifeMessage();
                    updateStats();
                    if (yogiLives <= 0) {
                        endGame();
                    } else {
                        timer.start();
                        initializeLevel();
                    }

                } else {
                    yogiRow = newYogiRow;
                    yogiCol = newYogiCol;

                    for (int i = 0; i < NUM_BASKETS; i++) {
                        if (basketRows[i] == newYogiRow && basketCols[i] == newYogiCol) {
                            basketRows[i] = -1;
                            collectedBaskets++;
                            updateStats();
                            isLevelComplete();
                        }

                    }
                }
            }
            repaint();
        }
    }

    /**
     * This method is used to check if there is a ranger collision.
     *
     * @param newYogiRow The new row of the yogi.
     * @param newYogiCol The new column of the yogi.
     * @return True if there is a ranger collision, false otherwise.
     */
    private boolean checkRangerCollision(int newYogiRow, int newYogiCol) {
        for (int i = 0; i < numRangers; i++) {
            int rangerRow = rangerRows[i];
            int rangerCol = rangerCols[i];

            int rowDistance = Math.abs(newYogiRow - rangerRow);
            int colDistance = Math.abs(newYogiCol - rangerCol);

            if ((rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1) || (rowDistance == 1 && colDistance == 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to check if there is a yogi collision.
     *
     * @param newRangerRow The new row of the ranger.
     * @param newRangerCol The new column of the ranger.
     * @return True if there is a yogi collision, false otherwise.
     */
    private boolean checkYogiCollision(int newRangerRow, int newRangerCol) {
        int rowDistance = Math.abs(yogiRow - newRangerRow);
        int colDistance = Math.abs(yogiCol - newRangerCol);

        if ((rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1) || (rowDistance == 1 && colDistance == 1)) {
            return true;
        }

        return false;
    }

    /**
     * This method is used to initialize the game.
     */
    public void initializeGame() {
        if (timer != null) {
            timer.stop();
        }
        if (rangerMovementTimer != null) {
            rangerMovementTimer.stop();
        }

        yogiLives = 3;
        currentLevel = 1;
        collectedBaskets = 0;
        totalCollectedBaskets = 0;
        elapsedTime = 0;
        startRangerMovement();
        initializeLevel();
        timer = new Timer(1000, timerAction);
        timer.start();
        updateStats();
    }

    /**
     * This method is used to end the game.
     */
    private void endGame() {
        if (rangerMovementTimer != null) {
            rangerMovementTimer.stop();
        }

        timer.stop();

        saveScoreToDatabase();

        JOptionPane.showMessageDialog(this, "Game Over! "
                + "\nYogi Bear has run out of lives. "
                + "\nTotal Collected Baskets: " + totalCollectedBaskets
                + "\nElapsed Time: " + elapsedTime + " seconds");
        initializeGame();
    }

    /**
     * This method is used to handle the key released event.
     *
     * @param e The KeyEvent object.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }
}