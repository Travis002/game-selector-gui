package games;

/**
 * This class provides the hangman itself and its pole, and provides its display
 * and movement.
 * 
 */
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class HangmanPane extends Pane {
    
    // the dimensions of the scene
    private double masterWidth, masterHeight;
    
    // the parts of the hangman that are revealed as the user guesses incorrectly.
    private Shape[] hangmanParts = new Shape[7];
    
    // the base of the pole the hangman's attached to
    private Arc base;
    
    // the part of the pole that goes up then over to the hangman
    private Polyline pole;
    
    // parts of the hangman
    private Line rope;
    private Circle head;
    private Line spine;
    private Line leftArm;
    private Line rightArm;
    private Line leftLeg;
    private Line rightLeg;
    
    // these make the hangman fade and move down at the same time when the player loses.
    private PathTransition fadeAnimationMovement = new PathTransition();
    private FadeTransition fadeAnimation = new FadeTransition();
    
    private boolean isAnimationFinished = false;
    private Text txtGameOver = new Text("Game Over!");
    
    public HangmanPane(double paneWidth, double paneHeight) {
        
        masterWidth = paneWidth;
        masterHeight = paneHeight;
        
        // make the "Gave Over!" text bold and italic.
        txtGameOver.setFont(Font.font(txtGameOver.getFont().getFamily(), FontWeight.BOLD, FontPosture.ITALIC, txtGameOver.getFont().getSize()));
    }
    
    /**
     * Lets the HangmanPane know a new game has begun
     */
    public void gameIsBegun() {
        isAnimationFinished = false;
        fadeAnimationMovement = new PathTransition();
        fadeAnimation = new FadeTransition();
    }
    
    /**
     * this exists so that the draw() method can be called without arguments
     */
    public void draw() {
        draw(masterWidth, masterHeight, hangmanParts.length);
    }
    
    /**
     * Draws the hangman and the pole
     * 
     * @param width
     * The available width to draw the hangman
     * @param height
     * The available height to draw the hangman
     * @param numberOfParts 
     * Specifies how many parts of the hangman to draw
     */
    public void draw(double width, double height, int numberOfParts) {
        // don't forget to erase the current window when redrawing something. It's VERY important.
        super.getChildren().clear();
        masterWidth = width;
        masterHeight = height;
        //super.setWidth(width);
        //super.setHeight(height);
        
        // create the base
        base = new Arc(masterWidth * 0.20, masterHeight * 0.95, 35, 15, 0, 180);
        base.setStroke(Color.BLACK);
        base.setFill(Color.WHITE);
        base.setType(ArcType.OPEN);
        super.getChildren().add(base);
        
        // create the bar extending from the base
        pole = new Polyline(base.getCenterX(), base.getCenterY() - base.getRadiusY(), base.getCenterX(), masterHeight * 0.05,
            base.getCenterX() + masterWidth * 0.35, masterHeight * 0.05);
        super.getChildren().add(pole);
        
        // create the parts for the hangman
        
        // the line that connects the head of the hangman to the pole
        rope = new Line(pole.getPoints().get(pole.getPoints().size() - 2), pole.getPoints().get(pole.getPoints().size() - 1),
                pole.getPoints().get(pole.getPoints().size() - 2), pole.getPoints().get(pole.getPoints().size() - 1) + (masterHeight * 0.1));
        hangmanParts[0] = rope;
        
        // this just serves as a line that the rope travels along to "create" the swinging animation
        Arc hangmanPivot = new Arc(rope.getStartX(), rope.getStartY(), 76, 22, 140, -85);
        hangmanPivot.setCenterY(hangmanPivot.getCenterY() + hangmanPivot.getRadiusY());
        hangmanPivot.setStroke(Color.BLACK);
        hangmanPivot.setFill(Color.WHITE);
        //super.getChildren().add(hangmanPivot);
        
        // the head
        double halfRopeLength = (rope.getEndY() - rope.getStartY()) / 2.0;
        if (masterHeight > (masterWidth * 2)) {
            
            halfRopeLength = (rope.getEndY() - rope.getStartY()) / 3.0;
        }
        head = new Circle(rope.getEndX(), rope.getEndY() + halfRopeLength,
                halfRopeLength);
        
        head.setStroke(Color.BLACK);
        head.setFill(Color.WHITE);
        hangmanParts[1] = head;
        
        // the spine
        spine = new Line(head.getCenterX(), head.getCenterY() + head.getRadius(), head.getCenterX(), (head.getCenterY() + head.getRadius()) + super.getHeight() / 5);
        hangmanParts[4] = spine;
        
        // the left arm
        leftArm = new Line();
        leftArm.setStartX(head.getCenterX() - head.getRadius() * 0.7);
        leftArm.setStartY(head.getCenterY() + head.getRadius() * 0.75);
        leftArm.setEndX(leftArm.getStartX() - head.getRadius() * 2.0);
        leftArm.setEndY(spine.getEndY() - 0.3 * (spine.getEndY() - spine.getStartY()));
        hangmanParts[2] = leftArm;
        
        // the right arm
        rightArm = new Line(leftArm.getStartX() + head.getRadius() * 1.4, leftArm.getStartY(),
            spine.getEndX() + head.getRadius() * 3, leftArm.getEndY());
        hangmanParts[3] = rightArm;
        
        // the left leg
        leftLeg = new Line(spine.getEndX(), spine.getEndY(), spine.getEndX() - head.getRadius() * 2, spine.getEndY() + head.getRadius() * 2);
        hangmanParts[5] = leftLeg;
        
        // the right leg
        rightLeg = new Line(spine.getEndX(), spine.getEndY(), spine.getEndX() + head.getRadius() * 2, spine.getEndY() + head.getRadius() * 2);
        hangmanParts[6] = rightLeg;
        
        // add all the hangman parts to the pane
        // this only adds an amount of parts that is equal to the amount of letters the user has guessed
        // for example, if the user has guessed 3 letters, 3 parts will be added
        for (int i = 0; i < numberOfParts; i++) {
            super.getChildren().add(hangmanParts[i]);
        }
        
        txtGameOver.setX(leftLeg.getEndX());
        txtGameOver.setY(spine.getEndY());
        if (isAnimationFinished) {
            txtGameOver.setVisible(true);
        }
        else {
            txtGameOver.setVisible(false);
        }
        super.getChildren().add(txtGameOver);
    }
    
    /**
     * Runs the hangman animation
     */
    public void playAnimation() {
        fadeAnimationMovement.play();
        fadeAnimation.play();
    }
    
    /**
     * Pauses the hangman animation
     */
    public void pauseAnimation() {
        fadeAnimationMovement.pause();
        fadeAnimation.pause();
    }
    
    /**
     * Stops the hangman animation
     */
    public void stopAnimation() {
        fadeAnimationMovement.stop();
        fadeAnimation.stop();
    }
    
    /**
     * Returns whether the animation is running
     */
    public boolean isAnimationRunning() {
        return fadeAnimation.getStatus() == Animation.Status.RUNNING;
    }
    
    /**
     * Sets up and starts the swinging animation of the hangman
     */
    public void startAnimation() {
        if (!isAnimationFinished) {
            Duration currentTimeFadeMovement = fadeAnimationMovement.getCurrentTime();
            Duration currentTimeFade = fadeAnimation.getCurrentTime();
            stopAnimation();
            // remove all parts of the hangman so that a continuous Polyline containing the parts can be added in
            // the head is removed even though it's added back in later, this is so that it appears on top of the Polyline
            super.getChildren().removeAll(leftLeg, rightLeg, rope, leftArm, rightArm, head, spine);

            // this Polyline is the hangman minus the head and with the arms and rope above the head and spine connected
            Polyline body = new Polyline(rope.getStartX(), rope.getStartY(), rope.getEndX(), head.getCenterY(),
                leftArm.getEndX(), leftArm.getEndY(), rope.getEndX(), head.getCenterY(),
                rightArm.getEndX(), rightArm.getEndY(), rope.getEndX(), head.getCenterY(),
                spine.getEndX(), spine.getEndY(), leftLeg.getEndX(), leftLeg.getEndY(),
                spine.getEndX(), spine.getEndY(), rightLeg.getEndX(), rightLeg.getEndY());

            // the animation for the Polyline (hangman)
            // calculate the middle y coordinate of the hangman (this is where the Arc for the PathTransition animation will go)
            Point2D ropeTop = new Point2D(rope.getStartX(), rope.getStartY());
            Point2D leftLegBottom = new Point2D(leftLeg.getEndX(), leftLeg.getEndY());
            double startPoint = leftLegBottom.midpoint(ropeTop).getY();


            // this puts all the hangman parts together
            Group partsGroup = new Group();
            partsGroup.getChildren().addAll(rope, head, spine, leftArm, rightArm, leftLeg, rightLeg);
            super.getChildren().add(partsGroup);






            // animation time
            // The first animation moves the hangman down and fades it out
            // this is for the movement
            Line path = new Line(rope.getStartX(), startPoint, spine.getEndX(), leftLeg.getEndY());
            fadeAnimationMovement = new PathTransition(Duration.seconds(2), path, partsGroup);

            fadeAnimationMovement.playFrom(currentTimeFadeMovement);


            // this animation slowly fades the hangman out
            fadeAnimation = new FadeTransition(Duration.seconds(2), partsGroup);
            fadeAnimation.setFromValue(1.0);
            fadeAnimation.setToValue(0.0);
            fadeAnimation.setByValue(0.1);

            // when it's done, "Game Over" appears.
            fadeAnimation.setOnFinished(e -> {
                isAnimationFinished = true;
                txtGameOver.setVisible(true);
            });

            fadeAnimation.playFrom(currentTimeFade);
        }
    }
}











