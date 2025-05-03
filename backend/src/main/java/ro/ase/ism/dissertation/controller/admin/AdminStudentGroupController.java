package ro.ase.ism.dissertation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupResponse;
import ro.ase.ism.dissertation.dto.studentgroup.AssignStudentsToGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupResponse;
import ro.ase.ism.dissertation.service.admin.AdminStudentGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/student-groups")
@RequiredArgsConstructor
public class AdminStudentGroupController {

    private final AdminStudentGroupService adminStudentGroupService;

    @PostMapping
    public ResponseEntity<StudentGroupResponse> createStudentGroup(
            @Valid @RequestBody StudentGroupRequest studentGroupRequest) {
        return ResponseEntity.ok(adminStudentGroupService.createStudentGroup(studentGroupRequest));
    }

    @GetMapping
    public ResponseEntity<List<StudentGroupResponse>> getStudentGroups(@RequestParam(required = false) Integer academicYear) {
        return ResponseEntity.ok(adminStudentGroupService.getStudentGroupsByAcademicYear(academicYear));
    }

    @GetMapping("/academic-years")
    public ResponseEntity<List<String>> getAllAcademicYears() {
        return ResponseEntity.ok(adminStudentGroupService.getAllAcademicYears());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<StudentGroupResponse> getStudentGroupById(
            @PathVariable Integer groupId,
            @RequestParam(required = false) Integer academicYear) {
        return ResponseEntity.ok(adminStudentGroupService.getStudentGroupByIdAndAcademicYear(groupId, academicYear));
    }

    @PostMapping("/{groupId}/assign-students")
    public ResponseEntity<Void> assignStudentsToGroup(
            @PathVariable Integer groupId,
            @RequestBody AssignStudentsToGroupRequest request) {
        adminStudentGroupService.assignStudentsToGroup(groupId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}/move-student/{studentId}")
    public ResponseEntity<Void> moveStudentToGroup(
            @PathVariable Integer groupId,
            @PathVariable Integer studentId) {
        adminStudentGroupService.moveStudentToGroup(studentId, groupId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove-student/{studentId}")
    public ResponseEntity<Void> removeStudentFromGroup(@PathVariable Integer studentId) {
        adminStudentGroupService.removeStudentFromGroup(studentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/courses")
    public ResponseEntity<List<CourseGroupResponse>> getCoursesForStudentGroup(
            @PathVariable Integer groupId,
            @RequestParam(required = false) Integer academicYear) {
        List<CourseGroupResponse> courses = adminStudentGroupService.getCoursesForStudentGroup(groupId, academicYear);
        return ResponseEntity.ok(courses);
    }
}
