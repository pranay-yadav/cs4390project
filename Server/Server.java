import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * Pranay Yadav
 * CS 4390 Spring 2022 UT Dallas
 * Socket Programming Project
 * 
 * Description: This file is the server portion of my application and must be run 
 *              first before the Client-side portion is run.
 *              Take the IP:Port address printed out to stdout and provide it as
 *              command line argument when running the ChatApp.java file.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class:   Server
 * Purpose: Run the server side logic for the Chat App.
 */
public class Server {
    /**
     * Main method. Runs the server logic in a while loop.
     */
    public static void main(String[] args) throws IOException, EOFException {
        int serverPort = 1600; // server port number
        ServerSocket serverSocket = new ServerSocket(serverPort); // Create TCP ServerSocket
        System.out.println("Server started on address:port >>> " + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort);
        boolean quitServer = false;
        HashMap<String, String> nameToIP = new HashMap<>(); // Will map usernames to IP Address & Port Number
        HashMap<String, String> waiting = new HashMap<>(); // Will contain <waiter, waitingFor> pairs of usernames

        while (!quitServer) { // Main loop to accept incoming connection requests.
            Socket clientSocket = serverSocket.accept(); // Accept incoming TCP connection

            /* Get associated output and input streams for the TCP connection */
            DataOutputStream clientOut = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream clientIn = new DataInputStream(clientSocket.getInputStream());
            clientSocket.setSoTimeout(250); // set timeout to 250 ms
            try {
                String rcvMessage = clientIn.readUTF(); // Read in the message from the TCP client
                /*  
                    On Initial Server contact, map the username to the IP Address that sent the username. 
                    Will be used to provide target address to clients attempting to connect to each other. 
                */
                if (rcvMessage.length() > 7 && rcvMessage.substring(0, 7).equals(("$HELLO:"))) { 
                    String[] rcvList = rcvMessage.split(":");
                    String username = rcvList[1];
                    String sessionAddress = rcvList[2] + ":" + rcvList[3];
                    nameToIP.put(username, sessionAddress);
                }
                /*
                    If server receives a $CONNECT request, it means the client is attempting to connect to another user.
                */
                else if (rcvMessage.length() > 9 && rcvMessage.substring(0, 9).equals("$CONNECT:")) {
                    String[] msgList = rcvMessage.split(":");
                    String fromUsername = msgList[1];
                    String toUsername = msgList[2];
                    if (!nameToIP.containsKey(toUsername) || !waiting.containsKey(toUsername)) { // Other user has not started their app or has not requested to join session
                        clientOut.writeUTF("$WAIT"); // Send $WAIT message to Client to signal to them to wait for connection
                        waiting.put(fromUsername, toUsername);
                    }
                    else if (waiting.containsKey(toUsername) && waiting.get(toUsername).equals(fromUsername)) { // Other user is already waiting, let incoming Client know what address to target. 
                        clientOut.writeUTF("$ADDRESS:" + nameToIP.get(toUsername));
                        waiting.remove(toUsername);
                        waiting.remove(fromUsername);
                    }
                }
                else if (rcvMessage.length() > 6 && rcvMessage.substring(0,6).equals("$QUIT:")) { // User has requested to manually remove their name from waiting list
                    String[] msgList = rcvMessage.split(":");
                    String username = msgList[1];
                    waiting.remove(username); // Remove the user from the waiting HashMap
                }
                // Close socket and streams
                clientIn.close();
                clientOut.close();
                clientSocket.close();
            } catch (Exception e) {
                clientIn.close();
                clientOut.close();
                clientSocket.close();
            }
        }
        serverSocket.close();
    }
}