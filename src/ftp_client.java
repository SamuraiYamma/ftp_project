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
            InetAddress ip = null;
            int port = 0;
            if(scn.next().equalsIgnoreCase("CONNECT")) {
                //get IP
                ip = convertToIP(scn.next());
                //get Port
                port = validatePort(scn.next());

            }
            else{
                System.exit(-1);
            }
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
                            break;
                        case "RETRIEVE":
                            recieveFile();
                            break;
                        case "STORE":
                            sendFile(scn.next(), dis, dos, scn);
                            break;
                        case "HELP":
                        case "?":
                        case "h":
                        case "-h":
                        case "help":
                            printHELP();
                            break;
                        default:
                            System.out.printf("Command: '%s' not recognized\n", tosend);
                            break;
                    }
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                    System.out.printf("File not found\n");
                } catch (IOException e){
                    e.printStackTrace();
                    System.out.printf("Could not get response from server");
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
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.printf("Connect reset by host\n");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("Could not establish socket: %s:%s\n", args[0], args[1]);
        } catch (Exception e){
            e.printStackTrace();
            System.out.printf("Not sure what went wrong\n");
        } finally {
            System.out.println("Terminating process...\n");
            System.exit(-1);
        }

    }
    //METHODS
    static void printHELP(){
        System.out.printf("\n--------------------------------------" +
                "\nHELP SECTION --- CLIENT COMMANDS\n" +
                "LIST\n" +
                "RETRIEVE <filename>\n" +
                "STORE <filename>\n" +
                "QUIT\n" +
                "--------------------------------------\n");
    }
    static void listFiles(){
    }
    static void recieveFile(){
    }
    static void sendFile(String filename, DataInputStream dis, DataOutputStream dos, Scanner scn) throws IOException {
        File file = new File(filename);
        if(!file.exists()){
            throw new FileNotFoundException();
        }
        //get input from server
        String response = dis.readUTF();
        if(response.equalsIgnoreCase("File already exists")){
            String choice;
            System.out.print("File already exists. Would you like to overwrite it? (Y/N)");
            choice = scn.next();
            //TODO: We should validate that it is a string
            switch (choice){
                case "Y":
                    //cool
                case "y":
                    //cool
                case "N":
                case "n":
                    dos.flush();
                    return;
                default:
                    System.out.print("Could not understand. Please enter Y or N.");
            }
            //get ready to send file
            FileOutputStream fos = new FileOutputStream(file);
            int ch;
            String tmp;
            do{
                tmp = dis.readUTF();
                ch = Integer.parseInt(tmp);
                if(ch != -1){
                    fos.write(ch);
                }
            }
            while(ch != -1);
            fos.close();
            //maybe show prompt conveying transfer success?
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
