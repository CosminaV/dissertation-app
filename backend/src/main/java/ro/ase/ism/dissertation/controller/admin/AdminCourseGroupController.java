package ro.ase.ism.dissertation.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ase.ism.dissertation.dto.coursegroup.AssignCourseGroupRequest;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupResponse;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupTeachersResponse;
import ro.ase.ism.dissertation.service.AdminCourseGroupService;

@RestController
@RequestMapping("/api/admin/course-groups")
@RequiredArgsConstructor
public class AdminCourseGroupController {


    private final AdminCourseGroupService adminCourseGroupService;

    @PostMapping
    public ResponseEntity<CourseGroupResponse> assignCourseToGroup(
            @RequestBody AssignCourseGroupRequest request) {
        return ResponseEntity.ok(adminCourseGroupService.assignCourseToGroup(request));
    }

    @GetMapping("/{courseGroupId}/teachers")
    public ResponseEntity<CourseGroupTeachersResponse> getTeachersForCourseGroup(@PathVariable Integer courseGroupId) {
        CourseGroupTeachersResponse response = adminCourseGroupService.getTeachersForCourseGroup(courseGroupId);
        return ResponseEntity.ok(response);
    }
}
