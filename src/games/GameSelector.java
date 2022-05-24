package games;

/**
 * This class provides a screen to select mini-games.
 */
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GameSelector extends Application {
    
    // contains everything on the game selector screen
    private VBox masterPane = new VBox();
    
    // contains the list of available mini games
    private GridPane gamesPane = new GridPane();
    
    private Stage masterStage;
    private Scene masterScene;
    
    // the mini game classes
    private HangmanGame hangmanGame = new HangmanGame();
    private ConnectFourGame connectFourGame = new ConnectFourGame();
    private TicTacToeGame ticTacToeGame = new TicTacToeGame();
    
    @Override
    public void start(Stage primaryStage) {
        masterStage = primaryStage;
        
        masterStage.setWidth(400);
        masterStage.setHeight(420);
        Settings.startClass();
        draw();
        masterStage.show();
    }
    
    /**
     * Draws the game selector screen
     */
    public void draw() {
        gamesPane = new GridPane();
        masterPane = new VBox();
        masterPane.setAlignment(Pos.CENTER);
        masterPane.setSpacing(20);
        
        masterScene = new Scene(masterPane, masterStage.getWidth(), masterStage.getHeight());
        
        Text txtInfo = new Text("Click to select a game");
        Button btnSettings = new Button("Settings");
        btnSettings.setOnAction(e -> {
            if (masterStage.getWidth() < 430 || masterStage.getHeight() < 400) {
                masterStage.setWidth(430);
                masterStage.setHeight(400);
            }
            masterStage.setScene(Settings.draw(this));
            masterStage.setTitle("Settings");
        });
        
        BorderPane topPane = new BorderPane();
        topPane.setPadding(new Insets(10, 10, 0, 10));
        topPane.setCenter(txtInfo);
        
        GameCell hangman = new GameCell("Hangman");
        GameCell connect4 = new GameCell("Connect 4");
        GameCell tictactoe = new GameCell("Tic Tac Toe");
        
        // set the GridPane properties
        gamesPane.setAlignment(Pos.CENTER);
        gamesPane.setHgap(20);
        gamesPane.setVgap(30);
        
        // add the games to the GridPane
        //gamesPane.add(btnSettings, 0, 1);
        gamesPane.add(hangman, 1, 1);
        gamesPane.add(connect4, 2, 1);
        gamesPane.add(tictactoe, 3, 1);
        
        // add the label "Click to select a game" and the games to the VBox
        masterPane.getChildren().addAll(txtInfo, gamesPane, btnSettings);
        masterStage.setScene(masterScene);
        masterStage.setTitle("Select a Mini-Game");
    }
    
    /**
     * Starts a mini game
     * 
     * @param game the name of the game to start
     */
    public void launchGame(String game) {
        masterPane.getChildren().clear();
        
        // this makes sure the window isn't maximized when a new game starts
        //masterStage.setMaximized(false);
        
        // determine which game to launch from the argument
        switch (game) {
            case "Hangman" ->  {
                if (Settings.dontSaveData) {
                    hangmanGame = new HangmanGame();
                    masterStage.setScene(hangmanGame.startClass(this));
                }
                else {
                    masterStage.setScene(hangmanGame.startClass(this));
                }
                if (masterStage.getWidth() < 300 || masterStage.getHeight() < 200) {
                    masterStage.setWidth(300);
                    masterStage.setHeight(200);
                }
                masterStage.setTitle("Hangman");
                //hangmanGame.draw();
            }
            
            case "Connect 4" ->  {
                if (Settings.dontSaveData) {
                    connectFourGame = new ConnectFourGame();
                    masterStage.setScene(connectFourGame.startClass(this));
                }
                else {
                    masterStage.setScene(connectFourGame.startClass(this));
                }
                
                if (masterStage.getWidth() < 400 || masterStage.getHeight() < 420) {
                    masterStage.setWidth(400);
                    masterStage.setHeight(420);
                }
                masterStage.setTitle("Connect 4");
            }
            
            case "Tic Tac Toe" ->  {
                if (Settings.dontSaveData) {
                    ticTacToeGame = new TicTacToeGame();
                    masterStage.setScene(ticTacToeGame.startClass(this));
                }
                else {
                    masterStage.setScene(ticTacToeGame.startClass(this));
                }
                
                if (masterStage.getWidth() < 240 || masterStage.getHeight() < 260) {
                    masterStage.setWidth(240);
                    masterStage.setHeight(260);
                }
                masterStage.setTitle("Tic Tac Toe");
            }
        }
        // either "new {ClassName}().startClass(this)" or "{objectName}.startClass(this)" will work
        // for example, "new HangmanGame().startClass(this)" or "hangmanGame.startClass(this)"
    }
    
    public double getSceneWidth() {
        return masterScene.getWidth();
    }
    
    public double getSceneHeight() {
        return masterScene.getHeight();
    }
    
    
    public double getStageWidth() {
        return masterStage.getWidth();
    }
    public double getStageHeight() {
        return masterStage.getHeight();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    /**
     * This class is for the games on the game selector screen
     */
    class GameCell extends VBox {
        private StackPane gameBox = new StackPane();
        private Text txtName = new Text();
        private Rectangle background = new Rectangle(70, 40);
        
        public GameCell(String gameName) {
            txtName.setText(gameName);
            background.setStroke(Color.BLACK);
            background.setFill(Color.WHITE);
            background.setStrokeWidth(2);
            background.setVisible(false);
            
            gameBox.setOnMouseEntered(e -> select());
            gameBox.setOnMouseExited(e -> unselect());
            gameBox.getChildren().addAll(background, txtName);
            gameBox.setOpacity(1.0);
            super.setAlignment(Pos.CENTER);
            super.getChildren().addAll(gameBox);
            
            super.setOnMouseClicked(e -> launchGame(gameName));
        }
        
        public void select() {
            background.setVisible(true);
        }
        
        public void unselect() {
            background.setVisible(false);
        }
    }
}
