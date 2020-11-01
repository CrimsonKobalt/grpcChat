package server;

import grpcchat.Empty;
import grpcchat.GroupChatGrpc;
import grpcchat.MessageLine;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import model.Message;
import model.User;

import java.io.IOException;
import java.util.Iterator;

public class ChatServer{
    private final int port;
    private final Server server;

    private static final Object newMessageMutex = new Object();
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


    //INNERKLASSE: MOET OP DEZE MANIER GEINIT WORDEN OF GEEN TOEGANG TOT SERVERSYSTEM...
    private static class ChatService extends GroupChatGrpc.GroupChatImplBase {

        @Override
        public synchronized void syncGroupChat(Empty request, StreamObserver<MessageLine> responseObserver) {
            Iterator<Message> groupChatIterator = serverSystem.getMessageIterator();
            while(groupChatIterator.hasNext()){
                Message msg = groupChatIterator.next();
                responseObserver.onNext(MessageLine.newBuilder().setMessage(msg.getContent())
                                                                .setSender(msg.getSenderName())
                                                                .build());
                if(!groupChatIterator.hasNext()){
                    try {
                        newMessageMutex.wait();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public synchronized void sendGroupMessage(MessageLine request, StreamObserver<Empty> responseObserver) {
            User sender = serverSystem.getUserSystem().findUserByName(request.getSender());
            serverSystem.addMessage(request.getMessage(), sender);
            //notify de syncGroupChat-methode voor alle clients dat er een nieuwe message in de stream is!
            newMessageMutex.notify();
        }

    }
}
