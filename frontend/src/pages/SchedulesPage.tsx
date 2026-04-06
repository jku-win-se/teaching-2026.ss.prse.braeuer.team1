import { useEffect, useState } from "react";
import { api } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import type { Schedule, Device, Room, Conflict } from "@/types/types";
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { Plus, Pencil, Trash2, CalendarClock, AlertTriangle } from "lucide-react";

/** Translates a 5-field cron expression into a human-readable German string. */
function cronToHuman(cron: string): string {
  const parts = cron.split(" ");
  if (parts.length !== 5) return cron;
  const [minute, hour, , , dow] = parts;
  const time = `${hour.padStart(2, "0")}:${minute.padStart(2, "0")}`;
  if (dow === "*") return `Täglich um ${time}`;
  if (dow === "MON-FRI" || dow === "1-5") return `Werktags um ${time}`;
  if (dow === "SAT,SUN" || dow === "6,0") return `Wochenende um ${time}`;
  return `${time} (${dow})`;
}

interface ScheduleForm {
  name: string;
  cronExpression: string;
  deviceId: string;
  actionValue: string;
  active: boolean;
}

const emptyForm: ScheduleForm = {
  name: "",
  cronExpression: "",
  deviceId: "",
  actionValue: "",
  active: true,
};

export default function SchedulesPage() {
  const { user, isOwner } = useAuth();
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [conflicts, setConflicts] = useState<Conflict[]>([]);
  const [loading, setLoading] = useState(true);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingSchedule, setEditingSchedule] = useState<Schedule | null>(null);
  const [form, setForm] = useState<ScheduleForm>(emptyForm);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingSchedule, setDeletingSchedule] = useState<Schedule | null>(null);

  const fetchConflicts = async () => {
    if (!user) return;
    try {
      const all = await api.get<Conflict[]>(`/conflicts?userId=${user.id}`);
      setConflicts(all.filter((c) => c.conflictType.includes("SCHEDULE")));
    } catch {
      // ignore — conflicts are non-critical
    }
  };

  const fetchData = async () => {
    if (!user) return;
    try {
      const [scheduleList, roomList] = await Promise.all([
        api.get<Schedule[]>(`/schedules${isOwner ? `?userId=${user.id}` : ""}`),
        api.get<Room[]>(`/rooms${isOwner ? `?userId=${user.id}` : ""}`),
      ]);
      setSchedules(scheduleList);
      const devs: Device[] = [];
      await Promise.all(
        roomList.map(async (room) => {
          const roomDevices = await api.get<Device[]>(`/rooms/${room.id}/devices`);
          devs.push(...roomDevices);
        })
      );
      setDevices(devs);
      fetchConflicts();
    } catch {
      toast.error("Zeitpläne konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const openCreate = () => {
    setEditingSchedule(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const openEdit = (schedule: Schedule) => {
    setEditingSchedule(schedule);
    setForm({
      name: schedule.name,
      cronExpression: schedule.cronExpression,
      deviceId: schedule.deviceId.toString(),
      actionValue: schedule.actionValue,
      active: schedule.active,
    });
    setDialogOpen(true);
  };

  const handleSave = async () => {
    if (!user) return;
    const payload = {
      name: form.name,
      cronExpression: form.cronExpression,
      deviceId: Number(form.deviceId),
      actionValue: form.actionValue,
      active: form.active,
      userId: user.id,
    };

    try {
      if (editingSchedule) {
        const updated = await api.put<Schedule>(
          `/schedules/${editingSchedule.id}`,
          payload
        );
        setSchedules((prev) =>
          prev.map((s) => (s.id === updated.id ? updated : s))
        );
        toast.success("Zeitplan aktualisiert.");
      } else {
        const created = await api.post<Schedule>("/schedules", payload);
        setSchedules((prev) => [...prev, created]);
        toast.success("Zeitplan erstellt.");
      }
      setDialogOpen(false);
    } catch (err: unknown) {
      if (err instanceof Error && "status" in err && (err as { status: number }).status === 409) {
        toast.error("Konflikt: Ein aktiver Zeitplan existiert bereits für dieses Gerät zur gleichen Zeit.");
      } else {
        toast.error("Fehler beim Speichern des Zeitplans.");
      }
    }
  };

  const confirmDelete = (schedule: Schedule) => {
    setDeletingSchedule(schedule);
    setDeleteDialogOpen(true);
  };

  const handleDelete = async () => {
    if (!deletingSchedule) return;
    try {
      await api.delete(`/schedules/${deletingSchedule.id}`);
      setSchedules((prev) => prev.filter((s) => s.id !== deletingSchedule.id));
      toast.success("Zeitplan gelöscht.");
      setDeleteDialogOpen(false);
    } catch {
      toast.error("Fehler beim Löschen.");
    }
  };

  const toggleActive = async (schedule: Schedule) => {
    if (!user) return;
    try {
      const updated = await api.put<Schedule>(`/schedules/${schedule.id}`, {
        name: schedule.name,
        cronExpression: schedule.cronExpression,
        deviceId: schedule.deviceId,
        actionValue: schedule.actionValue,
        active: !schedule.active,
        userId: user.id,
      });
      setSchedules((prev) =>
        prev.map((s) => (s.id === updated.id ? updated : s))
      );
      toast.success(updated.active ? "Zeitplan aktiviert." : "Zeitplan deaktiviert.");
    } catch {
      toast.error("Fehler beim Umschalten.");
    }
  };

  const isFormValid =
    form.name.trim() &&
    form.cronExpression.trim() &&
    form.deviceId &&
    form.actionValue.trim();

  if (loading) return <p className="text-muted-foreground">Lade Zeitpläne...</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Zeitpläne</h1>
        <Button onClick={openCreate}>
          <Plus className="mr-2 size-4" />
          Neuer Zeitplan
        </Button>
      </div>

      {conflicts.length > 0 && (
        <Card className="border-yellow-500 bg-yellow-50 dark:bg-yellow-950/20">
          <CardContent className="flex items-start gap-3 py-4">
            <AlertTriangle className="mt-0.5 size-5 shrink-0 text-yellow-600" />
            <div className="space-y-1">
              <p className="font-medium text-yellow-800 dark:text-yellow-400">
                Konflikte erkannt
              </p>
              {conflicts.map((c, i) => (
                <p key={i} className="text-sm text-yellow-700 dark:text-yellow-500">
                  {c.message}
                </p>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {schedules.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            <CalendarClock className="mx-auto mb-2 size-10" />
            <p>Noch keine Zeitpläne vorhanden. Erstelle einen Zeitplan um Geräte zeitgesteuert zu steuern.</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {schedules.map((schedule) => (
            <Card
              key={schedule.id}
              className={!schedule.active ? "opacity-60" : ""}
            >
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="flex items-center gap-2 text-base">
                  <CalendarClock className="size-5" />
                  {schedule.name}
                </CardTitle>
                <Switch
                  checked={schedule.active}
                  onCheckedChange={() => toggleActive(schedule)}
                />
              </CardHeader>
              <CardContent className="space-y-3">
                <Badge variant="secondary">
                  {cronToHuman(schedule.cronExpression)}
                </Badge>

                <div className="space-y-1 text-sm">
                  <p className="text-muted-foreground">
                    <span className="font-medium text-foreground">Cron: </span>
                    <code className="rounded bg-muted px-1 py-0.5 text-xs">
                      {schedule.cronExpression}
                    </code>
                  </p>
                  <p className="text-muted-foreground">
                    <span className="font-medium text-foreground">Gerät: </span>
                    {schedule.deviceName}
                  </p>
                  <p className="text-muted-foreground">
                    <span className="font-medium text-foreground">Aktion: </span>
                    {schedule.actionValue}
                  </p>
                </div>

                <div className="flex gap-2 pt-1">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => openEdit(schedule)}
                  >
                    <Pencil className="mr-1 size-3" />
                    Bearbeiten
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="text-destructive"
                    onClick={() => confirmDelete(schedule)}
                  >
                    <Trash2 className="mr-1 size-3" />
                    Löschen
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Create / Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>
              {editingSchedule ? "Zeitplan bearbeiten" : "Neuen Zeitplan erstellen"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="scheduleName">Name</Label>
              <Input
                id="scheduleName"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="z.B. Morgens Licht an"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="scheduleCron">Cron-Ausdruck</Label>
              <Input
                id="scheduleCron"
                value={form.cronExpression}
                onChange={(e) =>
                  setForm({ ...form, cronExpression: e.target.value })
                }
                placeholder="z.B. 0 7 * * MON-FRI"
              />
              <p className="text-xs text-muted-foreground">
                Format: Minute Stunde Tag Monat Wochentag (z.B. 0 7 * * MON-FRI = Werktags 07:00)
              </p>
            </div>

            <div className="space-y-2">
              <Label>Gerät</Label>
              <Select
                value={form.deviceId}
                onValueChange={(v) => setForm({ ...form, deviceId: v ?? "" })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Gerät wählen" />
                </SelectTrigger>
                <SelectContent>
                  {devices.map((d) => (
                    <SelectItem key={d.id} value={d.id.toString()}>
                      {d.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="scheduleAction">Aktionswert</Label>
              <Input
                id="scheduleAction"
                value={form.actionValue}
                onChange={(e) =>
                  setForm({ ...form, actionValue: e.target.value })
                }
                placeholder="z.B. true, false, 75.0, 22.5"
              />
            </div>

            <div className="flex items-center gap-2">
              <Switch
                checked={form.active}
                onCheckedChange={(checked) =>
                  setForm({ ...form, active: checked })
                }
              />
              <Label>Zeitplan aktiv</Label>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button onClick={handleSave} disabled={!isFormValid}>
              {editingSchedule ? "Speichern" : "Erstellen"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Zeitplan löschen</DialogTitle>
          </DialogHeader>
          <p className="py-4">
            Soll der Zeitplan <strong>"{deletingSchedule?.name}"</strong>{" "}
            wirklich gelöscht werden?
          </p>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setDeleteDialogOpen(false)}
            >
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
