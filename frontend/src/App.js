import React from "react";
import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import SetPasswordPage from "./pages/SetPassword";
import PrivateRoute from "./components/PrivateRoute";
import {AuthProvider} from "./context/AuthContext";
import UsersPage from "./pages/admin/users/UsersPage";
import CohortListPage from "./pages/admin/cohorts/CohortListPage";
import NotFound from "./pages/NotFound";
import Forbidden from "./pages/Forbidden";
import StudentGroupListPage from "./pages/admin/student-groups/StudentGroupListPage";
import StudentGroupDetailPage from "./pages/admin/student-groups/StudentGroupDetailPage";
import CourseListPage from "./pages/admin/courses/CourseListPage";
import CourseCohortsPage from "./pages/admin/course-cohorts/CourseCohortsPage";
import TeacherPage from "./pages/admin/users/TeacherPage";
import TeacherCoursesPage from "./pages/teacher/courses/TeacherCoursesPage";
import TeacherCourseGroupDetailPage from "./pages/teacher/courses/TeacherCourseGroupDetailPage";
import TeacherCourseCohortDetailPage from "./pages/teacher/courses/TeacherCourseCohortDetailPage";
import StudentCoursesPage from "./pages/student/courses/StudentCoursesPage";
import StudentCourseGroupDetailPage from "./pages/student/courses/StudentCourseGroupDetailPage";
import StudentCourseCohortDetailPage from "./pages/student/courses/StudentCourseCohortDetailPage";
import UploadProfileImage from "./pages/UploadProfileImage";
import TeacherExamDetailPage from "./pages/teacher/exams/TeacherExamDetailPage";
import StudentExamDetailPage from "./pages/student/exams/StudentExamDetailPage";
import TeacherExamMonitoringPage from "./pages/teacher/exams/TeacherExamMonitoringPage";
const App = () => {
  return (
      <Router>
          <AuthProvider>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<PrivateRoute />}>
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                </Route>
                <Route path="/admin/users" element={<PrivateRoute allowedRoles={["ADMIN"]}><UsersPage /></PrivateRoute>} />
                <Route path="/admin/cohorts" element={<PrivateRoute allowedRoles={["ADMIN"]}><CohortListPage /></PrivateRoute>} />
                <Route path="/admin/student-groups" element={<PrivateRoute allowedRoles={["ADMIN"]}><StudentGroupListPage /></PrivateRoute>} />
                <Route path="/admin/student-groups/:id" element={<PrivateRoute allowedRoles={["ADMIN"]}><StudentGroupDetailPage /></PrivateRoute>} />
                <Route path="/admin/courses" element={<PrivateRoute allowedRoles={["ADMIN"]}><CourseListPage /></PrivateRoute>} />
                <Route path="/admin/courses/:courseId/course-cohorts" element={<PrivateRoute allowedRoles={["ADMIN"]}><CourseCohortsPage /></PrivateRoute>} />
                <Route path="/admin/teachers/:id" element={<PrivateRoute allowedRoles={["ADMIN"]}><TeacherPage /></PrivateRoute>} />

                <Route path="/teacher/courses" element={<PrivateRoute allowedRoles={["TEACHER"]}><TeacherCoursesPage /></PrivateRoute>} />
                <Route path="/teacher/courses/course-groups/:targetId" element={<PrivateRoute allowedRoles={["TEACHER"]}><TeacherCourseGroupDetailPage /></PrivateRoute>} />
                <Route path="/teacher/courses/course-cohorts/:targetId" element={<PrivateRoute allowedRoles={["TEACHER"]}><TeacherCourseCohortDetailPage/></PrivateRoute>} />
                <Route path="/teacher/exams/:examId" element={<PrivateRoute allowedRoles={["TEACHER"]}><TeacherExamDetailPage/></PrivateRoute>} />
                <Route path="/teacher/exams/:examId/monitoring" element={<PrivateRoute allowedRoles={["TEACHER"]}><TeacherExamMonitoringPage/></PrivateRoute>} />

                <Route path="/student/courses" element={<PrivateRoute allowedRoles={["STUDENT"]}><StudentCoursesPage /></PrivateRoute>} />
                <Route path="/student/courses/course-groups/:targetId" element={<PrivateRoute allowedRoles={["STUDENT"]}><StudentCourseGroupDetailPage /></PrivateRoute>} />
                <Route path="/student/courses/course-cohorts/:targetId" element={<PrivateRoute allowedRoles={["STUDENT"]}><StudentCourseCohortDetailPage /></PrivateRoute>} />
                {/*<Route path="/student/exams" element={<PrivateRoute allowedRoles={["STUDENT"]}><StudentExamPage /></PrivateRoute>} />*/}
                <Route path="/student/exams/:examId" element={<PrivateRoute allowedRoles={["STUDENT"]}><StudentExamDetailPage /></PrivateRoute>} />

                <Route path="/set-password" element={<SetPasswordPage />} />
                <Route path="/upload-profile-image" element={<UploadProfileImage />} />
                <Route path="/forbidden" element={<Forbidden />} />
                <Route path="*" element={<NotFound />} />
            </Routes>
          </AuthProvider>
      </Router>
  );
};

export default App;

