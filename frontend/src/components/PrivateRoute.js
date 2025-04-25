import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Navbar from "./Navbar";
import { jwtDecode } from "jwt-decode";

const PrivateRoute = ({ allowedRoles = [], children }) => {
    const { accessToken, loading } = useAuth();

    if (loading) {
        return <div>Loading ....</div>
    }

    if (!accessToken) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles.length > 0) {
        const decoded = jwtDecode(accessToken);
        const userRole = decoded.role;

        if (!allowedRoles.includes(userRole)) {
            return <Navigate to="/forbidden" replace />;
        }
    }

    return (
        <>
            <Navbar />
            {children || <Outlet />}
        </>
    );
};

export default PrivateRoute;
