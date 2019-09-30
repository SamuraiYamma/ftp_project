import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.net.*;
public class ftp_client {
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static BufferedReader br;

    private static Path path= FileSystems.getDefault().getPath(".");
    private static File curDir = path.toFile();

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
            //get cur dir
            curDir = getCurDir();

            //establish new socket connection
            Socket clientsocket = new Socket(ip, port);

            //obtain input and output streams
            dis = new DataInputStream(clientsocket.getInputStream());
            dos = new DataOutputStream(clientsocket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));

            //exchange between client and client handler
            while (true){
                System.out.println(dis.readUTF());
                String tosend = scn.nextLine();
                dos.writeUTF(tosend);

                //TODO: DOING STUFF AS REQUESTED BY CLIENT
                try {
                    if(tosend.equalsIgnoreCase("LIST")){
                        System.out.println("Retrieving list data...");
                        //print the retrieved files
                        System.out.println(dis.readUTF());
                    }
                    else if (tosend.contains("STORE")){
                        System.out.println("Preparing to store...");
                        //send file to the server
                        sendFile(seperateCommand(tosend));
                        //break apart input and retrieve file location
                    }
                    else if (tosend.contains("RETRIEVE")){
                        System.out.println("Preparing to receive file...");
                        //receive a file from server
                        receiveFile(seperateCommand(tosend));
                        //break apart input and retrieve file name
                    }
                    else if(tosend.equalsIgnoreCase("QUIT")){
                        System.out.printf("Closing %s ...\n", clientsocket.toString());
                        clientsocket.close();
                        System.out.printf("Closed\n");
                        break;
                    }
                    else if (tosend.contains("HELP")){
                        printHELP();
                    }
                }
                catch (Exception e){
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
        System.out.printf("\n--------------------------------------\n" +
                "HELP SECTION --- CLIENT COMMANDS\n" +
                "--------------------------------------\n" +
                "LIST\n" +
                "RETRIEVE <filename>\n" +
                "STORE <filename>\n" +
                "QUIT\n" +
                "--------------------------------------\n");
    }
    static void listFiles(){
    }
    static void receiveFile(String fileName) throws IOException {

        dos.writeUTF(fileName);
        String msgFromServer=dis.readUTF();

        if(msgFromServer.compareTo("File Not Found")==0)
        {
            System.out.println("File not found on Server ...");
            return;
        }
        else if(msgFromServer.compareTo("READY")==0)
        {
            System.out.println("Receiving File ...");
            File f=new File( curDir + "/" + fileName); //this hotfix can lead to some serious issues down the road
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option=br.readLine();
                if(Option=="N")
                {
                    dos.flush();
                    return;
                }
            }
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do
            {
                temp=dis.readUTF();
                ch=Integer.parseInt(temp);
                if(ch!=-1)
                {
                    fout.write(ch);
                }
            }while(ch!=-1);
            fout.close();
            System.out.println(dis.readUTF());

        }


    }
    static void sendFile(String filename) throws IOException {

        File f=new File(filename); //try the long way
        if(!f.exists()) {
            f = new File(curDir + "/"+ filename); //try the shorthand way
            if (!f.exists()) {
                System.out.println("Cannot find file...");
                dos.writeUTF("File not found");
                return;
            }
        }


        dos.writeUTF(filename); //send message to server

        String msgFromServer=dis.readUTF();
        if(msgFromServer.compareTo("File Already Exists")==0)
        {
            String Option;
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
            Option=br.readLine();
            if(Option=="Y")
            {
                dos.writeUTF("Y");
            }
            else
            {
                dos.writeUTF("N");
                return;
            }
        }

        System.out.println("Sending File ...");
        FileInputStream fin=new FileInputStream(f);
        int ch;
        do
        {
            ch=fin.read();
            dos.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        fin.close();
        System.out.println(dis.readUTF());

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
    private static File getCurDir(){
        //search current
        String name = "ftp_server.java";
        File[] fileList = curDir.listFiles();
        for(File f:fileList){
            if(f.isDirectory()){
                File[] dir = f.listFiles();
                for (File target:dir){
                    if (target.isFile() && target.getName().equalsIgnoreCase(name)) {
//                        System.out.println("curDir: " + f); //testing
                        return f;
                    }
                }
            }
        }
        return curDir;
    }
    private static String seperateCommand(String command){
        String filename;
        if (command.contains("STORE")){
            filename = command.replace("STORE ","");
        }
        else if (command.contains("RETRIEVE")){
            filename = command.replace("RETRIEVE ", "");
        }
        else return command;
        return filename;
    }
}
