import java.io.*;
import java.util.*;
import java.net.*;
public class ftp_client {
    public static void main(String[] args) {
        //WELCOME MESSAGE
        System.out.printf("------------ ftp_client ------------\n" +
                "GET STARTED - CONNECT <address> <port>\n");


        try {
            //scanner obj
            Scanner scn = new Scanner(System.in);


            //get IP
            InetAddress ip = convertToIP(args[0]);
            //get Port
            int port = validatePort(args[1]);

            //establish new socket connection
            Socket clientsocket = new Socket(ip, port);

            //obtain input and output streams
            DataInputStream dis = new DataInputStream(clientsocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientsocket.getOutputStream());

            //exchange between client and client handler
            while (true){
                System.out.println(dis.readUTF());
                String tosend = scn.next();
                dos.writeUTF(tosend);

                //Close connection upon receiving QUIT
                if(tosend.equalsIgnoreCase("QUIT")){
                    System.out.printf("Closing %s ...\n", clientsocket.toString());
                    clientsocket.close();
                    System.out.printf("Closed\n");
                    break;
                }

                //TODO: DOING STUFF AS REQUESTED BY CLIENT
                try {
                    switch (tosend) {
                        case "LIST":
                            listFiles();
                        case "RETRIEVE":
                            recieveFile();
                        case "STORE":
                            sendFile(scn.next());
                        case "HELP":
                            printHELP();
                        case "help":
                            printHELP();
                        case "-h":
                            printHELP();
                        case "h":
                            printHELP();
                        case "?":
                            printHELP();
                        default:
                            System.out.printf("Command: '%s' not recognized\n", tosend);
                    }
                }
                catch (FileNotFoundException e){
                e.printStackTrace();
                System.out.printf("File not found\n");
                } catch (Exception e){
                e.printStackTrace();
                System.out.printf("Not sure what went wrong\n");
                }
            }

            //closing stuff
            scn.close();
            dis.close();
            dos.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.printf("Could not find address: %s\nTerminating connection...\n", args[0]);
            System.exit(-1);
        } catch (FileNotFoundException e){
            e.printStackTrace();
            System.out.printf("File not found\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("Could not establish socket: %s:%s\n", args[0], args[1]);
        } catch (Exception e){
            e.printStackTrace();
            System.out.printf("Not sure what went wrong\n");
        } finally {
            System.out.println("Terminating process...");
            System.exit(-1);
        }

    }
    //METHODS
    static void printHELP(){
        System.out.printf("HELP SECTION --- CLIENT COMMANDS\n" +
                "CONNECT <address> <port>\n" +
                "LIST\n" +
                "RETRIEVE <filename>\n" +
                "STORE <filename>\n" +
                "QUIT\n");
    }
    static void listFiles(){
    }
    static void recieveFile(){
    }
    static void sendFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if(!file.exists()){
            throw new FileNotFoundException();
        }
    }


    //HELPER METHODS
    private static InetAddress convertToIP(String address) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(address);
        return ip;
    }
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
