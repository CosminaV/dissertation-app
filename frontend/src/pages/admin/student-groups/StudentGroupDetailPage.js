import React, {useCallback, useEffect, useState} from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../api";
import "../../../styles/admin/student-groups.css";
import AssignStudentsForm from "../../../components/admin/AssignStudentsForm";
import MoveStudentForm from "../../../components/admin/MoveStudentForm";
import AssignCourseGroupForm from "../../../components/admin/AssignCourseGroupForm";

const StudentGroupDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [group, setGroup] = useState(null);
    const [students, setStudents] = useState([]);
    const [showAssignModal, setShowAssignModal] = useState(false);
    const [showMoveModal, setShowMoveModal] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [showAssignCourseModal, setShowAssignCourseModal] = useState(false);
    const [courses, setCourses] = useState([]);

    const fetchGroupDetails = useCallback(async () => {
        try {
            const groupRes = await api.get(`/admin/student-groups/${id}`);
            setGroup(groupRes.data);

            const studentsRes = await api.get(`/admin/student-groups/${id}/students`);
            setStudents(studentsRes.data);
        } catch (err) {
            console.error("Error loading group", err);
            navigate("/not-found");
        }
    }, [id, navigate]);

    const fetchCoursesById = useCallback(async () => {
        try {
            const res = await api.get(`admin/student-groups/${id}/courses`);
            setCourses(res.data);
        } catch (err) {
            console.error("Error loading courses", err);
            alert("Could not load assigned courses for this student group!");
        }
    }, [id]);

    const handleRemove = async (studentId) => {
        try {
            await api.delete(`/admin/student-groups/remove-student/${studentId}`);
            fetchGroupDetails();
        } catch (error) {
            console.error("Failed to remove student", error);
            alert("Could not remove student from group.");
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

    return (
        <div className="group-detail">
            <div className="group-header">
                <div>
                    <h2>Group: {group.name}</h2>
                    <p>Level: {group.educationLevel}</p>
                    <p>Year of study: {group.yearOfStudy}</p>
                    <p>{students.length} student(s)</p>
                </div>
                <div className="group-actions">
                    <button onClick={() => setShowAssignModal(true)}>Assign Students</button>
                    <button onClick={() => setShowAssignCourseModal(true)}>Assign Course</button>
                </div>
            </div>

            <h3>Students</h3>
            {students.length > 0 ? (
                // <ul className="student-list">
                //     {students.map(student => (
                //         <li key={student.id}>
                //             {student.firstName} {student.lastName} ({student.email})
                //         </li>
                //     ))}
                // </ul>
                <table className="student-table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    {students.map((student) => (
                        <tr key={student.id}>
                            <td>{student.firstName} {student.lastName}</td>
                            <td>{student.email}</td>
                            <td>
                                <div className="action-buttons">
                                    <button className="remove-btn" onClick={() => handleRemove(student.id)} title="Remove">✕</button>
                                    <button className="move-btn" onClick={() => openMoveModal(student)} title="Move">⇄</button>
                                </div>
                            </td>
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

            {showAssignModal && (
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

            {showMoveModal && selectedStudent && (
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

            {showAssignCourseModal && (
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
