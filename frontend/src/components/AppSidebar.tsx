import { useLocation, useNavigate } from "react-router-dom";
import {
  Home,
  DoorOpen,
  Lightbulb,
  Cog,
  Calendar,
  BarChart3,
  Bell,
  ClipboardList,
  Palmtree,
  Users,
  LogOut,
  FastForward,
} from "lucide-react";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarFooter,
  SidebarSeparator,
} from "@/components/ui/sidebar";
import { useAuth } from "@/contexts/AuthContext";

const navItems = [
  { title: "Dashboard", icon: Home, path: "/" },
  { title: "Räume", icon: DoorOpen, path: "/rooms" },
  { title: "Geräte", icon: Lightbulb, path: "/devices" },
  { title: "Szenen", icon: Cog, path: "/scenes" },
  { title: "Regeln", icon: ClipboardList, path: "/rules" },
  { title: "Zeitpläne", icon: Calendar, path: "/schedules" },
  { title: "Energie", icon: BarChart3, path: "/energy" },
  { title: "Aktivitäten", icon: ClipboardList, path: "/activity" },
  { title: "Benachrichtigungen", icon: Bell, path: "/notifications" },
  { title: "Urlaubsmodus", icon: Palmtree, path: "/vacation" },
  { title: "Simulation", icon: FastForward, path: "/simulation" },
];

const ownerItems = [
  { title: "Mitglieder", icon: Users, path: "/members" },
];

export function AppSidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout, isOwner } = useAuth();

  return (
    <Sidebar>
      <SidebarHeader className="p-4">
        <h2 className="text-lg font-semibold">Smarthomie</h2>
        {user && (
          <p className="text-sm text-muted-foreground truncate">{user.email}</p>
        )}
      </SidebarHeader>
      <SidebarSeparator />
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Navigation</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {navItems.map((item) => (
                <SidebarMenuItem key={item.path}>
                  <SidebarMenuButton
                    isActive={location.pathname === item.path}
                    onClick={() => navigate(item.path)}
                  >
                    <item.icon className="size-4" />
                    <span>{item.title}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {isOwner && (
          <SidebarGroup>
            <SidebarGroupLabel>Verwaltung</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {ownerItems.map((item) => (
                  <SidebarMenuItem key={item.path}>
                    <SidebarMenuButton
                      isActive={location.pathname === item.path}
                      onClick={() => navigate(item.path)}
                    >
                      <item.icon className="size-4" />
                      <span>{item.title}</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        )}
      </SidebarContent>
      <SidebarFooter className="p-4">
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton onClick={() => { logout(); navigate("/login"); }}>
              <LogOut className="size-4" />
              <span>Abmelden</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
}
