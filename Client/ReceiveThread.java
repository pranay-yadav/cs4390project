/**
 * Pranay Yadav
 * CS 4390 Spring 2022 UT Dallas
 * Socket Programming Project
 */
public class ReceiveThread extends Thread {
    /**
     * Class:   ReceiveThread
     * Purpose: Retrieve messages from the other Client in a chat session.
     * Usage:   Create a ReceiveThread object and call the start() method to run the thread.
     */
    private Client client; // Associated client object

    // Constructor accepts a Client object for whom to receive message from.
    public ReceiveThread(Client client) {
        this.client = client;
    }
    
    // Implementation of Thread.run()
    public void run() {
        String message = "";       
        do {
            try {
                message = client.receiveMessage(); // Use the Client object to receive any messages.

                if (message.contains("$QUIT") || message.contains("<q>")) { // If the message is a special quit indicator, then the other user has decide to quit the session.
                    System.out.println("\n\n\tUser " + client.getToUser() + " disconnected from the session. Press enter to exit...");
                    break;
                }
                else if (!message.isEmpty()) { // Otherwise, if the message is not blank, then print it out to screen.
                    System.out.print("\n\n\t[" + client.getToUser() + "] >>> " + message + "\n\n (continue message) >>> ");  
                }                
            } catch (Exception e) {
                break;
            }                
        } while (!message.equalsIgnoreCase("<q>") && !interrupted());
        return;
    }

}
