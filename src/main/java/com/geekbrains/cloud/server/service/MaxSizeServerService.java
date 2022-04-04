package com.geekbrains.cloud.server.service;

public class MaxSizeServerService {

    private final long MAX_SIZE_SERVER = 5242880;
    private long countByte = 0;

    public String getMaxSizeServerService(long filebytes) {
        if (countByte < MAX_SIZE_SERVER) {
            if (filebytes <= MAX_SIZE_SERVER - filebytes) {
                countByte = countByte + filebytes;
                return "Передача файлов разрешена";
            } else {
                return "Файл слишком велик для загрузки на сервер!";
            }
        } else {
            return "Недостаточно места на сервере";
        }

    }
}
