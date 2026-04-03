import { useEffect, useState } from "react";
import { api } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import type { Room, Device, DeviceType } from "@/types/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import {
  DoorOpen,
  Plus,
  Pencil,
  Trash2,
  Lightbulb,
  Thermometer,
  SunDim,
  Blinds,
  Radio,
} from "lucide-react";

const DEVICE_TYPE_CONFIG: Record<DeviceType, { label: string; icon: typeof Lightbulb }> = {
  SWITCH: { label: "Schalter", icon: Lightbulb },
  DIMMER: { label: "Dimmer", icon: SunDim },
  THERMOSTAT: { label: "Thermostat", icon: Thermometer },
  SENSOR: { label: "Sensor", icon: Radio },
  BLIND: { label: "Jalousie", icon: Blinds },
};

export default function RoomsPage() {
  const { user, isOwner } = useAuth();
  const [rooms, setRooms] = useState<Room[]>([]);
  const [roomDevices, setRoomDevices] = useState<Record<number, Device[]>>({});
  const [loading, setLoading] = useState(true);

  const [roomDialogOpen, setRoomDialogOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [roomName, setRoomName] = useState("");

  const [deviceDialogOpen, setDeviceDialogOpen] = useState(false);
  const [deviceRoomId, setDeviceRoomId] = useState<number | null>(null);
  const [deviceName, setDeviceName] = useState("");
  const [deviceType, setDeviceType] = useState<DeviceType>("SWITCH");
  const [devicePower, setDevicePower] = useState("");

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingRoom, setDeletingRoom] = useState<Room | null>(null);

  const fetchRooms = async () => {
    if (!user) return;
    try {
      const data = await api.get<Room[]>(`/rooms${isOwner ? `?userId=${user.id}` : ""}`);
      setRooms(data);
      const devicesMap: Record<number, Device[]> = {};
      await Promise.all(
        data.map(async (room) => {
          devicesMap[room.id] = await api.get<Device[]>(`/rooms/${room.id}/devices`);
        })
      );
      setRoomDevices(devicesMap);
    } catch {
      toast.error("Räume konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
  }, [user]);

  const openCreateRoom = () => {
    setEditingRoom(null);
    setRoomName("");
    setRoomDialogOpen(true);
  };

  const openEditRoom = (room: Room) => {
    setEditingRoom(room);
    setRoomName(room.name);
    setRoomDialogOpen(true);
  };

  const handleSaveRoom = async () => {
    if (!user || !roomName.trim()) return;
    try {
      if (editingRoom) {
        await api.put(`/rooms/${editingRoom.id}`, { name: roomName.trim(), userId: user.id });
        toast.success("Raum umbenannt.");
      } else {
        await api.post("/rooms", { name: roomName.trim(), userId: user.id });
        toast.success("Raum erstellt.");
      }
      setRoomDialogOpen(false);
      fetchRooms();
    } catch {
      toast.error("Fehler beim Speichern.");
    }
  };

  const openDeleteRoom = (room: Room) => {
    setDeletingRoom(room);
    setDeleteDialogOpen(true);
  };

  const handleDeleteRoom = async () => {
    if (!deletingRoom) return;
    try {
      await api.delete(`/rooms/${deletingRoom.id}`);
      toast.success("Raum gelöscht.");
      setDeleteDialogOpen(false);
      fetchRooms();
    } catch {
      toast.error("Fehler beim Löschen.");
    }
  };

  const openAddDevice = (roomId: number) => {
    setDeviceRoomId(roomId);
    setDeviceName("");
    setDeviceType("SWITCH");
    setDevicePower("");
    setDeviceDialogOpen(true);
  };

  const handleAddDevice = async () => {
    if (!deviceRoomId || !deviceName.trim()) return;
    try {
      await api.post(`/rooms/${deviceRoomId}/devices`, {
        name: deviceName.trim(),
        type: deviceType,
        powerConsumptionWatt: devicePower ? Number(devicePower) : null,
      });
      toast.success("Gerät hinzugefügt.");
      setDeviceDialogOpen(false);
      fetchRooms();
    } catch {
      toast.error("Fehler beim Hinzufügen.");
    }
  };

  const handleDeleteDevice = async (deviceId: number) => {
    try {
      await api.delete(`/devices/${deviceId}`);
      toast.success("Gerät gelöscht.");
      fetchRooms();
    } catch {
      toast.error("Fehler beim Löschen.");
    }
  };

  if (loading) return <p className="text-muted-foreground">Lade Räume...</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Räume</h1>
        <Button onClick={openCreateRoom}>
          <Plus className="mr-2 size-4" />
          Raum erstellen
        </Button>
      </div>

      {rooms.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            <DoorOpen className="mx-auto mb-2 size-10" />
            <p>Noch keine Räume vorhanden.</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {rooms.map((room) => {
            const devices = roomDevices[room.id] || [];
            return (
              <Card key={room.id}>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    <DoorOpen className="size-5" />
                    {room.name}
                  </CardTitle>
                  <div className="flex gap-1">
                    <Button variant="ghost" size="sm" onClick={() => openEditRoom(room)}>
                      <Pencil className="size-4" />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => openDeleteRoom(room)}>
                      <Trash2 className="size-4" />
                    </Button>
                  </div>
                </CardHeader>
                <CardContent className="space-y-3">
                  {devices.length === 0 ? (
                    <p className="text-sm text-muted-foreground">Keine Geräte</p>
                  ) : (
                    <div className="space-y-2">
                      {devices.map((device) => {
                        const cfg = DEVICE_TYPE_CONFIG[device.type];
                        const Icon = cfg.icon;
                        return (
                          <div
                            key={device.id}
                            className="flex items-center justify-between rounded-md border p-2"
                          >
                            <div className="flex items-center gap-2">
                              <Icon className="size-4 text-muted-foreground" />
                              <span className="text-sm">{device.name}</span>
                              <Badge variant="secondary" className="text-xs">
                                {cfg.label}
                              </Badge>
                            </div>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleDeleteDevice(device.id)}
                            >
                              <Trash2 className="size-3" />
                            </Button>
                          </div>
                        );
                      })}
                    </div>
                  )}
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full"
                    onClick={() => openAddDevice(room.id)}
                  >
                    <Plus className="mr-2 size-4" />
                    Gerät hinzufügen
                  </Button>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create/Edit Room Dialog */}
      <Dialog open={roomDialogOpen} onOpenChange={setRoomDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingRoom ? "Raum umbenennen" : "Neuer Raum"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="roomName">Name</Label>
              <Input
                id="roomName"
                value={roomName}
                onChange={(e) => setRoomName(e.target.value)}
                placeholder="z.B. Wohnzimmer"
                onKeyDown={(e) => e.key === "Enter" && handleSaveRoom()}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRoomDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button onClick={handleSaveRoom} disabled={!roomName.trim()}>
              {editingRoom ? "Speichern" : "Erstellen"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Room Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Raum löschen</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Möchtest du den Raum <strong>{deletingRoom?.name}</strong> wirklich löschen?
            Alle Geräte im Raum werden ebenfalls entfernt.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button variant="destructive" onClick={handleDeleteRoom}>
              Löschen
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Add Device Dialog */}
      <Dialog open={deviceDialogOpen} onOpenChange={setDeviceDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Gerät hinzufügen</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="deviceName">Name</Label>
              <Input
                id="deviceName"
                value={deviceName}
                onChange={(e) => setDeviceName(e.target.value)}
                placeholder="z.B. Deckenlampe"
              />
            </div>
            <div className="space-y-2">
              <Label>Typ</Label>
              <Select value={deviceType} onValueChange={(v) => setDeviceType(v as DeviceType)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(DEVICE_TYPE_CONFIG).map(([key, cfg]) => (
                    <SelectItem key={key} value={key}>
                      {cfg.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label htmlFor="devicePower">Leistung (Watt, optional)</Label>
              <Input
                id="devicePower"
                type="number"
                value={devicePower}
                onChange={(e) => setDevicePower(e.target.value)}
                placeholder="z.B. 60"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeviceDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button onClick={handleAddDevice} disabled={!deviceName.trim()}>
              Hinzufügen
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
