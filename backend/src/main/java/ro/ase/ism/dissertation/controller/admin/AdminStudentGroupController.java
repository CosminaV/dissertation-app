package ro.ase.ism.dissertation.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.ase.ism.dissertation.dto.coursegroup.CourseGroupResponse;
import ro.ase.ism.dissertation.dto.studentgroup.AssignStudentsToGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupRequest;
import ro.ase.ism.dissertation.dto.studentgroup.StudentGroupResponse;
import ro.ase.ism.dissertation.dto.studentgroup.StudentResponse;
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
    public ResponseEntity<List<StudentGroupResponse>> getAllStudentGroups() {
        return ResponseEntity.ok(adminStudentGroupService.getAllStudentGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentGroupResponse> getStudentGroupById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminStudentGroupService.getGroupById(id));
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

    @GetMapping("/{groupId}/students")
    public ResponseEntity<List<StudentResponse>> getStudentsInGroup(@PathVariable Integer groupId) {
        List<StudentResponse> students = adminStudentGroupService.getStudentsInGroup(groupId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{groupId}/courses")
    public ResponseEntity<List<CourseGroupResponse>> getCoursesForStudentGroup(@PathVariable Integer groupId) {
        List<CourseGroupResponse> courses = adminStudentGroupService.getCoursesForStudentGroup(groupId);
        return ResponseEntity.ok(courses);
    }
}
