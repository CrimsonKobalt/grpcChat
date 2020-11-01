package gui;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

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
	}

	public void initialize() {
	}

	public void setWrongPassword(String s) {
	}

}