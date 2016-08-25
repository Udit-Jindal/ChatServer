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
    ChatRoom chatRoom;
//    String chatRoomListString;
//    String userListString;
    
    String[] commandKeyWords = {"@SERVER"};
    
    public ChatServer() throws IOException{
        serverSocket = new ServerSocket(58000);
        chatRoomList = new ArrayList<>();
        userProxyList = new ArrayList<>();
        
        userListener = new ChatServer.UserListener();
        userListener.start();
    }
    
    private String getRoomListString(){
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
    
    private String getUserListString(){
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
    
    private void addUser(UserProxy userProxy){
        synchronized(userProxyList)
        {
            userProxyList.add(userProxy);
        }
    }
    
    private void addRoom(ChatRoom chatRoom){
        synchronized(chatRoomList)
        {
            chatRoomList.add(chatRoom);
        }
    }
    
    private void removeUser(UserProxy user){
        synchronized(userProxyList)
        {
            userProxyList.remove(user);
        }
    }
    
    private void removeRoom(ChatRoom chatRoom){
        synchronized(chatRoomList)
        {
            chatRoomList.remove(chatRoom);
        }
    }
    public ChatRoom getRoomFromName(String name){
        ChatRoom localChatRoom = null;
        try{
            synchronized(chatRoomList){
                for(ChatRoom chatRoomObj:chatRoomList){
                    localChatRoom = chatRoomObj;
                    if((localChatRoom.getName()).equalsIgnoreCase(name)){
                        return localChatRoom;
                    }else{
                        localChatRoom = null;
                    }
                }
            }
        }catch(NullPointerException ex){
            System.out.println(localChatRoom.getName()+" disconnected.");
            removeRoom(localChatRoom);
        }
        return localChatRoom;
    }
    
    class UserListener extends Thread {
        
        Thread userAction;
        
        @Override
        public void run(){
            while(true){
                try {
                    Socket socket= serverSocket.accept();
                    userAction = new UserProxy(socket);
                    addUser((UserProxy)userAction);
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
//        ChatRoom currentlyAssignedChatRooms;
        String userName;
        List<ChatRoom> userChatRoomList;
        StringBuilder stringBuilder;
        static final String seperator = " ";
        static final String serverResponsePrefix = "#SERVER";
        
        public UserProxy(Socket socket) throws IOException{
            this.setSocketStreams(socket);
            userChatRoomList = new ArrayList();
        }
        
        public void setSocketStreams(Socket socket) throws IOException {
            this.setStreams(socket.getInputStream(),socket.getOutputStream());
        }
        
        public void setStreams(InputStream inputStream,OutputStream outputStream){
            this.inputStream = new BufferedReader(new InputStreamReader(inputStream));
            this.outputStream = new PrintStream(outputStream);
        }
        
        public String getUserName() {
            return userName;
        }
        
        public BufferedReader getInputStream() {
            return inputStream;
        }
        
        public PrintStream getOutputStream() {
            return outputStream;
        }
        
        public void setUserName(String name) {
            this.userName = name;
        }
        
        
        public void closeInputStream() throws IOException{
            inputStream.close();
        }
        
        public void closeOutputStream() throws IOException{
            outputStream.close();
        }
        
        @Override
        public void run(){
            try{
                while(true){
                    
                    String line = (getInputStream().readLine()).trim();
                    if(!isEmpty(line))
                    {
                        
                        stringBuilder = new StringBuilder(line);
                        
                        String firstWord = getStartingWordOfInputString();
                        if(checkIfCommand(firstWord)){
                            processCommand();
                        }
                        else{
                            sendChat(firstWord);
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
                    this.closeInputStream();
                    this.closeOutputStream();
                } catch (Exception ex) {
                    System.out.println("Un-able to close the streams.");
                }
            }
        }
        
        public String getStartingWordOfInputString() throws StringIndexOutOfBoundsException,NullPointerException{
            
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
        
        void processCommand() throws IOException{
            
            String secondWord = getStartingWordOfInputString();
            secondWord = secondWord.toUpperCase();
            switch(secondWord){
                
                case "ADDME":{
                    setName(getStartingWordOfInputString());
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
                    String roomName = getStartingWordOfInputString();
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
                    String roomName = getStartingWordOfInputString();
                    if(!isEmpty(roomName)){
                        
                        ChatRoom chatRoomRequested = getRoomFromName(roomName);
                        if(chatRoomRequested == null){
                            // Create new room
                            
                            chatRoom = new ChatRoom(roomName);
                            chatRoom.addUser(this);
                            addRoom(chatRoom);
                            userChatRoomList.add(chatRoom);
                            giveResponseToClient(new String[]{"NEWROOMCREATED",roomName,"USERLIST-IN-ROOM",chatRoom.getUserListString()});
                        }else{
                            //Join room
                            
                            chatRoomRequested.addUser(this);
                            userChatRoomList.add(chatRoomRequested);
                            giveResponseToClient(new String[]{"ROOMJOINED",roomName,"USERLIST-IN-ROOM",chatRoom.getUserListString()});
                            writeToAllClientsOfRoom(chatRoomRequested,"#SERVER" ,"USERJOINED "+this.getUserName()+" USERLIST-IN-ROOM "+chatRoom.getUserListString());
                            
                        }
                    }else{
                        giveResponseToClient(new String[]{"CONNECTTOROOM","ROOMNAMEEMPTY"});
                    }
                }
                break;
                
                case "EXITROOM":{
                    String roomName = getStartingWordOfInputString();
                    if(!isEmpty(roomName)){
                        ChatRoom chatRoomRequested = getRoomFromName(roomName);
                        if(chatRoomRequested != null){
                            //Check if user is in that room.
                            
                            if(chatRoomRequested.chatRoomUserList.contains(this)){
                                chatRoomRequested.removeUser(this);
                                this.userChatRoomList.remove(chatRoomRequested);
                                giveResponseToClient(new String[]{"USERLIST-IN-ROOM",chatRoomRequested.getUserListString()});
                            }else{
                                giveResponseToClient(new String[]{"USERNOTINROOM"});
                            }
                            
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
        }
        
        void sendChat(String roomName){
            if(roomName.contains("@")){
                roomName = roomName.replace("@", "");
            }
            
            if(!isEmpty(roomName)){
                
                ChatRoom requestedChatRoom = getRoomFromName(roomName);
                
                if(requestedChatRoom!=null)
                {
                    if(requestedChatRoom.chatRoomUserList.contains(this))
                        writeToAllClientsOfRoom(requestedChatRoom,this.getUserName(),stringBuilder.toString());
                }
                else{
                    giveResponseToClient(new String[]{"CHAT","ROOMNOTFOUND"});
                }
            }else{
                giveResponseToClient(new String[]{"CHAT","ROOMNAMEEMPTY"});
            }
        }
        
        void giveResponseToClient(String[] data){
            
            StringBuilder sb = new StringBuilder(serverResponsePrefix);
            
            for(String s:data){
                sb.append(seperator).append(s);
            }
            
            getOutputStream().println(sb.toString());
        }
        
        public void writeToAllClientsOfRoom(ChatRoom chatRoom,String messageFrom,String line){
            UserProxy localUser = null;
            try{
                synchronized(chatRoom.chatRoomUserList){
                    for(UserProxy userObj:chatRoom.chatRoomUserList){
                        localUser = userObj;
                        if(!userObj.equals(this)){
                            userObj.getOutputStream().println(messageFrom+":-"+line);
                        }
                    }
                }
            }catch(NullPointerException ex){
                System.out.println(localUser.getUserName()+" disconnected.");
                chatRoom.removeUser(localUser);
                removeUser(this);
                try {
                    this.inputStream.close();
                    this.outputStream.close();
                } catch (IOException ex1) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        
    }
    
    boolean isEmpty(String string){
        if(string.equals("")
                || string==null
                || string.equalsIgnoreCase("null")
                || string.equals("\\r\\n")){
            return true;
        }else{
            return false;
        }
    }
    
}
