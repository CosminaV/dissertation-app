import React, { useCallback, useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import api from "../../../api";
import "../../../styles/admin/teacher.css";

const TeacherPage = () => {
    const { id } = useParams();
    const location = useLocation();

    const [teacher, setTeacher] = useState(location.state?.teacher || null);
    const [courses, setCourses] = useState([]);

    const fetchTeacher = useCallback(async () => {
        if (teacher) return; //skip call if present on the state
        try {
            const res = await api.get(`/admin/users/${id}`);
            setTeacher(res.data);
        } catch (err) {
            console.error("Failed to fetch teacher details", err);
        }
    }, [id, teacher]);

    const fetchTeacherCourses = useCallback(async () => {
        try {
            const res = await api.get(`/admin/teachers/${id}/courses`);
            setCourses(res.data);
        } catch (err) {
            console.error("Failed to fetch teacher courses", err);
        }
    }, [id]);

    useEffect(() => {
        fetchTeacher();
        fetchTeacherCourses();
    }, [fetchTeacher, fetchTeacherCourses]);

    const groupedCourses = useCallback(() => {
        const grouped = {};
        for (const course of courses) {
            const { educationLevel, yearOfStudy, semester } = course;
            grouped[educationLevel] ??= {};
            grouped[educationLevel][yearOfStudy] ??= {};
            grouped[educationLevel][yearOfStudy][semester] ??= [];
            grouped[educationLevel][yearOfStudy][semester].push(course);
        }
        return grouped;
    }, [courses]);

    if (!teacher) return <p>Loading teacher info...</p>;

    return (
        <div className="teacher-page">
            <div className="teacher-header">
                <h2>{teacher.firstName} {teacher.lastName}</h2>
                <p>Email: {teacher.email}</p>
                <h2>Assigned Courses</h2>
            </div>

            {courses.length > 0 ? (
                Object.entries(groupedCourses()).map(([level, years]) => (
                    <div key={level}>
                        <h3>{level}</h3>
                        {Object.entries(years).map(([year, semesters]) => (
                            <div key={year}>
                                <h4>Year {year}</h4>
                                {Object.entries(semesters).map(([sem, list]) => (
                                    <div key={sem}>
                                        <h5>Semester {sem}</h5>
                                        <table className="teacher-course-table">
                                            <thead>
                                            <tr>
                                                <th>Course</th>
                                                <th>Academic Year</th>
                                                <th>Education Level</th>
                                                <th>Year of Study</th>
                                                <th>Semester</th>
                                                <th>Role</th>
                                                <th>Target</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            {list.map(course => (
                                                <tr key={`${course.courseId}-${course.role}-${course.academicYear}`}>
                                                    <td>{course.courseName}</td>
                                                    <td>{course.academicYear}</td>
                                                    <td>{course.educationLevel}</td>
                                                    <td>{course.yearOfStudy}</td>
                                                    <td>{course.semester}</td>
                                                    <td>{course.role}</td>
                                                    <td>{course.target}</td>
                                                </tr>
                                            ))}
                                            </tbody>
                                        </table>
                                    </div>
                                ))}
                            </div>
                        ))}
                    </div>
                ))
            ) : (
                <p>No assigned courses yet.</p>
            )}
        </div>
    );
};

export default TeacherPage;