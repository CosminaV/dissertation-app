package ro.ase.ism.dissertation.controller.teacher;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.dto.material.CreateMaterialRequest;
import ro.ase.ism.dissertation.dto.material.MaterialDownloadResponse;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.dto.material.UpdateMaterialRequest;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.TeacherMaterialService;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/materials")
@RequiredArgsConstructor
public class TeacherMaterialController {

    private final TeacherMaterialService teacherMaterialService;

    @GetMapping("/course-groups/{courseGroupId}/materials")
    public ResponseEntity<List<MaterialResponse>> getMaterialsForCourseGroup(@PathVariable int courseGroupId) {
        return ResponseEntity.ok(teacherMaterialService.getMaterialsForCourseGroup(courseGroupId));
    }

    @PostMapping("/upload/text")
    public ResponseEntity<MaterialResponse> uploadTextMaterial(
            @RequestBody CreateMaterialRequest createMaterialRequest,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherMaterialService.createTextMaterial(createMaterialRequest, user.getId()));
    }

    @PostMapping(
            value = "/upload/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MaterialResponse> uploadFileMaterial(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseGroupId") Integer courseGroupId,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherMaterialService.createFileMateriel(title, file, courseGroupId, user.getId()));
    }

    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Integer materialId) {
        MaterialDownloadResponse downloadResponse = teacherMaterialService.downloadMaterial(materialId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResponse.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadResponse.getOriginalFilename() + "\"")
                .body(new InputStreamResource(downloadResponse.getInputStream()));
    }

    /*
    Endpoint used to test that a specific material holds a hidden watermark
     */
    @GetMapping("/{materialId}/extract-watermark")
    public ResponseEntity<String> extractWatermark(@PathVariable Integer materialId) {
        String watermark = teacherMaterialService.extractWatermark(materialId);
        return ResponseEntity.ok(watermark);
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Integer materialId) {
        teacherMaterialService.deleteMaterial(materialId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{materialId}")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable Integer materialId,
            @RequestBody UpdateMaterialRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherMaterialService.updateMaterial(materialId, request, user.getId()));
    }
}
