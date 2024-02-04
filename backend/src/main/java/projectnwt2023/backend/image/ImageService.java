package projectnwt2023.backend.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    @Value("${upload.directory}")
    private String uploadDirectory;


    public void savePropertyImage(MultipartFile file, String propertyId) throws IOException {
        String fileName = "property-" + propertyId + ".jpg";
        Path filePath = Paths.get(uploadDirectory, fileName);
        System.out.println(filePath);
        Files.write(filePath, file.getBytes());
    }

    public void saveDeviceImage(MultipartFile file, String customFileName) throws IOException {
        String fileName = "device-" + customFileName + ".jpg";
        Path filePath = Paths.get(uploadDirectory, fileName);
        System.out.println(filePath);
        Files.write(filePath, file.getBytes());
    }
}