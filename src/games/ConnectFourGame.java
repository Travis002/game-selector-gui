package games;

/**
* This class provides the logic and visuals of connect 4.
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;


public class ConnectFourGame {
    
    // this stores the state of the game (if it's still going or not)
    private boolean isGameRunning;

    // the current player.
    private int currentPlayer = 1;
    
    // this is a mini version of the current player's disk, it's displayed
    // on the left of the HUD
    // (PLayer 1 has red disks, Player 2 has blue disks)
    private Circle playerDisk = new Circle();
    
    // a Label used to display info about the game like whose turn it is and whether anyone won
    // from now on, I'll refer to it as HUD.
    private Label lblInfo = new Label();
    
    // takes the user back to the game selector screen
    private Button btnHome = new Button("Home");
    
    // contains the game board on top and a label with the player's turn on bottom
    private VBox masterPane = new VBox();
    
    // contains the score
    private Label lblScore = new Label();
    
    private int player1Score = 0, player2Score = 0;
    
    // stores all the cells in the game (the game board)
    // contains the cell data, doesn't display them.
    private Cell[][] gameGrid = new Cell[6][7];
    
    // this is the game board
    private GridPane gameBoard = new GridPane();
    
    private Scene masterScene = new Scene(masterPane, 400, 400);
    
    public Scene startClass(GameSelector selector) {
        // this VBox contains the game board (top) and the Label (bottom)
        masterPane.setAlignment(Pos.CENTER);
        masterPane.setSpacing(5);
        masterPane.setPadding(new Insets(5, 5, 5, 5));
        
        // set properties for the GridPane that displays the cells.
        gameBoard.setAlignment(Pos.CENTER);
        gameBoard.setHgap(15);
        gameBoard.setVgap(10);
        
        // allow the user to reset the game by pressing enter.
        masterPane.setOnKeyReleased(e -> {
            if (!(isGameRunning) && e.getCode() == KeyCode.ENTER) {
                beginGame();
            }
        });
        
        btnHome.setOnAction(e -> selector.draw());
        
        masterScene.widthProperty().addListener(e -> {
            draw();
        });
        masterScene.heightProperty().addListener(e -> {
            draw();
        });
        
        masterPane.requestFocus();
        
        // start the game
        beginGame();
        
        return masterScene;
    }
    
    /**
     * Initializes the game before it starts and is used to reset the game after
     * it finishes.
     */
    public void beginGame() {
        // reset the internal storage of the cells
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 7; column++) {
                gameGrid[row][column] = null;
            }
        }
        
        gameBoard.getChildren().clear();
        
        // make the grid of cells
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 7; column++) {
                Cell cell = new Cell(row, column);
                cell.setStroke(Color.BLACK);
                cell.setFill(Color.WHITE);
                gameGrid[row][column] = cell;
                gameBoard.add(gameGrid[row][column], column, row);
            }
        }
        
        if (Settings.doSaveDataPermanently) {
            // read in scores from a file.
            try {
                // this is for testing the app in NetBeans.
                File saveFile = new File("src/games", "connect4 save file.txt");
                
                if (saveFile.exists()) {
                    // read in each player's scores
                    try (FileInputStream inFile = new FileInputStream(saveFile); ObjectInputStream inObj = new ObjectInputStream(inFile)) {
                        int temp1 = inObj.readInt();
                        int temp2 = inObj.readInt();
                        
                        // if the in-game scores are higher than those in the
                        // file, save the higher scores.
                        if (player1Score > temp1 || player2Score > temp2) {
                            saveGame();
                        }
                        else {
                            player1Score = temp1;
                            player2Score = temp2;
                        }
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
        
        // make it player 1's turn
        lblScore.setText("Player 1: " + player1Score + "\tPlayer 2: " + player2Score);
        lblInfo.setText("Player 1's turn");
        currentPlayer = 1;
        isGameRunning = true;
        
        draw();
    }
    
    /**
     * Draws everything on the window.
     */
    private void draw() {
        masterPane.getChildren().clear();
        
        double windowSize;
        if (masterPane.getWidth() < masterPane.getHeight()) {
            windowSize = masterPane.getWidth();
        }
        else {
            windowSize = masterPane.getHeight();
        }
                
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 7; column++) {
                gameGrid[row][column].setRadius(windowSize * 0.05);
            }
        }
        
        // the HBox contains the HUD and player disk and is at the bottom.
        HBox playerHBox = new HBox();
        playerHBox.setAlignment(Pos.CENTER);
        playerHBox.setSpacing(10);
        
        double windowWidth = masterPane.getWidth();
        
        Font newInfoFont = Font.font(lblInfo.getFont().getFamily(), (windowWidth / 23.0 <= 17 ? 13 : 17));
        Font newScoreFont = Font.font(lblScore.getFont().getFamily(), (windowWidth / 17.0 <= 17 ? windowWidth / 17.0 : 17));
        
        // add the home button and label to the HBox
        playerDisk.setRadius(6);
        playerDisk.setFill(Color.RED);
        lblInfo.setAlignment(Pos.BOTTOM_CENTER);
        lblInfo.setFont(newInfoFont);
        lblScore.setFont(newScoreFont);
        playerHBox.getChildren().addAll(playerDisk, lblInfo);
        
        // add the game board and HUD to the VBox
        masterPane.getChildren().addAll(btnHome, lblScore, gameBoard, playerHBox);
    }
    
    /** 
     * after a player puts a disk in a Cell, make it the other player's turn
     * and update the HUD
     */
    private void changeTurn() {
        
        // switch from player 1 (r) to player 2 (y)
        if (currentPlayer == 1) {
            currentPlayer = 2;
            lblInfo.setText("Player 2's turn");
            playerDisk.setFill(Color.BLUE);
        }
        
        // switch from player 2 (y) to player 1 (r)
        else {
            currentPlayer = 1;
            lblInfo.setText("Player 1's turn");
            playerDisk.setFill(Color.RED);
        }
    }
    
    /** 
     * after a player's turn is over, check to see if there's a winner
     */
    private boolean checkForWin() {
        // check all possible win positions for the rows
        for (int row = 0; row < 6; row++) {
            for (int j = 0; j < 4; j++) {
                if (gameGrid[row][j + 0].getPlayer() == currentPlayer && gameGrid[row][j + 1].getPlayer() == currentPlayer
                        && gameGrid[row][j + 2].getPlayer() == currentPlayer && gameGrid[row][j + 3].getPlayer() == currentPlayer) {
                    animateWinningCells(new Cell[]{gameGrid[row][j + 0], gameGrid[row][j + 1], gameGrid[row][j + 2], gameGrid[row][j + 3]});
                    return true;
                }
            }
        }
        
        // check all all possible win positions for the columns
        for (int column = 0; column < 7; column++) {
            for (int j = 0; j < 3; j++) {
                if (gameGrid[j + 0][column].getPlayer() == currentPlayer && gameGrid[j + 1][column].getPlayer() == currentPlayer
                        && gameGrid[j + 2][column].getPlayer() == currentPlayer && gameGrid[j + 3][column].getPlayer() == currentPlayer) {
                    animateWinningCells(new Cell[]{gameGrid[j + 0][column], gameGrid[j + 1][column], gameGrid[j + 2][column], gameGrid[j + 3][column]});
                    return true;
                }
            }
        }
        
        // check all possible win positions for the diagonals originating from the left column going down right.
        for (int row = 2; row >= 0; row--) {
            int numberOfWinChecks = 3 - row;
            int startingRow = row;
            int startingColumn = 0;
            
            for (int i = 0; i < numberOfWinChecks; i++) {
                if (gameGrid[startingRow + 0][startingColumn + 0].getPlayer() == currentPlayer && gameGrid[startingRow + 1][startingColumn + 1].getPlayer() == currentPlayer
                        && gameGrid[startingRow + 2][startingColumn + 2].getPlayer() == currentPlayer && gameGrid[startingRow + 3][startingColumn + 3].getPlayer() == currentPlayer) {
                    animateWinningCells(new Cell[]{gameGrid[startingRow + 0][startingColumn + 0], gameGrid[startingRow + 1][startingColumn + 1], 
                        gameGrid[startingRow + 2][startingColumn + 2], gameGrid[startingRow + 3][startingColumn + 3]});
                    return true;
                }
                
                startingRow++;
                startingColumn++;
            }
        }
        
        // check all possible win positions for the diagonals originating from the top row second column going down right.
        for (int column = 1; column <= 3; column++) {
            int numberOfWinChecks = 4 - column;
            int startingColumn = column;
            int startingRow = 0;
            
            for (int i = 0; i < numberOfWinChecks; i++) {
                if (gameGrid[startingRow + 0][startingColumn + 0].getPlayer() == currentPlayer && gameGrid[startingRow + 1][startingColumn + 1].getPlayer() == currentPlayer
                        && gameGrid[startingRow + 2][startingColumn + 2].getPlayer() == currentPlayer && gameGrid[startingRow + 3][startingColumn + 3].getPlayer() == currentPlayer) {
                    animateWinningCells(new Cell[]{gameGrid[startingRow + 0][startingColumn + 0], gameGrid[startingRow + 1][startingColumn + 1], 
                        gameGrid[startingRow + 2][startingColumn + 2], gameGrid[startingRow + 3][startingColumn + 3]});
                    return true;
                }
                
                startingRow++;
                startingColumn++;
            }
        }
        
        // check all possible win positions for the diagonals originating from the right column going down left
        for (int row = 2; row >= 0; row--) {
            int numberOfWinChecks = 3 - row;
            int startingRow = row;
            int startingColumn = 6;
            
            for (int i = 0; i < numberOfWinChecks; i++) {
                if (gameGrid[startingRow + 0][startingColumn - 0].getPlayer() == currentPlayer && gameGrid[startingRow + 1][startingColumn - 1].getPlayer() == currentPlayer
                        && gameGrid[startingRow + 2][startingColumn - 2].getPlayer() == currentPlayer && gameGrid[startingRow + 3][startingColumn - 3].getPlayer() == currentPlayer) {
                    animateWinningCells(new Cell[]{gameGrid[startingRow + 0][startingColumn - 0], gameGrid[startingRow + 1][startingColumn - 1], 
                        gameGrid[startingRow + 2][startingColumn - 2], gameGrid[startingRow + 3][startingColumn - 3]});
                    return true;
                }
                
                startingRow++;
                startingColumn--;
            }
        }
        
        // check all possible win positions for the diagonals originating from the top row second-to-last column going down left.
        for (int column = 5; column >= 3; column--) {
            int numberOfWinChecks = column - 2;
            int startingColumn = column;
            int startingRow = 0;
            
            for (int i = 0; i < numberOfWinChecks; i++) {
                if (gameGrid[startingRow + 0][startingColumn - 0].getPlayer() == currentPlayer && gameGrid[startingRow + 1][startingColumn - 1].getPlayer() == currentPlayer
                        && gameGrid[startingRow + 2][startingColumn - 2].getPlayer() == currentPlayer && gameGrid[startingRow + 3][startingColumn - 3].getPlayer() == currentPlayer) {
                    animateWinningCells(new Cell[]{gameGrid[startingRow + 0][startingColumn - 0], gameGrid[startingRow + 1][startingColumn - 1], 
                        gameGrid[startingRow + 2][startingColumn - 2], gameGrid[startingRow + 3][startingColumn - 3]});
                    return true;
                }
                
                startingRow++;
                startingColumn--;
            }
        }
        
        // if it made it here, there's not a winner.
        return false;
    }
    
    /** after someone won, flash the cells that won.
     * 
     * @param cells the winning cells
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
            animations[i].setToValue(0.2);
            animations[i].setByValue(1);
            animations[i].setDuration(Duration.seconds(0.5));
            animations[i].play();
        }
        
    }
    
    /** 
     * when a player clicks on a Cell, this method places one of their
     * disks in the Cell if it's empty
     * 
     * @param c the cell the user clicked on
     */
    private void placeDisk(Cell c) {
        
        // if the Cell isn't empty or the game isn't running, the disk can't be put
        // in the Cell
        if (c.isEmpty() && isGameRunning) {
            
            boolean isBelowCellEmpty = true;
            
            // if the cell isn't on the bottom row, check if its downward neighbor is empty.
            if (c.getRow() < 5) {
                isBelowCellEmpty = !gameGrid[c.getRow() + 1][c.getColumn()].isEmpty();
            }
            
            // if the Cell's downward neighbor is empty or if it's on the bottom row,
            // place the current player's disc in it.
            if (isBelowCellEmpty) {
                
                // put the current player's disc in the Cell.
                c.setFill(currentPlayer == 1 ? Color.RED : Color.BLUE);
                
                // if someone won, stop the game
                if (checkForWin()) {
                    if (currentPlayer == 1) {
                        player1Score++;
                    }
                    else {
                        player2Score++;
                    }
                    
                    lblInfo.setText((currentPlayer == 1 ? "Player 1" : "Player 2") + " won!" +
                            " - Press ENTER to play again");
                    lblScore.setText("Player 1: " + player1Score + "\tPlayer 2: " + player2Score);
                    if (Settings.doSaveDataPermanently) {
                        saveGame();
                    }
                    isGameRunning = false;
                }
                
                // otherwise, change turns
                else {
                    changeTurn();
                }
            }
            
            // if the player clicked a Cell that can't have a disc put in it, alert the user
            else {
                if (!(lblInfo.getText().contains(" - Cannot place disk there"))) {
                    lblInfo.setText(lblInfo.getText() + " - Cannot place disk there");
                }
            }
            
            // this remains true unless the board isn't full
            // (in other words, this isn't necessarily true)
            boolean isBoardFull = true;
            
            // check for a draw
            if (isGameRunning) {
                // check if all Cells are full
                for (int row = 0; row < 6; row++) {
                    for (int column = 0; column < 7; column++) {
                        if (gameGrid[row][column].isEmpty()) {
                            isBoardFull = false;
                        }
                    }
                }
            
                // if all Cells are full, alert the user and stop the game
                if (isBoardFull) {
                    lblInfo.setText("It's a tie! - Press ENTER to play another game");
                    if (Settings.doSaveDataPermanently) {
                        saveGame();
                    }
                    isGameRunning = false;
                }
            }
        }
        
        // alert the user if they try to place a disk in an occupied Cell
        else if (c.isEmpty() == false && isGameRunning) {
            if (!(lblInfo.getText().contains(" - Cannot place disk there"))) {
                lblInfo.setText(lblInfo.getText() + " - Cannot place disk there");
            }
        }
    }
    
    /**
     * Saves each player's scores to a file
     */
    public void saveGame() {
        try {
            // file1 is for testing the app in NetBeans.
            File saveFile = new File("src/games", "connect4 save file.txt");
            
            if (saveFile.exists()) {
                // write each player's score to the file
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeInt(player1Score);
                    outObj.writeInt(player2Score);
                }
            }
        }
        catch (IOException ex) {
            System.out.println("An error occurred while writing to the file.");
            ex.printStackTrace();
        }
    }
    
    /**
     * Delete each player's scores from a file
     */
    public static void deleteSaveData() {
        try {
            // file1 is for testing the app in NetBeans.
            File saveFile = new File("src/games", "connect4 save file.txt");
            
            if (saveFile.exists()) {
                // erase the file's contents
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeInt(0);
                    outObj.writeInt(0);
                }
            }
        }
        catch (IOException ex) {
            System.out.println("An error occurred while writing to the file.");
            ex.printStackTrace();
        }
    }
    
    // this class is for each cell on the game board
    class Cell extends Circle {
        
        // where the Cell is located on the game board
        private int row, column;
        
        // no default constructor because the Cell NEEDS to know where it's at.
        public Cell(int r, int c) {
            row = r;
            column = c;
            super.setRadius(20);
            super.setFill(Color.WHITE);
            super.setOnMouseClicked(e -> placeDisk(this));
        }
        
        // this method returns which player's disk is in the Cell, if any
        public int getPlayer() {
            if (super.getFill() == Color.RED) {
                return 1;
            }
            else if (super.getFill() == Color.BLUE) {
                return 2;
            }
            else {
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
        
        // this method returns if the Cell has no disk in it
        public boolean isEmpty() {
            return getPlayer() == -1;
        }
    }
}
