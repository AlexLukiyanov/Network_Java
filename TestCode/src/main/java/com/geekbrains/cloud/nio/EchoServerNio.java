package com.geekbrains.cloud.nio;

import javax.xml.bind.SchemaOutputResolver;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class EchoServerNio {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buf;


    public EchoServerNio() throws Exception {
        buf = ByteBuffer.allocate(5);
        serverSocketChannel = ServerSocketChannel.open();
        selector = Selector.open();

        serverSocketChannel.bind(new InetSocketAddress(8189));

        System.out.println("Server started");

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocketChannel.isOpen()) {

            selector.select();

            Set<SelectionKey> keys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey currentKey = iterator.next();
                if (currentKey.isAcceptable()) {
                    handleAccept();
                }
                if (currentKey.isReadable()) {
                    handleRead(currentKey);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey currentKey) throws IOException {

        SocketChannel channel = (SocketChannel) currentKey.channel();
        StringBuilder reader = new StringBuilder();

        while (true) {
            int count = channel.read(buf);

            if (count == 0) {
                break;
            }

            if (count == -1) {
                channel.close();
                return;
            }

            buf.flip();

            while (buf.hasRemaining()) {
                reader.append((char) buf.get());
            }
            buf.clear();
        }

        String titr = "->";
        channel.write(ByteBuffer.wrap(titr.getBytes(StandardCharsets.UTF_8)));

        int positionSpace = reader.indexOf(" ");
        StringBuilder command = new StringBuilder(reader.substring(0, positionSpace));
        StringBuilder meaning = new StringBuilder(reader.substring(positionSpace + 1));

        String messageCommand = command.toString();
        String messageMeaning = meaning.toString();


        String cat = "cat";
        String ls = "ls";
        String touch = "touch";
        String mkdir = "mkdir";
        String cd = "cd";
        String help = "--help";

        // Работает но при вводе "--help ".
        if (messageCommand.startsWith(help)) {
            String commands = "Commands:\n" +
                    "1. ls - выводит список файлов на экран\n" +
                    "2. cd path - перемещается из текущей папки в папку из аргумента\n" +
                    "3. cat file - печатает содержание текстового файла на экран\n" +
                    "4. mkdir dir - создает папку в текущей директории\n" +
                    "5. touch file - создает пустой файл в текущей директории";
            channel.write(ByteBuffer.wrap(commands.getBytes(StandardCharsets.UTF_8)));
        }

        //ls - выводит список файлов на экран
        if (messageCommand.startsWith(ls)) {
            String[] catalog;
            File file = new File("server", "dir1");
            catalog = file.list();
            for (String c: catalog) {

            byte[] bytes = Files.readAllBytes(Paths.get(c));
            channel.write(ByteBuffer.wrap(bytes));
        }
        }

        //touch file - создает пустой файл в текущей директории
        // (работает, но только при переходе с командной сроки в IntelliJ, видимо обновляется)
        if (messageCommand.startsWith(touch)) {
            Path path = Paths.get("server", "dir1", messageMeaning);
            try {
                Path newFile = Files.createFile(path);
            } catch(FileAlreadyExistsException e){
                System.out.println("Файл с таким именем уже существует!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // cat file - печатает содержание текстового файла на экран
        // Работает если в .resolve передать "1.txt"
        if (messageCommand.startsWith(cat)) {
            Path path = Paths.get("server", "dir2").resolve("1.txt");
            byte[] bytes = Files.readAllBytes(path);
            channel.write(ByteBuffer.wrap(bytes));
        }

        //mkdir dir - создает папку в текущей директории
        // (работает, но только при переходе с командной сроки в IntelliJ, видимо обновляется)
        if (messageCommand.startsWith(mkdir)) {
            Path path = Paths.get("server", "dir1", messageMeaning);
            try {
                Path newDir = Files.createDirectories(path);
            } catch(FileAlreadyExistsException e){
                System.out.println("Папка с таким именем уже существует!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //cd path - перемещает файл из текущей папки в папку из аргумента
        // (работает, но только при переходе с командной сроки в IntelliJ, видимо обновляется)
        if (messageCommand.startsWith(cd)) {
            Path sourcePath = Paths.get("server","dir1");
            Path destinationPath = Paths.get("server","dir2", messageMeaning);
            try {
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAccept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");

        String helloMessage = "\n" +
                "Welcome in Mike terminal\n" +
                "\n" +
                "input --help to show command list\n" +
                "->";
        socketChannel.write(ByteBuffer.wrap(helloMessage.getBytes(StandardCharsets.UTF_8)));

    }

    public static void main(String[] args) throws Exception {
        new EchoServerNio();
    }
}
