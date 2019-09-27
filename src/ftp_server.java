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
