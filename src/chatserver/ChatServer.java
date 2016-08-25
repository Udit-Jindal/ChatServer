/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package chatserver;

import chatRoom.ChatRoom;
import com.sun.xml.internal.ws.api.pipe.Engine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Raghu
 */
public class ChatServer {
    
    List<ChatRoom> chatRoomList;
    List<UserProxy> userProxyList;
    Engine engine;
    ServerSocket serverSocket;
    Thread userListener;
    
    String[] commandKeyWords = {"@SERVER"};
    
    public ChatServer() throws IOException{
        serverSocket = new ServerSocket(58000);
        chatRoomList = new ArrayList<>();
        userProxyList = new ArrayList<>();
        
        userListener = new ChatServer.UserListener();
        userListener.start();
    }
    
    boolean isEmpty(String string){
        if(string.equals("")
                || (string==null)
                || string.equalsIgnoreCase("null")
                || string.equals("\\r\\n")){
            return true;
        }else{
            return false;
        }
    }
    
    String getRoomListString(){
        String chatRoomListString = "";
        int i = 1;
        synchronized(chatRoomList){
            for(ChatRoom rm:chatRoomList){
                chatRoomListString = chatRoomListString + "\n"+i++
                        +". " +rm.getName()+"\n";
            }
        }
        return chatRoomListString;
    }
    
    String getUserListString(){
        String userListString = "";
        int i = 1;
        synchronized(userProxyList){
            for(UserProxy us:userProxyList){
                userListString = userListString + "\n"+i++
                        +". " +us.getName()+"\n";
            }
        }
        return userListString;
    }
    
    void addUser(UserProxy userProxy){
        synchronized(userProxyList)
        {
            userProxyList.add(userProxy);
        }
    }
    
    void addRoom(ChatRoom chatRoom){
        synchronized(chatRoomList)
        {
            chatRoomList.add(chatRoom);
        }
    }
    
    void removeUser(UserProxy user){
        synchronized(userProxyList)
        {
            userProxyList.remove(user);
        }
    }
    
    void removeRoom(ChatRoom chatRoom){
        synchronized(chatRoomList)
        {
            chatRoomList.remove(chatRoom);
        }
    }
    
    public ChatRoom getRoomFromName(String name){
        synchronized(chatRoomList){
            for(ChatRoom chatRoomObj:chatRoomList){
                if((chatRoomObj.getName()).equalsIgnoreCase(name)){
                    return chatRoomObj;
                }
            }
        }
        return null;
    }
    
    class UserListener extends Thread {
        
