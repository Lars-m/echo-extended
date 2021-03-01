package file_read_write;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileReadWrite {

    public static void outputStreamTester() throws IOException {
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream("file.bin"));

        dataOut.writeUTF("Hello World");
        dataOut.writeInt(100);
        dataOut.writeDouble(345.45);
        dataOut.close();
        DataInputStream dataIn = new DataInputStream(new FileInputStream("file.bin"));

        System.out.println(dataIn.readUTF());
        System.out.println(dataIn.readInt());
        System.out.println(dataIn.readDouble());
        dataIn.close();
    }

    public static void writerStreamTester() throws IOException {
        PrintWriter pw = new PrintWriter(new FileOutputStream("file.txt"), true);

        pw.println("Hello World");
        pw.println(100);
        pw.println(345.45);
        pw.close();

        Scanner reader = new Scanner(new FileInputStream(("file.txt")));
        System.out.println(reader.nextLine());
        System.out.println(reader.nextLine());
        System.out.println(reader.nextLine());
        reader.close();
    }

    public static void main(String[] args) throws IOException {
        //outputStreamTester();
        writerStreamTester();

    }

}
