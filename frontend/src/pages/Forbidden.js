import { Link } from "react-router-dom";
import "../styles/errors.css";

const Forbidden = () => {
    return (
        <div className="error-page">
            <h2>403 - Access Denied</h2>
            <p>You don’t have permission to view this page.</p>
            <Link to="/">Go back to the Landing Page</Link>
        </div>
    );
};

export default Forbidden;
