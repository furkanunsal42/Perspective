import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import java.util.ArrayList;


public class Render2D extends Application {
    // define boolean movement parameters (each direction has it's boolean so that we can have a smoother movement system)
    static boolean move_x = false, move_inverse_x = false, move_jump = false;

    // define movement speed
    static int speed = 2;
    static int max_speed = 10;

    // main method (launches start function)
    public static void main(String[] args){
        launch(args);
    }

    // start method (runs to initialize javafx graphics window)
    @Override
    public void start(Stage primary_stage){

        // create standard javafx window elements
        Pane root = new Pane();
        Canvas canvas = new Canvas(800, 600);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, 800, 600);
        primary_stage.setScene(scene);

        // create objects in map_1
        create_map_1(primary_stage, root);

        // start rendering objects
        initialize_rendering_cycle(canvas.getGraphicsContext2D(), Object.all_objects.toArray(new Object[0]));

        // open window
        primary_stage.show();
    }

    static void create_map_1(Stage primary_stage, Pane root){
        // this function initialize the objects in the first map

        // definition of floor object
        Object platform = new Object(new Vertex[]{
                new Vertex(0, 600),
                new Vertex(0, 500),
                new Vertex(800, 500),
                new Vertex(800, 600)});
        platform.color = Color.DARKGRAY;

        // definition of golden rectangle
        Object rect = new Object(new Vertex[]{
                new Vertex(600, 100),
                new Vertex(600, 200),
                new Vertex(700, 200),
                new Vertex(700, 100)});
        rect.color = Color.GOLD;

        // definition of the player
        Player p = new Player(300, 300, 50);

        // starting the movement system (W A S D control of the player)
        initialize_movement_system(primary_stage, p);

        // starting the physics system (collision control and gravity)
        initialize_physics_system(p);
    }

    static void initialize_movement_system(Stage primary_stage, Player p){
        // detect pressing of W A S D buttons
        primary_stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()){
                case W -> move_jump = true;
                case A -> move_inverse_x = true;
                case D -> move_x = true;
            }
        });

        // detect releasing of W A S D buttons
        primary_stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            switch (event.getCode()){
                case W -> move_jump = false;

                case A -> {
                    p.velocity.x = 0;
                    move_inverse_x = false;
                }
                case D -> {
                    p.velocity.x = 0;
                    move_x = false;
                }
            }
        });

        // updating velocity variable based on key presses
        AnimationTimer movement = new AnimationTimer() {
            // time that previous frame was rendered (in nanoseconds)
            long previous_time = 0;
            @Override
            public void handle(long now) {
                // runs every 1/100 second 10ms(10 * 1_000_000 nanoseconds)
                if (now - previous_time > 10 * 1_000_000) {
                    // to calculate the time passed since last run, we have to remember the previous time
                    previous_time = now;

                    // if move_x is active and velocity is lower than max_speed
                    if(move_x && Math.abs(p.velocity.x) < max_speed)
                        // increase speed
                        p.velocity.x += speed;

                    // if move_inverse_x is active and velocity is lower than max_speed
                    if(move_inverse_x && Math.abs(p.velocity.x) < max_speed)
                        // increase speed
                        p.velocity.x -= speed;

                    // if move_jump is active and able to jump
                    if (move_jump && p.can_jump){
                        // if player is able to jump then give it a high upward velocity
                        p.velocity.y = -20;

                        // make the player unable to jump again in the air
                        p.can_jump = false;
                    }
                }
            }
        };
        movement.start();
    }

    static void initialize_rendering_cycle(GraphicsContext g, Object[] objects){
        // this function renders every object into the scene
        AnimationTimer object_renderer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // clear the canvas before rendering anything otherwise previous images from last frame still be present in the scene
                g.clearRect(0, 0, 800, 600);

                // for every object created
                for(Object obj: objects){
                    // render that object
                    obj.display(g, true, true);
                }
            }
        };
        object_renderer.start();

    }

    static void initialize_physics_system(Player p){
        // this function calculates the collision and gravity for player
        AnimationTimer physics_system = new AnimationTimer() {
            long previous_time = 0;
            @Override
            public void handle(long now) {
                for (Object obj: Object.all_objects){
                    // for every object that is not player
                    if (!(obj instanceof Player)) {
                        // runs every 1/100 second (10 ms)
                        if (now - previous_time > 1_0 * 1_000_000) {
                            previous_time = now;

                            // gravity

                            // increase downward velocity (because of gravity)
                            p.velocity.y += +1;
                            // update location based on velocity
                            p.apply_physics_movements();

                        }
                        // collusion
                        p.escape_collision(obj);
                    }
                }
            }
        };
        physics_system.start();
    }

}

class Vertex{
    // keep in mind that
    // a vertex can be used for a 2d point or a 2d vector

    // it only consists of x and y location property
    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
    }
    double x, y;
}

class Object{
    // Object is the general class for every wall and player

    // every time a object is created that object is stored in all_object list
    public static ArrayList<Object> all_objects = new ArrayList<Object>();

    // every vertex of the object is stored at vertex array: vertices
    Vertex[] vertices;

    // color of the object
    Color color = Color.GREEN;

    public Object(){
        // once a object is created add it to all_object list
        all_objects.add(this);
    }

    public Object(Vertex[] input_vertices){
        // set vertex array
        this.vertices = new Vertex[input_vertices.length];
        for(int i = 0; i < input_vertices.length; i++)
            this.vertices[i] = input_vertices[i];

        // once a object is created add it to all_object list
        all_objects.add(this);
    }

    ObservableList<PathElement> get_collision(Object obj){
        // this function returns the collision area between a given object and this object
        return ((Path)Shape.intersect(this.get_path(), obj.get_path())).getElements();
    }

