package es.um.asio.service.util;

import java.net.URL;

import static org.springframework.boot.web.servlet.server.Session.SessionTrackingMode.URL;

public class Utils {

    public static boolean isValidURL(String urlString) {
        try {
            java.net.URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
