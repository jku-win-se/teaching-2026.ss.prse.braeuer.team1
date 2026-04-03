import { useEffect, useState } from "react";
import { api } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import type { Room, Device, DeviceType } from "@/types/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import {
  Lightbulb,
  Thermometer,
  SunDim,
  Blinds,
  Radio,
  Pencil,
} from "lucide-react";

const DEVICE_ICONS: Record<DeviceType, typeof Lightbulb> = {
  SWITCH: Lightbulb,
  DIMMER: SunDim,
  THERMOSTAT: Thermometer,
  SENSOR: Radio,
  BLIND: Blinds,
};

const DEVICE_LABELS: Record<DeviceType, string> = {
  SWITCH: "Schalter",
  DIMMER: "Dimmer",
  THERMOSTAT: "Thermostat",
  SENSOR: "Sensor",
  BLIND: "Jalousie",
};

function levelLabel(type: DeviceType): string {
  switch (type) {
    case "DIMMER": return "Helligkeit (%)";
    case "THERMOSTAT": return "Temperatur (°C)";
    case "SENSOR": return "Sensorwert";
    case "BLIND": return "Position (0–100)";
    default: return "Wert";
  }
}

export default function DevicesPage() {
  const { user, isOwner } = useAuth();
  const [rooms, setRooms] = useState<Room[]>([]);
  const [allDevices, setAllDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);

  const [renameDialogOpen, setRenameDialogOpen] = useState(false);
  const [renamingDevice, setRenamingDevice] = useState<Device | null>(null);
  const [newName, setNewName] = useState("");

  const fetchData = async () => {
    if (!user) return;
    try {
      const roomList = await api.get<Room[]>(`/rooms${isOwner ? `?userId=${user.id}` : ""}`);
      setRooms(roomList);
      const devices: Device[] = [];
      await Promise.all(
        roomList.map(async (room) => {
          const devs = await api.get<Device[]>(`/rooms/${room.id}/devices`);
          devices.push(...devs);
        })
      );
      setAllDevices(devices);
    } catch {
      toast.error("Geräte konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const updateDeviceState = async (device: Device, switchedOn?: boolean, level?: number) => {
    try {
      const updated = await api.put<Device>(`/devices/${device.id}/state`, {
        switchedOn: switchedOn ?? device.switchedOn,
        level: level ?? device.level,
        actor: user?.email ?? "user",
      });
      setAllDevices((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
      toast.success(`${device.name} aktualisiert.`);
    } catch {
      toast.error("Fehler beim Aktualisieren.");
    }
  };

  const openRename = (device: Device) => {
    setRenamingDevice(device);
    setNewName(device.name);
    setRenameDialogOpen(true);
  };

  const handleRename = async () => {
    if (!renamingDevice || !newName.trim()) return;
    try {
      const updated = await api.put<Device>(`/devices/${renamingDevice.id}/rename`, {
        name: newName.trim(),
      });
      setAllDevices((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
      toast.success("Gerät umbenannt.");
      setRenameDialogOpen(false);
    } catch {
      toast.error("Fehler beim Umbenennen.");
    }
  };

  const getRoomName = (device: Device) => {
    return device.roomName || rooms.find((r) => r.id === device.roomId)?.name || "–";
  };

  if (loading) return <p className="text-muted-foreground">Lade Geräte...</p>;

  const grouped = rooms.map((room) => ({
    room,
    devices: allDevices.filter((d) => d.roomId === room.id),
  })).filter((g) => g.devices.length > 0);

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Geräte</h1>

      {allDevices.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            <Lightbulb className="mx-auto mb-2 size-10" />
            <p>Noch keine Geräte vorhanden. Erstelle zuerst einen Raum und füge Geräte hinzu.</p>
          </CardContent>
        </Card>
      ) : (
        grouped.map(({ room, devices }) => (
          <div key={room.id} className="space-y-3">
            <h2 className="text-xl font-semibold">{room.name}</h2>
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {devices.map((device) => {
                const Icon = DEVICE_ICONS[device.type];
                const isOn = device.switchedOn === true;
                return (
                  <Card key={device.id}>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                      <CardTitle className="flex items-center gap-2 text-base">
                        <Icon className="size-5" />
                        {device.name}
                      </CardTitle>
                      <div className="flex items-center gap-2">
                        <Badge variant="secondary">{DEVICE_LABELS[device.type]}</Badge>
                        <Button variant="ghost" size="sm" onClick={() => openRename(device)}>
                          <Pencil className="size-3" />
                        </Button>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      <p className="text-xs text-muted-foreground">
                        Raum: {getRoomName(device)}
                        {device.powerConsumptionWatt != null && ` · ${device.powerConsumptionWatt}W`}
                      </p>

                      {device.type === "SWITCH" && (
                        <div className="flex items-center justify-between">
                          <span className="text-sm">{isOn ? "Ein" : "Aus"}</span>
                          <Switch
                            checked={isOn}
                            onCheckedChange={(checked) => updateDeviceState(device, checked)}
                          />
                        </div>
                      )}

                      {device.type !== "SWITCH" && (
                        <div className="space-y-2">
                          <div className="flex items-center justify-between">
                            <Label className="text-sm">{levelLabel(device.type)}</Label>
                            <span className="text-sm font-medium">{device.level ?? 0}</span>
                          </div>
                          <input
                            type="range"
                            min={device.type === "THERMOSTAT" ? 5 : 0}
                            max={device.type === "THERMOSTAT" ? 35 : 100}
                            step={device.type === "THERMOSTAT" ? 0.5 : 1}
                            value={device.level ?? 0}
                            onChange={(e) =>
                              updateDeviceState(device, device.switchedOn ?? undefined, Number(e.target.value))
                            }
                            className="w-full accent-primary"
                          />
                        </div>
                      )}

                      {device.updatedAt && (
                        <p className="text-xs text-muted-foreground">
                          Aktualisiert: {new Date(device.updatedAt).toLocaleString("de-AT")}
                        </p>
                      )}
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>
        ))
      )}

      {/* Rename Dialog */}
      <Dialog open={renameDialogOpen} onOpenChange={setRenameDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Gerät umbenennen</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="deviceNewName">Neuer Name</Label>
              <Input
                id="deviceNewName"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleRename()}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRenameDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button onClick={handleRename} disabled={!newName.trim()}>
              Speichern
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
