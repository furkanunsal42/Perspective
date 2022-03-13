package NewGame;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import java.util.ArrayList;


public class Render2DNew extends Application {
    // define boolean movement parameters (each direction has it's boolean so that we can have a smoother movement system)
    static boolean move_x = false, move_inverse_x = false, move_jump = false;

    // define movement speed
    static int speed = 2;
    static int max_speed = 10;

    // main method (launches start function)
    public static void main(String[] args) {
        launch(args);
    }

    // start method (runs to initialize javafx graphics window)
    @Override
    public void start(Stage primary_stage) {

        // create standard javafx window elements
        Pane root = new Pane();
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
        primary_stage.setScene(scene);

        // create objects in map_1
        create_map_1(primary_stage);

        // open window
        primary_stage.show();
    }

    static void create_map_1(Stage primary_stage) {
        Map2D map = new Map2D(7);
        map.unit_square_length = 20;
        map.grid2D[0][0] = 1;
        map.grid2D[6][6] = 1;
        map.update_object_group();
        map.set_unit_length_by_height(600);

        Pane root = (Pane)primary_stage.getScene().getRoot();

        root.getChildren().add(map.object_group);
        // root.getChildren().add(new Rectangle(100, 100));
    }
}

class Vertex2D{
    double x, y, z;
    public Vertex2D(double x, double y){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public String toString(){
        return "X:"+x+" Y:"+y;
    }

    // arithmetic operations with 3d points
    public Vertex2D add(Vertex2D otherPoint){
        return new Vertex2D(this.x+otherPoint.x, this.y+otherPoint.y);
    }
    public Vertex2D add(double value){
        return new Vertex2D(this.x+value, this.y+value);
    }
    public Vertex2D multiply(Vertex3D otherPoint){
        return new Vertex2D(this.x*otherPoint.x, this.y*otherPoint.y);
    }
    public Vertex2D multiply(double value){
        return new Vertex2D(this.x*value, this.y*value);
    }
}

class Object2D{
    Shape mesh;
    static ArrayList<Object2D> all_objects = new ArrayList<>();

    public Object2D(){
        all_objects.add(this);
    }
    public Object2D(Shape mesh){
        all_objects.add(this);
        this.mesh = mesh;
    }
    public Object2D(Shape mesh, double x, double y){
        all_objects.add(this);
        mesh.translateXProperty().set(x);
        mesh.translateYProperty().set(y);
        this.mesh = mesh;
    }

    public void set_rotation(double x, double y, double z){
        mesh.getTransforms().add(new Rotate(x, new Point3D(1, 0, 0)));
        mesh.getTransforms().add(new Rotate(y, new Point3D(0, 1, 0)));
        mesh.getTransforms().add(new Rotate(z, new Point3D(0, 0, 1)));
    }
}

// player
/*
class Player extends Object {
    // Player class is a child class of Object
    // one of the main differences of player is that it is a circle so, it is not defined as vertex but an arc path

    // velocity vector is represented with a vertex
    Vertex velocity = new Vertex(0, 0);

    // location is represented with two double variables but IT SHOULD BECOME VERTEX AT SOME POINT
    double center_x, center_y, radius;
    Color color = Color.DARKRED;

    boolean can_jump = false;

    // it is possible to create a player with no parameters
    public Player(){}

    // it is possible to create a player with parameters
    public Player(double x, double y, double radii){
        this.center_x = x;
        this.center_y = y;
        this.radius = radii;
    }

    // update position based on velocity
    public void apply_physics_movements(){
        center_x += velocity.x;
        center_y += velocity.y;
    }
}
*/

class Map2D{
    int[][] grid2D;
    ArrayList<Object2D> all_world_objects = new ArrayList<>();
    Group object_group = new Group();
    double unit_square_length = 100;
    Map2D(){
        grid2D = new int[7][7];
    }
    Map2D(int grid_size){
        grid2D = new int[grid_size][grid_size];
    }

    public void set_unit_length_by_height(int height){
        unit_square_length = (double)height / grid2D.length;
        update_object_group();
    }

    public void update_object_group(){
        all_world_objects.clear();
        for (int x = 0; x < grid2D.length; x++){
            for (int y = 0; y < grid2D[x].length; y++){
                int value = grid2D[x][y];
                if (value == 1)
                    all_world_objects.add(new Object2D(new Rectangle(unit_square_length, unit_square_length), (grid2D.length-1-x)*unit_square_length, (grid2D[0].length-1-y)*unit_square_length));
            }
        }

        object_group.getChildren().clear();
        for (Object2D obj: all_world_objects) {
            object_group.getChildren().add(obj.mesh);
        }


    }

}
