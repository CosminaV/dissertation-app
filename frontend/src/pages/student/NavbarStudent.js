import React from "react";
import { Link } from "react-router-dom";
import NavbarCommon from "../../components/NavbarCommon";

const NavbarStudent = ({ userName }) => (
    <NavbarCommon userName={userName}>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/student/courses">Courses</Link>
        <Link to="/student/exams">Exams</Link>
    </NavbarCommon>
);

export default NavbarStudent;