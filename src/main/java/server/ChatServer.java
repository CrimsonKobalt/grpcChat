package server;

import exceptions.WrongPassException;
import grpcchat.*;
import io.grpc.Context;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import model.Message;
import model.PrivateChat;
import model.User;
import model.UserSystem;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

public class ChatServer{
    private final int port;
    private final Server server;

    private static final Object newMessageMutex = new Object();
    private static final Object newUserMutex = new Object();

    private static ServerSystem serverSystem;

    public ChatServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port, 10);
    }

    public ChatServer(ServerBuilder<?> serverBuilder, int port, int historySize) {
        this.port = port;
        if(serverSystem == null){
            serverSystem = new ServerSystem(historySize);
        }
        server = serverBuilder.addService(new ChatService()).build();
    }

    public void start() throws IOException{
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may has been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                ChatServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        serverSystem.closeServer();
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception{
        ChatServer server = new ChatServer(50050);
        server.start();
        System.out.println("press the enter key to shutdown server...");
        Scanner sc = new Scanner(System.in);
        while(!sc.hasNextLine()){

        }
        sc.nextLine();
        System.exit(0);
    }


    //INNERKLASSE: MOET OP DEZE MANIER GEINIT WORDEN OF GEEN TOEGANG TOT SERVERSYSTEM...
    private static class ChatService extends GroupChatGrpc.GroupChatImplBase {

        @Override
        public synchronized void getGroupHistory(Empty request, StreamObserver<grpcchat.MessageLine> responseObserver) {
            Iterator<Message> groupChatIterator = serverSystem.getMessageIterator();
            while(groupChatIterator.hasNext()){
                Message msg = groupChatIterator.next();
                responseObserver.onNext(MessageLine.newBuilder().setMessage(msg.getContent())
                                                                .setSender(msg.getSenderName())
                                                                .build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void syncGroupChat(Empty request, StreamObserver<MessageLine> responseObserver) {
            while(true){
                synchronized(newMessageMutex){
                    try {
                        newMessageMutex.wait();
                    } catch (Exception e){
                        e.printStackTrace();
                        responseObserver.onCompleted();
                    }
                    Message msg = serverSystem.getFinalMessage();
                    responseObserver.onNext(MessageLine.newBuilder().setMessage(msg.getContent())
                            .setSender(msg.getSenderName())
                            .build());
                }
            }
        }

        @Override
        public void sendGroupMessage(MessageLine request, StreamObserver<Empty> responseObserver) {
            User sender = serverSystem.getUserSystem().findUserByName(request.getSender());
            System.out.println("message received from: " + request.getSender());
            serverSystem.addMessage(request.getMessage(), sender, newMessageMutex);

            //make sure to send an empty response back to notify the client...
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void authenticateUser(UserDetails request, StreamObserver<Agreement> responseObserver) {
            try {
                System.out.println("auth request: "+ request.getName() +", "+request.getPassword());
                //might throw the WrongPassException
                User user = serverSystem.validateUser(request.getName(), request.getPassword(), newUserMutex);

                //notify all users of new join
                Iterator<User> allUsers = serverSystem.getUserSystem().getUserListIterator();
                while(allUsers.hasNext()){
                    User toNotify = allUsers.next();
                    synchronized (toNotify.getNotificationsMutex()){
                        toNotify.makeNotification(new Message("New user " + user.getName() + " has joined.", "SERVER"));
                        toNotify.getNotificationsMutex().notify();
                    }
                }

                //confirm login
                responseObserver.onNext(Agreement.newBuilder().setLoginSuccess(true)
                                                              .setName(request.getName())
                                                              .build());
                responseObserver.onCompleted();
            } catch (WrongPassException wpe) {
                wpe.printStackTrace();
                //deny login
                responseObserver.onNext(Agreement.newBuilder().setLoginSuccess(false)
                                                              .setName(request.getName())
                                                              .build());
                responseObserver.onCompleted();
            }
        }

        @Override
        public synchronized void getUserList(Empty request, StreamObserver<UserListEntry> responseObserver) {
            Iterator<User> userIterator = serverSystem.getUserListIterator();
            while(userIterator.hasNext()){
                User user = userIterator.next();
                responseObserver.onNext(UserListEntry.newBuilder().setUsername(user.getName()).build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void syncUserList(Empty request, StreamObserver<UserListEntry> responseObserver) {
            while(true){
                synchronized (newUserMutex){
                    try {
                        newUserMutex.wait();
                    } catch (Exception e){
                        e.printStackTrace();
                        responseObserver.onCompleted();
                    }
                    User user = serverSystem.getSyncUpdate();
                    responseObserver.onNext(UserListEntry.newBuilder().setUsername(user.getName()).build());
                }
            }
        }

        @Override
        public void fetchNotifications(UserListEntry request, StreamObserver<grpcchat.Notification> responseObserver) {
            User user = serverSystem.getUserSystem().findUserByName(request.getUsername());
            synchronized (user.getNotificationsMutex()){
                try {
                    while(user.hasMoreNotifications()){
                        responseObserver.onNext(Notification.newBuilder().setContent(user.getNotification().format()).build());
                    }
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    responseObserver.onCompleted();
                }
            }
        }

        @Override
        public void syncNotifications(UserListEntry request, StreamObserver<grpcchat.Notification> responseObserver) {
            User user = serverSystem.getUserSystem().findUserByName(request.getUsername());
            while(true) {
                synchronized (user.getNotificationsMutex()){
                    try {
                        user.getNotificationsMutex().wait();
                    } catch (Exception e){
                        e.printStackTrace();
                        responseObserver.onCompleted();
                    }
                    Message notification = user.getNotification();
                    responseObserver.onNext(Notification.newBuilder().setContent(notification.format()).build());
                }
            }
        }

        @Override
        public void sendPrivateMessage(PrivateMessageDetails request, StreamObserver<Empty> responseObserver) {
            User receiver = serverSystem.getUserSystem().findUserByName(request.getReceiver());
            User sender = serverSystem.getUserSystem().findUserByName(request.getSender());
            PrivateChat privateChat = serverSystem.findPrivateChat(sender, receiver);

            privateChat.addMessage(new Message(request.getContent(), sender));
            synchronized (receiver.getNotificationsMutex()){
                receiver.makeNotification(new Message("Private message received from " + sender.getName(), "SERVER"));
                receiver.getNotificationsMutex().notify();
            }

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public synchronized void getPrivateChat(PrivateMessageDetails request, StreamObserver<PrivateMessageDetails> responseObserver) {
            User askingParty = serverSystem.getUserSystem().findUserByName(request.getSender());
            User otherParty = serverSystem.getUserSystem().findUserByName(request.getReceiver());
            PrivateChat privateChat = serverSystem.findPrivateChat(askingParty, otherParty);

            Iterator<Message> msgIt = privateChat.getMessagesIterator();

            while(msgIt.hasNext()){
                Message toSend = msgIt.next();
                //het veld "receiver" is hier irrelevant eigenlijk, maar wordt gewoon ingevuld als zijnde de vragende partij
                responseObserver.onNext(PrivateMessageDetails.newBuilder().setContent(toSend.getContent())
                                                                          .setReceiver(askingParty.getName())
                                                                          .setSender(toSend.getSenderName()).build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void syncPrivateChat(PrivateMessageDetails request, StreamObserver<PrivateMessageDetails> responseObserver) {
            User askingParty = serverSystem.getUserSystem().findUserByName(request.getSender());
            User otherParty = serverSystem.getUserSystem().findUserByName(request.getReceiver());
            PrivateChat privateChat = serverSystem.findPrivateChat(askingParty, otherParty);

            while(true) {
                synchronized (privateChat.getMessagesMutex()) {
                    try {
                        privateChat.getMessagesMutex().wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseObserver.onCompleted();
                    }
                    Message toSend = privateChat.getNewMessage();
                    responseObserver.onNext(PrivateMessageDetails.newBuilder().setSender(toSend.getSenderName())
                                                                              .setReceiver(askingParty.getName())
                                                                              .setContent(toSend.getContent()).build());
                }
            }
        }
    }
}
