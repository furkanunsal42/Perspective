package NewGame;

import com.sun.scenario.animation.shared.ClipEnvelope;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;


public class Render3DNew extends Application{
    static boolean move_x=false, move_inverse_x=false, move_y=false, move_inverse_y=false, move_z=false, move_inverse_z=false;
    static boolean rotate_x=false, rotate_inverse_x=false, rotate_y=false, rotate_inverse_y=false, rotate_z=false, rotate_inverse_z=false;
    static double movement_speed = 2;
    static double rotation_speed = .5;
    final static String[][] direction_choosing_matrix = new String[][]{
            new String[] {"-z", "-x", "z", "x"},
            new String[] {"y", "y", "y", "y"},
            new String[] {"z", "x", "-z", "-x"},
            new String[] {"-y", "-y", "-y", "-y"}};
    static int direction_matrix_index_x = 1;
    static int direction_matrix_index_y = 2;

    static int screen_height = 600;
    static int screen_width = 800;


    static Map current_map = new Map();

    // to be able to close all timers attached to stage without having each of them in separate variables we have to store them in an array
    static ArrayList<AnimationTimer> timers = new ArrayList<>();

    public static void main(String[] args){
        launch(args);
    }


    public void start(Stage primary_stage){
        System.out.println("use WASD to move the character");
        System.out.println("use Q-E to rotate the world");
        System.out.println("use arrows to choose direction");
        System.out.println("use TAB to alter between 3D from 2D");

        // set standard javafx stage
        set_stage(primary_stage);

        // create the first map
        create_world_1(primary_stage);
    }

