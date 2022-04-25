/**
 * Pranay Yadav
 * CS 4390 Spring 2022 UT Dallas
 * Socket Programming Project
 */
import java.util.Scanner;

public class SendThread extends Thread {
    /**
     * Class:   SendThread
     * Purpose: Send messages to other Client in a chat session.
     * Usage:   Create SendThread object and call the start() method to begin thread execution.
     */
    private Client client; // Associated client object
    private Scanner kbIn; // Scanner to retrieve keyboard input

    // Constructor accepts a Client object for whom to send messages for and a Scanner object to accept keyboard input.
    public SendThread(Client client, Scanner scanner) {
        this.client = client;
        this.kbIn = scanner;
    }
    
    // Implementation of Thread.run()
    public void run() {
        String message = "";
        int numAttempts = 0; // numAttempts used to exit chat if cannot contact other user within a reasonable number of tries.
        do {
            boolean status = true;
            //System.out.print("\n\t\t >>> ");
            System.out.print("\n\t[" + client.getFromUser() + "] >>> ");
            try {
                message = kbIn.nextLine(); // Get message from keyboard input.
                status = client.sendMessage(message); // Use Client object to send message in chat.
                if (message.contains("<q>")) // If the keyboard input is the special quit message, then quit this thread.
                    break;
                if (numAttempts >= 2) { // If cannot reach other user within 2 attempts, quit chat.
                    System.out.println();
                    System.out.println("\t<Other user may be unavailable. Please restart application.>");
                    break;
                }
                else if (status == false) { // If client failed to send message, then print the below and increment numAttempts.
                    System.out.println();
                    System.out.println("\t<Previous message may not have been delivered. Please re-send the message.>");
                    numAttempts++;
                }
                else if (!message.isEmpty()) { // Otherwise, if the message is not blank then print the message to screen.
                }
            } catch (Exception e) {
                break;
            }
        } while (!message.contains("<q>") && !interrupted());
        return;
    }

}
