package NewGame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import java.util.ArrayList;


public class Render3DNew extends Application{
    static boolean move_x=false, move_inverse_x=false, move_y=false, move_inverse_y=false, move_z=false, move_inverse_z=false;
    static boolean rotate_x=false, rotate_inverse_x=false, rotate_y=false, rotate_inverse_y=false, rotate_z=false, rotate_inverse_z=false;
    static double movement_speed = 2;
    static double rotation_speed = .5;

    static Map current_map = new Map();

    // to be able to close all timers attached to stage without having each of them in separate variables we have to store them in an array
    static ArrayList<AnimationTimer> timers = new ArrayList<>();

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primary_stage){
        // set standard javafx stage
        set_stage(primary_stage);

        // create the first map
        create_world_1(primary_stage);
    }

    static public void set_stage(Stage stage){
        // standard javafx windows elements
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.show();
    }

    static public void create_world_1(Stage stage){

        // map
        Map map = new Map();
        current_map = map;

        /*
        map.grid3D[0][0][0] = 1;
        map.grid3D[6][0][0] = 1;
        map.grid3D[0][6][0] = 1;
        map.grid3D[6][6][0] = 1;
        map.grid3D[0][0][6] = 1;
        map.grid3D[6][0][6] = 1;
        map.grid3D[0][6][6] = 1;
        map.grid3D[6][6][6] = 1;
        map.grid3D[3][3][3] = 1;
        */


        // fill the entire 7x7x7 grid
        map.add_box_to_grid(1, 0, 0, 0, 6, 6, 6);
        // poke 5x7x5 hollow spaces from all three directions
        map.add_box_to_grid(0, 0, 1, 1, 6, 5, 5);
        map.add_box_to_grid(0, 1, 0, 1, 5, 6, 5);
        map.add_box_to_grid(0, 1, 1, 0, 5, 5, 6);

        map.grid3D[3][2][2] = 1;
        map.grid3D[5][1][2] = 1;

        /*
        int[][] image_x = map.create_2d_image_by_x(1);
        for(int[] y: image_x){
            for(int x: y){
                System.out.print(x + " ");
            }
            System.out.println();
        }
        */

        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0)));
        scene.setCamera(camera);
    }

    static void return_to_current_map(Stage stage){
        set_stage(stage);
        Map map = current_map;
        map.update_object_group();
        initialize_movement_system(stage, map);
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();
        root.getChildren().add(map.object_group);
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0)));
        scene.setCamera(camera);
    }


    static public void initialize_movement_system(Stage stage, Map map){
        stage.getScene().setOnKeyPressed(event ->{
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

                case I -> {
                    Render2DNew.create_map(stage, map.create_2d_image(1, "y"));
                    close_all_timers();
                }
                case L -> {
                    Render2DNew.create_map(stage, map.create_2d_image(1, "x"));
                    close_all_timers();
                }
                case K -> {
                    Render2DNew.create_map(stage, map.create_2d_image(1, "z"));
                    close_all_timers();
                }
            }
        });

        stage.getScene().setOnKeyReleased(event ->{
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

        AnimationTimer movement = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (move_x)
                    map.object_group.translateXProperty().set(map.object_group.getTranslateX() - movement_speed);
                if (move_inverse_x)
                    map.object_group.translateXProperty().set(map.object_group.getTranslateX() + movement_speed);
                if (move_y)
                    map.object_group.translateYProperty().set(map.object_group.getTranslateY() - movement_speed);
                if (move_inverse_y)
                    map.object_group.translateYProperty().set(map.object_group.getTranslateY() + movement_speed);
                if (move_z)
                    map.object_group.translateZProperty().set(map.object_group.getTranslateZ() + movement_speed);
                if (move_inverse_z)
                    map.object_group.translateZProperty().set(map.object_group.getTranslateZ() - movement_speed);
                if (rotate_x)
                    map.object_group.getTransforms().add(new Rotate(-rotation_speed, new Point3D(1, 0, 0)));
                if (rotate_inverse_x)
                    map.object_group.getTransforms().add(new Rotate(+rotation_speed, new Point3D(1, 0, 0)));
                if (rotate_y)
                    map.object_group.getTransforms().add(new Rotate(+rotation_speed, new Point3D(0, 1, 0)));
                if (rotate_inverse_y)
                    map.object_group.getTransforms().add(new Rotate(-rotation_speed, new Point3D(0, 1, 0)));
                if (rotate_z)
                    map.object_group.getTransforms().add(new Rotate(+rotation_speed, new Point3D(0, 0, 1)));
                if (rotate_inverse_z)
                    map.object_group.getTransforms().add(new Rotate(-rotation_speed, new Point3D(0, 0, 1)));
            }
        };
        movement.start();
        timers.add(movement);
    }

    static public void close_all_timers(){
        for(AnimationTimer timer: timers){
            timer.stop();
        }
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


    public void set_transparency(double value){
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(new Color(.5, .5, .5, 1-value));
        this.mesh.setMaterial(material);
    }
}

