import { useEffect, useState } from "react";
import { api } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import type { EnergyDashboard } from "@/types/types";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { toast } from "sonner";
import { Download, Zap, Home, Cpu } from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";

const COLORS = [
  "hsl(221, 83%, 53%)",
  "hsl(262, 83%, 58%)",
  "hsl(173, 58%, 39%)",
  "hsl(43, 96%, 56%)",
  "hsl(346, 77%, 49%)",
  "hsl(199, 89%, 48%)",
  "hsl(24, 95%, 53%)",
  "hsl(142, 71%, 45%)",
];

function formatWh(wh: number): string {
  if (wh >= 1000) return `${(wh / 1000).toFixed(2)} kWh`;
  return `${wh.toFixed(1)} Wh`;
}

export default function EnergyPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState<EnergyDashboard | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchDashboard = async () => {
    if (!user) return;
    try {
      const data = await api.get<EnergyDashboard>(
        `/energy/dashboard?userId=${user.id}`
      );
      setDashboard(data);
    } catch {
      toast.error("Energiedaten konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, [user]);

  const handleExport = async () => {
    if (!user) return;
    try {
      const blob = await api.getBlob(`/energy/export?userId=${user.id}`);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "energy-summary.csv";
      a.click();
      URL.revokeObjectURL(url);
      toast.success("CSV exportiert.");
    } catch {
      toast.error("Fehler beim Export.");
    }
  };

  if (loading) return <p className="text-muted-foreground">Lade Energiedaten...</p>;

  if (!dashboard) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Energieverbrauch</h1>
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            <Zap className="mx-auto mb-2 size-10" />
            <p>Keine Energiedaten verfügbar.</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const deviceChartData = dashboard.byDevice
    .sort((a, b) => b.weekWh - a.weekWh)
    .map((d) => ({
      name: d.deviceName,
      heute: Math.round(d.todayWh * 10) / 10,
      woche: Math.round(d.weekWh * 10) / 10,
    }));

  const roomPieData = dashboard.byRoom
    .filter((r) => r.weekWh > 0)
    .map((r) => ({
      name: r.roomName,
      value: Math.round(r.weekWh * 10) / 10,
    }));

  const roomBarData = dashboard.byRoom
    .sort((a, b) => b.weekWh - a.weekWh)
    .map((r) => ({
      name: r.roomName,
      heute: Math.round(r.todayWh * 10) / 10,
      woche: Math.round(r.weekWh * 10) / 10,
    }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Energieverbrauch</h1>
        <Button variant="outline" onClick={handleExport}>
          <Download className="mr-2 size-4" />
          CSV Export
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Heute gesamt</CardTitle>
            <Zap className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatWh(dashboard.totalTodayWh)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">
              Diese Woche gesamt
            </CardTitle>
            <Zap className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatWh(dashboard.totalWeekWh)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Geräte</CardTitle>
            <Cpu className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {dashboard.byDevice.length}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium">Räume</CardTitle>
            <Home className="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {dashboard.byRoom.length}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Tabs: by Device / by Room */}
      <Tabs defaultValue="devices">
        <TabsList>
          <TabsTrigger value="devices">Nach Gerät</TabsTrigger>
          <TabsTrigger value="rooms">Nach Raum</TabsTrigger>
        </TabsList>

        <TabsContent value="devices" className="space-y-4">
          {dashboard.byDevice.length === 0 ? (
            <p className="text-muted-foreground">
              Keine Gerätedaten vorhanden.
            </p>
          ) : (
            <>
              {/* Bar Chart - Devices */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">
                    Verbrauch pro Gerät (Wh)
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={350}>
                    <BarChart data={deviceChartData}>
                      <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                      <XAxis
                        dataKey="name"
                        tick={{ fontSize: 12 }}
                        angle={-25}
                        textAnchor="end"
                        height={60}
                      />
                      <YAxis tick={{ fontSize: 12 }} />
                      <Tooltip
                        formatter={(value: number) => [`${value} Wh`]}
                        contentStyle={{
                          backgroundColor: "#fff",
                          color: "#333",
                          border: "1px solid #e5e7eb",
                          borderRadius: "6px",
                        }}
                        cursor={{ fill: "rgba(0, 0, 0, 0.05)" }}
                      />
                      <Legend />
                      <Bar
                        dataKey="heute"
                        name="Heute"
                        fill="hsl(221, 83%, 53%)"
                        radius={[4, 4, 0, 0]}
                      />
                      <Bar
                        dataKey="woche"
                        name="Woche"
                        fill="hsl(262, 83%, 58%)"
                        radius={[4, 4, 0, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              {/* Table - Devices */}
              <Card>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Gerät</TableHead>
                      <TableHead>Raum</TableHead>
                      <TableHead className="text-right">Heute</TableHead>
                      <TableHead className="text-right">Woche</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {dashboard.byDevice
                      .sort((a, b) => b.weekWh - a.weekWh)
                      .map((device) => (
                        <TableRow key={device.deviceId}>
                          <TableCell className="font-medium">
                            {device.deviceName}
                          </TableCell>
                          <TableCell>{device.roomName}</TableCell>
                          <TableCell className="text-right">
                            {formatWh(device.todayWh)}
                          </TableCell>
                          <TableCell className="text-right">
                            {formatWh(device.weekWh)}
                          </TableCell>
                        </TableRow>
                      ))}
                  </TableBody>
                </Table>
              </Card>
            </>
          )}
        </TabsContent>

        <TabsContent value="rooms" className="space-y-4">
          {dashboard.byRoom.length === 0 ? (
            <p className="text-muted-foreground">
              Keine Raumdaten vorhanden.
            </p>
          ) : (
            <>
              {/* Charts - Rooms: Bar + Pie side by side */}
              <div className="grid gap-4 lg:grid-cols-2">
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">
                      Verbrauch pro Raum (Wh)
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart data={roomBarData}>
                        <CartesianGrid
                          strokeDasharray="3 3"
                          className="stroke-muted"
                        />
                        <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                        <YAxis tick={{ fontSize: 12 }} />
                        <Tooltip
                          formatter={(value: number) => [`${value} Wh`]}
                          contentStyle={{
                            backgroundColor: "#fff",
                            color: "#333",
                            border: "1px solid #e5e7eb",
                            borderRadius: "6px",
                          }}
                          cursor={{ fill: "rgba(0, 0, 0, 0.05)" }}
                        />
                        <Legend />
                        <Bar
                          dataKey="heute"
                          name="Heute"
                          fill="hsl(173, 58%, 39%)"
                          radius={[4, 4, 0, 0]}
                        />
                        <Bar
                          dataKey="woche"
                          name="Woche"
                          fill="hsl(43, 96%, 56%)"
                          radius={[4, 4, 0, 0]}
                        />
                      </BarChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">
                      Verteilung diese Woche
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    {roomPieData.length > 0 ? (
                      <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                          <Pie
                            data={roomPieData}
                            cx="50%"
                            cy="50%"
                            innerRadius={60}
                            outerRadius={100}
                            paddingAngle={3}
                            dataKey="value"
                            label={({ name, value }) =>
                              `${name}: ${value} Wh`
                            }
                          >
                            {roomPieData.map((_, index) => (
                              <Cell
                                key={index}
                                fill={COLORS[index % COLORS.length]}
                              />
                            ))}
                          </Pie>
                          <Tooltip
                            formatter={(value: number) => [`${value} Wh`]}
                            contentStyle={{
                              backgroundColor: "#fff",
                              color: "#333",
                              border: "1px solid #e5e7eb",
                              borderRadius: "6px",
                            }}
                          />
                        </PieChart>
                      </ResponsiveContainer>
                    ) : (
                      <p className="py-10 text-center text-muted-foreground">
                        Keine Verbrauchsdaten diese Woche.
                      </p>
                    )}
                  </CardContent>
                </Card>
              </div>

              {/* Table - Rooms */}
              <Card>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Raum</TableHead>
                      <TableHead className="text-right">Heute</TableHead>
                      <TableHead className="text-right">Woche</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {dashboard.byRoom
                      .sort((a, b) => b.weekWh - a.weekWh)
                      .map((room) => (
                        <TableRow key={room.roomId}>
                          <TableCell className="font-medium">
                            {room.roomName}
                          </TableCell>
                          <TableCell className="text-right">
                            {formatWh(room.todayWh)}
                          </TableCell>
                          <TableCell className="text-right">
                            {formatWh(room.weekWh)}
                          </TableCell>
                        </TableRow>
                      ))}
                  </TableBody>
                </Table>
              </Card>
            </>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
