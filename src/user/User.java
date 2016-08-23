/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package user;

import chatRoom.ChatRoom;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raghu
 */
public class User{
    
    BufferedReader inputStream;
    PrintStream outputStream;
    ChatRoom currentlyAssignedChatRoom;
    Socket socket;
    String name;
    
    public User(Socket socket) throws IOException{
        this.setSocketStreams(socket);
    }
    
    public String getName() {
        return name;
    }
    
    public BufferedReader getInputStream() {
        return inputStream;
    }
    
    public PrintStream getOutputStream() {
        return outputStream;
    }
    
    public void setSocketStreams(Socket socket) throws IOException {
        this.socket = socket;
        this.setStreams(socket.getInputStream(),socket.getOutputStream());
    }
    
    public void setStreams(InputStream inputStream,OutputStream outputStream){
        this.inputStream = new BufferedReader(new InputStreamReader(inputStream));
        this.outputStream = new PrintStream(outputStream);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void closeInputStream() throws IOException{
        inputStream.close();
    }
    
    public void closeOutputStream() throws IOException{
        outputStream.close();
    }
    
}
