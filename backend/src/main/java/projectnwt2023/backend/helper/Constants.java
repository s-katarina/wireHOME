package projectnwt2023.backend.helper;

import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

public class Constants {

    public static String sendgridKey = "";
    public static String fromEmail = "project.nwt2023@gmail.com";
    public static String superAdminPath = "./src/main/resources/superadmin.txt";
    public static String profileImageFolderPath = "./profileImages";

    public static String imgPathForFrontend = "images/";

    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static DateTimeFormatter dateTimeFormatterWithSeconds = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    public static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy.");
}
