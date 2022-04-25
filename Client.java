/**
 * Pranay Yadav
 * CS 4390 Spring 2022 UT Dallas
 * Socket Programming Project
 */
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Client {
    /**
     * Class:   Client
     * Purpose: Provide an object that handles all client-side functionality in the chat program.
     * Usage:   Create a Client object with a username, a target server hostname, and the server port number. 
     *          Use this Client object to establish/end chat sessions, and send/receive messages while inside a session.
     */

    // Server Information.
    private String serverDomain;
    private int serverPort;
    // If this client intitializes session, it makes use of a ServerSocket object. 
    private ServerSocket thisSocket;
    private int thisPort;
    private String thisAddress;
    // If this client does not initialize the session, it makes use of a client Socket object.
    private Socket otherSocket;
    private int otherPort;
    private String otherAddress;
    private DataInputStream otherIn; 
    private DataOutputStream otherOut;

    // Participating usernames.
    private String thisUser;
    private String otherUser;

    // Other helper variables.
    private final int MAX_ATTEMPTS = 1;
    private final int TIMEOUT = 20*1000;
    private boolean connected;
    private boolean session;

    /**
     * Constructor for Client Object.
     * Accepts a username, a server hostname, and a server port number as arguments.
     */
    public Client(String thisUser, String serverDomain, int serverPort) throws IOException {
        this.serverDomain = serverDomain;
        this.serverPort = serverPort; 
        this.thisUser = thisUser;
        this.connected = false;
        this.session = false;
        this.thisSocket = new ServerSocket(0);
        this.thisPort = thisSocket.getLocalPort();
        this.thisAddress = InetAddress.getLocalHost().getHostAddress();
        int numAttempts = 0;
        do {
            this.connected = initialServerContact();
            numAttempts++;
        } while (!this.connected && numAttempts < MAX_ATTEMPTS);

    }

    // Returns True if this Client object is able to contact the server.
    public boolean isConnected() {
        return connected;
    }  

    // Returns True if this Client object is currently in a session with another Client object.
    public boolean inSession() {
        return session;
    }

    // Return the name of the other user.
    public String getToUser() {
        return otherUser;
    }
    
    // Return the name of this Client object's user.
    public String getFromUser() {
        return thisUser;
    }

    // Helper method to establish initial contact with server. Provides this Client's IP Address and Port Number for use on Server-side.
    private boolean initialServerContact() {
        try {
            Socket serverSocket = new Socket(serverDomain, serverPort); // Open TCP connection to server.
            DataOutputStream serverOut = new DataOutputStream(serverSocket.getOutputStream());
            serverSocket.setSoTimeout(TIMEOUT);
            serverOut.writeUTF("$HELLO:" + thisUser + ":" + thisAddress + ":" + thisPort); // Let server know to associate this username with this IP Address and Port Number
            serverOut.close();
            serverSocket.close(); // Close connection to server.
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    // Helper method to signal to server when this Client is not able to connect with another Client.
    private boolean setAvailableStatus() {
        try {
            if (!otherUser.isEmpty()) {
                Socket serverSocket = new Socket(serverDomain, serverPort); // Open TCP Connection to server.
                DataOutputStream serverOut = new DataOutputStream(serverSocket.getOutputStream());
                serverOut.writeUTF("$QUIT" + ":" + thisUser); // Tell server that this Client is now free to connect with other Clients.
                serverOut.close();
                serverSocket.close(); // Close TCP connection to server.
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Method to establish a chat session with the given otherUser.
    public boolean establishSession(String otherUser) {
        try {
            Socket serverSocket = new Socket(serverDomain, serverPort); // Open connection to server.
            DataOutputStream serverOut = new DataOutputStream(serverSocket.getOutputStream());
            DataInputStream serverIn = new DataInputStream(serverSocket.getInputStream());
            serverOut.writeUTF("$CONNECT:" + thisUser + ":" + otherUser); // Let server know that this Client is attempting to connect to the other Client.
            serverSocket.setSoTimeout(TIMEOUT);
            String rcvMessage = serverIn.readUTF();
            if (rcvMessage.equals("$WAIT")) { // If server tells this Client to wait, it means other Client is not yet ready to connect.
                this.otherUser = otherUser;
                thisSocket.setSoTimeout(TIMEOUT);
                otherSocket = thisSocket.accept(); // Open a TCP ServerSocket that will accept other Client's connection.
                session = true;
                otherOut = new DataOutputStream(otherSocket.getOutputStream());
                otherIn = new DataInputStream(otherSocket.getInputStream());
            }
            else if (rcvMessage.contains("$ADDRESS:")) { // If server provides an address of the other Client, it means they are ready to connect.
                String[] rcvList = rcvMessage.split(":");
                otherAddress = rcvList[1];
                otherPort = Integer.parseInt(rcvList[2]);
                otherSocket = new Socket(otherAddress, otherPort); // Create a client-side TCP Socket to connect to the other Client.
                this.otherUser = otherUser;
                session = true;
                otherOut = new DataOutputStream(otherSocket.getOutputStream());
                otherIn = new DataInputStream(otherSocket.getInputStream());
            }
            serverOut.close();
            serverIn.close();
            serverSocket.close(); // Close TCP connection to server (not other Client).
            return inSession();
        } catch (Exception e) {
            return false;
        }
    }

    // Send a message to the other Client in the chat session.
    public boolean sendMessage(String message) {
        if (inSession() && !otherSocket.isClosed()) {
            try {
                otherOut.writeUTF(message); // Write message to DataOutputStream object associated with the TCP connection.               
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }   
        return false;
    }

    // Retrieve any message sent by other Client in the chat session.
    public String receiveMessage() {
        if (inSession() && !otherSocket.isClosed()) {
            try {
                String message = "";
                message = otherIn.readUTF(); // Read in message from DataInputStream object associated with the TCP connection.
                return message;
            }
            catch (Exception e) {
                return "";
            }
        }  
        return "";
    }

    // End the chat session. Takes care of closing all associated streams and connections.
    public boolean endSession() {
        try {
            setAvailableStatus(); // Signal to the server that this Client is free to accept other connections.
            /* Reset all chat-associated variables and close streams/sockets as needed. */
            otherUser = ""; 
            otherAddress = "";
            otherPort = 0;
            otherIn.close();
            otherOut.close();
            otherSocket.setSoTimeout(0);
            otherSocket.close();
            thisSocket.close();
            return otherSocket.isClosed() && thisSocket.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

}