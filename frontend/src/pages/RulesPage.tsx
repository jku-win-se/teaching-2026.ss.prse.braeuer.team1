import { useEffect, useState } from "react";
import { api } from "@/services/api";
import { useAuth } from "@/contexts/AuthContext";
import type { Rule, Device, Room, TriggerType } from "@/types/types";
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
import {
  Plus,
  Pencil,
  Trash2,
  Zap,
  Clock,
  Activity,
  Radio,
} from "lucide-react";

const TRIGGER_LABELS: Record<TriggerType, string> = {
  TIME_BASED: "Zeitgesteuert",
  THRESHOLD: "Schwellenwert",
  EVENT: "Ereignis",
};

const TRIGGER_ICONS: Record<TriggerType, typeof Clock> = {
  TIME_BASED: Clock,
  THRESHOLD: Activity,
  EVENT: Radio,
};

interface RuleForm {
  name: string;
  triggerType: TriggerType;
  triggerCondition: string;
  triggerDeviceId: string;
  triggerThresholdValue: string;
  actionDeviceId: string;
  actionValue: string;
  active: boolean;
}

const emptyForm: RuleForm = {
  name: "",
  triggerType: "TIME_BASED",
  triggerCondition: "",
  triggerDeviceId: "",
  triggerThresholdValue: "",
  actionDeviceId: "",
  actionValue: "",
  active: true,
};

