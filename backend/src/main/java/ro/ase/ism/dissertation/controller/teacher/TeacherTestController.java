package ro.ase.ism.dissertation.controller.teacher;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.service.digitalwatermarking.TestWatermarkService;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherTestController {

    private final TestWatermarkService testWatermarkService;

    @GetMapping("/test-watermark")
    public ResponseEntity<String> testWatermarkExtraction() {
        return ResponseEntity.ok(testWatermarkService.testWatermark());
    }
}
