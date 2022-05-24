package games;

/**
 * This class defines settings for the mini games.
 * 
 * Currently, it only allows for changing how data in the mini games are saved.
 */
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Settings implements Serializable {
    
    /**
     * This setting disables the saving of mini-game data.
     */
    public static boolean dontSaveData;
    
    /**
     * This setting saves mini-game data after the user exits a mini-game.
     */
    public static boolean doSaveDataTemporarily;
    
    /**
     * This setting saves mini-game data in a file.
     */
    public static boolean doSaveDataPermanently;

    private static BorderPane masterPane = new BorderPane();
    private static VBox settingsPane = new VBox();
    private static Scene masterScene = new Scene(masterPane, 400, 420);
    private static Button btnHome = new Button("Home");
    
    public static void startClass() {
        // this reads in mini game save preferences.
        try {
            // this is for testing the app in NetBeans.
            File saveFile = new File("src/games", "settings.txt");
            
            
            
            if (saveFile.length() == 0) {
                dontSaveData = false;
                doSaveDataTemporarily = true;
                doSaveDataPermanently = false;
                
                // write the settings to a save file
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeBoolean(dontSaveData);
                    outObj.writeBoolean(doSaveDataTemporarily);
                    outObj.writeBoolean(doSaveDataPermanently);
                }
            }
            else {
                // read the settings from a save file
                try (FileInputStream inFile = new FileInputStream(saveFile); ObjectInputStream inObj = new ObjectInputStream(inFile)) {
                    dontSaveData = inObj.readBoolean();
                    doSaveDataTemporarily = inObj.readBoolean();
                    doSaveDataPermanently = inObj.readBoolean();
                }
            }
        }
        catch (EOFException e) {
            System.out.println("End of file reached.");
        }
        catch (IOException e) {
            System.out.println("An error occurred while reading from the file.");
            e.printStackTrace();
        }
    }
    
    /**
     * Draws the settings screen
     * @param selector the game selector's stage
     */
    public static Scene draw(GameSelector selector) {
        Text txtHeader = new Text("Settings");
        Text txtSubheader = new Text("Mini-game scores are saved temporarily by default\n"
                + "when you exit a mini-game.");
        
        RadioButton rbDontSaveData = new RadioButton("Don't save mini-game scores when I exit a mini-game\n"
                + "WARNING: Progress WILL be lost upon entering a mini-game.");
        
        RadioButton rbDoSaveDataTemporarily = new RadioButton("Save mini-game scores temporarily\nProgress is "
                + "lost when the application closes");
        
        RadioButton rbDoSaveDataPermanently = new RadioButton("Save mini-game scores permanently\nProgress is saved "
                + "when the application closes");
        
        rbDontSaveData.setSelected(dontSaveData);
        rbDontSaveData.setOnAction(e -> {
            dontSaveData = rbDontSaveData.isSelected();
            doSaveDataTemporarily = false;
            doSaveDataPermanently = false;
            try {
                // this is for testing the app in NetBeans.
                File saveFile = new File("src/games", "settings.txt");
                
                // write the settings to a save file
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeBoolean(dontSaveData);
                    outObj.writeBoolean(doSaveDataTemporarily);
                    outObj.writeBoolean(doSaveDataPermanently);
                }
            }
            catch (IOException ex) {
                System.out.println("An error occurred while writing to the file.");
                ex.printStackTrace();
            }
        });
        
        
        rbDoSaveDataTemporarily.setSelected(doSaveDataTemporarily);
        rbDoSaveDataTemporarily.setOnAction(e -> {
            doSaveDataTemporarily = rbDoSaveDataTemporarily.isSelected();
            dontSaveData = false;
            doSaveDataPermanently = false;
            
            try {
                // this is for testing the app in NetBeans.
                File saveFile = new File("src/games", "settings.txt");
                
                // write the settings to a save file
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeBoolean(dontSaveData);
                    outObj.writeBoolean(doSaveDataTemporarily);
                    outObj.writeBoolean(doSaveDataPermanently);
                }
            }
            catch (IOException ex) {
                System.out.println("An error occurred while writing to the file.");
                ex.printStackTrace();
            }
        });
        
        
        rbDoSaveDataPermanently.setSelected(doSaveDataPermanently);
        rbDoSaveDataPermanently.setOnAction(e -> {
            doSaveDataPermanently = rbDoSaveDataPermanently.isSelected();
            dontSaveData = false;
            doSaveDataTemporarily = false;
            
            try {
                // this is for testing the app in NetBeans.
                File saveFile = new File("src/games", "settings.txt");
                
                // write the settings to a save file
                try (FileOutputStream outFile = new FileOutputStream(saveFile); ObjectOutputStream outObj = new ObjectOutputStream(outFile)) {
                    outObj.writeBoolean(dontSaveData);
                    outObj.writeBoolean(doSaveDataTemporarily);
                    outObj.writeBoolean(doSaveDataPermanently);
                }
            }
            catch (IOException ex) {
                System.out.println("An error occurred while writing to the file.");
                ex.printStackTrace();
            }
        });
        
        ToggleGroup rbGroup = new ToggleGroup();
        rbDontSaveData.setToggleGroup(rbGroup);
        rbDoSaveDataTemporarily.setToggleGroup(rbGroup);
        rbDoSaveDataPermanently.setToggleGroup(rbGroup); 
        
        settingsPane.setAlignment(Pos.CENTER);
        settingsPane.setSpacing(20);
        settingsPane.getChildren().clear();
        
        // this contains the stuff at the top (Home button and the headers)
        VBox headerBox = new VBox();
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setSpacing(10);
        headerBox.getChildren().addAll(btnHome, txtHeader, txtSubheader);
        
        // this contains the settings themselves
        VBox settingsVbox = new VBox();
        settingsVbox.setAlignment(Pos.CENTER);
        settingsVbox.setSpacing(10);
        settingsVbox.getChildren().addAll(rbDontSaveData, rbDoSaveDataTemporarily, rbDoSaveDataPermanently);
        
        // this contains everything that's displayed
        settingsPane.getChildren().addAll(headerBox, settingsVbox);
        
        btnHome.setOnAction(e -> selector.draw());
        
        masterPane.setCenter(settingsPane);
        
        return masterScene;
    }
}
