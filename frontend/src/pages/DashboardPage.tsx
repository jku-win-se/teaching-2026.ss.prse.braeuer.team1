import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/services/api";
import { Badge } from "@/components/ui/badge";
import { DoorOpen, Lightbulb, Zap, ShieldCheck, Clock, Activity, Wifi, WifiOff } from "lucide-react";

interface Room {
  id: number;
  name: string;
}

interface Device {
  id: number;
  name: string;
  type: string;
  switchedOn?: boolean;
  level?: number;
  roomName?: string;
}

interface Rule {
  id: number;
  active: boolean;
}

interface Schedule {
  id: number;
  active: boolean;
}

interface EnergyDashboard {
  totalTodayWh: number;
  totalWeekWh: number;
}

interface ActivityLog {
  id: number;
  deviceName: string;
  roomName: string;
  actor: string;
  description: string;
  timestamp: string;
}

interface IoTStatus {
  connected: boolean;
  protocol: string;
}

export default function DashboardPage() {
  const { user, isOwner } = useAuth();
  const [rooms, setRooms] = useState<Room[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [rules, setRules] = useState<Rule[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [energy, setEnergy] = useState<EnergyDashboard | null>(null);
  const [recentActivity, setRecentActivity] = useState<ActivityLog[]>([]);
  const [iotStatus, setIoTStatus] = useState<IoTStatus | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    try {
      const userId = isOwner ? user?.id : undefined;
      const [roomsRes, rulesRes, schedulesRes, energyRes, activityRes] =
        await Promise.all([
          api.get<Room[]>(`/rooms${userId ? `?userId=${userId}` : ""}`),
          api.get<Rule[]>(`/rules${userId ? `?userId=${userId}` : ""}`),
          api.get<Schedule[]>(`/schedules${userId ? `?userId=${userId}` : ""}`),
          api.get<EnergyDashboard>(`/energy/dashboard${userId ? `?userId=${userId}` : ""}`).catch(() => null),
          api.get<ActivityLog[]>(`/activity-logs${userId ? `?userId=${userId}` : ""}`).catch(() => []),
        ]);
      setRooms(roomsRes);
      // Load devices per room
      const allDevices: Device[] = [];
      for (const room of roomsRes) {
        const roomDevices = await api.get<Device[]>(`/rooms/${room.id}/devices`);
        allDevices.push(...roomDevices);
      }
      setDevices(allDevices);
      setRules(rulesRes);
      setSchedules(schedulesRes);
      setEnergy(energyRes);
      const sorted = activityRes
        .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
        .slice(0, 5);
      setRecentActivity(sorted);
      api.get<IoTStatus>("/iot/status").then(setIoTStatus).catch(() => null);
    } catch {
      /* empty */
    } finally {
      setLoading(false);
    }
  }

  const activeDevices = devices.filter(
    (d) => d.switchedOn === true || (d.level !== null && d.level !== undefined && d.level > 0)
  ).length;
  const activeRules = rules.filter((r) => r.active).length;
  const activeSchedules = schedules.filter((s) => s.active).length;

  function formatTimestamp(ts: string) {
    const d = new Date(ts);
    const now = new Date();
    const diffMin = Math.floor((now.getTime() - d.getTime()) / 60000);
    if (diffMin < 1) return "Gerade eben";
    if (diffMin < 60) return `vor ${diffMin} Min.`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return `vor ${diffH} Std.`;
    return d.toLocaleDateString("de-AT", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" });
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">
          Willkommen, {user?.email?.split("@")[0]}
        </h1>
        {iotStatus && (
          <Badge variant={iotStatus.connected ? "default" : "secondary"} className="flex items-center gap-1.5 px-3 py-1">
            {iotStatus.connected ? <Wifi className="h-3.5 w-3.5" /> : <WifiOff className="h-3.5 w-3.5" />}
            IoT: {iotStatus.protocol} {iotStatus.connected ? "Connected" : "Disconnected"}
          </Badge>
        )}
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Räume</CardTitle>
            <DoorOpen className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{rooms.length}</p>
            <p className="text-xs text-muted-foreground">{devices.length} Geräte gesamt</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Aktive Geräte</CardTitle>
            <Lightbulb className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{activeDevices}</p>
            <p className="text-xs text-muted-foreground">von {devices.length} Geräten</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Regeln & Zeitpläne</CardTitle>
            <ShieldCheck className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{activeRules + activeSchedules}</p>
            <p className="text-xs text-muted-foreground">
              {activeRules} Regeln, {activeSchedules} Zeitpläne aktiv
            </p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Energie heute</CardTitle>
            <Zap className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">
              {energy?.totalTodayWh != null ? `${energy.totalTodayWh.toFixed(0)} Wh` : "–"}
            </p>
            <p className="text-xs text-muted-foreground">
              {energy?.totalWeekWh != null ? `${energy.totalWeekWh.toFixed(0)} Wh diese Woche` : "Keine Daten"}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Device Overview + Recent Activity */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* Devices by Room */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Lightbulb className="h-5 w-5" /> Geräte nach Raum
            </CardTitle>
          </CardHeader>
          <CardContent>
            {rooms.length === 0 ? (
              <p className="text-muted-foreground text-sm">Keine Räume vorhanden.</p>
            ) : (
              <div className="space-y-3">
                {rooms.map((room) => {
                  const roomDevices = devices.filter((d) => d.roomName === room.name);
                  return (
                    <div key={room.id}>
                      <div className="flex items-center justify-between">
                        <span className="font-medium text-sm">{room.name}</span>
                        <span className="text-xs text-muted-foreground">
                          {roomDevices.length} Gerät{roomDevices.length !== 1 && "e"}
                        </span>
                      </div>
                      <div className="flex flex-wrap gap-1 mt-1">
                        {roomDevices.map((d) => (
                          <span
                            key={d.id}
                            className={`text-xs px-2 py-0.5 rounded-full ${
                              d.switchedOn || (d.level && d.level > 0)
                                ? "bg-primary/10 text-primary"
                                : "bg-muted text-muted-foreground"
                            }`}
                          >
                            {d.name}
                          </span>
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Recent Activity */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Activity className="h-5 w-5" /> Letzte Aktivitäten
            </CardTitle>
          </CardHeader>
          <CardContent>
            {recentActivity.length === 0 ? (
              <p className="text-muted-foreground text-sm">Keine Aktivitäten.</p>
            ) : (
              <div className="space-y-3">
                {recentActivity.map((log) => (
                  <div key={log.id} className="flex items-start gap-3">
                    <Clock className="h-4 w-4 mt-0.5 text-muted-foreground shrink-0" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm truncate">{log.description}</p>
                      <p className="text-xs text-muted-foreground">
                        {log.deviceName} · {log.roomName} · {formatTimestamp(log.timestamp)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
