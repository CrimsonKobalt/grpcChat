package model;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

//aangemaakt om niet te moeten prutsen met die printwriters bij gebruikers
public class User implements Comparable<User>{
    private String name;
    private String password;

    private final Queue<Message> notifications;

    public User(String name, String password) {
        super();
        this.name = name;
        this.password = password;
        this.notifications = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

    @Override
    public String toString() {
        return this.name + "," + this.password;
    }

    @Override
    public int compareTo(User o) {
        return this.name.compareTo(o.name);
    }

    public Queue<Message> getNotificationsMutex(){
        return this.notifications;
    }

    public void makeNotification(Message message){
        this.notifications.offer(message);
    }

    public boolean hasMoreNotifications(){
        return (this.notifications.peek() != null);
    }

    public Message getNotification(){
        return this.notifications.poll();
    }
}