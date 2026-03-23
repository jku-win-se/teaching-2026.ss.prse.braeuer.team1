import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Bell, BellOff, CheckCheck, Mail, MailOpen, Clock } from "lucide-react";
import api from "@/lib/api";

interface Notification {
  id: number;
  userId: number;
  message: string;
  createdAt: string;
  read: boolean;
}

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState("all");

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    loadNotifications();
  }, []);

  async function loadNotifications() {
    if (!userId) return;
    setLoading(true);
    try {
      const res = await api.get(`/api/notifications?userId=${userId}`);
      const sorted = (res.data as Notification[]).sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
      setNotifications(sorted);
    } catch {
      /* empty */
    } finally {
      setLoading(false);
    }
  }

  async function markAsRead(id: number) {
    try {
      await api.put(`/api/notifications/${id}/read`);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch {
      /* empty */
    }
  }

  async function markAllAsRead() {
    if (!userId) return;
    try {
      await api.put(`/api/notifications/read-all?userId=${userId}`);
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch {
      /* empty */
    }
  }

  const unreadCount = notifications.filter((n) => !n.read).length;

  const filtered =
    tab === "unread"
      ? notifications.filter((n) => !n.read)
      : tab === "read"
        ? notifications.filter((n) => n.read)
        : notifications;

  function formatTimestamp(ts: string) {
    const d = new Date(ts);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    const diffH = Math.floor(diffMs / 3600000);
    const diffD = Math.floor(diffMs / 86400000);

    if (diffMin < 1) return "Gerade eben";
    if (diffMin < 60) return `vor ${diffMin} Min.`;
    if (diffH < 24) return `vor ${diffH} Std.`;
    if (diffD < 7) return `vor ${diffD} Tag${diffD > 1 ? "en" : ""}`;

    return d.toLocaleDateString("de-AT", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
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
            <Bell className="h-8 w-8" /> Benachrichtigungen
          </h1>
          <p className="text-muted-foreground mt-1">
            {unreadCount > 0 ? (
              <span>
                <span className="font-semibold text-primary">{unreadCount}</span> ungelesene
                Benachrichtigung{unreadCount !== 1 && "en"}
              </span>
            ) : (
              "Keine ungelesenen Benachrichtigungen"
            )}
          </p>
        </div>
        {unreadCount > 0 && (
          <Button onClick={markAllAsRead} variant="outline">
            <CheckCheck className="h-4 w-4 mr-2" /> Alle als gelesen markieren
          </Button>
        )}
      </div>

      {/* Tabs */}
      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="all">
            Alle ({notifications.length})
          </TabsTrigger>
          <TabsTrigger value="unread">
            Ungelesen ({unreadCount})
          </TabsTrigger>
          <TabsTrigger value="read">
            Gelesen ({notifications.length - unreadCount})
          </TabsTrigger>
        </TabsList>

        <TabsContent value={tab} className="mt-4">
          {filtered.length === 0 ? (
            <Card>
              <CardContent className="py-12">
                <div className="text-center text-muted-foreground">
                  <BellOff className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p className="text-lg font-medium">Keine Benachrichtigungen</p>
                  <p className="text-sm">
                    {tab === "unread"
                      ? "Alle Benachrichtigungen wurden gelesen."
                      : "Sobald Regeln ausgelöst werden, erscheinen hier Benachrichtigungen."}
                  </p>
                </div>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-2">
              {filtered.map((notif) => (
                <Card
                  key={notif.id}
                  className={`transition-colors ${!notif.read ? "border-primary/30 bg-primary/5" : ""}`}
                >
                  <CardContent className="py-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3 flex-1">
                        <div className="mt-0.5">
                          {notif.read ? (
                            <MailOpen className="h-5 w-5 text-muted-foreground" />
                          ) : (
                            <Mail className="h-5 w-5 text-primary" />
                          )}
                        </div>
                        <div className="flex-1">
                          <p className={`${!notif.read ? "font-semibold" : "text-muted-foreground"}`}>
                            {notif.message}
                          </p>
                          <div className="flex items-center gap-2 mt-1">
                            <Clock className="h-3 w-3 text-muted-foreground" />
                            <span className="text-xs text-muted-foreground">
                              {formatTimestamp(notif.createdAt)}
                            </span>
                            {!notif.read && <Badge className="text-xs">Neu</Badge>}
                          </div>
                        </div>
                      </div>
                      {!notif.read && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => markAsRead(notif.id)}
                        >
                          <CheckCheck className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
