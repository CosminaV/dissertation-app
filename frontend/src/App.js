import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Register from "./pages/Register";

const App = () => {
  return (
      <Router>
        <Routes>
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<div>Login Page (to be implemented)</div>} />
        </Routes>
      </Router>
  );
};

export default App;

