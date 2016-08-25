/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatRoom;

import chatserver.ChatServer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import chatserver.ChatServer.UserProxy;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Raghu
 */
public class ChatRoom {
    List<UserProxy> chatRoomUserList;
    String name;
    
    public ChatRoom(String roomName){
        chatRoomUserList = new ArrayList();
        this.name = roomName;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void removeUser(UserProxy user){
        synchronized(chatRoomUserList){
            chatRoomUserList.remove(user);
        }
    }
    
    public void addUser(UserProxy user){
        synchronized(chatRoomUserList){
            chatRoomUserList.add(user);
        }
    }
    
    public String getUserListString(){
        String userListString = "";
        int i = 1;
        synchronized(chatRoomUserList){
            for(UserProxy userObj:chatRoomUserList){
                userListString = userListString + i++
                        +". " +userObj.getName()+"\n";
            }
        }
        return userListString;
    }
    
    public void writeToAllUsers(UserProxy user,String messageFrom,String line){
        synchronized(chatRoomUserList){
            for(UserProxy userObj:chatRoomUserList){
                if(!userObj.equals(user)){
                    userObj.writeMessage(messageFrom+":-"+line);
                }
            }
        }
    }
    
}