export default function RulesPage() {
  const { user } = useAuth();
  const [rules, setRules] = useState<Rule[]>([]);
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<Rule | null>(null);
  const [form, setForm] = useState<RuleForm>(emptyForm);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deletingRule, setDeletingRule] = useState<Rule | null>(null);

  const fetchData = async () => {
    if (!user) return;
    try {
      const [ruleList, roomList] = await Promise.all([
        api.get<Rule[]>(`/rules?userId=${user.id}`),
        api.get<Room[]>(`/rooms?userId=${user.id}`),
      ]);
      setRules(ruleList);
      const devs: Device[] = [];
      await Promise.all(
        roomList.map(async (room) => {
          const roomDevices = await api.get<Device[]>(`/rooms/${room.id}/devices`);
          devs.push(...roomDevices);
        })
      );
      setDevices(devs);
    } catch {
      toast.error("Regeln konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const openCreate = () => {
    setEditingRule(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const openEdit = (rule: Rule) => {
    setEditingRule(rule);
    setForm({
      name: rule.name,
      triggerType: rule.triggerType,
      triggerCondition: rule.triggerCondition || "",
      triggerDeviceId: rule.triggerDeviceId?.toString() || "",
      triggerThresholdValue: rule.triggerThresholdValue?.toString() || "",
      actionDeviceId: rule.actionDeviceId.toString(),
      actionValue: rule.actionValue,
      active: rule.active,
    });
    setDialogOpen(true);
  };

  const handleSave = async () => {
    if (!user) return;
    const payload = {
      name: form.name,
      triggerType: form.triggerType,
      triggerCondition: form.triggerCondition || null,
      triggerDeviceId: form.triggerDeviceId ? Number(form.triggerDeviceId) : null,
      triggerThresholdValue: form.triggerThresholdValue
        ? Number(form.triggerThresholdValue)
        : null,
      actionDeviceId: Number(form.actionDeviceId),
      actionValue: form.actionValue,
      active: form.active,
      userId: user.id,
    };

    try {
      if (editingRule) {
        const updated = await api.put<Rule>(`/rules/${editingRule.id}`, payload);
        setRules((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
        toast.success("Regel aktualisiert.");
      } else {
        const created = await api.post<Rule>("/rules", payload);
        setRules((prev) => [...prev, created]);
        toast.success("Regel erstellt.");
      }
      setDialogOpen(false);
    } catch {
      toast.error("Fehler beim Speichern der Regel.");
    }
  };

  const confirmDelete = (rule: Rule) => {
    setDeletingRule(rule);
    setDeleteDialogOpen(true);
  };

  const handleDelete = async () => {
    if (!deletingRule) return;
    try {
      await api.delete(`/rules/${deletingRule.id}`);
      setRules((prev) => prev.filter((r) => r.id !== deletingRule.id));
      toast.success("Regel gelöscht.");
      setDeleteDialogOpen(false);
    } catch {
      toast.error("Fehler beim Löschen.");
    }
  };

  const toggleActive = async (rule: Rule) => {
    if (!user) return;
    try {
      const updated = await api.put<Rule>(`/rules/${rule.id}`, {
        name: rule.name,
        triggerType: rule.triggerType,
        triggerCondition: rule.triggerCondition,
        triggerDeviceId: rule.triggerDeviceId,
        triggerThresholdValue: rule.triggerThresholdValue,
        actionDeviceId: rule.actionDeviceId,
        actionValue: rule.actionValue,
        active: !rule.active,
        userId: user.id,
      });
      setRules((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
      toast.success(updated.active ? "Regel aktiviert." : "Regel deaktiviert.");
    } catch {
      toast.error("Fehler beim Umschalten.");
    }
  };

  const isFormValid =
    form.name.trim() &&
    form.actionDeviceId &&
    form.actionValue.trim() &&
    (form.triggerType === "TIME_BASED"
      ? form.triggerCondition.trim()
      : form.triggerDeviceId);

  if (loading) return <p className="text-muted-foreground">Lade Regeln...</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">Regeln</h1>
        <Button onClick={openCreate}>
          <Plus className="mr-2 size-4" />
          Neue Regel
        </Button>
      </div>

      {rules.length === 0 ? (
        <Card>
          <CardContent className="py-10 text-center text-muted-foreground">
            <Zap className="mx-auto mb-2 size-10" />
            <p>Noch keine Regeln vorhanden. Erstelle eine Wenn-Dann-Regel um Geräte zu automatisieren.</p>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {rules.map((rule) => {
            const TriggerIcon = TRIGGER_ICONS[rule.triggerType];
            return (
              <Card key={rule.id} className={!rule.active ? "opacity-60" : ""}>
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                  <CardTitle className="flex items-center gap-2 text-base">
                    <TriggerIcon className="size-5" />
                    {rule.name}
                  </CardTitle>
                  <div className="flex items-center gap-1">
                    <Switch
                      checked={rule.active}
                      onCheckedChange={() => toggleActive(rule)}
                    />
                  </div>
                </CardHeader>
                <CardContent className="space-y-3">
                  <Badge variant="secondary">
                    {TRIGGER_LABELS[rule.triggerType]}
                  </Badge>

                  <div className="space-y-1 text-sm">
                    <p className="text-muted-foreground">
                      <span className="font-medium text-foreground">Wenn: </span>
                      {rule.triggerType === "TIME_BASED" && (
                        <>Cron: {rule.triggerCondition}</>
                      )}
                      {rule.triggerType === "THRESHOLD" && (
                        <>
                          {rule.triggerDeviceName} Wert{" "}
                          {rule.triggerCondition || `≥ ${rule.triggerThresholdValue}`}
                        </>
                      )}
                      {rule.triggerType === "EVENT" && (
                        <>
                          {rule.triggerDeviceName}{" "}
                          {rule.triggerCondition || "Zustandsänderung"}
                        </>
                      )}
                    </p>
                    <p className="text-muted-foreground">
                      <span className="font-medium text-foreground">Dann: </span>
                      {rule.actionDeviceName} → {rule.actionValue}
                    </p>
                  </div>

                  <div className="flex gap-2 pt-1">
                    <Button variant="outline" size="sm" onClick={() => openEdit(rule)}>
                      <Pencil className="mr-1 size-3" />
                      Bearbeiten
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-destructive"
                      onClick={() => confirmDelete(rule)}
                    >
                      <Trash2 className="mr-1 size-3" />
                      Löschen
                    </Button>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create / Edit Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>
              {editingRule ? "Regel bearbeiten" : "Neue Regel erstellen"}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="ruleName">Name</Label>
              <Input
                id="ruleName"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="z.B. Bewegung schaltet Licht ein"
              />
            </div>

            <div className="space-y-2">
              <Label>Auslöser-Typ</Label>
              <Select
                value={form.triggerType}
                onValueChange={(v) =>
                  setForm({
                    ...form,
                    triggerType: v as TriggerType,
                    triggerDeviceId: "",
                    triggerThresholdValue: "",
                    triggerCondition: "",
                  })
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="TIME_BASED">Zeitgesteuert</SelectItem>
                  <SelectItem value="THRESHOLD">Schwellenwert</SelectItem>
                  <SelectItem value="EVENT">Ereignis</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {form.triggerType === "TIME_BASED" && (
              <div className="space-y-2">
                <Label htmlFor="triggerCron">Cron-Ausdruck</Label>
                <Input
                  id="triggerCron"
                  value={form.triggerCondition}
                  onChange={(e) =>
                    setForm({ ...form, triggerCondition: e.target.value })
                  }
                  placeholder="z.B. 0 22 * * * (täglich 22:00)"
                />
              </div>
            )}

            {(form.triggerType === "THRESHOLD" || form.triggerType === "EVENT") && (
              <>
                <div className="space-y-2">
                  <Label>Trigger-Gerät</Label>
                  <Select
                    value={form.triggerDeviceId}
                    onValueChange={(v) =>
                      setForm({ ...form, triggerDeviceId: v })
                    }
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

                {form.triggerType === "THRESHOLD" && (
                  <div className="space-y-2">
                    <Label htmlFor="thresholdValue">Schwellenwert</Label>
                    <Input
                      id="thresholdValue"
                      type="number"
                      value={form.triggerThresholdValue}
                      onChange={(e) =>
                        setForm({ ...form, triggerThresholdValue: e.target.value })
                      }
                      placeholder="z.B. 18.0"
                    />
                  </div>
                )}

                <div className="space-y-2">
                  <Label htmlFor="triggerConditionDesc">Bedingung (Beschreibung)</Label>
                  <Input
                    id="triggerConditionDesc"
                    value={form.triggerCondition}
                    onChange={(e) =>
                      setForm({ ...form, triggerCondition: e.target.value })
                    }
                    placeholder="z.B. Temperatur < 18°C"
                  />
                </div>
              </>
            )}

            <div className="space-y-2">
              <Label>Aktion-Gerät</Label>
              <Select
                value={form.actionDeviceId}
                onValueChange={(v) => setForm({ ...form, actionDeviceId: v })}
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
              <Label htmlFor="actionValue">Aktionswert</Label>
              <Input
                id="actionValue"
                value={form.actionValue}
                onChange={(e) => setForm({ ...form, actionValue: e.target.value })}
                placeholder="z.B. on, off, 50, 22.5"
              />
            </div>

            <div className="flex items-center gap-2">
              <Switch
                checked={form.active}
                onCheckedChange={(checked) => setForm({ ...form, active: checked })}
              />
              <Label>Regel aktiv</Label>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>
              Abbrechen
            </Button>
            <Button onClick={handleSave} disabled={!isFormValid}>
              {editingRule ? "Speichern" : "Erstellen"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Regel löschen</DialogTitle>
          </DialogHeader>
          <p className="py-4">
            Soll die Regel <strong>"{deletingRule?.name}"</strong> wirklich
            gelöscht werden?
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
