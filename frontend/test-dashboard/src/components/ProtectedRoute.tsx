import React, { useContext } from "react";
import { Navigate } from "react-router-dom";
import AuthContext from "../context/AuthContext";

interface Props {
  children: JSX.Element;
}

const ProtectedRoute: React.FC<Props> = ({ children }) => {
  const { authTokens } = useContext(AuthContext)!;
  return authTokens ? children : <Navigate to="/login" />;
};

export default ProtectedRoute;
