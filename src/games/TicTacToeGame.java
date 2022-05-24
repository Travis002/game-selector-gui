package games;

/**
 * This class provides the logic and design of tic tac toe.
 */
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;


public class TicTacToeGame {
    
    // this stores the state of the game (if it's still going or not)
    private boolean isGameRunning;

    // the current player.
    private int currentPlayer = 1;
    
    // scores for each player (how many games they've won)
    private int player1Score = 0, player2Score = 0;
    
    // this goes back to the game selector screen
    private Button btnHome = new Button("Home");
    
    // this displays the scores
    private Label lblScore = new Label();
    
    // this displays whose turn it is
    // from now on, I'll refer to it as HUD.
    private Label lblInfo1 = new Label();
    
    // this displays who won or if the game was a tie
    private Label lblInfo2 = new Label();
    
    // contains the game board on top and a label with the player's turn on bottom
    // from now on, I'll refer to it as HUD.
    private VBox masterPane = new VBox();
    
    // this stores Cell data
    private Cell[][] gameGrid = new Cell[3][3];
    
    // ...and this displays the Cells
    private GridPane gameBoard = new GridPane();
    
    private Scene masterScene = new Scene(masterPane, 240, 230);
    
    // this is the size of the X's and O's
    private static double letterSize;
    
    /**
     * Initializes the instance variables
     * 
     * @param selector the stage to put everything in
     */
    public Scene startClass(GameSelector selector) {
        masterPane.setAlignment(Pos.CENTER);
        masterPane.setSpacing(7);
        
        // set properties for the GridPane that displays the cells.
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setPadding(new Insets(5, 5, 5, 5));
        gameBoard.setHgap(15);
        gameBoard.setVgap(10);
        gameBoard.setGridLinesVisible(true);
        
        masterScene.widthProperty().addListener(e -> {
            draw();
        });
        masterScene.heightProperty().addListener(e -> {
            draw();
        });
        
        masterPane.requestFocus();
        
        // allow the user to reset the game by pressing enter.
        masterPane.setOnKeyReleased(e -> {
            if (!(isGameRunning) && e.getCode() == KeyCode.ENTER) {
                beginGame();
            }
        });
        
        btnHome.setOnAction(e -> selector.draw());
        
        // start the game
        beginGame();
        
        return masterScene;
    }
    
