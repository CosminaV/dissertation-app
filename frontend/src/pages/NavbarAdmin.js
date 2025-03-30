import React from "react";
import { Link } from "react-router-dom";
import NavbarCommon from "../components/NavbarCommon";

const NavbarAdmin = ({ userName }) => (
    <NavbarCommon userName={userName}>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/admin/users">Users</Link>
        <Link to="/admin/courses">Courses</Link>
    </NavbarCommon>
);

export default NavbarAdmin;
