import React from "react";
import {BrowserRouter as Router, Routes, Route, Navigate} from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import PrivateRoute from "./components/PrivateRoute";
import {AuthProvider} from "./context/AuthContext";

const App = () => {
  return (
      <Router>
          <AuthProvider>
            <Routes>
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<PrivateRoute />}>
                    <Route index element={<Navigate to="/dashboard" replace={true} />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                </Route>
            </Routes>
          </AuthProvider>
      </Router>
  );
};

export default App;