class Slice extends Object3D{
    public Slice(){
        all_objects.add(this);
    }
    public Slice(Shape3D mesh){
        all_objects.add(this);
        this.mesh = mesh;
    }
    public Slice(Shape3D mesh, double x, double y, double z){
        all_objects.add(this);
        mesh.translateXProperty().set(x);
        mesh.translateYProperty().set(y);
        mesh.translateZProperty().set(z);
        this.mesh = mesh;
    }

}

class Vertex3D{
    // a vertex3d can be used to represent a 3d point or a 3d vector
    double x, y, z;
    public Vertex3D(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public String toString(){
        return "X:"+x+" Y:"+y+" Z:"+z;
    }

    // arithmetic operations with 3d points
    public Vertex3D add(Vertex3D otherPoint){
        return new Vertex3D(this.x+otherPoint.x, this.y+otherPoint.y, this.z+otherPoint.z);
    }
    public Vertex3D add(double value){
        return new Vertex3D(this.x+value, this.y+value, this.z+value);
    }
    public Vertex3D multiply(Vertex3D otherPoint){
        return new Vertex3D(this.x*otherPoint.x, this.y*otherPoint.y, this.z*otherPoint.z);
    }
    public Vertex3D multiply(double value){
        return new Vertex3D(this.x*value, this.y*value, this.z*value);
    }
}

class Map{
    int[][][] grid3D;
    double unit_cube_length = 100;
    ArrayList<Object3D> all_world_objects = new ArrayList<>();

    public Map(){
        grid3D = new int[7][7][7];
    }
    public Map(int grid_size){
        grid3D = new int[grid_size][grid_size][grid_size];
    }

    Group object_group = new Group();
    Vertex3D rotation = new Vertex3D(0, 0, 0);
    Vertex3D position = new Vertex3D(0, 0, 0);

    public void update_object_group(){
        all_world_objects.clear();
        for (int x = 0; x < grid3D.length; x++){
            for (int y = 0; y < grid3D[x].length; y++){
                for (int z = 0; z < grid3D[x][y].length; z++){
                    int value = grid3D[x][y][z];
                    if (value == 1)
                        all_world_objects.add(new Object3D(new Box(unit_cube_length, unit_cube_length, unit_cube_length), (grid3D.length-x)*unit_cube_length, (grid3D[0].length-y)*unit_cube_length, z*unit_cube_length));

                    if (value == 2) {
                        PhongMaterial material = new PhongMaterial();
                        material.setDiffuseColor(Color.GREEN);
                        Box box = new Box(unit_cube_length, unit_cube_length, unit_cube_length);
                        box.setMaterial(material);
                        all_world_objects.add(new Object3D(box, (grid3D.length - x) * unit_cube_length, (grid3D[0].length - y) * unit_cube_length, z * unit_cube_length));
                    }
                }
            }
        }

        // recreate object_group from world_objects
        object_group.getChildren().clear();
        for (Object3D obj: all_world_objects) {
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

    void add_box_to_grid(int type, int x1, int y1, int z1, int x2, int y2, int z2){
        for (int x = x1; x <= x2; x++){
            for (int y = y1; y <= y2; y++){
                for (int z = z1; z <= z2; z++){
                    grid3D[x][y][z] = type;
                }
            }
        }
    }

    int[][] create_2d_image(int type, String direction){
        int unit_length = grid3D.length;
        int[][] image = new int[unit_length][unit_length];
        for (int i = 0; i < unit_length; i++) {
            for (int j = 0; j < unit_length; j++) {
                for (int k = 0; k < unit_length; k++) {
                    switch (direction){
                        case "x": {
                            int x = k, y = j, z = i;
                            int value = grid3D[x][y][z];
                            if (value == type) {
                                image[unit_length-1-y][z] = type;
                            }
                            break;
                        }
                        case "y": {
                            int x = i, y = k, z = j;
                            int value = grid3D[x][y][z];
                            if (value == type) {
                                image[unit_length-1-z][unit_length-1-x] = type;
                            }
                            break;
                        }
                        case "z": {
                            int x = i, y = j, z = k;
                            int value = grid3D[x][y][z];
                            if (value == type) {
                                image[unit_length-1-y][unit_length-1-x] = type;
                            }
                            break;
                        }

                    }
                }
            }
        }
        for(int[] a: image) {
            for (int b : a) {
                System.out.print(b + " ");
            }
            System.out.println();
        }
        return image;
    }
}

