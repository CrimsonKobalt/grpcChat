package gui;

import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.User;

public class GroupChatController {

	@FXML
	private TextField messageField;

	@FXML
	private Button sendMessageButton;

	@FXML
	private TextArea textArea;

	@FXML
	private ListView<User> userListTable;

	@FXML
	private Label usnTextField;

	public void initialize() {
	}

	public void update() {
	}

	@FXML
	public void sendGroupMessage(ActionEvent event) {
	}

	@FXML
	public void clickUserName(ActionEvent ae) {
		// System.out.println(userListTable.getSelectionModel().getSelectedItem());
	}

}
