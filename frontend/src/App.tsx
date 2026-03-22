import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { AppLayout } from "@/components/AppLayout";
import LoginPage from "@/pages/LoginPage";
import RegisterPage from "@/pages/RegisterPage";
import DashboardPage from "@/pages/DashboardPage";
import RoomsPage from "@/pages/RoomsPage";
import DevicesPage from "@/pages/DevicesPage";
import ScenesPage from "@/pages/ScenesPage";
import RulesPage from "@/pages/RulesPage";
import SchedulesPage from "@/pages/SchedulesPage";
import EnergyPage from "@/pages/EnergyPage";
import ActivityPage from "@/pages/ActivityPage";
import NotificationsPage from "@/pages/NotificationsPage";
import VacationPage from "@/pages/VacationPage";
import MembersPage from "@/pages/MembersPage";
import { Toaster } from "@/components/ui/sonner";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route element={<AppLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/rooms" element={<RoomsPage />} />
            <Route path="/devices" element={<DevicesPage />} />
            <Route path="/scenes" element={<ScenesPage />} />
            <Route path="/rules" element={<RulesPage />} />
            <Route path="/schedules" element={<SchedulesPage />} />
            <Route path="/energy" element={<EnergyPage />} />
            <Route path="/activity" element={<ActivityPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/vacation" element={<VacationPage />} />
            <Route path="/members" element={<MembersPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <Toaster />
      </AuthProvider>
    </BrowserRouter>
  );
}
