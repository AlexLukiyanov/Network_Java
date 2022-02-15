package com.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final int BUFFER_SIZE = 8192;

    public TextField clientPath;
    public TextField serverPath;
    public ListView<String> clientView;
    public ListView<String> serverView;
    private File currentDirectory;
    private File serverCurrentDirectory;

    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;

    // Platform.runLater(() -> {})
    private void updateClientView() {
        Platform.runLater(() -> {
            clientPath.setText(currentDirectory.getAbsolutePath());
            clientView.getItems().clear();
            clientView.getItems().add("...");
            clientView.getItems()
                    .addAll(currentDirectory.list());
        });
    }

    //добавлен метод для обновления поля папок и файлов на сервере
    private void updateServerView() {
        Platform.runLater(() -> {
            serverPath.setText(serverCurrentDirectory.getAbsolutePath());
            serverView.getItems().clear();
            serverView.getItems().add("...");
            serverView.getItems()
                    .addAll(serverCurrentDirectory.list());
        });
    }
    // Добвлен метод загрузки файлов с сервера // не доделал
    public void download(ActionEvent actionEvent) throws IOException {
        String item = serverView.getSelectionModel().getSelectedItem();
        File selected = serverCurrentDirectory.toPath().resolve(item).toFile();
        if (selected.isFile()) {
            os.writeUTF("#file_message_down#");
            os.writeUTF(selected.getName());
            os.writeLong(selected.length());
            try (InputStream fis = new FileInputStream(selected)) {
                while (fis.available() > 0) {
                    int readBytes = fis.read(buf);
                    os.write(buf, 0, readBytes);
                }
            }
            os.flush();
        }
    }

    // upload file to server
    public void upload(ActionEvent actionEvent) throws IOException {
        String item = clientView.getSelectionModel().getSelectedItem();
        File selected = currentDirectory.toPath().resolve(item).toFile();
        if (selected.isFile()) {
            os.writeUTF("#file_message#");
            os.writeUTF(selected.getName());
            os.writeLong(selected.length());
            try (OutputStream fos = new FileOutputStream(selected)) {
                    int readBytes = is.read(buf);
                    os.write(buf, 0, readBytes);
                }
            }
            os.flush();
        }


    private void initNetwork() {
        try {
            buf = new byte[BUFFER_SIZE];
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDirectory = new File(System.getProperty("user.home"));

        serverCurrentDirectory = new File("server");   // Добавил


        // run in FX Thread
        // :: - method reference
        updateClientView();
        updateServerView();

        initNetwork();
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    currentDirectory = currentDirectory.getParentFile();
                    updateClientView();
                } else {
                    File selected = currentDirectory.toPath().resolve(item).toFile();
                    if (selected.isDirectory()) {
                        currentDirectory = selected;
                        updateClientView();
                    }
                }
            }
        });
//Добавлена навигация по папкам на сервере
        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    serverCurrentDirectory = serverCurrentDirectory.getParentFile();
                    updateServerView();
                } else {
                    File selected = serverCurrentDirectory.toPath().resolve(item).toFile();
                    if (selected.isDirectory()) {
                        serverCurrentDirectory = selected;
                        updateServerView();
                    }
                }
            }
        });
    }
}
