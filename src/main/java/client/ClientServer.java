package client;


import exceptions.WrongPassException;
import grpcchat.Agreement;
import grpcchat.GroupChatGrpc;
import grpcchat.UserDetails;
import gui.GroupChatController;
import gui.LoginController;
import gui.PrivateChatController;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import grpcchat.GroupChatGrpc.GroupChatBlockingStub;
import grpcchat.GroupChatGrpc.GroupChatStub;

import io.grpc.StatusRuntimeException;

import javafx.application.Application;
import model.User;

public class ClientServer {
    private static User user;

    private static GroupChatController groupChatController;
    private static LoginController loginController;
    private static PrivateChatController privateChatController;

    private final ManagedChannel channel;
    private final GroupChatBlockingStub blockingStub;
    private final GroupChatStub asyncStub;

    private static ClientServer currentClient;

    public ClientServer(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public ClientServer(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = grpcchat.GroupChatGrpc.newBlockingStub(channel);
        asyncStub = grpcchat.GroupChatGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        ClientServer client = new ClientServer("localhost", 50050);
        currentClient = client;
        Application.launch(GUIstarter.class, args);
    }

    public String validateUser(String username, String password) throws WrongPassException {
        UserDetails userDetails = UserDetails.newBuilder().setName(username).setPassword(password).build();
        Agreement reply;
        try {
            reply = this.blockingStub.authenticateUser(userDetails);
            if (reply.getLoginSuccess()) {
                return reply.getName();
            } else throw new WrongPassException(username);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        ClientServer.user = user;
    }

    public static void setGroupChatController(GroupChatController groupChatController) {
        ClientServer.groupChatController = groupChatController;
    }

    public static void setLoginController(LoginController loginController) {
        ClientServer.loginController = loginController;
    }

    public static void setPrivateChatController(PrivateChatController privateChatController) {
        ClientServer.privateChatController = privateChatController;
    }

    public static ClientServer getCurrentClient() {
        return currentClient;
    }
}