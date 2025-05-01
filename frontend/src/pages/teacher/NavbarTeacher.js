import React from "react";
import { Link } from "react-router-dom";
import NavbarCommon from "../../components/NavbarCommon";

const NavbarTeacher = ({ userName }) => (
    <NavbarCommon userName={userName}>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/teacher/courses">Courses</Link>
    </NavbarCommon>
);

export default NavbarTeacher;