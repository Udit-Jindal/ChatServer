/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raghu
 */
public class Client {
    
    
    public Socket _socket;
    public String _name;
    public Thread _keyboardReadStream;
    public BufferedReader _inputFromStream;
    public Thread _userReadStream;
    
    public Client(String name) throws IOException {
        
        _socket = new Socket("localhost", 58000);
        
        this._name = name;
        
        System.out.println("Please use the following commands to connect to the server.\n"
        +"1. @SERVER SHOWROOMLIST => To view the list of all rooms"
        +"\n2. @SERVER SHOWUSERLIST => To view the list of all users"
        +"\n3. @SERVER SHOWUSERINROOMLIST {room name}=> To view the list of all users in the room"
        +"\n4. @SERVER CONNECTTOROOM {room name}=> To connect to a room or start a new one"
        +"\n5. @SERVER EXITROOM {room name} => To exit a room"
        +"\n6. @SERVER EXIT => To disconnect from the server"
        +"\n7. @{room name} {say something} => Will brodcast the chat to the room.");
        
        _keyboardReadStream = new ReadWriteStream(System.in,_socket.getOutputStream(),name,true);
        _userReadStream = new ReadWriteStream(_socket.getInputStream(),System.out,name,false);
        
        _keyboardReadStream.start();
        _userReadStream.start();
        
    }
}