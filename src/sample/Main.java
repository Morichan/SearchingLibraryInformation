package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Main extends Application {
    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        controller = (Controller) fxmlLoader.getController();
        primaryStage.setTitle("Searching Library Information");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        controller.destroy();
        System.out.println("Bye");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
