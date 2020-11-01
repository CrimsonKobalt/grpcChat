package client;

import gui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GUIstarter extends Application {
    private static Stage window;
    private static AnchorPane layout;
    private static GUIstarter gui;

    public GUIstarter(){
        if(gui == null){
            gui = this;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setResizable(false);
        window.setTitle("grpcChat");

        try {
            showLoginForm();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void stop() {
        System.out.println("Shutting down...");
    }

    public void showLoginForm() throws MalformedURLException {
        showView(new URL("file:src/main/java/gui/LoginForm.fxml"));
    }

    public void showGroupChat() throws MalformedURLException{
        showView(new URL("file:src/main/java/gui/GroupChat.fxml"));
    }

    public void showView(URL viewLocation) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(viewLocation);
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

    public static GUIstarter getCurrentGUI(){
        return gui;
    }
}
