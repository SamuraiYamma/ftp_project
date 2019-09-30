import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
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
            System.out.println("Server running on: " + serverSocket);
            System.out.println("--------------------------------------------------");
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
//    private File curDir = new File(System.getProperty(user.dir));
    Path path= FileSystems.getDefault().getPath(".");
    File curDir = path.toFile();
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

        curDir = getCurDir();

        while (true)
        {
            try {

                // Ask user what he wants
                dos.writeUTF("Enter command or enter HELP to view commands: \n");

                // receive the answer from client
                received = dis.readUTF();

                // write on output stream based on the
                // answer from the client

                if(received.compareTo("LIST") == 0){
                    System.out.println("LIST command from: " + s);
                    dos.writeUTF(ListFiles());

                    continue;
                }
                else if(received.contains("STORE")){
                    System.out.println("STORE command from: " + s);
                    //receive a file from client
                    ReceiveFile();
                    continue;
                }
                else if(received.contains("RETRIEVE")){
                    System.out.println("RETRIEVE command from: " + s);
                    //send a file to client
                    SendFile(seperateCommand(received));
                    continue;
                }
                else if(received.equalsIgnoreCase("QUIT")) {
                    System.out.println("Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
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

    //HELPER METHODS
    private String ListFiles() throws IOException {
        File[] fileList = curDir.listFiles();
        StringBuilder toReturn = new StringBuilder();
        for(File f : fileList){
            System.out.println(f.getName());
            if(f.isFile())
                toReturn.append(String.format("file: %s\n", f.getName()));
        }
        return toReturn.toString();
    }
    private File getCurDir(){
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
    private void SendFile(String filename) throws Exception
    {
        File f=new File(filename); //try the long way
        if(!f.exists()) {
            f = new File(curDir + "/" + filename);
            if (!f.exists()){
                dos.writeUTF("File Not Found");
                return;
            }
        }
        else {
            dos.writeUTF("READY");
            FileInputStream fin=new FileInputStream(f);
            int ch;
            do {
                ch=fin.read();
                dos.writeUTF(String.valueOf(ch));
            }
            while(ch!=-1);
            fin.close();
            dos.writeUTF("File Receive Successfully");
        }
    }

    private void ReceiveFile() throws Exception {
        String filename=dis.readUTF();
        if(filename.compareTo("File not found")==0) {
            return;
        }
        File f = new File (filename);
        if(!f.exists()){
            f=new File(curDir + "/" + filename); //This line can make a huge mess really easily
        }
        String option;

        if(f.exists()){

            dos.writeUTF("File Already Exists");
            option=dis.readUTF();
        }
        else {
            dos.writeUTF("SendFile");
            option="Y";
        }

        if(option.compareTo("Y")==0) {
            FileOutputStream fout=new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp=dis.readUTF();
                ch=Integer.parseInt(temp);
                if(ch!=-1) {
                    fout.write(ch);
                }
            }while(ch!=-1);
            fout.close();
            dos.writeUTF("File Sent Successfully");
        }
        else {
            return;
        }
    }
    private String seperateCommand(String command){
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
