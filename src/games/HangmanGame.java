package games;

/**
 * 
 * This class provides all the behind-the-scenes logic of hangman
 */
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class HangmanGame {
    // this is true when the game is active, false when it's over.
    private boolean isGameRunning = false;
    
    // lblWord displays the word to guess. lblInfo displays wrong guesses when
    // the game is active and displays "To continue the game, press ENTER" when
    // the game is over.
    private Text txtWord = new Text("Guess a word: "), txtInfo = new Text();
    
    // a LinkedList containing all letters guessed by the user.
    private LinkedList<Character> guessedLetters = new LinkedList<>();
    private LinkedList<Character> missedLetters = new LinkedList<>();
    
    // words that are randomly chosen to be guessed.
    // there is a file of words, but these are used if the file is unavailable.
    private String[] words = {"banana", "chip", "car", "boat", "ground", "textile", "tree"};
    
    // the word to guess
    private String word;
    
    // the pane that contains the hangman and its pole
    private HangmanPane hangmanPane;
    
    // the pane that contains the hangman plus everything else in the game.
    private Pane masterPane;
    
    private Scene masterScene;
    private Button btnHome = new Button("Home");
    
    private int numberOfWins = 0, numberOfLosses = 0;
    
    public Scene startClass(GameSelector selector) {
        masterPane = new Pane();
        hangmanPane = new HangmanPane(selector.getSceneWidth(), selector.getSceneHeight());
        masterScene = new Scene(masterPane, selector.getSceneWidth(), selector.getSceneHeight());
        // allow the hangman pane to receive character input
        masterPane.requestFocus();
        masterScene.setOnKeyTyped(e -> processLetter(e.getCharacter().charAt(0)));
        
        // allow the user to reset the game by pressing enter
        masterPane.setOnKeyReleased(e -> {
            if (!(isGameRunning) && e.getCode() == KeyCode.ENTER) {
                hangmanPane.stopAnimation();
                beginGame();
            }
        });
        
        btnHome.setOnAction(e -> selector.draw());
        
        // redraw the window when it changes size
        masterScene.widthProperty().addListener(e -> draw());
        masterScene.heightProperty().addListener(e -> draw());
        
        
        // start the game
        beginGame();
        
        return masterScene;
    }
    
    /** this method resets the game after a game ends and is used to initially start the game
     * 
     */
    private void beginGame() {
        // this reads in words from a .txt file
        try {
            // this is for testing the app in NetBeans.
            File saveFile = new File("src/games", "hangman words.txt");
            
            if (saveFile.exists()) {
                Scanner txtFileInput = new Scanner(saveFile);
                
                words = txtFileInput.nextLine().split(",");
            }
        }
        catch (IOException e) {
            System.out.println("An error occurred while reading from the text file");
        }
        
        if (Settings.doSaveDataPermanently) {
            // read in scores from a file.
            try {
                // this is for testing the app in NetBeans.
                File saveFile = new File("src/games", "hangman save file.txt");
                
                // read in each player's scores from a file
                try (FileInputStream inFile = new FileInputStream(saveFile); ObjectInputStream inObj = new ObjectInputStream(inFile)) {
                    int temp1 = inObj.readInt();
                    int temp2 = inObj.readInt();
                    
                    // update the scores in the file if the in-game scores are higher
                    if (numberOfWins > temp1 || numberOfLosses > temp2) {
                        saveGame();
                    }
                    else {
                        numberOfWins = temp1;
                        numberOfLosses = temp2;
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
        
        // assign a random word to be guessed.
        word = words[(int)(Math.random() * words.length)];
        
        
        guessedLetters.clear();
        missedLetters.clear();
        
        // reset the Texts for the word and missed letters.
        txtWord.setText("Guess a word: ");
        txtInfo.setText("");
        
        // hide the letters in the word with '*'
        for (int i = 0; i < word.length(); i++) {
            txtWord.setText(txtWord.getText() + "*");
        }
        
        isGameRunning = true;
        hangmanPane.gameIsBegun();
        
        // draw everything
        draw();
    }
    
    /**
     * Draws the text boxes for the word to guess and letters guessed, then
     * calls the draw() method in HangmanPane to draw the hangman and the pole
     */
    public void draw() {
        
        masterPane.getChildren().clear();
        // contains the Home button and the text boxes that display the number of wins and losses
        VBox controlBox = new VBox();
        controlBox.setAlignment(Pos.CENTER_LEFT);
        controlBox.setSpacing(5);
        controlBox.setLayoutX(masterScene.getWidth() * 0.02);
        controlBox.setLayoutY(masterScene.getHeight() * 0.05);
                
        Text txtWins = new Text("Wins: " + numberOfWins);
        Text txtLosses = new Text("Losses: " + numberOfLosses);
        
        controlBox.getChildren().addAll(btnHome, txtWins, txtLosses);
        
        txtWord.setX(masterScene.getWidth() * 0.4);
        txtWord.setY(masterScene.getHeight() * 0.85);
        
        txtInfo.setX(txtWord.getX());
        txtInfo.setY(txtWord.getY() + 15);
        hangmanPane.draw(masterScene.getWidth(), masterScene.getHeight(), missedLetters.size());
        
        masterPane.getChildren().addAll(hangmanPane, txtWord, txtInfo, controlBox);
        
        // animate the hangman if the game is over
        if (!(isGameRunning)) {
            hangmanPane.startAnimation();
        }
    }
    
    /** this method is called when a user guesses a letter. it reveals any letters
     * in the word that match the guess and adds the guess to the list of guessed letters
     * (if it's a newly guessed word) and to the label of wrong letters if the guess is wrong.
     * 
     * @param guess the letter that the user guesses
     */
    public void processLetter(char guess) {
        
        // make the guess lowercase if it isn't.
        guess = (guess + "").toLowerCase().charAt(0);
        
        // check the guess only if the guess is a letter
        if (((int)guess >= 97 && (int)guess <= 122)) {
        
            // only check the guess if it hasn't been guessed before.
            if (Collections.disjoint(guessedLetters, Arrays.asList(guess)) && isGameRunning) {
                
                // add it to the list of guessed letters
                guessedLetters.add(guess);
                
                // this word is the word in txtWord with all letters that = the user's current guess revealed.
                String newWord = "";
                
                // get the current String that represents the word to guess in txtWord
                // (i.e. if the word to guess is "apple" the word in txtWord might be "*pp*e" or "a****". get THAT word.)
                String currentWord = txtWord.getText().substring(txtWord.getText().indexOf(":") + 2);
                boolean isGuessCorrect = false;
                
                // reveal all letters in the word that match the guess
                for (int i = 0; i < word.length(); i++) {
                    
                    // if the current char is "*" (hidden), check if the actual char in the word to guess
                    // is = to the user's guess. If it is, replace the * with the actual char.
                    if (currentWord.charAt(i) == '*') {
                        if (word.charAt(i) == guess) {
                            newWord += guess;
                            isGuessCorrect = true;
                        }
                        else {
                            newWord += "*";
                        }
                    }
                    // if the char is already revealed, simply add it to the new word.
                    else {
                        newWord += word.charAt(i);
                    }
                }
                
                // replace the old word with the new word.
                txtWord.setText("Guess a word: " + newWord);
                
                // if the user won, end the game
                if (newWord.equals(word)) {
                    numberOfWins++;
                    endGame();
                }
            
                // if the guess is wrong, add it to the label which displays wrong guesses.
                if (!isGuessCorrect) {
                    missedLetters.add(guess);
                    
                    // draw the next part of the hangman
                    hangmanPane.draw(masterPane.getWidth(), masterPane.getHeight(), missedLetters.size());
                    
                    // if the user has missed 7 letters, end the game.
                    if (missedLetters.size() == 7) {
                        numberOfLosses++;
                        endGame();
                    }
                    
                    // otherwise, add their guess to txtInfo.
                    else {
                        if (missedLetters.size() == 1)
                            txtInfo.setText("Missed letters: ");
                        txtInfo.setText(txtInfo.getText() + guess);
                    }
                }
            }
        }
    }
    
    /**
     * Returns the number of letters that the user guessed incorrectly. 
     */
    public int getNumberOfLettersMissed() {
        return missedLetters.size();
    }
    
    /** this method ends the game.
     * 
     */
    private void endGame() {
        // get the current String that represents the word to guess in txtWord
        // (i.e. if the word to guess is "apple" the word in txtWord might be "*pp*e" or "a****". get THAT word.)
        String currentWord = txtWord.getText().substring(txtWord.getText().indexOf(":") + 2);
        
        // update the labels and allow the user to end the game by pressing enter.
        txtWord.setText("The word is: " + word);
        txtInfo.setText("Press ENTER to restart");
        
        // if the user didn't guess the word, animate the hangman
        if (!(currentWord.equals(word))) {
            hangmanPane.startAnimation();
        }
        
        // reset the guessed letters and missed letters LinkedLists.
        guessedLetters = new LinkedList<>();
        missedLetters = new LinkedList<>();
        
        if (Settings.doSaveDataPermanently) {
            saveGame();
        }
        
        isGameRunning = false;
    }
    
    /**
     * Saves the player's scores to a file
     */
    public void saveGame() {
        try {
            // this is for testing the app in NetBeans.
            File saveFile = new File("src/games", "hangman save file.txt");
            
            // save the player's scores
            try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                outObj.writeInt(numberOfWins);
                outObj.writeInt(numberOfLosses);
            }
        }
        catch (IOException ex) {
            System.out.println("An error occurred while writing to the file.");
            ex.printStackTrace();
        }
    }
    
    /**
     * Deletes the player's scores from a file
     */
    public static void deleteSaveData() {
        try {
            // this is for testing the app in NetBeans.
            File saveFile = new File("src/games", "hangman save file.txt");
            
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
}








