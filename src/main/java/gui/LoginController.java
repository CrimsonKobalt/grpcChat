package gui;

import java.io.IOException;

import client.ClientServer;
import client.GUIstarter;
import exceptions.WrongPassException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;

public class LoginController {
	private ClientServer server;
	private GUIstarter gui;

	@FXML
	private Button EnterChatButton;

	@FXML
	private PasswordField passwdTextField;

	@FXML
	private Label usnTakenLabel;

	@FXML
	private TextField usnTextField;

	@FXML
	public void enterChatButtonClicked(ActionEvent event) throws IOException {
		String usn = usnTextField.getText();
		String pw = passwdTextField.getText();
		try {
			String user = this.server.validateUser(usn, pw);
			//open chatroom...
			if(user == null) throw new IOException("No response received from server: authenticateUser-method");
			System.out.println("Authenticated as user: " + user);
			gui.showGroupChat();
		} catch (WrongPassException wpe) {
			usnTakenLabel.setText("Wrong authentication details.");
		}
	}

	public void initialize() {
		this.server = ClientServer.getCurrentClient();
		this.gui = GUIstarter.getCurrentGUI();
		usnTakenLabel.setText("");
		System.out.println("|Login Screen initialised.");
	}
}