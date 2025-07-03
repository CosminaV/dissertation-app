import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { jwtDecode } from "jwt-decode";

export default function DefaultRedirect() {
    const { accessToken } = useAuth();
    const decodedJwt = jwtDecode(accessToken);
    const role = decodedJwt.role;

    if (role === "ADMIN")   return <Navigate to="/admin/users" replace />;
    if (role === "TEACHER") return <Navigate to="/teacher/courses" replace />;
    if (role === "STUDENT") return <Navigate to="/student/courses" replace />;

    return <Navigate to="/forbidden" replace />;
}