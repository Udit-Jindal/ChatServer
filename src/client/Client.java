/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

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
//        System.out.println("Please start chating.");
        this._name = name;
        
        _keyboardReadStream = new ReadWriteStream(System.in,_socket.getOutputStream(),name,true);
        _userReadStream = new ReadWriteStream(_socket.getInputStream(),System.out,name,false);
        
        _keyboardReadStream.start();
        _userReadStream.start();
        
    }
}