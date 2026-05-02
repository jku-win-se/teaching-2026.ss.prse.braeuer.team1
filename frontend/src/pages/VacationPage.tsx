import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import {
  Palmtree,
  Plus,
  Pencil,
  Trash2,
  CalendarDays,
  Plane,
} from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/services/api";

interface VacationMode {
  id: number;
  userId: number;
  startDate: string;
  endDate: string;
  scheduleId: number;
  scheduleName: string;
  active: boolean;
}

interface Schedule {
  id: number;
  name: string;
}

export default function VacationPage() {
  const [vacations, setVacations] = useState<VacationMode[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  // toast from sonner

  const { user } = useAuth();
  const userId = user?.id;

  const [form, setForm] = useState({
    startDate: "",
    endDate: "",
    scheduleId: "",
    active: true,
  });

  useEffect(() => {
    if (userId) loadData();
  }, [userId]);

  async function loadData() {
    setLoading(true);
    try {
      const [vacRes, schedRes] = await Promise.all([
        api.get(`/vacation-modes?userId=${userId}`),
        api.get("/schedules"),
      ]);
      setVacations(vacRes as VacationMode[]);
      setSchedules(schedRes as Schedule[]);
    } catch {
      /* empty */
    } finally {
      setLoading(false);
    }
  }

  function openCreate() {
    setEditId(null);
    setForm({ startDate: "", endDate: "", scheduleId: "", active: true });
    setDialogOpen(true);
  }

  function openEdit(v: VacationMode) {
    setEditId(v.id);
    setForm({
      startDate: v.startDate,
      endDate: v.endDate,
      scheduleId: String(v.scheduleId),
      active: v.active,
    });
    setDialogOpen(true);
  }

  async function handleSave() {
    if (!form.startDate || !form.endDate || !form.scheduleId) {
      toast.error("Bitte alle Felder ausfüllen.");
      return;
    }
    if (form.endDate < form.startDate) {
      toast.error("Enddatum muss nach Startdatum liegen.");
      return;
    }
    const body = {
      userId: Number(userId),
      startDate: form.startDate,
      endDate: form.endDate,
      scheduleId: Number(form.scheduleId),
      active: form.active,
    };
    try {
      if (editId) {
        await api.put(`/vacation-modes/${editId}`, body);
        toast.success("Urlaubsmodus aktualisiert");
      } else {
        await api.post("/vacation-modes", body);
        toast.success("Urlaubsmodus erstellt");
      }
      setDialogOpen(false);
      loadData();
    } catch {
      toast.error("Speichern fehlgeschlagen.");
    }
  }

  async function handleDelete(id: number) {
    try {
      await api.delete(`/vacation-modes/${id}`);
      toast.success("Urlaubsmodus gelöscht");
      loadData();
    } catch {
      toast.error("Löschen fehlgeschlagen.");
    }
  }

  function formatDate(d: string) {
    return new Date(d).toLocaleDateString("de-AT", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  }

  function getDurationDays(start: string, end: string) {
    const ms = new Date(end).getTime() - new Date(start).getTime();
    return Math.max(1, Math.floor(ms / 86400000) + 1);
  }

  function isCurrentlyActive(v: VacationMode) {
    if (!v.active) return false;
    const today = new Date().toISOString().split("T")[0];
    return today >= v.startDate && today <= v.endDate;
  }

  const scheduleItems = Object.fromEntries(schedules.map((s) => [String(s.id), s.name]));

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  const activeCount = vacations.filter((v) => isCurrentlyActive(v)).length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Palmtree className="h-8 w-8" /> Urlaubsmodus
          </h1>
          <p className="text-muted-foreground mt-1">
            {activeCount > 0 ? (
              <span className="text-primary font-semibold">
                {activeCount} Urlaubsmod{activeCount === 1 ? "us" : "i"} aktiv
              </span>
            ) : (
              "Kein Urlaubsmodus aktiv"
            )}
          </p>
        </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger>
            <Button onClick={openCreate}>
              <Plus className="h-4 w-4 mr-2" /> Neuer Urlaubsmodus
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editId ? "Urlaubsmodus bearbeiten" : "Neuer Urlaubsmodus"}
              </DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label>Startdatum</Label>
                  <Input
                    type="date"
                    value={form.startDate}
                    onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Enddatum</Label>
                  <Input
                    type="date"
                    value={form.endDate}
                    onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label>Zeitplan während Urlaub</Label>
                <Select
                  value={form.scheduleId}
                  items={scheduleItems}
                  onValueChange={(v) => setForm({ ...form, scheduleId: v ?? "" })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Zeitplan auswählen..." />
                  </SelectTrigger>
                  <SelectContent>
                    {schedules.map((s) => (
                      <SelectItem key={s.id} value={String(s.id)}>
                        {s.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center justify-between">
                <Label>Aktiv</Label>
                <Switch
                  checked={form.active}
                  onCheckedChange={(c) => setForm({ ...form, active: c })}
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setDialogOpen(false)}>
                Abbrechen
              </Button>
              <Button onClick={handleSave}>Speichern</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Gesamt
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{vacations.length}</div>
            <p className="text-xs text-muted-foreground">Urlaubskonfigurationen</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Aktuell aktiv
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{activeCount}</div>
            <p className="text-xs text-muted-foreground">im Urlaubsmodus</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Geplant
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {vacations.filter((v) => v.active && v.startDate > new Date().toISOString().split("T")[0]).length}
            </div>
            <p className="text-xs text-muted-foreground">zukünftige Urlaubsmodi</p>
          </CardContent>
        </Card>
      </div>

      {/* Vacation List */}
      {vacations.length === 0 ? (
        <Card>
          <CardContent className="py-12">
            <div className="text-center text-muted-foreground">
              <Plane className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p className="text-lg font-medium">Kein Urlaubsmodus konfiguriert</p>
              <p className="text-sm">
                Erstellen Sie einen Urlaubsmodus, um während Ihrer Abwesenheit
                automatisch einen speziellen Zeitplan zu aktivieren.
              </p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {vacations.map((v) => {
            const current = isCurrentlyActive(v);
            const past = v.endDate < new Date().toISOString().split("T")[0];
            const future = v.startDate > new Date().toISOString().split("T")[0];
            const days = getDurationDays(v.startDate, v.endDate);

            return (
              <Card
                key={v.id}
                className={`transition-colors ${current ? "border-primary/30 bg-primary/5" : past ? "opacity-60" : ""}`}
              >
                <CardContent className="py-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="p-2 rounded-lg bg-muted">
                        <Palmtree className={`h-6 w-6 ${current ? "text-primary" : "text-muted-foreground"}`} />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-semibold flex items-center gap-1">
                            <CalendarDays className="h-4 w-4" />
                            {formatDate(v.startDate)} – {formatDate(v.endDate)}
                          </span>
                          <Badge variant="outline">{days} Tag{days !== 1 && "e"}</Badge>
                          {current && <Badge>Aktiv</Badge>}
                          {future && v.active && (
                            <Badge variant="secondary">Geplant</Badge>
                          )}
                          {past && <Badge variant="secondary">Abgelaufen</Badge>}
                          {!v.active && <Badge variant="destructive">Deaktiviert</Badge>}
                        </div>
                        <p className="text-sm text-muted-foreground mt-1">
                          Zeitplan: <span className="font-medium">{v.scheduleName || `#${v.scheduleId}`}</span>
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button variant="ghost" size="icon" onClick={() => openEdit(v)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <AlertDialog>
                        <AlertDialogTrigger>
                          <Button variant="ghost" size="icon">
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        </AlertDialogTrigger>
                        <AlertDialogContent>
                          <AlertDialogHeader>
                            <AlertDialogTitle>Urlaubsmodus löschen?</AlertDialogTitle>
                            <AlertDialogDescription>
                              Der Urlaubsmodus vom {formatDate(v.startDate)} bis{" "}
                              {formatDate(v.endDate)} wird unwiderruflich gelöscht.
                            </AlertDialogDescription>
                          </AlertDialogHeader>
                          <AlertDialogFooter>
                            <AlertDialogCancel>Abbrechen</AlertDialogCancel>
                            <AlertDialogAction onClick={() => handleDelete(v.id)}>
                              Löschen
                            </AlertDialogAction>
                          </AlertDialogFooter>
                        </AlertDialogContent>
                      </AlertDialog>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
