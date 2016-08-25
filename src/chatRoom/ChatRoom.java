/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatRoom;

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

    public void setName(String name) {
        this.name = name;
    }
    
    public void addUser(User user){
        users.add(user);
        updateUserList();
    }
    
    public void removeUser(User user){
        users.remove(user);
        updateUserList();
    }
    
    public void updateUserList(){
        userListString = "";
        int i = 1;
        synchronized(users){
            for(User userObj:users){
                userListString = userListString + i++
                        +". " +userObj.getName()+"\n";
            }
        }
    }
}
