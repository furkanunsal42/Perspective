import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;


public class Render3D extends Application{
    boolean move_x=false, move_inverse_x=false, move_y=false, move_inverse_y=false, move_z=false, move_inverse_z=false;
    boolean rotate_x=false, rotate_inverse_x=false, rotate_y=false, rotate_inverse_y=false, rotate_z=false, rotate_inverse_z=false;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primary_stage){
        Pane root = new Pane();



        Sphere sphere = new Sphere(50);
        sphere.setTranslateX(200);
        sphere.setTranslateY(200);
        root.getChildren().add(sphere);

        Box rect_prism = new Box(200, 300, 400);
        rect_prism.setTranslateX(100);
        rect_prism.setTranslateY(200);
        rect_prism.setTranslateZ(10);

        Box rect = new Box(800, 600, 1);
        rect.translateXProperty().set(300);
        rect.translateYProperty().set(300);
        rect.translateZProperty().set(300);
        rect.getTransforms().add(new Rotate(-45, new Point3D(0, 1, 0)));
        root.getChildren().add(rect);
        root.getChildren().add(rect_prism);

        Scene scene = new Scene(root, 800, 600, true);
        Camera camera =  new PerspectiveCamera();
        scene.setCamera(camera);
        scene.setFill(Color.SILVER);

        AnimationTimer movement = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (move_x)
                    sphere.translateXProperty().set(sphere.getTranslateX() - 10);
                if (move_inverse_x)
                    sphere.translateXProperty().set(sphere.getTranslateX() + 10);
                if (move_y)
                    sphere.translateYProperty().set(sphere.getTranslateY() - 10);
                if (move_inverse_y)
                    sphere.translateYProperty().set(sphere.getTranslateY() + 10);
                if (move_z)
                    sphere.translateZProperty().set(sphere.getTranslateZ() + 10);
                if (move_inverse_z)
                    sphere.translateZProperty().set(sphere.getTranslateZ() - 10);
                if (rotate_x)
                    sphere.getTransforms().add(new Rotate(-2, new Point3D(1, 0, 0)));
                if (rotate_inverse_x)
                    sphere.getTransforms().add(new Rotate(+2, new Point3D(1, 0, 0)));
                if (rotate_y)
                    sphere.getTransforms().add(new Rotate(+2, new Point3D(0, 1, 0)));
                if (rotate_inverse_y)
                    sphere.getTransforms().add(new Rotate(-2, new Point3D(0, 1, 0)));
                if (rotate_z)
                    sphere.getTransforms().add(new Rotate(+2, new Point3D(0, 0, 1)));
                if (rotate_inverse_z)
                    sphere.getTransforms().add(new Rotate(-2, new Point3D(0, 0, 1)));
            }
        };

        primary_stage.addEventHandler(KeyEvent.KEY_PRESSED, event ->{
            switch (event.getCode()) {
                case W -> move_z = true;
                case S -> move_inverse_z = true;
                case A -> move_x = true;
                case D -> move_inverse_x = true;
                case SHIFT -> move_y = true;
                case CONTROL -> move_inverse_y = true;
                case E -> rotate_z = true;
                case Q -> rotate_inverse_z = true;
                case R -> rotate_x = true;
                case F -> rotate_inverse_x = true;
                case Z -> rotate_y = true;
                case X -> rotate_inverse_y = true;
            }
        });

        primary_stage.addEventHandler(KeyEvent.KEY_RELEASED, event ->{
            switch (event.getCode()) {
                case W -> move_z = false;
                case S -> move_inverse_z = false;
                case A -> move_x = false;
                case D -> move_inverse_x = false;
                case SHIFT -> move_y = false;
                case CONTROL -> move_inverse_y = false;
                case E -> rotate_z = false;
                case Q -> rotate_inverse_z = false;
                case R -> rotate_x = false;
                case F -> rotate_inverse_x = false;
                case Z -> rotate_y = false;
                case X -> rotate_inverse_y = false;
            }
        });
        movement.start();

        primary_stage.setScene(scene);
        primary_stage.show();

    }
}

