package projectnwt2023.backend.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projectnwt2023.backend.helper.ApiResponse;
import projectnwt2023.backend.helper.MessageDTO;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost"})
@Validated
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping(value="/property/upload", consumes = { "multipart/form-data" })
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<MessageDTO>> uploadPropertyImage(@RequestParam("file") MultipartFile file,
                                                                       @RequestParam("propertyId") String propertyId) {
        try {
            imageService.savePropertyImage(file, propertyId);
            return new ResponseEntity<>(new ApiResponse<>(200, new MessageDTO("Image upload success.")), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse<>(400, new MessageDTO("Image upload fail.")), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value="/device/upload", consumes = { "multipart/form-data" })
    @PreAuthorize(value = "hasRole('AUTH_USER')")
    public ResponseEntity<ApiResponse<MessageDTO>> uploadDeviceImage(@RequestParam("file") MultipartFile file,
                                                                       @RequestParam("customFileName") String customFileName) {
        try {
            imageService.saveDeviceImage(file, customFileName);
            return new ResponseEntity<>(new ApiResponse<>(200, new MessageDTO("Image upload success.")), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse<>(400, new MessageDTO("Image upload fail.")), HttpStatus.BAD_REQUEST);
        }
    }
}