    boolean does_collide(Object obj){
        // this function returns true is object collides with given obj. if not, returns false
        return (get_collision(obj).size() > 0);
    }

    public void escape_collision(Object colliding_object){
        // teleports the object to nearest location that wouldn't cause a collusion
        // this function isn't in its final state since actually player's definion of the class is used rather than this
        // this function is not important for 2d game

        while(this.does_collide(colliding_object)){
            for(Vertex v: vertices){
                //v.x += direction_vector.x;
                //v.y += direction_vector.y;
            }
        }
    }

    Path get_path(){
        // returns the object's shape as a path for drawing or collision purposes

        // create a path
        Path path = new Path();

        // begin the path from the first vertex
        path.getElements().add(new MoveTo(vertices[0].x, vertices[0].y));

        // for every vertex, continue to draw the line
        for(Vertex v: vertices){
            path.getElements().add(new LineTo(v.x, v.y));
        }

        // finish the line
        path.getElements().add(new ClosePath());

        return path;
    }

    Path get_path(boolean fill, boolean stroke){
        // this function returns the path as a stroke or a fill or both but doesn't used since I decided not to add the path's into root
        // but to draw them to canvas

        Path path = this.get_path();
        if (fill) {
            path.setStrokeWidth(0);
            path.setFill(this.color);
        }
        if (stroke)
            path.setStrokeWidth(1);
            path.setStroke(Color.BLACK);
        return path;
    }

    void display(GraphicsContext g){
        // basic rendering function for an object

        g.beginPath();
        for(Vertex v: vertices){
            g.lineTo(v.x, v.y);
        }
        g.closePath();
        g.setFill(Color.SILVER);
        g.stroke();
    }

    void display(GraphicsContext g, boolean fill, boolean stroke){
        // same function with the other display() but it has fill or stroke options

        g.beginPath();
        for(Vertex v: vertices){
            g.lineTo(v.x, v.y);
        }
        g.closePath();
        if (fill) {
            g.setLineWidth(0);
            g.setFill(this.color);
            g.fill();
        }
        if (stroke) {
            g.setLineWidth(1);
            g.setStroke(Color.BLACK);
            g.stroke();
        }
    }
}

class Player extends Object{
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

    // get_path works with circular arcs rather than vertices
    @Override
    Path get_path(){
        Path path = new Path();
        ArcTo arcTo = new ArcTo();
        arcTo.setX(center_x+1); // to simulate a full 360 degree celcius circle.
        arcTo.setY(center_y-radius);
        arcTo.setSweepFlag(false);
        arcTo.setLargeArcFlag(true);
        arcTo.setRadiusX(radius);
        arcTo.setRadiusY(radius);
        arcTo.setXAxisRotation(0);

        path.getElements().addAll(
                        new MoveTo(center_x, center_y-radius),
                        arcTo,
                        new ClosePath()); // close 1 px gap.;
        return path;
    }

    // same as normal escape_collision but rather than moving vertices center point is moved
    @Override
    public void escape_collision(Object colliding_object){
        // have all 8 directions
        Vertex[] all_direction_vectors = new Vertex[]{
                new Vertex(0, -1),  // up
                new Vertex(1, 0),   // right
                new Vertex(0, 1),   // down
                new Vertex(-1, 0),  // left
                new Vertex(1, -1),  // top-right
                new Vertex(1, 1),  // bottom-right
                new Vertex(-1, 1),  // bottom-left
                new Vertex(-1, -1),  // top-left
        };
        // save original x and y values
        double original_x = center_x, original_y = center_y;
        double result_x = center_x, result_y = center_y;

        // each iteration counts the change number, minimum of them is saved here
        int min_change = 0;
        int top_change = 0;
        // for each direction
        for (int i = 0; i < all_direction_vectors.length; i++){
            int current_change = 0;

            // push the player toward the direction as long as it still collides
            // an optimization is that if we already pushed the player more than minimum amount we can skip this direction
            while(this.does_collide(colliding_object) && (i == 0 || current_change < min_change)) {
                // count the push count
                current_change++;

                // push the player
                center_x += all_direction_vectors[i].x;
                center_y += all_direction_vectors[i].y;
            }

            // if this direction was the closest then save it
            // if this was the very first direction then again, save it
            if(i == 0 || current_change < min_change){
                min_change = current_change;
                result_x = center_x;
                result_y = center_y;
            }
            // remember the push amount for top direction, this information will be useful to reset can_jump
            if (i == 0)
                top_change = current_change;

            // set the player to it's original position to check other directions too
            center_y = original_y;
            center_x = original_x;
        }
        // teleport the player to saved (closest, non-colliding) location
        center_x = result_x;
        center_y = result_y;

        // if it was in collusion then it must have hit something, reset its velocity
        if (min_change != 0){
            velocity.y = 0;

            // if player is on top of something, reset can_jump
            if (min_change == top_change){
                can_jump = true;
            }
        }
    }

    // update position based on velocity
    public void apply_physics_movements(){
        center_x += velocity.x;
        center_y += velocity.y;
    }

    // draw the player (circle) on the canvas
    @Override
    void display(GraphicsContext g){
        g.beginPath();
        g.strokeOval(center_x-radius, center_y-radius, radius * 2, radius * 2);
        g.closePath();
        g.stroke();
        g.fillRect(center_x,center_y, 10, 10);
    }

    // same as other display function but with fill/stroke option
    @Override
    void display(GraphicsContext g, boolean fill, boolean stroke){
        if (fill) {
            g.setFill(this.color);
            g.fillOval(center_x-radius, center_y-radius, radius * 2, radius * 2);
        }
        if (stroke) {
            g.setLineWidth(1);
            g.setStroke(Color.BLACK);
            g.strokeOval(center_x-radius, center_y-radius, radius * 2, radius * 2);
        }
    }
}