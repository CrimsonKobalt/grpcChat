package server;

import exceptions.WrongPassException;
import grpcchat.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import model.Message;
import model.User;
import model.UserSystem;

import java.io.IOException;
import java.util.Iterator;

public class ChatServer{
    private final int port;
    private final Server server;

    private static final Object newMessageMutex = new Object();
    private static final Object newUserMutex = new Object();

    private static ServerSystem serverSystem;

    public ChatServer(int port) throws IOException {
        this(ServerBuilder.forPort(port), port, 5);
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
        if (server != null) {
            server.shutdown();
        }
        serverSystem.closeServer();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception{
        ChatServer server = new ChatServer(50050);
        server.start();
        server.blockUntilShutdown();
    }


    //INNERKLASSE: MOET OP DEZE MANIER GEINIT WORDEN OF GEEN TOEGANG TOT SERVERSYSTEM...
    private static class ChatService extends GroupChatGrpc.GroupChatImplBase {

        @Override
        public void getGroupHistory(Empty request, StreamObserver<grpcchat.MessageLine> responseObserver) {
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
        public synchronized void syncGroupChat(Empty request, StreamObserver<MessageLine> responseObserver) {
            while(true){
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

        @Override
        public synchronized void sendGroupMessage(MessageLine request, StreamObserver<Empty> responseObserver) {
            User sender = serverSystem.getUserSystem().findUserByName(request.getSender());
            serverSystem.addMessage(request.getMessage(), sender);

            //notify de syncGroupChat-methode voor alle clients dat er een nieuwe message in de stream is!
            newMessageMutex.notify();

            //make sure to send an empty response back to notify the client...
            responseObserver.onCompleted();
        }

        @Override
        public synchronized void authenticateUser(UserDetails request, StreamObserver<Agreement> responseObserver) {
            try {
                User user = serverSystem.validateUser(request.getName(), request.getPassword(), newUserMutex);
                responseObserver.onNext(Agreement.newBuilder().setLoginSuccess(true)
                                                              .setName(request.getName())
                                                              .build());
                responseObserver.onCompleted();
            } catch (WrongPassException wpe) {
                wpe.printStackTrace();
                responseObserver.onNext(Agreement.newBuilder().setLoginSuccess(false)
                                                              .setName(request.getName())
                                                              .build());
                responseObserver.onCompleted();
            }
        }

        public synchronized void getUserList(Empty request, StreamObserver<UserListEntry> responseObserver) {
            Iterator<User> userIterator = serverSystem.getUserListIterator();
            while(userIterator.hasNext()){
                User user = userIterator.next();
                responseObserver.onNext(UserListEntry.newBuilder().setUsername(user.getName()).build());
            }
            responseObserver.onCompleted();
        }

        public synchronized void syncUserList(Empty request, StreamObserver<UserListEntry> responseObserver) {
            while(true){
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
}
