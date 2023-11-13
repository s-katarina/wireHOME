package projectnwt2023.backend.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import projectnwt2023.backend.helper.ApiResponse;
import projectnwt2023.backend.helper.MessageDTO;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping(value="/property/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<ApiResponse<MessageDTO>> uploadPropertyImage(@RequestParam("file") MultipartFile file,
                                                                       @RequestParam("customFileName") String customFileName) {
        try {
            imageService.savePropertyImage(file, customFileName);
            return new ResponseEntity<>(new ApiResponse<>(200, new MessageDTO("Image upload success.")), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ApiResponse<>(400, new MessageDTO("Image upload fail.")), HttpStatus.BAD_REQUEST);
        }
    }
}