    /**
     * Initializes the game before it starts and is used to reset the game after
     * it finishes.
     */
    public void beginGame() {
        // reset the internal storage of the Cells
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameGrid[i][j] = null;
            }
        }
        
        gameBoard.getChildren().clear();
        
        // make the grid of cells
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                Cell cell = new Cell(row, column);
                cell.setAlignment(Pos.CENTER);
                gameGrid[row][column] = cell;
                gameBoard.add(gameGrid[row][column], column, row);
            }
        }
        
        if (Settings.doSaveDataPermanently) {
            // read in scores from a file.
            try {
                // this is for testing the app in NetBeans.
                File saveFile = new File("src/games", "tictactoe save file.txt");

                // read in each player's scores from a file
                try (FileInputStream inFile = new FileInputStream(saveFile); ObjectInputStream inObj = new ObjectInputStream(inFile)) {
                    int temp1 = inObj.readInt();
                    int temp2 = inObj.readInt();
                    
                    // update the scores in the file if the in-game scores are higher
                    if (player1Score > temp1 || player2Score > temp2) {
                        saveGame();
                    }
                    else {
                        player1Score = temp1;
                        player2Score = temp2;
                    }
                }
            }
            catch (EOFException ex) {
                System.out.println("End of file reached: There is no data in the save file to be read.");
            }
            catch (IOException ex) {
                System.out.println("An error occurred while reading from the file.");
                ex.printStackTrace();
            }
        }
        // override saved data
        else if (Settings.dontSaveData) {
            deleteSaveData();
        }
        
        lblScore.setText("Player 1:   " + player1Score + "\t\tPlayer 2:   " + player2Score);
        // make it player 1's turn
        lblInfo1.setText("X - Player 1's turn");
        lblInfo1.setAlignment(Pos.CENTER);
        lblInfo2.setText("");
        lblInfo2.setAlignment(Pos.CENTER);
        currentPlayer = 1;
        isGameRunning = true;
        
        draw();
    }
    
    /**
     * Draws everything on the window.
     */
    private void draw() {
        // clear the window
        masterPane.getChildren().clear();
        
        double windowSize = 0;
        if (masterPane.getHeight() < masterPane.getWidth()) {
            windowSize = masterPane.getHeight();
        }
        else {
            windowSize = masterPane.getWidth();
        }
        
        letterSize = windowSize / 20.0;
        
        // make the text in the labels bigger
        Font newFont1 = Font.font(lblInfo1.getFont().getFamily(), (windowSize / 15.0 <= 17 ? windowSize / 15.0 : 17));
        Font newFont2 = Font.font(lblInfo2.getFont().getFamily(), (windowSize / 17.0 <= 17 ? windowSize / 17.0 : 17));
        Font scoreFont = Font.font(lblScore.getFont().getFamily(), (windowSize / 17.0 <= 17 ? windowSize / 17.0 : 17));
        lblScore.setFont(scoreFont);
        lblInfo1.setFont(newFont1);
        lblInfo2.setFont(newFont2);
        
        // reset the size of the grid cells
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                gameGrid[row][column].setSize(windowSize / 10.0);
            }
        }
        
        // draw everything again
        masterPane.getChildren().addAll(btnHome, lblScore, gameBoard, lblInfo1, lblInfo2);
    }
    
    /** after a player puts a disk in a Cell, make it the other player's turn
     *  and update the HUD
     */
    private void changeTurn() {
        
        // switch from player 1 (X) to player 2 (O)
        if (currentPlayer == 1) {
            currentPlayer = 2;
            lblInfo1.setText("O - Player 2's turn");
        }
        
        // switch from player 2 (O) to player 1 (X)
        else {
            currentPlayer = 1;
            lblInfo1.setText("X - Player 1's turn");
        }
    }
    
    /** after a player's turn is over, check to see if there's a winner
     * 
     */
    private boolean checkForWin() {
        // check all possible win positions for the rows
        for (int row = 0; row < 3; row++) {
            if (gameGrid[row][0].getPlayer() == currentPlayer && gameGrid[row][1].getPlayer() == currentPlayer
                    && gameGrid[row][2].getPlayer() == currentPlayer) {
                animateWinningCells(new Cell[]{gameGrid[row][0], gameGrid[row][1], gameGrid[row][2]});
                return true;
            }
        }
        
        // check all possible win positions for the columns
        for (int column = 0; column < 3; column++) {
            if (gameGrid[0][column].getPlayer() == currentPlayer && gameGrid[1][column].getPlayer() == currentPlayer
                    && gameGrid[2][column].getPlayer() == currentPlayer) {
                animateWinningCells(new Cell[]{gameGrid[0][column], gameGrid[1][column], gameGrid[2][column]});
                return true;
            }
        }
        
        // check the diagonal that goes from the top left to the bottom right
        if (gameGrid[0][0].getPlayer() == currentPlayer && gameGrid[1][1].getPlayer() == currentPlayer
                && gameGrid[2][2].getPlayer() == currentPlayer) {
            animateWinningCells(new Cell[]{gameGrid[0][0], gameGrid[1][1], gameGrid[2][2]});
            return true;
        }
        
        // check the diagonal that goes from the bottom left to the top right
        if (gameGrid[2][0].getPlayer() == currentPlayer && gameGrid[1][1].getPlayer() == currentPlayer
                && gameGrid[0][2].getPlayer() == currentPlayer) {
            animateWinningCells(new Cell[]{gameGrid[2][0], gameGrid[1][1], gameGrid[0][2]});
            return true;
        }
        // if it made it here, there's not a winner.
        return false;
    }
    
    /** after someone won, flash the cells that won.
     * 
     * @param cells the game grid
     */
    private void animateWinningCells(Cell[] cells) {
        
        // create the animation, which I'm using a FadeTransition
        FadeTransition[] animations = new FadeTransition[4];
        
        // start the animation on each of the winning Cells
        for (int i = 0; i < cells.length; i++) {
            animations[i] = new FadeTransition(Duration.seconds(1));
            animations[i].setNode(cells[i]);
            animations[i].setCycleCount(FadeTransition.INDEFINITE);
            animations[i].setAutoReverse(true);
            animations[i].setFromValue(1);
            animations[i].setToValue(0.1);
            animations[i].setByValue(1);
            animations[i].setDuration(Duration.seconds(0.5));
            animations[i].play();
        }
        
    }
    
    /** when a player clicks on a Cell, this method places one of their
     *  disks in the Cell if it's empty
     * 
     *  @param c the cell the user clicked on
     */
    private void placeDisk(Cell c) {
        
        // if the Cell isn't empty or the game isn't running, the disk can't be put
        // in the Cell
        if (c.isEmpty() && isGameRunning) {
            // clear the 2nd label's text
            lblInfo2.setText("");
            
            // put the current player's disc in the Cell.
            c.setLetter(currentPlayer);
            
            // if someone won, stop the game
            if (checkForWin()) {
                if (currentPlayer == 1) {
                    player1Score++;
                }
                else {
                    player2Score++;
                }
                
                lblInfo1.setText((currentPlayer == 1 ? "X - Player 1" : "O - Player 2") + " won!");
                lblInfo2.setText("Press ENTER to play another game");
                if (Settings.doSaveDataPermanently) {
                    saveGame();
                }
                isGameRunning = false;
            }
            
            // otherwise, change turns
            else {
                changeTurn();
            }
            
            // this remains true unless the board isn't full
            // (in other words, this isn't necessarily true)
            boolean isBoardFull = true;
            
            // check for a draw
            if (isGameRunning) {
                // check if all Cells are full
                for (int row = 0; row < 3; row++) {
                    for (int column = 0; column < 3; column++) {
                        if (gameGrid[row][column].isEmpty()) {
                            isBoardFull = false;
                        }
                    }
                }
            
                // if all Cells are full, alert the user and stop the game
                if (isBoardFull) {
                    lblInfo1.setText("It's a tie!");
                    lblInfo2.setText("Press ENTER to play another game");
                    if (Settings.doSaveDataPermanently) {
                        saveGame();
                    }
                    isGameRunning = false;
                }
            }
        }
        
        // alert the user if they try to place a disk in an occupied Cell
        else if (c.isEmpty() == false && isGameRunning) {
            if (!(lblInfo2.getText().contains("Cannot go there"))) {
                lblInfo2.setText(lblInfo2.getText() + "Cannot go there");
            }
        }
    }
    
    /**
     * Saves each player's score to a file
     */
    public void saveGame() {
        try {
            // this is for testing the app in NetBeans.
            File saveFile = new File("src/games", "tictactoe save file.txt");
            
            // write the player's scores to a file
            try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                outObj.writeInt(player1Score);
                outObj.writeInt(player2Score);
            }
        }
        catch (IOException ex) {
            System.out.println("An error occurred while writing to the file.");
            ex.printStackTrace();
        }
    }
    
    /**
     * Removes each player's score from the file
     */
    public static void deleteSaveData() {
        try {
            // this is for testing the app in NetBeans.
            File saveFile = new File("src/games", "tictactoe save file.txt");
            
            // erase the data in the save file
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeInt(0);
                    outObj.writeInt(0);
                }
        }
        catch (IOException ex) {
            System.out.println("An error occurred while writing to the file.");
            ex.printStackTrace();
        }
    }
    
    // this class is for each cell on the game board
    class Cell extends StackPane {
        
        // where the Cell is located on the game board
        private int row, column;
        
        private Rectangle background = new Rectangle(20, 20);
        
        private Label label = new Label();
        
        // no default constructor because the Cell NEEDS to know where it's at.
        public Cell(int r, int c) {
            row = r;
            column = c;
            background.setStroke(Color.BLACK);
            background.setFill(Color.WHITE);
            super.getChildren().add(background);
            super.setOnMouseClicked(e -> placeDisk(this));
        }
        
        // this method returns which player's disk is in the Cell, if any
        public int getPlayer() {
            switch (getLetter()) {
                case 'X':
                    return 1;
                case 'O':
                    return 2;
                default:
                    return -1;
            }
        }
        
        // getters for the x and y coordinates
        public int getRow() {
            return row;
        }
        
        public int getColumn() {
            return column;
        }
        
        // this is used to put either an X or an O in the Cell
        public void setLetter(int player) {
            char newLetter;
            if (player == 1) {
                newLetter = 'X';
            }
            else {
                newLetter = 'O';
            }
            
            label.setText(newLetter + "");
            label.setFont(Font.font(label.getFont().getFamily(), background.getWidth() / 2.0));
            super.getChildren().add(label);
        }
        
        // retrieves the letter in this Cell
        public char getLetter() {
            if (!label.getText().equals("")) {
                return label.getText().charAt(0);
            }
            else {
                return ' ';
            }
        }
        
        // sets the size of the Rectangle (width and height are equal, like a square)
        public void setSize(double size) {
            if (size > 0) {
                background.setWidth(size);
                background.setHeight(size);
                
                if (super.getChildren().size() > 1) {
                    Font newFont = Font.font(label.getFont().getFamily(), size / 2.0);
                    label.setFont(newFont);
                }
            }
        }
        
        // this method returns if the Cell has no disk in it
        public boolean isEmpty() {
            return getPlayer() == -1;
        }
    }
}
