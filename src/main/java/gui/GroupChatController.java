package gui;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import client.ClientServer;
import client.GUIstarter;
import grpcchat.MessageLine;
import grpcchat.Notification;
import grpcchat.UserDetails;
import grpcchat.UserListEntry;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
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

	private static StreamObserver<Notification> notificationObserver;
	private static StreamObserver<MessageLine> messageObserver;
	private static StreamObserver<UserListEntry> userObserver;

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

	public GroupChatController(){
		notificationObserver = null;
		messageObserver = null;
		userObserver = null;
	}

	public void initialize() {
		//set GUIstarter reference
		this.gui = GUIstarter.getCurrentGUI();
		this.server = ClientServer.getCurrentClient();

		//set TextArea
		this.messages = server.initGroupChatTextArea();
		this.textArea.clear();
		//fetching groupchat...
		this.messages.forEach(message -> {
			this.textArea.appendText(message.format());
		});
		//fetching notifications...
		server.getNotifications().forEach(string -> {
			this.textArea.appendText(string);
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

		//add onClick event to listview
		User currentUser = this.currentUser;
		userListTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				System.out.println("ListView selection changed from oldValue = "
						+ oldValue + " to newValue = " + newValue);
				try {
					if(!currentUser.getName().equals(newValue)){
						gui.showPrivateChat(newValue);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		//console logging
		System.out.println("|GroupChat initialised.");

		//monitor updates
		if(userObserver == null){
			userObserver = server.syncUserList(this);
		}
		if(notificationObserver == null){
			notificationObserver = server.syncNotifications(this);
		}
		if(messageObserver == null) {
			messageObserver = server.syncMessageList(this);
		}
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

	//this goes unused as is.
	@FXML
	public void clickUserName(ActionEvent ae) {
		// System.out.println(userListTable.getSelectionModel().getSelectedItem());
	}

	public void addMessage(Message message) {
		this.messages.add(message);
		this.textArea.appendText(message.format());
	}

	public void addNotification(String notification){
		this.textArea.appendText(notification);
	}

	public void addUser(String username) {
		Platform.runLater(() -> {
			this.userlist.add(username);
			Collections.sort(userlist);
			this.userListTable.setItems(userlist);
		});
	}
}
