package gui;

import client.ClientServer;
import client.GUIstarter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Message;
import model.User;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class PrivateChatController {
    private User currentUser;
    private String otherUserName;
    private ClientServer server;
    private GUIstarter gui;

    @FXML
    private TextArea textArea;

    @FXML
    private Label usnTextField;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendMessageButton;

    @FXML
    private Button backToChatroomBtn;

    @FXML
    private Label chattingWithUsn;

    @FXML
    public void openMainChatRoom(ActionEvent event) {
        try {
            gui.showGroupChat();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendPrivateMessage(ActionEvent event) {
        String content = messageField.getText();
        messageField.clear();
        Message error = server.sendPrivateMessage(this.otherUserName, content);
        if(error != null){
            textArea.appendText(error.format());
        }
    }
    
    @FXML
    public void initialize() {
        this.textArea.clear();
        this.messageField.clear();

        this.server = ClientServer.getCurrentClient();
        this.gui = GUIstarter.getCurrentGUI();
        this.currentUser = server.getUser();

        this.usnTextField.setText(this.currentUser.getName());
        this.chattingWithUsn.setText(this.otherUserName);

        //load in the messages
        server.getPrivateChatMessages(this.otherUserName).forEach(string -> {
            textArea.appendText(string);
        });

        //console logging
        System.out.println("|PrivateChat initialised.");

        server.syncPrivateMessages(this, this.otherUserName);
    }

    public PrivateChatController(String otherUserName){
        this.otherUserName = otherUserName;
    }

    public void addMessage(String string){
        textArea.appendText(string);
    }

    //unused
    public void setOtherUserName(String name){
        this.otherUserName = name;
        this.chattingWithUsn.setText(this.otherUserName);
    }
}
