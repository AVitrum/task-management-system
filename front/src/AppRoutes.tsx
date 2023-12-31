import { Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import HomePage from "./pages/HomePage";
import RegisterPage from "./pages/Auth/RegisterPage";
import LoginPage from "./pages/Auth/LoginPage";
import { UserContextProvider } from "./components/UserContext";
import ResetPage from "./pages/Auth/ResetPasswordPage";
import RecoveryPage from "./pages/Auth/RecoveryPage";
import CreateTeam from "./pages/Tasks/CreateTeam";
import TeamPage from "./pages/Tasks/TeamPage";
import MembersPage from "./pages/Tasks/MembersPage";
import TasksLayout from "./pages/Tasks/TasksLayout";
import AboutPage from "./pages/AboutPage";
import HelpPage from "./pages/HelpPage";
import HistoryPage from "./pages/Tasks/HistoryPage";
import CommentPage from "./pages/Tasks/CommentPage";


export default function AppRoutes() {
  return (
    <UserContextProvider>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/help" element={<HelpPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/reset" element={<ResetPage />} />
          <Route path="/recovery" element={<RecoveryPage />} />

          <Route path="/createTeam" element={<CreateTeam />} />
          <Route path="/showTeam" element={<TeamPage />} />
          <Route path="/members/:id" element={<MembersPage />} />
          <Route path="/tasksLayout/:teamid" element={<TasksLayout />} />

          <Route
            path="/tasksLayout/:teamid/:taskid"
            element={<TasksLayout />}
          />
          <Route path="/history/:teamid/:taskid" element={<HistoryPage />} />
          <Route path="/сomment/:teamid/:taskid" element={ <CommentPage/>} />
         
         
        </Route>
      </Routes>
    </UserContextProvider>
  );
}
