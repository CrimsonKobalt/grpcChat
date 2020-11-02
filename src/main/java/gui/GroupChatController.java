package gui;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import client.ClientServer;
import client.GUIstarter;
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
import model.Message;
import model.User;

public class GroupChatController {
	private List<Message> messages;
	private User currentUser;
	private ClientServer server;
	private GUIstarter gui;
	private ObservableList<String> userlist;

	@FXML
	private TextField messageField;

	@FXML
	private Button sendMessageButton;

	@FXML
	private TextArea textArea;

	@FXML
	private ListView<String> userListTable;

	@FXML
	private Label usnTextField;

	public void initialize() {
		//set GUIstarter reference
		this.gui = GUIstarter.getCurrentGUI();

		//set TextArea
		this.server = ClientServer.getCurrentClient();
		this.messages = server.initGroupChatTextArea();
		this.textArea.clear();
		this.messages.forEach(message -> {
			this.textArea.appendText(message.format());
		});

		//set User data
		this.currentUser = server.getUser();
		usnTextField.setText(currentUser.getName());

		//set TextField
		this.messageField.clear();

		//set userList
		List<String> usernames = server.getCurrentUserList();
		if(usernames == null){
			textArea.appendText(new Message("Error occurred while fetching user data", "Server").format());
		} else {
			Collections.sort(usernames);
			userlist = FXCollections.observableList(usernames);
			userListTable.setItems(userlist);
		}

		//add onClick event


		//monitor updates
		server.syncMessageList(this);
		server.syncUserList(this);
	}

	public void update() {
	}

	@FXML
	public void sendGroupMessage(ActionEvent event) {
		String content = messageField.getText();
		messageField.clear();
		Message error = server.sendGroupMessage(content);
		if(error != null){
			textArea.appendText(error.format());
		}
	}

	@FXML
	public void clickUserName(ActionEvent ae) {
		// System.out.println(userListTable.getSelectionModel().getSelectedItem());
	}

	public void addMessage(Message message) {
		this.messages.add(message);
		this.textArea.appendText(message.format());
	}

	public void addUser(String username) {
		Platform.runLater(() -> {
			this.userlist.add(username);
			Collections.sort(userlist);
			this.userListTable.setItems(userlist);
		});
	}
}
