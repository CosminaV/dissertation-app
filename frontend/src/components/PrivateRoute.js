import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const PrivateRoute = () => {
    const { accessToken, loading } = useAuth();

    if (loading) {
        return <div>Loading ....</div>
    }
    return accessToken ? <Outlet /> : <Navigate to="/login" replace />;
};

export default PrivateRoute;
