package client;


import exceptions.WrongPassException;
import grpcchat.*;
import gui.GroupChatController;
import gui.LoginController;
import gui.PrivateChatController;
import io.grpc.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import grpcchat.GroupChatGrpc.GroupChatBlockingStub;
import grpcchat.GroupChatGrpc.GroupChatStub;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import javafx.application.Application;
import model.Message;
import model.User;

public class ClientServer {
    private User user;

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
                this.setUser(new User(reply.getName(), password));
                return reply.getName();
            } else throw new WrongPassException(username);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Message> initGroupChatTextArea() {
        List<Message> result = new ArrayList<>();
        Iterator<MessageLine> msgListIterator;
        try {
            msgListIterator = blockingStub.getGroupHistory(Empty.newBuilder().build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        while(msgListIterator.hasNext()){
            MessageLine ml = msgListIterator.next();
            result.add(new Message(ml.getMessage(), ml.getSender()));
        }
        return result;
    }

    public StreamObserver<MessageLine> syncMessageList(GroupChatController gui){
        StreamObserver<MessageLine> observer = new StreamObserver<MessageLine>() {
            @Override
            public void onNext(MessageLine value) {
                System.out.println("Message received: ");
                System.out.println("\t"+new Message(value.getMessage(), value.getSender()).format());
                gui.addMessage(new Message(value.getMessage(), value.getSender()));
            }

            @Override
            public void onError(Throwable t) {
                gui.addMessage(new Message("Server Error encountered. Messages will unsync. Please try again later.", "SERVER"));
            }

            @Override
            public void onCompleted() {
                return;
            }
        };
        try {
            System.out.println("|GroupChat|Entering textArea-syncing...");
            asyncStub.syncGroupChat(Empty.newBuilder().build(), observer);
            return observer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //returns null if correct, returns a system message if failed.
    public Message sendGroupMessage(String content){
        MessageLine ml = MessageLine.newBuilder().setMessage(content).setSender(this.user.getName()).build();
        Empty response;
        try {
            response = blockingStub.sendGroupMessage(ml);
        } catch (StatusRuntimeException srte){
            srte.printStackTrace();
            return new Message("Message not delivered: connection failed.", "SYSTEM");
        }
        return null;
    }

    //return null upon error
    public List<String> getCurrentUserList() {
        List<String> response = new ArrayList<>();
        Iterator<UserListEntry> listIterator;
        try {
            listIterator = blockingStub.getUserList(Empty.newBuilder().build());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        while(listIterator.hasNext()){
            response.add(listIterator.next().getUsername());
        }
        return response;
    }

    public StreamObserver<UserListEntry> syncUserList(GroupChatController gui) {
        StreamObserver<UserListEntry> usernames = new StreamObserver<UserListEntry>() {
            @Override
            public void onNext(UserListEntry value) {
                System.out.println("New User logged:");
                System.out.println("\t"+value.getUsername());
                gui.addUser(value.getUsername());
            }

            @Override
            public void onError(Throwable t) {
                gui.addMessage(new Message("Server error encountered. List with active users desynced. Please try again later", "SERVER"));
            }

            @Override
            public void onCompleted() {
                return;
            }
        };
        try {
            System.out.println("|GroupChat|Syncing Userlist...");
            asyncStub.syncUserList(Empty.newBuilder().build(), usernames);
            return usernames;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getPrivateChatMessages(String otherUserName){
        List<String> result = new ArrayList<>();
        Iterator<PrivateMessageDetails> prvIterator;
        try {
            prvIterator = blockingStub.getPrivateChat(PrivateMessageDetails.newBuilder().setSender(this.user.getName())
                                                                                        .setReceiver(otherUserName)
                                                                                        .setContent("").build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        while(prvIterator.hasNext()) {
            PrivateMessageDetails details = prvIterator.next();
            result.add(new Message(details.getContent(), details.getSender()).format());
        }
        return result;
    }

    public void syncPrivateMessages(PrivateChatController gui, String otherUser){
        StreamObserver<PrivateMessageDetails> observer = new StreamObserver<PrivateMessageDetails>() {
            @Override
            public void onNext(PrivateMessageDetails value) {
                System.out.println("New Private Message received:");
                System.out.println("\tSender: " + value.getSender());
                System.out.println("\tContent: " + value.getContent());
                gui.addMessage(new Message(value.getContent(), value.getSender()).format());
            }

            @Override
            public void onError(Throwable t) {
                gui.addMessage(new Message("Error encountered while syncing messages.", "SERVER").format());
            }

            @Override
            public void onCompleted() {
                return;
            }
        };
        PrivateMessageDetails request = PrivateMessageDetails.newBuilder().setSender(this.user.getName())
                                                                          .setReceiver(otherUser)
                                                                          .setContent("").build();
        try {
            System.out.println("|PrivateChat|Syncing messages...");
            asyncStub.syncPrivateChat(request, observer);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //returns an error message if failed, returns null if success.
    public Message sendPrivateMessage(String receiverName, String content){
        PrivateMessageDetails toSend = PrivateMessageDetails.newBuilder().setContent(content)
                                                                         .setReceiver(receiverName)
                                                                         .setSender(this.user.getName()).build();
        Empty response;
        try {
            response  = blockingStub.sendPrivateMessage(toSend);
        } catch (Exception e){
            e.printStackTrace();
            return new Message("ERROR: Message could not be deliverd to server", "SYSTEM");
        }
        return null;
    }

    public List<String> getNotifications(){
        List<String> result = new ArrayList<>();
        Iterator<Notification> notifIt;
        try {
            notifIt = blockingStub.fetchNotifications(UserListEntry.newBuilder().setUsername(this.user.getName()).build());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        while(notifIt.hasNext()){
            Notification details = notifIt.next();
            result.add(details.getContent());
        }
        return result;
    }

    public StreamObserver<Notification> syncNotifications(GroupChatController gui){
        StreamObserver<Notification> response = new StreamObserver<Notification>() {
            @Override
            public void onNext(Notification value) {
                System.out.println("New notification received:");
                System.out.println("\t" + value.getContent());
                gui.addNotification(value.getContent());
            }

            @Override
            public void onError(Throwable t) {
                gui.addMessage(new Message("Error encountered while syncing notifications.", "SERVER"));
            }

            @Override
            public void onCompleted() {

            }
        };
        UserListEntry request = UserListEntry.newBuilder().setUsername(this.user.getName()).build();
        try {
            System.out.println("|GroupChat|Syncing notifications...");
            asyncStub.syncNotifications(request, response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
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