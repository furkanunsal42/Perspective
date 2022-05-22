package NewGame;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import java.util.ArrayList;


public class Render2DNew extends Application {

    // define movement speed
    static int speed = 2;
    static int max_speed = 10;

    static int HEIGHT = 600;
    static int WIDTH = 800;

    // main method (launches start function)
    public static void main(String[] args) {launch(args);}

    // start method (runs to initialize javafx graphics window)
    @Override
    public void start(Stage primary_stage) {

        // set standard javafx window elements
        set_stage(primary_stage);
    }

    private static void set_stage(Stage stage){
        // create standard javafx window elements
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        stage.setScene(scene);
        // open window
        stage.show();

    }

    public static void create_map(Stage stage, int[][] map_grid, boolean topdown) {
        set_stage(stage);

        Map2D map = new Map2D(map_grid.length);
        map.grid2D = map_grid;
        map.topdown = topdown;
        map.update_object_group();
        map.set_unit_length_by_height(HEIGHT);

        initialize_movement_system(stage, map);

        Pane root = (Pane)stage.getScene().getRoot();

        root.getChildren().add(map.object_group);

        stage.getScene().setFill(Color.BLACK);
    }

    public static SubScene create_panel_view(int[][] map_grid, int view_length){

        Map2D map = new Map2D(map_grid.length);
        map.grid2D = map_grid;
        //map.topdown = topdown;
        map.update_object_group();
        map.set_unit_length_by_height(view_length);

        Pane root = new Pane();
        root.getChildren().add(map.object_group);
        SubScene sub_scene = new SubScene(root, view_length, view_length);
        sub_scene.setOpacity(0.6);
        return sub_scene;
    }

    static void initialize_movement_system(Stage stage, Map2D map){
        stage.getScene().setOnKeyPressed(event -> {
            switch (event.getCode()){
                case TAB -> {
                    Render3DNew.transition_sound.stop();
                    Render3DNew.transition_sound.play();
                    Render3DNew.return_to_current_map(stage, map.grid2D);
                }
                case W -> {
                    map.player_move(new Vertex2D(0, -1));
                }
                case S -> {
                    map.player_move(new Vertex2D(0, 1));
                }
                case A -> {
                    map.player_move(new Vertex2D(-1, 0));
                }
                case D -> {
                    map.player_move(new Vertex2D(1, 0));
                }
            }
        });
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

class Map2D{
    int[][] grid2D;
    ArrayList<Object2D> all_world_objects = new ArrayList<>();
    Group object_group = new Group();
    double unit_square_length = 100;

    // weather the direction is in topdown direction(y) or side directions (x, z)
    // topdown and side directions have different display and player movement mechanics
    boolean topdown = true;

    ArrayList<Vertex2D> player_positions = new ArrayList<>();

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
        player_positions.clear();

        for (int x = 0; x < grid2D.length; x++){
            for (int y = 0; y < grid2D[x].length; y++){
                int value = grid2D[y][x];
                if (value == 1)
                    all_world_objects.add(new Object2D(new Rectangle(unit_square_length, unit_square_length, Color.WHITE), (x)*unit_square_length, (y)*unit_square_length));
                if (value == 2) {
                    if (topdown)
                        all_world_objects.add(new Object2D(new Rectangle(unit_square_length, unit_square_length, Color.WHITE), (x)*unit_square_length, (y)*unit_square_length));
                    player_positions.add(new Vertex2D(x, y));
                }
                if (value == 4)
                    all_world_objects.add(new Object2D(new Rectangle(unit_square_length, unit_square_length, Color.GRAY), (x)*unit_square_length, (y)*unit_square_length));
            }
        }

        // draw players
        for(Vertex2D player_loc: player_positions){
            int p_x = (int)player_loc.x, p_y = (int)player_loc.y;
            Rectangle rect = new Rectangle(unit_square_length, unit_square_length, Color.DARKRED);
            rect.setOpacity(0.7);
            all_world_objects.add(new Object2D(rect, (p_x)*unit_square_length, (p_y)*unit_square_length));
        }

        object_group.getChildren().clear();
        for (Object2D obj: all_world_objects) {
            object_group.getChildren().add(obj.mesh);
        }
    }

    public void player_move(Vertex2D direction){
        if (!topdown && direction.y != 0) {
            return;
        }
        // warning: this code is a spaghetti, x and y should be inverted.
        ArrayList<Vertex2D> new_locations = new ArrayList<>();
        for(int x = 0; x < grid2D.length; x++) {
            for (int y = 0; y < grid2D.length; y++) {
                    if (grid2D[x][y] == 2){
                        if (direction.x + y < grid2D.length && direction.y + x < grid2D.length
                                && direction.x + y >= 0 && direction.y + x >= 0){

                            // inverted purposely      y+x                         x+y
                            int new_x = (int)direction.y+x, new_y = (int)direction.x+y;
                            if (topdown) {
                                if (grid2D[new_x][new_y] == 1 || grid2D[new_x][new_y] == 4) {
                                    grid2D[x][y] = grid2D[new_x][new_y]; // 1
                                    new_locations.add(new Vertex2D(new_x, new_y));
                                }
                            }
                            else{
                                if (grid2D[new_x][new_y] == 0) {
                                    grid2D[x][y] = 0;
                                    int below_y = new_x+1;
                                    if (below_y >= 0 && below_y < grid2D.length) {
                                        if (grid2D[below_y][new_y] != 0)
                                            new_locations.add(new Vertex2D(new_x, new_y));
                                        else
                                            new_locations.add((new Vertex2D(x, y)));

                                    }
                                }
                            }
                        }
                    }
                }
            }
        for(Vertex2D location: new_locations){
            grid2D[(int)location.x][(int)location.y] = 2;
        }
        update_object_group();
        Render3DNew.movement_sound.stop();
        Render3DNew.movement_sound.play();
    }
}


