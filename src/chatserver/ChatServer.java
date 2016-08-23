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
        final StringParser stringParser = new StringParser();
        User user;
        
        public UserAction(User user){
            this.user = user;
        }
        
        @Override
        public void run(){
            String line;
            try{
                while(true){
                    line = user.getInputStream().readLine();
                    if(!line.isEmpty())
                    {
                        stringParser.processInput(line);
                        if(stringParser.isCommand){
                           performAction(stringParser.getReturnCode());
                        }
                        else{
                            writeToClients(line);
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
            
        }
        
        public void writeToClients(String line){
            synchronized(userList){
                for(User userObj:userList){
                    if(!userObj.equals(this.user)){
                        userObj.getOutputStream().println(line);//try
                    }
                }
            }
            
        }
        
    }
}
