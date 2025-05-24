package ro.ase.ism.dissertation.controller.student;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.material.MaterialDownloadResponse;
import ro.ase.ism.dissertation.dto.material.MaterialResponse;
import ro.ase.ism.dissertation.model.user.User;
import ro.ase.ism.dissertation.service.student.StudentMaterialService;

import java.util.List;

@RestController
@RequestMapping("/api/student/materials")
@RequiredArgsConstructor
public class StudentMaterialController {

    private final StudentMaterialService studentMaterialService;

    @GetMapping("/course-groups/{courseGroupId}/materials")
    public ResponseEntity<List<MaterialResponse>> getMaterialsForCourseGroup(
            @PathVariable Integer courseGroupId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(studentMaterialService.getMaterialsForCourseGroup(user.getId(), courseGroupId));
    }

    @GetMapping("/course-cohorts/{courseCohortId}/materials")
    public ResponseEntity<List<MaterialResponse>> getMaterialsForCourseCohort(
            @PathVariable Integer courseCohortId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(studentMaterialService.getMaterialsForCourseCohort(user.getId(), courseCohortId));
    }

    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Integer materialId,
            @AuthenticationPrincipal User user) {
        MaterialDownloadResponse downloadResponse = studentMaterialService.downloadMaterial(materialId, user.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResponse.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + downloadResponse.getOriginalFilename() + "\"")
                .body(new InputStreamResource(downloadResponse.getInputStream()));
    }
}
