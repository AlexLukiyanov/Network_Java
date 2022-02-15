package com.geekbrains.cloud.server;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;

public class CloudFileHandler implements Runnable {

    private static final int BUFFER_SIZE = 8192;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final byte[] buf;
    private File serverDirectory;
    private File currentDirectory;

    public ListView<String> clientView;
    public ListView<String> serverView;

    public CloudFileHandler(Socket socket) throws IOException {
        System.out.println("Client connected!");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        buf = new byte[BUFFER_SIZE];
        serverDirectory = new File("server");
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();

                if ("#file_message#".equals(command)) {
                    String name = is.readUTF();
                    long size = is.readLong();
                    File newFile = serverDirectory.toPath().resolve(name).toFile();

                    try (OutputStream fos = new FileOutputStream(newFile)) {
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int readCount = is.read(buf);
                            fos.write(buf, 0, readCount);
                        }
                    }

                    System.out.println("File: " + name + " is uploaded");
                } else if ("#file_message_down#".equals(command)) {
                    String name = is.readUTF();
                    long size = is.readLong();
                    File newFile = serverDirectory.toPath().resolve(name).toFile();
                    try (InputStream fis = new FileInputStream(newFile)) {
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            //while (fis.available() > 0) {
                                int writeCount = is.read(buf);
                                os.write(buf, 0, writeCount);
                            }
                        }
                    //}
                    System.out.println("File: " + name + " is downloaded");
                } else {
                    System.err.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Добавлено для решения задачи загрузки файлов с сервера // не закончено
        /* try {
            while (true) {
                String command = is.readUTF();
                if ("#file_message#".equals(command)) {
                    String name = is.readUTF();
                    long size = is.readLong();
                    File newFile = clientDirectory.toPath().resolve(name).toFile();
                    try (InputStream fis = new FileInputStream(newFile)) {
                        for (int i = 0; i < (size + BUFFER_SIZE - 1) / BUFFER_SIZE; i++) {
                            int writeCount = is.read(buf);
                            fis.read(buf, 0, writeCount);
                        }
                    }

                    System.out.println("File: " + name + " is downloaded");
                } else {
                    System.err.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } */

    }
}

