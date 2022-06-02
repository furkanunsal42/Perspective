import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import javafx.scene.image.Image;


public class Render3D extends Application{
    static Stage stage;
    static int screen_height = 600;
    static int screen_width = 800;
    static Map current_map = new Map();
    static int level_number = 1;
    static ArrayList<AnimationTimer> timers = new ArrayList<>();
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
    static boolean level_is_finished = false;
    static int image_type = 1;
    static MediaPlayer movement_sound = new MediaPlayer(new Media(new File("ES_Apple Keyboard 13 - SFX Producer.mp3").toURI().toString()));
    static MediaPlayer transition_sound = new MediaPlayer(new Media(new File("ES_Fire Torch Move - SFX Producer.mp3").toURI().toString()));
    static MediaPlayer music = new MediaPlayer(new Media(new File("Nocturne.mp3").toURI().toString()));
    static boolean is_music_playing = true;

    public static void main(String[] args){
        launch(args);
    }

    public void start(Stage primary_stage){
        System.out.println("use WASD to move the character");
        System.out.println("use Q-E to rotate the world");
        System.out.println("use arrows to choose direction");
        System.out.println("use TAB to alter between 3D from 2D");
        stage = primary_stage;

        // create the first map
        create_menu();
    }

    static public void set_stage(){
        // standard javafx windows elements
        Image logo = new Image("file:logo.jpg");
        stage.setResizable(false);
        stage.getIcons().add(logo);
        stage.setTitle("Perspective");
        Pane root = new Pane();
        Scene scene = new Scene(root, screen_width, screen_height, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        stage.setScene(scene);
        stage.show();
    }

    static public void create_menu(){
        set_stage();
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        Canvas canvas = new Canvas(screen_width, screen_height);
        GraphicsContext g =  canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        AnimationTimer text_animaton = new AnimationTimer() {
            @Override
            public void handle(long l) {
                Vertex2D button_size = new Vertex2D(100, 60);
                g.setFill(Color.WHITE);
                g.drawImage(new Image("file:Background.png"), 0, 0);

                g.setFill(Color.rgb(229,83,85));
                Font font2 = Font.loadFont("file:Font2.ttf", 90);
                g.setFont(font2);
                g.fillText("PERSPECTIVE", screen_width/2.0 - 230, screen_height/2.0 - 100);

                font2 = Font.loadFont("file:Font2.ttf", 20);
                g.setFont(font2);
                g.fillText("press m to mute music",550,screen_height/2.0 + 200);
                g.fillText("use wasd to move",550,screen_height/2.0 + 230);
                g.fillText("use tab to change perspective",550,screen_height/2.0 + 260);
                g.fillText("use arrows to rotate",550,screen_height/2.0 + 290);
                g.setFill(Color.MINTCREAM);
                Font font = Font.loadFont("file:Font.ttf", 30);
                g.setFont(font);
                g.fillText("press enter to play!", screen_width/2.0 - 170, screen_height/2.0 + 50 + 20*Math.cos(l/30000000000.0 * 180 / Math.PI));

            }
        };
        text_animaton.start();
        timers.add(text_animaton);

        music.setAutoPlay(true);
        music.setVolume(60);
        music.play();
        is_music_playing = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.M){
                    if (is_music_playing) {
                        music.pause();
                        is_music_playing = false;
                    }
                    else {
                        music.play();
                        is_music_playing = true;
                    }
                }
                if (keyEvent.getCode() == KeyCode.ENTER)
                    create_world_1();
            }
        });

    }



    static public void create_world_by_number(int number){
        System.out.println(number);
        switch (number){
            case 1 -> create_world_1();
            case 2 -> create_world_2();
            case 3 -> create_world_3();
            case 4 -> create_world_4();
            case 5 -> create_world_5();
            case 6 -> create_world_6();
            case 7 -> create_world_7();
        }
    }

    static public void create_world_1(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();
        // map
        Map map = new Map();
        current_map = map;


        // disable panel
        map.panel_enable = false;

        map.add_box_to_grid(1, 0, 2, 0, 6, 2, 6);

        // map.add_box_to_grid(3, 0, 0, 0, 6, 6, 6);

        // type 2 is player
        map.grid3D[5][3][1] = 2;
        // type 0 blank
        // map.grid3D[4][4][6] = 0;
        //type 3 is target
        map.grid3D[1][3][5] = 3;

        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map, "block_panel_display");

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        // panel initial position
        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static public void create_world_2(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();

        // map
        Map map = new Map();
        current_map = map;
        map.direction = "y";

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
        initialize_movement_system(stage, map, "block_panel_movement");

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        // panel initial position
        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static public void create_world_3(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();

        // map
        Map map = new Map();
        current_map = map;
        map.direction = "y";

        map.add_box_to_grid(1, 0, 2, 0, 2, 2, 2);
        map.add_box_to_grid(1, 3, 2, 4, 6, 2, 6);

        // type 2 is player
        map.grid3D[1][3][1] = 2;

        //type 3 is target
        map.grid3D[5][3][5] = 3;

        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        // panel initial position
        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static public void create_world_4(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();

        // map
        Map map = new Map();
        current_map = map;
        map.direction = "y";

        map.add_box_to_grid(1, 4, 4, 0, 6, 4, 1);
        map.add_box_to_grid(1, 4, 2, 0, 6, 2, 2);
        map.add_box_to_grid(1, 3, 2, 4, 1, 2, 5);
        map.add_box_to_grid(1, 4, 5, 4, 6, 5, 5);


        // type 2 is player
        map.grid3D[6][5][0] = 2;

        //type 3 is target
        map.grid3D[6][6][5] = 3;

        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        // panel initial position
        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static public void create_world_5(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();

        // map
        Map map = new Map();
        current_map = map;
        map.direction = "y";

        map.add_box_to_grid(4, 6, 4, 0, 4, 4, 2);
        map.add_box_to_grid(1, 3, 4, 0, 2, 4, 1);
        map.add_box_to_grid(1, 3, 4, 5, 2, 4, 5);
        map.add_box_to_grid(1, 6, 3, 3, 4, 3, 5);

        // type 2 is player
        map.grid3D[5][5][1] = 2;

        //type 3 is target
        map.grid3D[5][4][5] = 3;

        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        // panel initial position
        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static public void create_world_6(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();

        // map
        Map map = new Map();
        current_map = map;
        map.direction = "y";

        map.grid3D[5][4][0] = 4;
        map.grid3D[6][4][1] = 4;
        map.grid3D[5][4][2] = 4;
        map.grid3D[6][4][3] = 4;
        map.grid3D[5][4][4] = 4;
        map.grid3D[6][4][5] = 4;
        map.grid3D[5][4][6] = 4;

        map.grid3D[5][3][1] = 1;
        map.grid3D[6][3][2] = 1;
        map.grid3D[5][3][3] = 1;
        map.grid3D[6][3][4] = 1;
        map.grid3D[5][3][5] = 1;
        map.grid3D[6][3][6] = 1;

        map.grid3D[4][4][6] = 1;
        map.grid3D[3][4][6] = 1;

        map.grid3D[3][3][5] = 1;

        map.add_box_to_grid(1, 4, 3, 0, 2, 3, 0);
        map.grid3D[5][5][0] = 2;
        map.grid3D[4][4][0] = 3;

        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static public void create_world_7(){
        // stop all timers from previous level
        set_stage();
        close_all_timers();

        // map
        Map map = new Map();
        current_map = map;
        map.direction = "y";

        map.add_box_to_grid(4, 6, 4, 0, 5, 4, 1);
        map.grid3D[6][3][2] = 4;
        map.grid3D[6][2][3] = 4;
        map.grid3D[6][2][4] = 4;

        map.grid3D[3][2][4] = 4;
        map.grid3D[3][3][3] = 4;

        map.add_box_to_grid(1, 4, 3, 1, 3, 3, 2);
        map.grid3D[4][3][2] = 0;

        map.add_box_to_grid(1, 6, 2, 0, 5, 2, 1);

        map.grid3D[6][5][0] = 2;
        map.grid3D[6][3][0] = 3;
        // finish creating map
        map.update_object_group();

        // start movement system
        initialize_movement_system(stage, map);

        // access to scene and root
        Scene scene = stage.getScene();
        Pane root = (Pane)scene.getRoot();

        // display objects in the scene
        root.getChildren().add(map.object_group);

        int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        // setup camera
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.translateXProperty().set(500);
        camera.translateYProperty().set(-500);
        camera.translateZProperty().set(-500);
        camera.getTransforms().add(new Rotate(-45, new Point3D(1, 1, 0.13)));
        scene.setCamera(camera);
    }

    static void return_to_current_map(Stage stage, int[][] image){
        set_stage();
        Map map = current_map;
        map.reposition_player_from_image(image, image_type);
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

        // reset panel
        int[][] grid_image = map.create_2d_image(image_type, map.direction);
        map.panel.sub_scene = Render2D.create_panel_view(grid_image, 700);
        map.update_object_group();
        map.panel.transform_according_to_direction(map.direction);

        if(level_is_finished)
            create_world_by_number(++level_number);
    }

    static public void initialize_movement_system(Stage stage, Map map, String... arguments){
        boolean panel_movement = true, panel_display = true;
        for(String argument: arguments){
            if(argument.equals("block_panel_movement"))
                panel_movement = false;
            if(argument.equals("block_panel_display"))
                panel_movement = false;
        }

        /*
        direction choosing matrix
        -z  -x   z   x
         y   y   y   y
         z   x  -z  -x
        -y  -y  -y  -y
        */

        // effectively final variables are necessary to use them locally in lambda expression
        boolean finalPanel_movement = panel_movement;

        stage.getScene().setOnKeyPressed(event ->{
            switch (event.getCode()) {
                case W -> map.move_player(new Vertex3D(0, 0, 1));
                case S -> map.move_player(new Vertex3D(0, 0, -1));
                case A -> map.move_player(new Vertex3D(1, 0, 0));
                case D -> map.move_player(new Vertex3D(-1, 0, 0));

                case E -> rotate_y = true;
                case Q -> rotate_inverse_y = true;

                case UP -> {
                    if (!finalPanel_movement)
                        break;
                    direction_matrix_index_y -= 1;
                    direction_matrix_index_y = Math.floorMod(direction_matrix_index_y, 4);
                    map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                }
                case DOWN -> {
                    if (!finalPanel_movement)
                        break;
                    direction_matrix_index_y += 1;
                    direction_matrix_index_y = Math.floorMod(direction_matrix_index_y, 4);
                    map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                }
                case LEFT -> {
                    if (!finalPanel_movement)
                        break;
                    if(direction_matrix_index_y != 1 && direction_matrix_index_y != 3) {
                        direction_matrix_index_x -= 1;
                        direction_matrix_index_x = Math.floorMod(direction_matrix_index_x, 4);
                        map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                    }
                }
                case RIGHT -> {
                    if (!finalPanel_movement)
                        break;
                    if(direction_matrix_index_y != 1 && direction_matrix_index_y != 3) {
                        direction_matrix_index_x += 1;
                        direction_matrix_index_x = Math.floorMod(direction_matrix_index_x, 4);
                        map.direction = direction_choosing_matrix[direction_matrix_index_y][direction_matrix_index_x];
                    }
                }
                case TAB-> {
                    if(!map.panel_enable)
                        break;
                    boolean topdown = map.direction.equals("y") || map.direction.equals("-y");
                    Render3D.image_type = map.get_platform_below_player();
                    Render2D.create_map(stage, map.create_2d_image(Render3D.image_type, map.direction), topdown);
                    close_all_timers();
                    Render3D.transition_sound.stop();
                    Render3D.transition_sound.play();
                }
                case M -> {
                    if (is_music_playing) {
                        music.pause();
                        is_music_playing = false;
                    }
                    else {
                        music.play();
                        is_music_playing = true;
                    }
                }
            }
            int[][] image = map.create_2d_image(map.get_platform_below_player(), map.direction);
            map.panel.sub_scene = Render2D.create_panel_view(image, 700);
            map.update_object_group();
            map.panel.transform_according_to_direction(map.direction);
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
                if (rotate_x)
                    map.object_group.getTransforms().add(new Rotate(-rotation_speed, new Point3D(1, 0, 0)));
                if (rotate_inverse_x)
                    map.object_group.getTransforms().add(new Rotate(+rotation_speed, new Point3D(1, 0, 0)));
                if (rotate_y) {
                    Rotate rotation = new Rotate(+rotation_speed, new Point3D(0, 1, 0));
                    rotation.setPivotX(map.object_group.getBoundsInLocal().getCenterX());
                    rotation.setPivotY(map.object_group.getBoundsInLocal().getCenterY());
                    rotation.setPivotZ(map.object_group.getBoundsInLocal().getCenterZ());
                    map.object_group.getTransforms().add(rotation);
                }
                if (rotate_inverse_y) {
                    Rotate rotation = new Rotate(-rotation_speed, new Point3D(0, 1, 0));
                    rotation.setPivotX(map.object_group.getBoundsInLocal().getCenterX());
                    rotation.setPivotY(map.object_group.getBoundsInLocal().getCenterY());
                    rotation.setPivotZ(map.object_group.getBoundsInLocal().getCenterZ());
                    map.object_group.getTransforms().add(rotation);
                }
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

    public void add_rotation(double x, double y, double z){
        mesh.getTransforms().add(new Rotate(x, new Point3D(1, 0, 0)));
        mesh.getTransforms().add(new Rotate(y, new Point3D(0, 1, 0)));
        mesh.getTransforms().add(new Rotate(z, new Point3D(0, 0, 1)));
    }

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
                this.sub_scene.translateZProperty().set(-50);
                this.sub_scene.getTransforms().add(new Rotate(-90, new Point3D(0, 1, 0)));
            }
            case "y" -> {
                this.sub_scene.translateXProperty().set(50);
                this.sub_scene.translateYProperty().set(50 - offset);
                this.sub_scene.translateZProperty().set(length-50);
                this.sub_scene.getTransforms().add(new Rotate(-90, new Point3D(1, 0, 0)));
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
                this.sub_scene.translateZProperty().set(length-50);
                this.sub_scene.getTransforms().add(new Rotate(90, new Point3D(0, 1, 0)));
            }
            case "-y" -> {
                this.sub_scene.translateXProperty().set(50);
                this.sub_scene.translateYProperty().set(length + 50 + offset);
                this.sub_scene.translateZProperty().set(-50);
                this.sub_scene.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
            }
            case "-z" -> {
                this.sub_scene.translateXProperty().set(length + 50);
                this.sub_scene.translateYProperty().set(50);
                this.sub_scene.translateZProperty().set(length - 50 + offset);
                this.sub_scene.getTransforms().add(new Rotate(180, new Point3D(0, 1, 0)));
            }
        }
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

    ArrayList<Object3D> all_world_objects = new ArrayList<>();
    int[][][] grid3D;
    Slice panel;
    String direction = "x";
    boolean panel_enable = true;
    Group object_group = new Group();
    Vertex3D rotation = new Vertex3D(0, 0, 0);
    Vertex3D position = new Vertex3D(0, 0, 0);
    double unit_cube_length = 100;

    public Map(){
        grid3D = new int[7][7][7];
        panel = new Slice(new SubScene(new Pane(), 7 * unit_cube_length, 7* unit_cube_length, true, SceneAntialiasing.BALANCED));
    }
    public Map(int grid_size){
        grid3D = new int[grid_size][grid_size][grid_size];
        panel = new Slice(new SubScene(new Pane(), grid_size * unit_cube_length, grid_size * unit_cube_length, true, SceneAntialiasing.BALANCED));
    }

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
                    else if (value == 4) {
                        material.setDiffuseColor(Color.GRAY);
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
        if(panel_enable)
            object_group.getChildren().add(panel.sub_scene);

        // apply rotation to group
        object_group.getTransforms().add(new Rotate(rotation.x, new Point3D(1, 0, 0)));
        object_group.getTransforms().add(new Rotate(rotation.y, new Point3D(0, 1, 0)));
        object_group.getTransforms().add(new Rotate(rotation.z, new Point3D(0, 0, 1)));

        // apply position to group
        object_group.translateXProperty().set(position.x);
        object_group.translateXProperty().set(position.y);
        object_group.translateXProperty().set(position.z);

        // check if level ended
        Render3D.level_is_finished = false;
        for(int x = 0; x < grid3D.length; x++){
            for(int y = 0; y < grid3D.length; y++){
                for(int z = 0; z < grid3D.length; z++){
                    if(grid3D[x][y][z] == 2) {
                        if (x+1 < grid3D.length && grid3D[x+1][y][z] == 3){
                            Render3D.level_is_finished = true;
                        }
                        else if (x-1 >= 0 && grid3D[x-1][y][z] == 3){
                            Render3D.level_is_finished = true;
                        }
                        else if (y+1 < grid3D.length && grid3D[x][y+1][z] == 3){
                            Render3D.level_is_finished = true;
                        }
                        else if (y-1 >= 0 && grid3D[x][y-1][z] == 3){
                            Render3D.level_is_finished = true;
                        }
                        else if (z+1 < grid3D.length && grid3D[x][y][z+1] == 3){
                            Render3D.level_is_finished = true;
                        }
                        else if (z-1 >= 0 && grid3D[x][y][z-1] == 3){
                            Render3D.level_is_finished = true;
                        }
                    }
                }
            }
        }
    }

    void add_box_to_grid(int type, int x1, int y1, int z1, int x2, int y2, int z2){
        if (x1 > x2) {
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }
        if (y1 > y2) {
            int temp = y2;
            y2 = y1;
            y1 = temp;
        }
        if (z1 > z2) {
            int temp = z2;
            z2 = z1;
            z1 = temp;
        }

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
                            if(grid3D[new_x][new_y][new_z] == 0 && (grid3D[new_x][new_y-1][new_z] == 1 || grid3D[new_x][new_y-1][new_z] == 4)) {
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
        Render3D.movement_sound.stop();
        Render3D.movement_sound.play();

        // if the level is finished create the next level
        if(Render3D.level_is_finished){
            Render3D.level_number++;
            Render3D.create_world_by_number(Render3D.level_number);
        }
    }

    void reposition_player_from_image(int[][] image, int type){

        ArrayList<Vertex2D> player_positions_2D = new ArrayList<>();
        for (int x = 0; x < image.length; x++){
            for (int y = 0; y < image.length; y++){
                if (image[x][y] == 2)
                    player_positions_2D.add(new Vertex2D(y, x));
            }
        }
        ArrayList<Vertex3D> player_old_positions = new ArrayList<>();
        ArrayList<Vertex3D> player_positions_3D = new ArrayList<>();
        int unit_length = grid3D.length;

        for (int x = 0; x < unit_length; x++) {
            for (int y = 0; y < unit_length; y++) {
                for (int z = 0; z < unit_length; z++) {
                    if (grid3D[x][y][z] == 2)
                        player_old_positions.add(new Vertex3D(x, y, z));
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
                        if (value == 0 && below_value == type) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("y")) {
                    int x = unit_length-1-(int)player_position.x, y = unit_length - 1 - i, z = unit_length-1-(int)player_position.y;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == type) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("z")) {
                    int x = unit_length-1-(int)player_position.x, y = unit_length-1-(int)player_position.y, z = i;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == type) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("-x")) {
                    int x = unit_length-1-i, y = unit_length-1-(int)player_position.y, z = unit_length-1-(int)player_position.x;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == type) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("-y")) {
                    int x = (int)player_position.x, y = i, z = (int)player_position.y;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == type) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                } else if (direction.equals("-z")) {
                    int x = (int)player_position.x, y = unit_length - 1 -(int)player_position.y, z = unit_length - 1 - i;
                    int value = grid3D[x][y][z];
                    if (y - 1 > 0) {
                        int below_value = grid3D[x][y - 1][z];
                        if (value == 0 && below_value == type) {
                            player_positions_3D.add(new Vertex3D(x, y, z));
                            break;
                        }
                    }
                }
            }
        }
        // create the player in new location
        for (Vertex3D p_position: player_positions_3D){
            grid3D[(int)p_position.x][(int)p_position.y][(int)p_position.z] = 2;
        }
        // if and only if player's position has altered clear the previous position of the player.
        if(player_positions_3D.size() > 0) {
            for( Vertex3D player: player_old_positions){
                grid3D[(int)player.x][(int)player.y][(int)player.z] = 0;
            }
        }

        update_object_group();
    }

    public int get_platform_below_player(){
        for(int x = 0; x < grid3D.length; x++) {
            for (int y = 0; y < grid3D.length; y++) {
                for (int z = 0; z < grid3D.length; z++) {
                    if (grid3D[x][y][z] == 2){
                        if (y-1 >= 0){
                            return grid3D[x][y-1][z];
                        }
                    }
                }
            }
        }
        return 0;
    }
}

