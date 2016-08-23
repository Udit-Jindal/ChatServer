/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatRoom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import user.User;

/**
 *
 * @author Raghu
 */
public class ChatRoom {
    public List<User> users;
    Socket socket;
    String name;
    public String userListString;
    
    public ChatRoom(User user){
        users = new ArrayList();
        users.add(user);
        updateUserList();
    }
    
    public String getName() {
        return name;
    }
    
    public void updateUserList(){
        userListString = "";
        int i = 0;
        synchronized(users){
            for(User userObj:users){
                userListString = userListString + i++
                        +". " +userObj.getName()+"\n";
            }
        }
    }
}
