package client;

import gui.GroupChatController;
import gui.LoginController;
import gui.PrivateChatController;
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

    private LoginController loginController = new LoginController();
    private GroupChatController groupChatController = new GroupChatController();

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
        showView(new URL("file:src/main/java/gui/LoginForm.fxml"), loginController);
    }

    public void showGroupChat() throws MalformedURLException{
        showView(new URL("file:src/main/java/gui/GroupChat.fxml"), groupChatController);
    }

    public void showPrivateChat(String otherUser) throws MalformedURLException{
        showView(new URL("file:src/main/java/gui/PrivateChat.fxml"), otherUser);
    }

    public void showView(URL viewLocation, Object controller) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(viewLocation);
        loader.setController(controller);
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

    public void showView(URL viewLocation, String otherUser) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(viewLocation);
        loader.setController(new PrivateChatController(otherUser));
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
