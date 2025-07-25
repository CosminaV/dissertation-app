import React from "react";
import { Link } from "react-router-dom";
import NavbarCommon from "../../components/NavbarCommon";

const NavbarAdmin = ({ userName }) => (
    <NavbarCommon userName={userName}>
        <Link to="/admin/users">Users</Link>
        <Link to="/admin/cohorts">Cohorts</Link>
        <Link to="/admin/student-groups">Student Groups</Link>
        <Link to="/admin/courses">Courses</Link>
    </NavbarCommon>
);

export default NavbarAdmin;
