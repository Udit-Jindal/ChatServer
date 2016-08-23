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
        int i = 0;
        synchronized(chatRoomList){
            for(ChatRoom rm:chatRoomList){
                chatRoomListString = chatRoomListString + i
                        +". " +rm.getName()+"\n";
            }
        }
    }
    
    class UserListener extends Thread {
        
        @Override
        public void run(){
            while(true){
                try {
                    Socket socket= serverSocket.accept();
                    User user = new User(socket);
                    synchronized(userList)
                    {
                        userList.add(user);
                    }
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
                                }
                                else
                                {
                                    performAction(stringParser.getReturnCode());
                                }
                            }
                            else{
                                if(stringParser.inputFor.equalsIgnoreCase("ALL")){
                                    writeToAllClients(line);
                                }else{
                                    User localUser = getUserFromName(stringParser.parcedData.userName);
                                    writeToOneClient(localUser, line);
                                }
                            }
                        }else{
                            user.getOutputStream().println("#FROMSERVER DISCONNECTED");
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                try {
                    user.closeInputStream();
                    user.closeOutputStream();
                } catch (Exception ex) {
                    System.out.println("Un-able to close the streams.");
                }
            }
        }
        
        public void performAction(int code){
            
            switch(code){
                case 001:{
                    user.getOutputStream().println("#FROMSERVER"+" "
                            +"ROOMLIST"
                            +" "
                            +chatRoomListString);
                }
                break;
                
                case 002:{
                    //Show user list of current room.
                }
                break;
                
                case 003:{
                    //Show user list in a particular room.
                }
                break;
                
                case 004:{
                    //Connect to a room
                }
                break;
                
                case 005:{
                    //Exit the room
                }
                break;
                
                default:
                {
                    user.getOutputStream().println("#FROMSERVER"+" "+"WRONGINPUT");
                }
            }
        }
        
        public void writeToAllClients(String line){
            User localUser = null;
            try{
                synchronized(userList){
                    for(User userObj:userList){
                        if(!userObj.equals(this.user)){
                            userObj.getOutputStream().println(line);//try
                        }
                    }
                }
            }catch(NullPointerException ex){
                System.out.println(localUser.getName()+" disconnected.");
                userList.remove(localUser);
            }
            
        }
        
        public void writeToOneClient(User user,String line){
            user.getOutputStream().println(line);
        }
        
        public User getUserFromName(String name){
            User localUser = null;
            try{
                synchronized(userList){
                    for(User userObj:userList){
                        localUser = userObj;
                        if((localUser.getName()).equals(name)){
                            return localUser;
                        }
                    }
                }
            }catch(NullPointerException ex){
                System.out.println(localUser.getName()+" disconnected.");
                userList.remove(localUser);
            }
            return localUser;
        }
    }
}
