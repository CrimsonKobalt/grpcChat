package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PrivateChatController {

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

    }

    @FXML
    public void sendPrivateMessage(ActionEvent event) {
    }
    
    public void update() {
	}
    
    @FXML
    public void initialize() {
    }
}
