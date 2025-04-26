package ro.ase.ism.dissertation.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.service.digitalwatermarking.TestWatermarkService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTestController {

    private final TestWatermarkService testWatermarkService;

    /*
    Endpoint used to test if a file holds a hidden watermark
     */
    @PostMapping(
            value = "/materials/watermark/extract-from-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> extractWatermarkFromUploadedFile(@RequestParam("file") MultipartFile file) {
        String extractedInfo = testWatermarkService.extractWatermarkFromUploadedFile(file);
        return ResponseEntity.ok(extractedInfo);
    }
}