    static public void set_stage(Stage stage){
        // standard javafx windows elements
        Image logo=new Image("file:logo.jpg");
        stage.getIcons().add(logo);
        stage.setTitle("Perspective");
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

        map.add_box_to_grid(1, 0, 2, 0, 6, 2, 3);
        map.add_box_to_grid(1, 0, 2, 3, 6, 4, 3);
        map.add_box_to_grid(1, 0, 4, 3, 6, 4, 6);

        // type 2 is player
        map.grid3D[5][3][1] = 2;
        // type 0 blank
        // map.grid3D[4][4][6] = 0;
        //type 3 is target
        map.grid3D[1][5][5] = 3;

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

    static void return_to_current_map(Stage stage, int[][] image){
        set_stage(stage);
        Map map = current_map;
        map.reposition_player_from_image(image);
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
        /*
        direction choosing matrix
        -z  -x   z   x  up
         y   y   y   y  ^
         z   x  -z  -x  |
        -y  -y  -y  -y  down
        left --> right
        */
        stage.getScene().setOnKeyPressed(event ->{
            switch (event.getCode()) {
                case W -> map.move_player(new Vertex3D(0, 0, 1));
                case S -> map.move_player(new Vertex3D(0, 0, -1));
                case A -> map.move_player(new Vertex3D(1, 0, 0));
                case D -> map.move_player(new Vertex3D(-1, 0, 0));

                case E -> rotate_y = true;
                case Q -> rotate_inverse_y = true;

                case UP -> {
                    direction_matrix_index_y -= 1;
                    direction_matrix_index_y = Math.floorMod(direction_matrix_index_y, 4);
                    map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                    map.panel.transform_according_to_direction(map.direction);
                }
                case DOWN -> {
                    direction_matrix_index_y += 1;
                    direction_matrix_index_y = Math.floorMod(direction_matrix_index_y, 4);
                    map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                    map.panel.transform_according_to_direction(map.direction);
                }
                case LEFT -> {
                    direction_matrix_index_x -= 1;
                    direction_matrix_index_x = Math.floorMod(direction_matrix_index_x, 4);
                    map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                    map.panel.transform_according_to_direction(map.direction);
                }
                case RIGHT -> {
                    direction_matrix_index_x += 1;
                    direction_matrix_index_x = Math.floorMod(direction_matrix_index_x, 4);
                    map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                    map.panel.transform_according_to_direction(map.direction);
                }
                case TAB-> {
                    boolean topdown = map.direction.equals("y") || map.direction.equals("-y");
                    Render2DNew.create_map(stage, map.create_2d_image(1, map.direction), topdown);
                    close_all_timers();
                }
            }
        });

        stage.getScene().setOnKeyReleased(event ->{
            switch (event.getCode()) {
                case E -> rotate_y = false;
                case Q -> rotate_inverse_y = false;
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
        move_x=false; move_inverse_x=false; move_y=false; move_inverse_y=false; move_z=false; move_inverse_z=false;
        rotate_x=false; rotate_inverse_x=false; rotate_y=false; rotate_inverse_y=false; rotate_z=false; rotate_inverse_z=false;
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

class Slice{
    SubScene sub_scene;
    public Slice(SubScene sub_scene){
        this.sub_scene = sub_scene;
        this.sub_scene.setOpacity(0.3);
        transform_according_to_direction("x");
        sub_scene.setFill(Color.WHITE);
    }
    public void transform_according_to_direction(String direction){
        int offset = 100;
        double length  = sub_scene.getBoundsInLocal().getWidth();
        this.sub_scene.getTransforms().clear();
        switch (direction){
            case "x" -> {
                this.sub_scene.translateXProperty().set(length + 50 + offset);
                this.sub_scene.translateYProperty().set(50);
                this.sub_scene.translateZProperty().set(length-50);
                this.sub_scene.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
            }
            case "y" -> {
                this.sub_scene.translateXProperty().set(50);
                this.sub_scene.translateYProperty().set(50 - offset);
                this.sub_scene.translateZProperty().set(-50);
                this.sub_scene.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
            }
            case "z" -> {
                this.sub_scene.translateXProperty().set(50);
                this.sub_scene.translateYProperty().set(50);
                this.sub_scene.translateZProperty().set(-50 - offset);
                this.sub_scene.getTransforms().add(new Rotate(0, new Point3D(0, 1, 0)));
            }
            case "-x" -> {
                this.sub_scene.translateXProperty().set(50 - offset);
                this.sub_scene.translateYProperty().set(50);
                this.sub_scene.translateZProperty().set(-50);
                this.sub_scene.getTransforms().add(new Rotate(-90, new Point3D(0, 1, 0)));
            }
            case "-y" -> {
                this.sub_scene.translateXProperty().set(50);
                this.sub_scene.translateYProperty().set(length + 50 + offset);
                this.sub_scene.translateZProperty().set(length - 50);
                this.sub_scene.getTransforms().add(new Rotate(-90, new Point3D(1, 0, 0)));
            }
            case "-z" -> {
                this.sub_scene.translateXProperty().set(length + 50);
                this.sub_scene.translateYProperty().set(50);
                this.sub_scene.translateZProperty().set(length - 50 + offset);
                this.sub_scene.getTransforms().add(new Rotate(180, new Point3D(0, 1, 0)));
            }
        }
    }

    public void set_sub_scene(SubScene sub_scene){
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
    Slice panel;
    //Slice panel = new Slice(new Box(Render3DNew.screen_width, Render3DNew.screen_height, 0.3));
    //Slice panel = new Slice(new Box(10, 10, 10));
    String direction = "x";

    public Map(){
        grid3D = new int[7][7][7];
        panel = new Slice(new SubScene(new Pane(), 7 * unit_cube_length, 7* unit_cube_length, true, SceneAntialiasing.BALANCED));
    }
    public Map(int grid_size){
        grid3D = new int[grid_size][grid_size][grid_size];
        panel = new Slice(new SubScene(new Pane(), grid_size * unit_cube_length, grid_size * unit_cube_length, true, SceneAntialiasing.BALANCED));
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
                    PhongMaterial material = new PhongMaterial();
                    Box box = new Box(unit_cube_length, unit_cube_length, unit_cube_length);
                    if (value == 1) {
                        material.setDiffuseColor(Color.WHITE);
                        box.setMaterial(material);
                        all_world_objects.add(new Object3D(box, (grid3D.length - x) * unit_cube_length, (grid3D[0].length - y) * unit_cube_length, z * unit_cube_length));

                    }
                    else if (value == 2) {
                        material.setDiffuseColor(Color.DARKRED);
                        box.setMaterial(material);
                        all_world_objects.add(new Object3D(box, (grid3D.length - x) * unit_cube_length, (grid3D[0].length - y) * unit_cube_length, z * unit_cube_length));
                    }
                    else if (value == 3) {
                        material.setDiffuseColor(Color.GOLD);
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
        object_group.getChildren().add(panel.sub_scene);

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
        this.direction = direction;
        int unit_length = grid3D.length;
        int[][] image = new int[unit_length][unit_length];
        for (int i = 0; i < unit_length; i++) {
            for (int j = 0; j < unit_length; j++) {
                for (int k = 0; k < unit_length; k++) {
                    if (direction.equals("x")){
                        int x = unit_length-1-k, y = j, z = i;
                        int value = grid3D[x][y][z];
                        if (value == type) {
                            image[unit_length-1-y][z] = type;
                            break;
                        }
                        else if (value == 2) {
                            image[unit_length-1-y][z] = 2;
                            break;
                        }
                    }
                    else if (direction.equals("y")){
                        int x = i, y = unit_length-1-k, z = j;
                        int value = grid3D[x][y][z];
                        if (value == type) {
                            image[unit_length-1-z][unit_length-1-x] = type;
                            break;
                        }
                        else if (value == 2) {
                            image[unit_length-1-z][unit_length-1-x] = 2;
                            break;
                        }
                    }
                    else if (direction.equals("z")){
                        int x = i, y = j, z = k;
                        int value = grid3D[x][y][z];
                        if (value == type) {
                            image[unit_length-1-y][unit_length-1-x] = type;
                            break;
                        }
                        else if (value == 2) {
                            image[unit_length-1-y][unit_length-1-x] = 2;
                            break;
                        }
                    }
                    else if (direction.equals("-x")){
                        int x = k, y = j, z = i;
                        int value = grid3D[x][y][z];
                        if (value == type) {
                            image[unit_length-1-y][unit_length-1-z] = type;
                            break;
                        }
                        else if (value == 2) {
                            image[unit_length-1-y][unit_length-1-z] = 2;
                            break;
                        }
                    }
                    else if (direction.equals("-y")){
                        int x = i, y = k, z = j;
                        int value = grid3D[x][y][z];
                        if (value == type) {
                            image[z][unit_length-1-x] = type;
                            break;
                        }
                        else if (value == 2) {
                            image[z][unit_length-1-x] = 2;
                            break;
                        }
                    }
                    else if (direction.equals("-z")){
                        int x = i, y = j, z = unit_length-1-k;
                        int value = grid3D[x][y][z];
                        if (value == type) {
                            image[unit_length-1-y][x] = type;
                            break;
                        }
                        else if (value == 2) {
                            image[unit_length-1-y][x] = 2;
                            break;
                        }
                    }
                }
            }
        }

        return image;
    }

    public void move_player(Vertex3D direction){
        ArrayList<Vertex3D> new_locations = new ArrayList<>();
        for(int x = 0; x < grid3D.length; x++) {
            for (int y = 0; y < grid3D.length; y++) {
                for (int z = 0; z < grid3D.length; z++) {
                    if (grid3D[x][y][z] == 2){
                        if (direction.x + x < grid3D.length && direction.y + y < grid3D.length && direction.z + z < grid3D.length
                                && direction.x + x >= 0 && direction.y + y >= 0  && direction.z + z >= 0){
                            int new_x = (int)direction.x+x,new_y = (int)direction.y+y, new_z = (int)direction.z+z;
                            if(grid3D[new_x][new_y][new_z] == 0) {
                                grid3D[x][y][z] = 0;
                                new_locations.add(new Vertex3D(new_x, new_y, new_z));
                            }
                        }
                    }
                }
            }
        }
        for(Vertex3D location: new_locations){
            grid3D[(int)location.x][(int)location.y][(int)location.z] = 2;
        }
        update_object_group();
    }

    void reposition_player_from_image(int[][] image){

        ArrayList<Vertex2D> player_positions_2D = new ArrayList<>();

        for (int x = 0; x < image.length; x++){
            for (int y = 0; y < image.length; y++){
                if (image[x][y] == 2)
                    player_positions_2D.add(new Vertex2D(y, x));
            }
        }

        ArrayList<Vertex3D> player_positions_3D = new ArrayList<>();
        int unit_length = grid3D.length;

        for (int x = 0; x < unit_length; x++) {
            for (int y = 0; y < unit_length; y++) {
                for (int z = 0; z < unit_length; z++) {
                    if (grid3D[x][y][z] == 2)
                        grid3D[x][y][z] = 0;
                }
            }
        }

        for(Vertex2D player_position: player_positions_2D) {
            for (int i = 0; i < unit_length; i++) {
                if (direction.equals("x")) {
                    int x = i, y = unit_length-1-(int)player_position.y, z = (int)player_position.x;
                    int value = grid3D[x][y][z];
                    if (y-1 > 0 && y-1 < unit_length) {
                        int below_value = grid3D[x][y-1][z];
                        if (value == 0 && below_value == 1) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("y")) {
                    int x = unit_length-1-(int)player_position.x, y = unit_length - 1 - i, z = unit_length-1-(int)player_position.y;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == 1) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("z")) {
                    int x = unit_length-1-(int)player_position.x, y = unit_length-1-(int)player_position.y, z = i;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == 1) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("-x")) {
                    int x = unit_length-1-i, y = unit_length-1-(int)player_position.y, z = unit_length-1-(int)player_position.x;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == 1) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("-y")) {
                    int x = (int)player_position.x, y = i, z = (int)player_position.y;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == 1) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("-z")) {
                    int x = (int)player_position.x, y = (int)player_position.y, z = unit_length - 1 - i;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == 1) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                }
            }
        }
        for (Vertex3D p_position: player_positions_3D){
            grid3D[(int)p_position.x][(int)p_position.y][(int)p_position.z] = 2;
        }
        update_object_group();
    }
}

