import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class Render2D extends Application {
    public static void main(String[] args){
        launch(args);
    }
    @Override
    public void start(Stage primary_stage){
        Pane root = new Pane();

        object shape = new object(new vertex[]{
                new vertex(0, 600),
                new vertex(0, 500),
                new vertex(800, 500),
                new vertex(800, 600)});
        shape.color = Color.DARKGRAY;

        object shape2 = new object(new vertex[]{
                new vertex(600, 100),
                new vertex(600, 200),
                new vertex(700, 200),
                new vertex(700, 100)});
        shape2.color = Color.GOLD;

        player p = new player(300, 300, 50);

        System.out.println( shape.does_collide(shape2));

        root.getChildren().add(shape.get_path(true, true));
        root.getChildren().add(shape2.get_path(true,true));
        root.getChildren().add(p.get_path(true,true));
        Scene scene = new Scene(root, 800, 600);
        primary_stage.setScene(scene);
        primary_stage.show();
    }


}

class vertex{
    public vertex(int x, int y){
        this.x = x;
        this.y = y;
    }
    double x, y;
}

class object{
    vertex[] vertices;
    Color color = Color.GREEN;
    public object(){}

    public object(vertex[] input_vertices){
        this.vertices = new vertex[input_vertices.length];
        for(int i = 0; i < input_vertices.length; i++)
            this.vertices[i] = input_vertices[i];
    }

    boolean does_collide(object obj){
        return ((Path)Shape.intersect(this.get_path(), obj.get_path())).getElements().size() > 0;
    }

    ObservableList<PathElement> get_collision(object obj){
        return ((Path)Shape.intersect(this.get_path(), obj.get_path())).getElements();
    }

    Path get_path(){
        Path path = new Path();
        path.getElements().add(new MoveTo(vertices[0].x, vertices[0].y));
        for(vertex v: vertices){
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
        for(vertex v: vertices){
            g.lineTo(v.x, v.y);
        }
        g.closePath();
        g.setFill(Color.SILVER);
        g.stroke();
    }
}

class player extends object{

    double center_x, center_y, radius;
    Color color = Color.GREEN;

    public player(){}

    public player(double x, double y, double radii){
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
    void display(GraphicsContext g){
        g.beginPath();
        g.strokeOval(center_x-radius, center_y-radius, radius * 2, radius * 2);
        g.closePath();
        g.stroke();
        g.fillRect(center_x,center_y, 10, 10);
    }
}