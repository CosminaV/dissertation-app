import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ProtectedRoute = () => {
    const { accessToken } = useAuth();

    return accessToken ? <Navigate to="/dashboard" replace /> : <Outlet />;
};

export default ProtectedRoute;
