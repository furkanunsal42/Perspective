import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.ObjectExpression;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;


public class Render3D extends Application{
    boolean move_x=false, move_inverse_x=false, move_y=false, move_inverse_y=false, move_z=false, move_inverse_z=false;
    boolean rotate_x=false, rotate_inverse_x=false, rotate_y=false, rotate_inverse_y=false, rotate_z=false, rotate_inverse_z=false;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primary_stage){
        // standard javafx windows elements
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600, true);
        Camera camera =  new PerspectiveCamera();
        scene.setCamera(camera);
        scene.setFill(Color.SILVER);
        primary_stage.setScene(scene);
        primary_stage.show();

        // create the first map
        create_world_1(primary_stage, root);
    }

    public void create_world_1(Stage stage, Pane root){
        // objects
        Object3D sphere = new Object3D(new Sphere(50), 200, 200, 0);
        Object3D prism1 = new Object3D(new Box(200, 300, 400), 100, 200, 10);
        Object3D prism2 = new Object3D(new Box(800, 600, 1), 300, 300, 300);
        prism2.set_rotation(0, -45, 0);

        // map
        Map map = new Map();
        map.world_objects.add(sphere);
        map.world_objects.add(prism1);
        map.world_objects.add(prism2);
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // display objects in the scene
        root.getChildren().add(map.object_group);
    }

    public void initialize_movement_system(Stage stage, Map map){
        AnimationTimer movement = new AnimationTimer() {
            @Override
            public void handle(long l) {
                stage.addEventHandler(KeyEvent.KEY_PRESSED, event ->{
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

                stage.addEventHandler(KeyEvent.KEY_RELEASED, event ->{
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

                if (move_x)
                    map.object_group.translateXProperty().set(map.object_group.getTranslateX() - 10);
                if (move_inverse_x)
                    map.object_group.translateXProperty().set(map.object_group.getTranslateX() + 10);
                if (move_y)
                    map.object_group.translateYProperty().set(map.object_group.getTranslateY() - 10);
                if (move_inverse_y)
                    map.object_group.translateYProperty().set(map.object_group.getTranslateY() + 10);
                if (move_z)
                    map.object_group.translateZProperty().set(map.object_group.getTranslateZ() + 10);
                if (move_inverse_z)
                    map.object_group.translateZProperty().set(map.object_group.getTranslateZ() - 10);
                if (rotate_x)
                    map.object_group.getTransforms().add(new Rotate(-2, new Point3D(1, 0, 0)));
                if (rotate_inverse_x)
                    map.object_group.getTransforms().add(new Rotate(+2, new Point3D(1, 0, 0)));
                if (rotate_y)
                    map.object_group.getTransforms().add(new Rotate(+2, new Point3D(0, 1, 0)));
                if (rotate_inverse_y)
                    map.object_group.getTransforms().add(new Rotate(-2, new Point3D(0, 1, 0)));
                if (rotate_z)
                    map.object_group.getTransforms().add(new Rotate(+2, new Point3D(0, 0, 1)));
                if (rotate_inverse_z)
                    map.object_group.getTransforms().add(new Rotate(-2, new Point3D(0, 0, 1)));
            }
        };
        movement.start();
    }
}

class Object3D{
    Shape3D mesh;
    static ArrayList<Object3D> all_objects = new ArrayList<>();

    public Object3D(){
        all_objects.add(this);
    }
    public Object3D(Shape3D mesh){
        all_objects.add(this);
        this.mesh = mesh;
    }
    public Object3D(Shape3D mesh, double x, double y, double z){
        all_objects.add(this);
        mesh.translateXProperty().set(x);
        mesh.translateYProperty().set(y);
        mesh.translateZProperty().set(z);
        this.mesh = mesh;
    }

    public void set_rotation(double x, double y, double z){
        mesh.getTransforms().add(new Rotate(x, new Point3D(1, 0, 0)));
        mesh.getTransforms().add(new Rotate(y, new Point3D(0, 1, 0)));
        mesh.getTransforms().add(new Rotate(z, new Point3D(0, 0, 1)));
    }


    // about display:
    // there is no integrated way in javafx to draw 3D shapes using canvas and graphical context
    // so object3d instances will be displayed by adding them to scene directly
    // so no need to implement a void display method
}

class Vertex3D{
    // a vertex3d can be used to represent a 3d point or a 3d vector
    double x, y, z;
    public Vertex3D(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class Map{
    ArrayList<Object3D> world_objects = new ArrayList<>();
    Group object_group = new Group();
    Vertex3D rotation = new Vertex3D(0, 0, 0);
    Vertex3D position = new Vertex3D(0, 0, 0);

    public void update_object_group(){
        // recreate object_group from world_objects
        object_group.getChildren().clear();
        for (Object3D obj: world_objects) {
            object_group.getChildren().add(obj.mesh);
        }
        // apply rotation to group
        object_group.getTransforms().add(new Rotate(rotation.x, new Point3D(1, 0, 0)));
        object_group.getTransforms().add(new Rotate(rotation.y, new Point3D(0, 1, 0)));
        object_group.getTransforms().add(new Rotate(rotation.z, new Point3D(0, 0, 1)));

        // apply position to group
        object_group.translateXProperty().set(position.x);
        object_group.translateXProperty().set(position.y);
        object_group.translateXProperty().set(position.z);
    }
}

