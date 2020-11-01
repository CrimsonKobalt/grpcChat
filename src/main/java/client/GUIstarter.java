package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GUIstarter extends Application {
    private static Stage window;
    private static AnchorPane layout;

    public GUIstarter(){
        //default empty
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setResizable(false);
        window.setTitle("grpcChat");

        showLoginForm();
    }

    @Override
    public void stop() {
        System.out.println("Shutting down...");
    }

    public void showLoginForm() {
        showView("gui.LoginForm.fxml");
    }

    public void showGroupChat() {
        showView("../gui/GroupChat.fxml");
    }

    public void showView(String viewLocation) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource(viewLocation));
        try {
            layout = loader.load();
        } catch (IOException e) {
            System.out.println("IOException in showView()");
            e.printStackTrace();
            System.exit(1);
        }
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();
    }
}
