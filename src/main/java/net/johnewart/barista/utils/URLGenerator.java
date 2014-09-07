package net.johnewart.barista.utils;


public class URLGenerator {
    final static String BASE_URL = "http://localhost:9090";
    //final static String BASE_URL = "http://binky.local:9090";
    //final static String BASE_URL = "http://192.168.59.3:9090";
    public static String generateUrl(String path) {
        return String.format("%s/%s", BASE_URL, path);
    }
}
