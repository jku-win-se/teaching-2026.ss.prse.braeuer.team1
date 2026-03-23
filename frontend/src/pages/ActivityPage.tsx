import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Activity, Download, Search, Filter, Clock, User, Monitor, Home } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/services/api";

interface ActivityLog {
  id: number;
  deviceId: number;
  deviceName: string;
  roomName: string;
  actor: string;
  description: string;
  timestamp: string;
}

interface Device {
  id: number;
  name: string;
}

export default function ActivityPage() {
  const [logs, setLogs] = useState<ActivityLog[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [filterDevice, setFilterDevice] = useState<string>("all");
  const [filterActor, setFilterActor] = useState<string>("all");

  const { user } = useAuth();
  const userId = user?.id;

  useEffect(() => {
    if (userId) loadData();
  }, [userId]);

  async function loadData() {
    setLoading(true);
    try {
      const [logsRes, roomsRes] = await Promise.all([
        api.get<ActivityLog[]>(`/activity-logs${userId ? `?userId=${userId}` : ""}`),
        api.get<{ id: number; name: string }[]>(`/rooms${userId ? `?userId=${userId}` : ""}`),
      ]);
      const sortedLogs = logsRes.sort(
        (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
      );
      setLogs(sortedLogs);
      // Load devices per room
      const allDevices: Device[] = [];
      for (const room of roomsRes) {
        const roomDevices = await api.get<Device[]>(`/rooms/${room.id}/devices`);
        allDevices.push(...roomDevices.map((d) => ({ id: d.id, name: d.name })));
      }
      setDevices(allDevices);
    } catch {
      /* empty */
    } finally {
      setLoading(false);
    }
  }

  async function handleExportCsv() {
    if (!userId) return;
    try {
      const csvText = await api.get<string>(`/activity-logs/export?userId=${userId}`);
      const url = window.URL.createObjectURL(new Blob([csvText]));
      const a = document.createElement("a");
      a.href = url;
      a.download = "activity-log.csv";
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      /* empty */
    }
  }

  // Unique actors for filter
  const actors = [...new Set(logs.map((l) => l.actor))].sort();

  // Filtered logs
  const filtered = logs.filter((log) => {
    const matchesSearch =
      searchTerm === "" ||
      log.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.deviceName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.roomName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.actor.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesDevice = filterDevice === "all" || log.deviceId.toString() === filterDevice;
    const matchesActor = filterActor === "all" || log.actor === filterActor;
    return matchesSearch && matchesDevice && matchesActor;
  });

  function formatTimestamp(ts: string) {
    const d = new Date(ts);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    const diffH = Math.floor(diffMs / 3600000);

    if (diffMin < 1) return "Gerade eben";
    if (diffMin < 60) return `vor ${diffMin} Min.`;
    if (diffH < 24) return `vor ${diffH} Std.`;

    return d.toLocaleDateString("de-AT", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  function getActorBadge(actor: string) {
    if (actor.toLowerCase().includes("rule") || actor.toLowerCase().includes("auto")) {
      return <Badge variant="secondary">⚡ Automation</Badge>;
    }
    if (actor.toLowerCase().includes("schedule") || actor.toLowerCase().includes("cron")) {
      return <Badge variant="secondary">🕐 Zeitplan</Badge>;
    }
    return <Badge variant="outline">👤 {actor}</Badge>;
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
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Activity className="h-8 w-8" /> Aktivitäten
          </h1>
          <p className="text-muted-foreground mt-1">
            {filtered.length} Einträge {filtered.length !== logs.length && `(von ${logs.length} gesamt)`}
          </p>
        </div>
        <Button onClick={handleExportCsv} variant="outline">
          <Download className="h-4 w-4 mr-2" /> CSV Export
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-4">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Suche in Beschreibung, Gerät, Raum, Akteur..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <Select value={filterDevice} onValueChange={setFilterDevice}>
              <SelectTrigger className="w-full sm:w-[200px]">
                <Filter className="h-4 w-4 mr-2" />
                <SelectValue placeholder="Gerät filtern" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Alle Geräte</SelectItem>
                {devices.map((d) => (
                  <SelectItem key={d.id} value={d.id.toString()}>
                    {d.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={filterActor} onValueChange={setFilterActor}>
              <SelectTrigger className="w-full sm:w-[200px]">
                <User className="h-4 w-4 mr-2" />
                <SelectValue placeholder="Akteur filtern" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Alle Akteure</SelectItem>
                {actors.map((a) => (
                  <SelectItem key={a} value={a}>
                    {a}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Heute</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {logs.filter((l) => {
                const d = new Date(l.timestamp);
                const now = new Date();
                return d.toDateString() === now.toDateString();
              }).length}
            </div>
            <p className="text-xs text-muted-foreground">Aktivitäten heute</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Geräte</CardTitle>
            <Monitor className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {new Set(logs.map((l) => l.deviceId)).size}
            </div>
            <p className="text-xs text-muted-foreground">Aktive Geräte</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Räume</CardTitle>
            <Home className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {new Set(logs.map((l) => l.roomName)).size}
            </div>
            <p className="text-xs text-muted-foreground">Betroffene Räume</p>
          </CardContent>
        </Card>
      </div>

      {/* Activity Table */}
      <Card>
        <CardHeader>
          <CardTitle>Aktivitäts-Log</CardTitle>
        </CardHeader>
        <CardContent>
          {filtered.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <Activity className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p className="text-lg font-medium">Keine Aktivitäten gefunden</p>
              <p className="text-sm">
                {searchTerm || filterDevice !== "all" || filterActor !== "all"
                  ? "Versuche andere Filterkriterien."
                  : "Sobald Geräte gesteuert werden, erscheinen hier die Einträge."}
              </p>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[180px]">Zeitpunkt</TableHead>
                    <TableHead>Gerät</TableHead>
                    <TableHead>Raum</TableHead>
                    <TableHead>Akteur</TableHead>
                    <TableHead>Beschreibung</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filtered.map((log) => (
                    <TableRow key={log.id}>
                      <TableCell className="text-muted-foreground text-sm">
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {formatTimestamp(log.timestamp)}
                        </div>
                      </TableCell>
                      <TableCell className="font-medium">{log.deviceName}</TableCell>
                      <TableCell>{log.roomName}</TableCell>
                      <TableCell>{getActorBadge(log.actor)}</TableCell>
                      <TableCell>{log.description}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
