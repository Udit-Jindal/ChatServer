/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatRoom;

import java.net.Socket;
import java.util.List;
import user.User;

/**
 *
 * @author Raghu
 */
public class ChatRoom {
    List<User> users;
    Socket socket;
    String name;

    public String getName() {
        return name;
    }
        
}
