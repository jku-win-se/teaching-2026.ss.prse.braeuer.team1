import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
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
import { Users, Plus, Trash2, UserPlus, Shield, User } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/services/api";

interface Member {
  id: number;
  email: string;
  role: "OWNER" | "MEMBER";
  createdAt: string;
}

export default function MembersPage() {
  const [members, setMembers] = useState<Member[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);

  const { user, isOwner } = useAuth();

  const [form, setForm] = useState({
    email: "",
    temporaryPassword: "",
  });

  useEffect(() => {
    loadMembers();
  }, []);

  async function loadMembers() {
    setLoading(true);
    try {
      const res = await api.get("/users");
      setMembers(res as Member[]);
    } catch {
      /* empty */
    } finally {
      setLoading(false);
    }
  }

  function openInvite() {
    setForm({ email: "", temporaryPassword: "" });
    setDialogOpen(true);
  }

  async function handleInvite() {
    if (!form.email || !form.temporaryPassword) {
      toast.error("Bitte alle Felder ausfüllen.");
      return;
    }
    if (form.temporaryPassword.length < 6) {
      toast.error("Passwort muss mindestens 6 Zeichen haben.");
      return;
    }
    try {
      await api.post(`/users/${user?.id}/invite`, {
        email: form.email,
        temporaryPassword: form.temporaryPassword,
      });
      toast.success("Mitglied erfolgreich eingeladen");
      setDialogOpen(false);
      loadMembers();
    } catch {
      toast.error("Einladung fehlgeschlagen. E-Mail bereits vergeben?");
    }
  }

  async function handleRevoke(memberId: number) {
    try {
      await api.delete(`/users/${memberId}/revoke`);
      toast.success("Zugriff widerrufen");
      loadMembers();
    } catch {
      toast.error("Widerrufen fehlgeschlagen.");
    }
  }

  function formatDate(d: string) {
    return new Date(d).toLocaleDateString("de-AT", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  const owners = members.filter((m) => m.role === "OWNER");
  const regularMembers = members.filter((m) => m.role === "MEMBER");

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Users className="h-8 w-8" /> Mitglieder
          </h1>
          <p className="text-muted-foreground mt-1">
            {members.length} Mitglied{members.length !== 1 && "er"} im Haushalt
          </p>
        </div>
        {isOwner && (
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger>
              <Button onClick={openInvite}>
                <Plus className="h-4 w-4 mr-2" /> Mitglied einladen
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Neues Mitglied einladen</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label>E-Mail-Adresse</Label>
                  <Input
                    type="email"
                    placeholder="mitglied@example.com"
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Temporäres Passwort</Label>
                  <Input
                    type="text"
                    placeholder="Min. 6 Zeichen"
                    value={form.temporaryPassword}
                    onChange={(e) =>
                      setForm({ ...form, temporaryPassword: e.target.value })
                    }
                  />
                  <p className="text-xs text-muted-foreground">
                    Das Mitglied kann sich mit diesem Passwort anmelden.
                  </p>
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setDialogOpen(false)}>
                  Abbrechen
                </Button>
                <Button onClick={handleInvite}>Einladen</Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
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
            <div className="text-2xl font-bold">{members.length}</div>
            <p className="text-xs text-muted-foreground">Mitglieder</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Eigentümer
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary">{owners.length}</div>
            <p className="text-xs text-muted-foreground">mit vollen Rechten</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Mitglieder
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{regularMembers.length}</div>
            <p className="text-xs text-muted-foreground">mit eingeschränkten Rechten</p>
          </CardContent>
        </Card>
      </div>

      {/* Members List */}
      {members.length === 0 ? (
        <Card>
          <CardContent className="py-12">
            <div className="text-center text-muted-foreground">
              <UserPlus className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p className="text-lg font-medium">Keine Mitglieder vorhanden</p>
              <p className="text-sm">
                Laden Sie Mitglieder ein, um Ihren Haushalt gemeinsam zu verwalten.
              </p>
            </div>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-3">
          {members.map((m) => {
            const isSelf = m.id === user?.id;

            return (
              <Card
                key={m.id}
                className={`transition-colors ${isSelf ? "border-primary/30 bg-primary/5" : ""}`}
              >
                <CardContent className="py-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="p-2 rounded-lg bg-muted">
                        {m.role === "OWNER" ? (
                          <Shield className="h-6 w-6 text-primary" />
                        ) : (
                          <User className="h-6 w-6 text-muted-foreground" />
                        )}
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-semibold">{m.email}</span>
                          <Badge
                            variant={m.role === "OWNER" ? "default" : "secondary"}
                          >
                            {m.role === "OWNER" ? "Eigentümer" : "Mitglied"}
                          </Badge>
                          {isSelf && <Badge variant="outline">Du</Badge>}
                        </div>
                        <p className="text-sm text-muted-foreground mt-1">
                          Beigetreten am {formatDate(m.createdAt)}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      {isOwner && m.role === "MEMBER" && !isSelf && (
                        <AlertDialog>
                          <AlertDialogTrigger>
                            <Button variant="ghost" size="icon">
                              <Trash2 className="h-4 w-4 text-destructive" />
                            </Button>
                          </AlertDialogTrigger>
                          <AlertDialogContent>
                            <AlertDialogHeader>
                              <AlertDialogTitle>
                                Zugriff widerrufen?
                              </AlertDialogTitle>
                              <AlertDialogDescription>
                                {m.email} wird den Zugriff auf den Haushalt
                                verlieren. Diese Aktion kann nicht rückgängig
                                gemacht werden.
                              </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                              <AlertDialogCancel>Abbrechen</AlertDialogCancel>
                              <AlertDialogAction
                                onClick={() => handleRevoke(m.id)}
                              >
                                Zugriff widerrufen
                              </AlertDialogAction>
                            </AlertDialogFooter>
                          </AlertDialogContent>
                        </AlertDialog>
                      )}
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
