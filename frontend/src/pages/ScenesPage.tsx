import { useEffect, useState } from "react";
import { api } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import type { Room, Device, Scene } from "@/types/types";
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
import { Cog, Plus, Trash2, Play, Pencil } from "lucide-react";

interface DeviceStateEntry {
  deviceId: number;
  targetSwitchedOn: boolean | null;
  targetLevel: number | null;
}

export default function ScenesPage() {
  const { user, isOwner } = useAuth();
  const [scenes, setScenes] = useState<Scene[]>([]);
  const [allDevices, setAllDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingScene, setEditingScene] = useState<Scene | null>(null);
  const [sceneName, setSceneName] = useState("");
  const [deviceStates, setDeviceStates] = useState<DeviceStateEntry[]>([]);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingScene, setDeletingScene] = useState<Scene | null>(null);

  const fetchData = async () => {
    if (!user) return;
    try {
      const [sceneList, rooms] = await Promise.all([
        api.get<Scene[]>(`/scenes${isOwner ? `?userId=${user.id}` : ""}`),
        api.get<Room[]>(`/rooms${isOwner ? `?userId=${user.id}` : ""}`),
      ]);
      setScenes(sceneList);
      const devices: Device[] = [];
      await Promise.all(
        rooms.map(async (room) => {
          const devs = await api.get<Device[]>(`/rooms/${room.id}/devices`);
          devices.push(...devs);
        })
      );
      setAllDevices(devices);
    } catch {
      toast.error("Szenen konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const openCreate = () => {
    setEditingScene(null);
    setSceneName("");
    setDeviceStates([]);
    setDialogOpen(true);
  };

  const openEdit = (scene: Scene) => {
    setEditingScene(scene);
    setSceneName(scene.name);
    setDeviceStates(
      scene.deviceStates.map((ds) => ({
        deviceId: ds.deviceId,
        targetSwitchedOn: ds.targetSwitchedOn,
        targetLevel: ds.targetLevel,
      }))
    );
    setDialogOpen(true);
  };

  const addDeviceToScene = (deviceId: number) => {
    if (deviceStates.some((ds) => ds.deviceId === deviceId)) return;
    const device = allDevices.find((d) => d.id === deviceId);
    if (!device) return;
    setDeviceStates((prev) => [
      ...prev,
      {
        deviceId,
        targetSwitchedOn: device.type === "SWITCH" ? false : null,
        targetLevel: device.type !== "SWITCH" ? 0 : null,
      },
    ]);
  };

  const removeDeviceFromScene = (deviceId: number) => {
    setDeviceStates((prev) => prev.filter((ds) => ds.deviceId !== deviceId));
  };

  const updateEntry = (deviceId: number, patch: Partial<DeviceStateEntry>) => {
    setDeviceStates((prev) =>
      prev.map((ds) => (ds.deviceId === deviceId ? { ...ds, ...patch } : ds))
    );
  };

  const handleSave = async () => {
    if (!user || !sceneName.trim()) return;
    try {
      const body = {
        name: sceneName.trim(),
        userId: user.id,
        deviceStates: deviceStates.map((ds) => ({
          deviceId: ds.deviceId,
          targetSwitchedOn: ds.targetSwitchedOn,
          targetLevel: ds.targetLevel,
        })),
      };
      if (editingScene) {
        await api.put(`/scenes/${editingScene.id}`, body);
        toast.success("Szene aktualisiert.");
      } else {
        await api.post("/scenes", body);
        toast.success("Szene erstellt.");
      }
      setDialogOpen(false);
      fetchData();
    } catch {
      toast.error("Fehler beim Speichern.");
    }
  };

  const handleActivate = async (scene: Scene) => {
    try {
      const res = await api.post<{ message: string; devicesChanged: number }>(
        `/scenes/${scene.id}/activate`
      );
      toast.success(`${res.message} (${res.devicesChanged} Geräte)`);
      fetchData();
    } catch {
      toast.error("Fehler beim Aktivieren.");
    }
  };

  const openDelete = (scene: Scene) => {
    setDeletingScene(scene);
    setDeleteDialogOpen(true);
  };

  const handleDelete = async () => {
    if (!deletingScene) return;
    try {
      await api.delete(`/scenes/${deletingScene.id}`);
      toast.success("Szene gelöscht.");
      setDeleteDialogOpen(false);
      fetchData();
    } catch {
      toast.error("Fehler beim Löschen.");
    }
  };

  const getDevice = (id: number) => allDevices.find((d) => d.id === id);
  const availableDevices = allDevices.filter(
    (d) => !deviceStates.some((ds) => ds.deviceId === d.id)
  );

  if (loading) return <p className="text-muted-foreground">Lade Szenen...</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Szenen</h1>
        <Button onClick={openCreate}>
          <Plus className="mr-2 size-4" />
          Szene erstellen
        </Button>
      </div>

      {scenes.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            <Cog className="mx-auto mb-2 size-10" />
            <p>Noch keine Szenen vorhanden.</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {scenes.map((scene) => (
            <Card key={scene.id}>
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  <Cog className="size-5" />
                  {scene.name}
                </CardTitle>
                <div className="flex gap-1">
                  <Button variant="ghost" size="sm" onClick={() => handleActivate(scene)}>
                    <Play className="size-4" />
                  </Button>
                  <Button variant="ghost" size="sm" onClick={() => openEdit(scene)}>
                    <Pencil className="size-4" />
                  </Button>
                  <Button variant="ghost" size="sm" onClick={() => openDelete(scene)}>
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                {scene.deviceStates.length === 0 ? (
                  <p className="text-sm text-muted-foreground">Keine Geräte konfiguriert</p>
                ) : (
                  <div className="space-y-1">
                    {scene.deviceStates.map((ds) => (
                      <div key={ds.id} className="flex items-center justify-between text-sm">
                        <span>{ds.deviceName}</span>
                        <Badge variant="outline">
                          {ds.targetSwitchedOn != null
                            ? ds.targetSwitchedOn
                              ? "Ein"
                              : "Aus"
                            : `${ds.targetLevel}`}
                        </Badge>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Create/Edit Scene Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{editingScene ? "Szene bearbeiten" : "Neue Szene"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="sceneName">Name</Label>
              <Input
                id="sceneName"
                value={sceneName}
                onChange={(e) => setSceneName(e.target.value)}
                placeholder="z.B. Filmabend"
              />
            </div>

            <div className="space-y-2">
              <Label>Geräte in der Szene</Label>
              {deviceStates.length === 0 && (
                <p className="text-sm text-muted-foreground">
                  Füge Geräte hinzu, um ihre Zielzustände festzulegen.
                </p>
              )}
              {deviceStates.map((entry) => {
                const device = getDevice(entry.deviceId);
                if (!device) return null;
                return (
                  <div key={entry.deviceId} className="flex items-center gap-3 rounded-md border p-3">
                    <div className="flex-1">
                      <p className="text-sm font-medium">{device.name}</p>
                      <p className="text-xs text-muted-foreground">{device.roomName}</p>
                    </div>
                    {device.type === "SWITCH" ? (
                      <Switch
                        checked={entry.targetSwitchedOn === true}
                        onCheckedChange={(checked) =>
                          updateEntry(entry.deviceId, { targetSwitchedOn: checked })
                        }
                      />
                    ) : (
                      <Input
                        type="number"
                        className="w-20"
                        value={entry.targetLevel ?? 0}
                        onChange={(e) =>
                          updateEntry(entry.deviceId, { targetLevel: Number(e.target.value) })
                        }
                      />
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => removeDeviceFromScene(entry.deviceId)}
                    >
                      <Trash2 className="size-3" />
                    </Button>
                  </div>
                );
              })}
            </div>

            {availableDevices.length > 0 && (
              <div className="space-y-2">
                <Label>Gerät hinzufügen</Label>
                <div className="flex flex-wrap gap-2">
                  {availableDevices.map((device) => (
                    <Button
                      key={device.id}
                      variant="outline"
                      size="sm"
                      onClick={() => addDeviceToScene(device.id)}
                    >
                      <Plus className="mr-1 size-3" />
                      {device.name}
                    </Button>
                  ))}
                </div>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button onClick={handleSave} disabled={!sceneName.trim()}>
              {editingScene ? "Speichern" : "Erstellen"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Szene löschen</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Möchtest du die Szene <strong>{deletingScene?.name}</strong> wirklich löschen?
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              Löschen
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
