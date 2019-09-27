import java.io.*;
import java.util.*;
import java.net.*;
public class ftp_server {
    public static void main(String[] args) {
        //valid number of arguments
        if(args.length != 1){
            System.out.println("usage - ftp_server <port>");
            System.exit(-1);
        }

        int port = validatePort(args[0]);
        Scanner scn = new Scanner(System.in);
        try{
            ServerSocket serverSocket = new ServerSocket(port);

//            //ACCEPT THE QUIT COMMAND
//            if(scn.nextLine().equalsIgnoreCase("QUIT")){
//                System.out.print("Force closing the server...\n");
//                System.exit(0);
//            }

            while (true)
            {
                Socket s = null;
                try
                {
                    // socket object to receive incoming client requests
                    s = serverSocket.accept();

                    System.out.println("A new client is connected : " + s);

                    // obtaining input and out streams
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                    System.out.println("Assigning new thread for this client");

                    // create a new thread object
                    Thread t = new ClientHandler(s, dis, dos);

                    // Invoking the start() method
                    t.start();
                }
                catch (Exception e){
                    s.close();
                    e.printStackTrace();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not open server");
        }

    }

    //HELPER METHODS
    private static int validatePort(String port){
        int p;
        try {
            p = Integer.parseInt(port);
            if (p < 1024 || p > 65535){
                System.out.println("Not a valid port\n" +
                        "Closing client...\n");
                System.exit(1);
            }
            else{
                return p;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return Integer.parseInt(null);
    }
}

// ClientHandler class
class ClientHandler extends Thread
{
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;


    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run()
    {
        String received;
        String toreturn;
        while (true)
        {
            try {

                // Ask user what he wants
                dos.writeUTF("Enter command or enter HELP to view commands: \n");

                // receive the answer from client
                received = dis.readUTF();

                if(received.equalsIgnoreCase("QUIT"))
                {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }

                /**
                 * Expected output streams are:
                 *  - File already exists
                 *  -
                 */
                // write on output stream based on the
                // answer from the client
                else if(received.substring(0,3).equalsIgnoreCase("LIST")){
                    //TODO create list function
                    dos.writeUTF("listing files...\n");
                }
                else if (received.substring(0,4).equalsIgnoreCase("STORE")){
                    //TODO create store function
                    String fileName = received.substring(5);
                    dos.writeUTF("storing " + fileName + "...");
                }
                else if (received.substring(0, 6).equalsIgnoreCase("RETRIEVE")){
                    //TODO create retrieve function
                    String fileName = received.substring(7);
                    dos.writeUTF("retrieving " + fileName + "...");
                }
                else{
                    dos.writeUTF("Invalid Input");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
