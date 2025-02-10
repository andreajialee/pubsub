package com.example;

public class MainApplication {
    public static void main(String[] args) throws Exception {
        PubSubSyncPull puller = new PubSubSyncPull();
        puller.init();
    }
}