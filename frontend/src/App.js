import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import PrivateRoute from "./components/PrivateRoute";
import {AuthProvider} from "./context/AuthContext";

const App = () => {
  return (
      <Router>
          <AuthProvider>
            <Routes>
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<ProtectedRoute />}>
                    <Route path="/" element={<Login />} />
                </Route>
                <Route element={<PrivateRoute />}>
                    <Route path="/dashboard" element={<Dashboard />} />
                </Route>
            </Routes>
          </AuthProvider>
      </Router>
  );
};

export default App;

