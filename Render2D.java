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
    static boolean move_x = false, move_inverse_x = false, move_y = false, move_inverse_y = false;

    // define movement speed
    static int speed = 4;

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
        Object shape = new Object(new Vertex[]{
                new Vertex(0, 600),
                new Vertex(0, 500),
                new Vertex(800, 500),
                new Vertex(800, 600)});
        shape.color = Color.DARKGRAY;

        Object shape2 = new Object(new Vertex[]{
                new Vertex(600, 100),
                new Vertex(600, 200),
                new Vertex(700, 200),
                new Vertex(700, 100)});
        shape2.color = Color.GOLD;

        Player p = new Player(300, 300, 50);
        initialize_movement_system(primary_stage, p);
        initialize_physics_system(p);
    }

    static void initialize_movement_system(Stage primary_stage, Player p){

        primary_stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()){
                case W -> {
                    if (p.velocity.y < 3 && p.velocity.y > -3) p.velocity.y = -20;
                }
                case S -> move_y = true;
                case A -> move_inverse_x = true;
                case D -> move_x = true;
            }
        });

        primary_stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            switch (event.getCode()){
                case W -> move_inverse_y = false;
                case S -> move_y = false;
                case A -> move_inverse_x = false;
                case D -> move_x = false;
            }
        });

        AnimationTimer movement = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if(move_x)
                    p.center_x += speed;
                if(move_inverse_x)
                    p.center_x -= speed;
                if(move_y)
                    p.center_y += speed;
                if(move_inverse_y)
                    p.center_y -= speed;
            }
        };
        movement.start();
    }

    static void initialize_rendering_cycle(GraphicsContext g, Object[] objects){
        AnimationTimer object_renderer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                g.clearRect(0, 0, 800, 600);

                for(Object obj: objects){
                    obj.display(g, true, true);
                }
            }
        };
        object_renderer.start();

    }

    static void initialize_physics_system(Player p){
        AnimationTimer physics_system = new AnimationTimer() {
            @Override
            public void handle(long l) {
                for (Object obj: Object.all_objects){
                    if (!(obj instanceof Player)) {

                        // gravity
                        p.apply_physics_movements();
                        p.velocity.y += +2;

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
    public Vertex(double x, double y){
        this.x = x;
        this.y = y;
    }
    double x, y;
}

class Object{
    public static ArrayList<Object> all_objects = new ArrayList<Object>();
    Vertex[] vertices;
    Color color = Color.GREEN;
    public Object(){
        all_objects.add(this);
    }

    public Object(Vertex[] input_vertices){
        this.vertices = new Vertex[input_vertices.length];
        for(int i = 0; i < input_vertices.length; i++)
            this.vertices[i] = input_vertices[i];
        all_objects.add(this);
    }

    boolean does_collide(Object obj){
        return ((Path)Shape.intersect(this.get_path(), obj.get_path())).getElements().size() > 0;
    }

    public void escape_collision(Object colliding_object){
        while(this.does_collide(colliding_object)){
            for(Vertex v: vertices){
                //v.x += direction_vector.x;
                //v.y += direction_vector.y;
            }
        }
    }

    ObservableList<PathElement> get_collision(Object obj){
        return ((Path)Shape.intersect(this.get_path(), obj.get_path())).getElements();
    }

    Path get_path(){
        Path path = new Path();
        path.getElements().add(new MoveTo(vertices[0].x, vertices[0].y));
        for(Vertex v: vertices){
            path.getElements().add(new LineTo(v.x, v.y));
        }
        path.getElements().add(new ClosePath());
        return path;
    }

    Path get_path(boolean fill, boolean stroke){
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
        g.beginPath();
        for(Vertex v: vertices){
            g.lineTo(v.x, v.y);
        }
        g.closePath();
        g.setFill(Color.SILVER);
        g.stroke();
    }

    void display(GraphicsContext g, boolean fill, boolean stroke){
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

    Vertex velocity = new Vertex(0, 0);

    double center_x, center_y, radius;
    Color color = Color.DARKRED;

    public Player(){}

    public Player(double x, double y, double radii){
        this.center_x = x;
        this.center_y = y;
        this.radius = radii;
    }

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

    @Override
    public void escape_collision(Object colliding_object){
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

        double original_x = center_x, original_y = center_y;
        double result_x = center_x, result_y = center_y;
        int min_change = 0;
        for (int i = 0; i < all_direction_vectors.length; i++){
            int current_change = 0;
            while(this.does_collide(colliding_object) && (i == 0 || current_change < min_change)) {
                current_change++;
                center_x += all_direction_vectors[i].x;
                center_y += all_direction_vectors[i].y;
            }
            if(i == 0 || current_change < min_change){
                min_change = current_change;
                result_x = center_x;
                result_y = center_y;
            }
            center_y = original_y;
            center_x = original_x;
        }
        center_x = result_x;
        center_y = result_y;
        if (min_change != 0){
            velocity.x = 0;
            velocity.y = 0;
        }
    }

    public void apply_physics_movements(){
        center_x += velocity.x;
        center_y += velocity.y;
    }

    @Override
    void display(GraphicsContext g){
        g.beginPath();
        g.strokeOval(center_x-radius, center_y-radius, radius * 2, radius * 2);
        g.closePath();
        g.stroke();
        g.fillRect(center_x,center_y, 10, 10);
    }

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