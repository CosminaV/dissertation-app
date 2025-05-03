import React, { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import api from "../../../api";
import "../../../styles/admin/student-group-detail.css";
import AssignStudentsForm from "../../../components/admin/AssignStudentsForm";
import MoveStudentForm from "../../../components/admin/MoveStudentForm";
import AssignCourseGroupForm from "../../../components/admin/AssignCourseGroupForm";

// Helper to get current academic year (e.g., May 2025 => 2024, Sept 2025 => 2025)
function getCurrentAcademicYear() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth(); // 0 = Jan, 8 = Sept
    return month >= 8 ? year : year - 1;
}

const StudentGroupDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const academicYear = searchParams.get("academicYear") ? Number(searchParams.get("academicYear")) : null;

    const [group, setGroup] = useState(null);
    const [showAssignModal, setShowAssignModal] = useState(false);
    const [showMoveModal, setShowMoveModal] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [showAssignCourseModal, setShowAssignCourseModal] = useState(false);
    const [courses, setCourses] = useState([]);

    const fetchGroupDetails = useCallback(async () => {
        try {
            const endpoint = academicYear
                ? `/admin/student-groups/${id}?academicYear=${academicYear}`
                : `/admin/student-groups/${id}`;
            const groupRes = await api.get(endpoint);
            setGroup(groupRes.data);
        } catch (err) {
            console.error("Error loading group", err);
            navigate("/not-found");
        }
    }, [id, academicYear, navigate]);

    const fetchCoursesById = useCallback(async () => {
        try {
            const endpoint = academicYear
                ? `admin/student-groups/${id}/courses?academicYear=${academicYear}`
                : `admin/student-groups/${id}/courses`;
            const res = await api.get(endpoint);
            setCourses(res.data);
        } catch (err) {
            console.error("Error loading courses", err);
            alert("Could not load assigned courses for this student group: " + err.response.data.error);
        }
    }, [id, academicYear]);

    const handleRemove = async (studentId) => {
        try {
            await api.delete(`/admin/student-groups/remove-student/${studentId}`);
            fetchGroupDetails();
        } catch (error) {
            console.error("Failed to remove student", error);
            alert("Could not remove student from group: " + error.response.data.error);
        }
    };

    const openMoveModal = (student) => {
        setSelectedStudent(student);
        setShowMoveModal(true);
    };

    const closeMoveModal = () => {
        setSelectedStudent(null);
        setShowMoveModal(false);
    };

    useEffect(() => {
        fetchGroupDetails();
        fetchCoursesById();
    }, [fetchGroupDetails, fetchCoursesById]);

    if (!group) return <p>Loading group...</p>;

    const currentAcademicYear = getCurrentAcademicYear();
    const groupAcademicYear = group.academicYear ? Number(group.academicYear.split("-")[0]) : undefined;
    const isHistoric = groupAcademicYear !== undefined && groupAcademicYear !== currentAcademicYear;

    return (
        <div className="group-detail">
            <div className="group-detail-header">
                <div>
                    <h2>Group: {group.name + group.cohortName}</h2>
                    <p>Level: {group.educationLevel}</p>
                    <p>Year of study: {group.yearOfStudy}</p>
                    <p>{group.students.length} student(s)</p>
                    {group.academicYear && <p><b>Academic year: {group.academicYear}</b></p>}
                </div>
                <div className="group-actions">
                    {!isHistoric && (
                        <>
                            <button onClick={() => setShowAssignModal(true)}>Assign Students</button>
                            <button onClick={() => setShowAssignCourseModal(true)}>Assign Course</button>
                        </>
                    )}

                </div>
            </div>

            <h3>Students</h3>
            {group.students.length > 0 ? (
                <table className="student-table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        {!isHistoric && <th></th>}
                    </tr>
                    </thead>
                    <tbody>
                    {group.students.map((student) => (
                        <tr key={student.id}>
                            <td>{student.firstName} {student.lastName}</td>
                            <td>{student.email}</td>
                            {!isHistoric && (
                                <td>
                                    <div className="action-buttons">
                                        <button className="remove-btn" onClick={() => handleRemove(student.id)} title="Remove">✕</button>
                                        <button className="move-btn" onClick={() => openMoveModal(student)} title="Move">⇄</button>
                                    </div>
                                </td>
                            )}
                        </tr>
                    ))}
                    </tbody>
                </table>
            ) : (
                <p>No students in this group.</p>
            )}

            <h3>Assigned Courses</h3>
            {courses.length > 0 ? (
                <table className="course-table">
                    <thead>
                    <tr>
                        <th>Course</th>
                        <th>Lecture Teacher</th>
                        <th>Practical Teacher</th>
                        <th>Academic year</th>
                    </tr>
                    </thead>
                    <tbody>
                    {courses.map((cg) => (
                        <tr key={cg.id}>
                            <td>{cg.courseName}</td>
                            <td>{cg.lectureTeacherName}</td>
                            <td>{cg.practicalTeacherName}</td>
                            <td>{cg.academicYear}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            ) : (
                <p>No assigned courses yet.</p>
            )}

            {!isHistoric && showAssignModal && (
                <AssignStudentsForm
                    groupId={group.id}
                    educationLevel={group.educationLevel}
                    onClose={() => setShowAssignModal(false)}
                    onSuccess={() => {
                        fetchGroupDetails();
                        setShowAssignModal(false);
                    }}
                />
            )}

            {!isHistoric && showMoveModal && selectedStudent && (
                <MoveStudentForm
                    student={selectedStudent}
                    currentGroupId={group.id}
                    educationLevel={group.educationLevel}
                    onClose={closeMoveModal}
                    onSuccess={() => {
                        fetchGroupDetails();
                        closeMoveModal();
                    }}
                />
            )}

            {!isHistoric && showAssignCourseModal && (
                <AssignCourseGroupForm
                    studentGroupId={group.id}
                    onClose={() => setShowAssignCourseModal(false)}
                    onSuccess={() => {
                        fetchGroupDetails();
                        fetchCoursesById();
                        setShowAssignCourseModal(false);
                    }}
                />
            )}
        </div>
    );
};

export default StudentGroupDetailPage;