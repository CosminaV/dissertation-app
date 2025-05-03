package ro.ase.ism.dissertation.controller.teacher;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.ase.ism.dissertation.dto.material.FileMaterialUploadRequest;
import ro.ase.ism.dissertation.dto.material.TextMaterialUploadRequest;
import ro.ase.ism.dissertation.dto.material.MaterialDownloadResponse;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.dto.material.UpdateMaterialRequest;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.teacher.TeacherMaterialService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/teacher/materials")
@RequiredArgsConstructor
public class TeacherMaterialController {

    private final TeacherMaterialService teacherMaterialService;
    private final Validator validator;

    @GetMapping("/course-groups/{courseGroupId}/materials")
    public ResponseEntity<List<MaterialResponse>> getMaterialsForCourseGroup(
            @PathVariable Integer courseGroupId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(teacherMaterialService.getMaterialsForCourseGroup(courseGroupId, user.getId()));
    }

    @GetMapping("/course-cohorts/{courseCohortId}/materials")
    public ResponseEntity<List<MaterialResponse>> getMaterialsForCourseCohort(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(teacherMaterialService.getMaterialsForCourseCohort(courseCohortId, user.getId()));
    }

    @PostMapping("/upload/text")
    public ResponseEntity<MaterialResponse> uploadTextMaterial(
            @Valid @RequestBody TextMaterialUploadRequest textMaterialUploadRequest,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(teacherMaterialService.createTextMaterial(textMaterialUploadRequest, user.getId()));
    }

    @PostMapping(
            value = "/upload/file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MaterialResponse> uploadFileMaterial(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") FileMaterialUploadRequest fileMaterialUploadRequest,
            @AuthenticationPrincipal User user
    ) {
        Set<ConstraintViolation<FileMaterialUploadRequest>> violations = validator.validate(fileMaterialUploadRequest);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return ResponseEntity.ok(teacherMaterialService.createFileMaterial(fileMaterialUploadRequest, file, user.getId()));
    }

    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Integer materialId,
            @AuthenticationPrincipal User user) {
        MaterialDownloadResponse downloadResponse = teacherMaterialService.downloadMaterial(materialId, user.getId());

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
    public ResponseEntity<String> extractWatermark(
            @PathVariable Integer materialId,
            @AuthenticationPrincipal User user) {
        String watermark = teacherMaterialService.extractWatermark(materialId, user.getId());
        return ResponseEntity.ok(watermark);
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Integer materialId,
            @AuthenticationPrincipal User user) {
        teacherMaterialService.deleteMaterial(materialId, user.getId());
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