        @Override
        public void run(){
            while(true){
                try {
                    Socket socket= serverSocket.accept();
                    UserProxy userAction = new UserProxy(socket);
                    addUser(userAction);
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
    
    public class UserProxy extends Thread{
        
        BufferedReader inputStream;
        PrintStream outputStream;
        String userName;
        List<ChatRoom> userChatRoomList;
        static final String seperator = " ";
        static final String serverResponsePrefix = "#SERVER";
        
        public UserProxy(Socket socket) throws IOException{
            this.inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.outputStream = new PrintStream(socket.getOutputStream());
            userChatRoomList = new ArrayList();
        }
        
        @Override
        public void run(){
            try{
                while(true){
                    
                    String line = (inputStream.readLine()).trim();
                    if(!isEmpty(line))
                    {
                        
                        StringBuilder stringBuilder = new StringBuilder(line);
                        
                        String firstWord = getStartingWordOfInputString(stringBuilder);
                        if(checkIfCommand(firstWord)){
                            
                            //<editor-fold defaultstate="collapsed" desc="Protocol code">
                            
                            String secondWord = getStartingWordOfInputString(stringBuilder);
                            secondWord = secondWord.toUpperCase();
                            switch(secondWord){
                                
                                case "ADDME":{
                                    setName(getStartingWordOfInputString(stringBuilder));
                                }
                                break;
                                
                                case "SHOWROOMLIST":{
                                    giveResponseToClient(new String[]{"ROOMLIST",getRoomListString()});
                                }
                                break;
                                
                                case "SHOWUSERLIST":{
                                    giveResponseToClient(new String[]{"USERLIST",getUserListString()});
                                }
                                break;
                                
                                case "SHOWUSERINROOMLIST":{
                                    String roomName = getStartingWordOfInputString(stringBuilder);
                                    if(!isEmpty(roomName)){
                                        
                                        ChatRoom chatRoomRequested = getRoomFromName(roomName);
                                        if(chatRoomRequested != null){
                                            giveResponseToClient(new String[]{"USERLIST-IN-ROOM",chatRoomRequested.getUserListString()});
                                        }else{
                                            giveResponseToClient(new String[]{"USERLIST-IN-ROOM","ROOMNOTFOUND"});
                                        }
                                    }else{
                                        giveResponseToClient(new String[]{"USERLIST-IN-ROOM","ROOMNAMEEMPTY"});
                                        
                                    }
                                }
                                break;
                                
                                case "CONNECTTOROOM":{
                                    String roomName = getStartingWordOfInputString(stringBuilder);
                                    if(!isEmpty(roomName)){
                                        
                                        ChatRoom chatRoomRequested = getRoomFromName(roomName);
                                        if(chatRoomRequested == null){
                                            // Create new room
                                            
                                            ChatRoom chatRoom = new ChatRoom(roomName);
                                            chatRoom.addUser(this);
                                            addRoom(chatRoom);
                                            userChatRoomList.add(chatRoom);
                                            giveResponseToClient(new String[]{"NEWROOMCREATED",roomName,"USERLIST-IN-ROOM",chatRoom.getUserListString()});
                                        }else{
                                            //Join room
                                            
                                            chatRoomRequested.addUser(this);
                                            userChatRoomList.add(chatRoomRequested);
                                            giveResponseToClient(new String[]{"ROOMJOINED",roomName,"USERLIST-IN-ROOM",chatRoomRequested.getUserListString()});
                                            chatRoomRequested.writeToAllUsers(this,"#SERVER" ,"USERJOINED "+this.userName+" USERLIST-IN-ROOM "+chatRoomRequested.getUserListString());
                                            
                                        }
                                    }else{
                                        giveResponseToClient(new String[]{"CONNECTTOROOM","ROOMNAMEEMPTY"});
                                    }
                                }
                                break;
                                
                                case "EXITROOM":{
                                    String roomName = getStartingWordOfInputString(stringBuilder);
                                    if(!isEmpty(roomName)){
                                        ChatRoom chatRoomRequested = checkIfRoomExistsInUserList(roomName);
                                        if(chatRoomRequested != null){
                                            chatRoomRequested.removeUser(this);
                                            this.userChatRoomList.remove(chatRoomRequested);
                                            giveResponseToClient(new String[]{"USERLIST-IN-ROOM",chatRoomRequested.getUserListString()});
                                            
                                        }else{
                                            giveResponseToClient(new String[]{"EXITROOM","ROOMNOTFOUND"});
                                        }
                                    }else{
                                        giveResponseToClient(new String[]{"EXITROOM","ROOMNAMEEMPTY"});
                                    }
                                }
                                break;
                                
                                case "EXIT":{
                                    giveResponseToClient(new String[]{"EXITROOM","DISCONNECTED"});
                                    this.inputStream.close();
                                    this.outputStream.close();
                                }
                                break;
                                
                                default:
                                {
                                    giveResponseToClient(new String[]{"WRONGINPUT"});
                                }
                            }
                            
                            //</editor-fold>
                            
                        }
                        else{
                            //<editor-fold defaultstate="collapsed" desc="Sending chat to user">
                            if(firstWord.contains("@")){
                                firstWord = firstWord.replace("@", "");
                            }
                            
                            if(!isEmpty(firstWord)){
                                
                                ChatRoom requestedChatRoom = checkIfRoomExistsInUserList(firstWord);
                                
                                if(requestedChatRoom!=null)
                                {
                                    requestedChatRoom.writeToAllUsers(this,userName,stringBuilder.toString());
                                }
                                else{
                                    giveResponseToClient(new String[]{"CHAT","ROOMNOTFOUND"});
                                }
                            }else{
                                giveResponseToClient(new String[]{"CHAT","ROOMNAMEEMPTY"});
                            }
                            //</editor-fold>
                        }
                        
                        
                    }
                }
            } catch (SocketException ex) {
                
                removeUser(this);
            }catch(IOException ex){
                Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
            }
            finally{
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (Exception ex) {
                    System.out.println("Un-able to close the streams.");
                }
            }
        }
        
        public void writeMessage(String data){
            outputStream.println(data);
        }
        
        public String getStartingWordOfInputString(StringBuilder stringBuilder) throws StringIndexOutOfBoundsException,NullPointerException{
            
            int indexofFirstSpace = stringBuilder.indexOf(" ");
            
            if(indexofFirstSpace>0)
            {
                String firstWord = (String) stringBuilder.subSequence(0, indexofFirstSpace);
                
                stringBuilder.delete(0, indexofFirstSpace+1);
                
                return firstWord;
            }
            else{
                String str = stringBuilder.toString();
                stringBuilder.delete(0, stringBuilder.length());
                return str;
            }
        }
        
        boolean checkIfCommand(String firstWord){
            
            if(Arrays.asList(commandKeyWords).contains(firstWord)){
                return true;
            }else{
                return false;
            }
        }
        
        void giveResponseToClient(String[] data){
            
            StringBuilder sb = new StringBuilder(serverResponsePrefix);
            
            for(String s:data){
                sb.append(seperator).append(s);
            }
            
            outputStream.println(sb.toString());
        }
       
        ChatRoom checkIfRoomExistsInUserList(String roomName){
            for(ChatRoom chtrm:userChatRoomList){
                if(chtrm.getName().equalsIgnoreCase(roomName)){
                    return chtrm;
                }
            }
            return null;
        }
        
    }
    
}
