/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatserver;

import chatEngine.StringParser;
import chatRoom.ChatRoom;
import com.sun.xml.internal.ws.api.pipe.Engine;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import user.User;

/**
 *
 * @author Raghu
 */
public class ChatServer {
    
    static List<ChatRoom> chatRoomList;
    static List<User> userList;
    static Engine engine;
    static ServerSocket serverSocket;
    static Thread userListener;
    static String chatRoomListString;
    static String userListString;
    
    public ChatServer(){
        try {
            serverSocket = new ServerSocket(58000);
            chatRoomList = new ArrayList<>();
            userList = new ArrayList<>();
        } catch (IOException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        userListener = new ChatServer.UserListener();
        userListener.start();
        
    }
    
    private void updateRoomListString(){
        chatRoomListString = "";
        int i = 1;
        synchronized(chatRoomList){
            for(ChatRoom rm:chatRoomList){
                chatRoomListString = chatRoomListString + "\n"+i++
                        +". " +rm.getName()+"\n";
            }
        }
    }
    
    private void updateUserListString(){
        userListString = "";
        int i = 1;
        synchronized(userList){
            for(User us:userList){
                userListString = userListString + "\n"+i++
                        +". " +us.getName()+"\n";
            }
        }
    }
    
    private void addUser(User user){
        synchronized(userList)
        {
            userList.add(user);
            updateUserListString();
        }
    }
    
    private void addRoom(ChatRoom chatRoom){
        synchronized(chatRoomList)
        {
            chatRoomList.add(chatRoom);
            updateRoomListString();
        }
    }
    
    private void removeUser(User user){
        synchronized(userList)
        {
            userList.remove(user);
            updateUserListString();
        }
    }
    
    private void removeRoom(ChatRoom chatRoom){
        synchronized(chatRoomList)
        {
            chatRoomList.remove(chatRoom);
            updateRoomListString();
        }
    }
    public ChatRoom getRoomFromName(String name){
        ChatRoom localChatRoom = null;
        try{
            synchronized(chatRoomList){
                for(ChatRoom chatRoomObj:chatRoomList){
                    localChatRoom = chatRoomObj;
                    if((localChatRoom.getName()).equalsIgnoreCase(name)){
                        System.out.println("localchatroomname:-"+localChatRoom.getName());
                        System.out.println("\npassed name:-"+name);
                        return localChatRoom;
                    }
                }
            }
        }catch(NullPointerException ex){
            System.out.println(localChatRoom.getName()+" disconnected.");
            removeRoom(localChatRoom);
        }
        return localChatRoom;
    }
    
    class UserListener extends Thread {
        
        @Override
        public void run(){
            while(true){
                try {
                    Socket socket= serverSocket.accept();
                    User user = new User(socket);
                    addUser(user);
                    Thread userAction = new UserAction(user);
                    userAction.start();
                } catch (IOException ex) {
                    System.out.println("IOException Occoured." + ex);
                }
                catch (Exception ex){
                    System.out.println("Generic exception Occoured. " + ex);
                }finally{
                }
            }
        }
        
    }
    
    class UserAction extends Thread{
        User user;
        
        public UserAction(User user){
            this.user = user;
        }
        
        @Override
        public void run(){
            String line;
            try{
                while(true){
                    StringParser stringParser = new StringParser();
                    line = user.getInputStream().readLine();
                    if(!line.isEmpty())
                    {
                        stringParser.processInput(line);
                        if(stringParser.getReturnCode() != 111){
                            if(stringParser.isCommand){
                                if(stringParser.getReturnCode()==000){
                                    user.setName(stringParser.parcedData.userName);
                                    updateUserListString();
                                }
                                else
                                {
                                    performAction(stringParser.getReturnCode(),stringParser.parcedData.roomName);
                                }
                            }
                            else{
                                if(stringParser.inputFor.equalsIgnoreCase("ALL")){
                                    writeToAllClients(getRoomFromName(user.getChatRoomname()),line);
                                }else{
                                    ChatRoom chatRoom = getRoomFromName(user.getChatRoomname());
                                    User localUser = getUserFromName(chatRoom,stringParser.parcedData.userName);
                                    writeToOneClient(localUser, line);
                                }
                            }
                        }else{
                            user.getOutputStream().println("#FROMSERVER EXIT DISCONNECTED");
                            removeUser(user);
                            break;
                        }
                    }
                }
            } catch (SocketException ex) {
                
                removeUser(user);
            }catch(IOException ex){
                Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                try {
                    user.closeInputStream();
                    user.closeOutputStream();
                } catch (Exception ex) {
                    System.out.println("Un-able to close the streams.");
                }
            }
        }
        
        public void performAction(int code,String roomName){
            
            switch(code){
                case 001:{
                    user.getOutputStream().println("#FROMSERVER"+" "
                            +"ROOMLIST"
                            +" "
                            +chatRoomListString);
                }
                break;
                
                case 002:{
                    
                    user.getOutputStream().println("#FROMSERVER"+ " "
                            +"SHOWUSERLIST"
                            + " "
                            + "USERLIST"
                            + " "
                            + userListString);
                }
                break;
                
                case 003:{
                    System.out.println("room entered:-"+roomName);
                    ChatRoom chatRoomRequested = getRoomFromName(roomName);
                    if(chatRoomRequested != null){
                        
                        user.getOutputStream().println("#FROMSERVER"+ " "
                                +"SHOWUSERINROOMLIST"
                                + " "
                                + "USERLIST"
                                + " "
                                + chatRoomRequested.userListString);
                    }else{
                        user.getOutputStream().println("#FROMSERVER"+ " "
                                +"SHOWUSERINROOMLIST"
                                + " "
                                + "NOROOMFOUND");
                    }
                }
                break;
                
                case 004:{
                    ChatRoom localChatRoom = getRoomFromName(roomName);
                    if(localChatRoom == null){
                        ChatRoom newChatRoom = new ChatRoom(user);
                        newChatRoom.setName(roomName);
                        addRoom(newChatRoom);
                        user.setChatRoomname(roomName);
                        user.getOutputStream().println("#FROMSERVER"+ " "
                                +"NEWROOMCREATED"
                                + " "
                                + roomName
                                + " "
                                + "USERLIST"
                                + " "
                                + newChatRoom.userListString);
                    }else{
                        
                        
                        localChatRoom.addUser(user);
                        user.setChatRoomname(localChatRoom.getName());
                        user.getOutputStream().println("#FROMSERVER"+ " "
                                +"ROOMJOINED"
                                + " "
                                + localChatRoom.getName()
                                + " "
                                + "USERLIST"
                                + " "
                                + localChatRoom.userListString);
                    }
                }
                break;
                
                case 005:{
                    ChatRoom oldChatRoom = getRoomFromName(user.getChatRoomname());
                    user.setChatRoomname(null);
                    if(oldChatRoom != null){
                        oldChatRoom.removeUser(user);
                    }else{
                        user.getOutputStream().println("#FROMSERVER"
                                +" "
                                +"EXITROOM"
                                +" "
                                +"ROOMDOESNOTEXISTS");
                    }
                }
                break;
                
                default:
                {
                    user.getOutputStream().println("#FROMSERVER"+" "+"WRONGINPUT");
                }
            }
        }
        
        public void writeToAllClients(ChatRoom chatRoom,String line){
            User localUser = null;
            try{
                synchronized(chatRoom.users){
                    for(User userObj:chatRoom.users){
                        localUser = userObj;
                        if(!userObj.equals(this.user)){
                            userObj.getOutputStream().println(this.user.getName()+":-"+line);//try
                        }
                    }
                }
            }catch(NullPointerException ex){
                System.out.println(localUser.getName()+" disconnected.");
                chatRoom.removeUser(localUser);
            }
            
        }
        
        public void writeToOneClient(User user,String line){
            user.getOutputStream().println(this.user.getName()+":-"+line);
        }
        
        public User getUserFromName(ChatRoom chatRoom,String name){
            User localUser = null;
            try{
                synchronized(chatRoom.users){
                    for(User userObj:chatRoom.users){
                        localUser = userObj;
                        if((localUser.getName()).equals(name)){
                            return localUser;
                        }
                    }
                }
            }catch(NullPointerException ex){
                System.out.println(localUser.getName()+" disconnected.");
                chatRoom.removeUser(localUser);
            }
            return localUser;
        }
        
    }
    
}
