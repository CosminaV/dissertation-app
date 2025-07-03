import { Link } from "react-router-dom";
import "../styles/errors.css";

const NotFound = () => {
    return (
        <div className="error-page">
            <h2>404 - Page Not Found</h2>
            <p>The page you are looking for doesn’t exist or was moved.</p>
            <Link to="/">Return to the Landing Page</Link>
        </div>
    );
};

export default NotFound;

