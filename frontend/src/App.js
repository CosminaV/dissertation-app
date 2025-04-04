import React from "react";
import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import SetPasswordPage from "./pages/SetPassword";
import PrivateRoute from "./components/PrivateRoute";
import {AuthProvider} from "./context/AuthContext";
import AdminUsersPage from "./pages/AdminUsersPage";

const App = () => {
  return (
      <Router>
          <AuthProvider>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<PrivateRoute />}>
                    <Route index element={<Navigate to="/dashboard" replace={true} />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/admin/users" element={<AdminUsersPage />} />
                </Route>
                <Route path="/set-password" element={<SetPasswordPage />} />
            </Routes>
          </AuthProvider>
      </Router>
  );
};

export default App;

