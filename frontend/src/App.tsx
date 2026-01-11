import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import FriendsPage from './pages/FriendsPage';
import FriendIdeasPage from './pages/FriendIdeasPage';
import ProtectedRoute from './components/ProtectedRoute';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/friends"
        element={
          <ProtectedRoute>
            <FriendsPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/friends/:friendId"
        element={
          <ProtectedRoute>
            <FriendIdeasPage />
          </ProtectedRoute>
        }
      />

      <Route path="/" element={<Navigate to="/friends" replace />} />
      <Route path="*" element={<Navigate to="/friends" replace />} />
    </Routes>
  );
}
