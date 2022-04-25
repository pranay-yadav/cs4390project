/**
 * Pranay Yadav
 * CS 4390 Spring 2022 UT Dallas
 * Socket Programming Project
 * 
 * Description: This file is the main executable file for the client side portion of my project.
 *              First run the Server.java file. Take the <IP>:<Port> portion of the output to stdout
 *              and provide it as a command line argument to this file. For example:
 *                  $ javac ChatApp.java
 *                  $ java ChatApp 192.168.1.1:1600
 *              or if this entire package is combined into a .jar file:
 *                  $ java -jar ./Client.jar 192.169.1.1:1600
 */
import java.util.Scanner;
import java.io.IOException;

public class ChatApp {
    /**
     * Class:   ChatApp
     * Purpose: Main routine to run a chat application between two users.
     *          Responsible for the flow of the application, from collecting user information, 
     *          creating Client objects, and managing the Send and Receive Threads.
     */

    // Useful variables
    private static final String BANNER = "+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+";
    private static String serverDomain = "localhost";
    private static int serverPort = 1600;
    private static Scanner kbIn;

    // Main method. Entry point into the application.
    public static void main(String[] args) throws IOException {
        /* Parse command line args (if any) */
        if (args.length > 0) {
            String[] arg1 = args[0].split(":");
            if (arg1.length > 0) {
                serverDomain = arg1[0];
                serverPort = Integer.parseInt(arg1[1]);
            }
        }
        kbIn = new Scanner(System.in);
        boolean status = runChat(); // runChat() takes care of the flow of the chat sessions. Returns true if everything went well, otherwise false.
        if (status == false) // Something went wrong
            System.out.println("Something went wrong, please try again. Exiting Chat App.");
        else // Everything went ok
            System.out.println("Exiting Chat App. Goodbye!");
        System.out.println(BANNER);
        kbIn.close(); // Close Scanner and exit application.
    }

    // Chat application logic and flow. Returns true if everything goes well, false otherwise.
    private static boolean runChat() {
        try {
            // Application Intro
            System.out.println(BANNER);
            System.out.println("Welcome to the Chat App!\n\tAt any point, enter \'<q>\' to quit the app or exit the chatroom.");
            String username = getInput(kbIn, "Enter your username to begin >>> ");
            if (isQuit(username)) // check for special '<q>' quit message.
                return true;
                       
            System.out.println(BANNER);
            System.out.println("Welcome " + username + "!");

            Client client = new Client(username, serverDomain, serverPort); // Instantiate new Client object for this user
            String otherUsername = "";

            if (!client.isConnected()) { // If the client could not contact server, return false.
                System.out.println("There was an issue contacting the server. Please try again later.");
                return false;
            }

            // Main Chat Loop
            do {
                otherUsername = getInput(kbIn, "\tWho would you like to chat with? (enter \'<q>\' to quit) >>> "); // Get the target user to chat with.
                if (isQuit(otherUsername)) 
                    return true;

                if (username.equals(otherUsername)) { // Quick check to make sure this user does not message themselves.
                    System.out.println("You cannot message yourself. Please enter a valid username to message.");
                    System.out.println(BANNER);
                    continue;
                }

                System.out.println("\tWaiting for " + otherUsername + " to join session...");
                boolean sessionStatus = client.establishSession(otherUsername); // Use the Client object to establish a chat session.
                
                if (!sessionStatus) { // If unable to establish a session, go back to beginning of loop.
                    System.out.println("Unable to establish session. " + otherUsername + " may not be online. Press enter to try again, or <q> to exit.");
                    client.endSession();
                    String dummy = kbIn.nextLine();
                    if (isQuit(dummy))
                        break;
                    System.out.println(BANNER);
                    continue; 
                }

                System.out.println(BANNER);
                System.out.println("\tBeginning chat with " + otherUsername + "\n");
                
                /* Instantiate Send and Receive Threads for this Client object. */
                SendThread sendThread = new SendThread(client, kbIn);
                ReceiveThread receiveThread = new ReceiveThread(client);
               
                sendThread.start();
                receiveThread.start();

                while (receiveThread.isAlive() && sendThread.isAlive()) {
                    // Let the sender and reciever threads run. Don't do anything on main thread.
                    Thread.sleep(200);
                }
                // Once finished, interrupt both Threads.
                sendThread.interrupt();
                receiveThread.interrupt();
                try {
                    Thread.sleep(200); // Allow threads to fully complete interrupt() call process.
                } catch (InterruptedException e) { // An error occured while attempting to exit the session.
                    System.out.println("There was an issue exiting the chatroom.");
                }

                System.in.skip(1); // Skip any leftover bytes from SendThread's Scanner.
                
                System.out.println("\n\tExiting chatroom with " + otherUsername);
                client.endSession(); // Client object will close involved sockets and streams.
                System.out.println(BANNER);

            } while (!isQuit(otherUsername));
            // End of session, return true since everything went well
            return true;
        } catch (Exception e) {
            // If something goes wrong, return false
            //System.out.println(e.getMessage());
            return false;
        }
    }

    // Helper method to get input from the screen with a prompt.
    private static String getInput(Scanner scan, String prompt) {
        try {
            System.out.print(prompt);
            String input = "";
            input = scan.nextLine();
            return input;
        } catch (Exception e) {
            return "";
        }
        
    }

    // Helper method to determine if a string is the quit message.
    private static boolean isQuit(String str) {
        return str.equalsIgnoreCase("<q>");
    }   
    

} 


