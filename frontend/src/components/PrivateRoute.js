import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Navbar from "./Navbar";

const PrivateRoute = () => {
    const { accessToken, loading } = useAuth();

    if (loading) {
        return <div>Loading ....</div>
    }

    if (!accessToken) {
        return <Navigate to="/login" replace />;
    }

    return (
        <>
            <Navbar />
            <Outlet />
        </>
    );
};

export default PrivateRoute;
