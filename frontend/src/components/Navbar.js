import React, {useEffect, useState} from "react";
import NavbarAdmin from "../pages/NavbarAdmin";
import { useAuth } from "../context/AuthContext";
import { jwtDecode } from "jwt-decode";
import api from "../api";

const Navbar = () => {
    const { accessToken } = useAuth();
    const [userInfo, setUserInfo] = useState(null);

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const response = await api.get("/auth/me");
                setUserInfo(response.data);
            } catch (error) {
                console.error("Failed to fetch user info: ", error);
            }
        };

        if (accessToken) {
            fetchUserInfo();
        }
    }, [accessToken])

    if (!accessToken || !userInfo) {
        return null;
    }

    const decodedToken = jwtDecode(accessToken);
    const role = decodedToken.role;
    const userName = `${userInfo.firstName} ${userInfo.lastName}`;

    switch (role) {
        // case "STUDENT":
        //     return <NavbarStudent userName={userName} />;
        // case "TEACHER":
        //     return <NavbarTeacher userName={userName} />;
        case "ADMIN":
            return <NavbarAdmin userName={userName} />;
        default:
            return null;
    }
};

export default Navbar;
