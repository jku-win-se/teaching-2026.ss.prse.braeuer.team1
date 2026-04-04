import { useEffect, useRef, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Play,
  Square,
  FastForward,
  Lightbulb,
  Thermometer,
  SunDim,
  Blinds,
  Radio,
  Clock,
  RotateCcw,
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { api } from "@/services/api";

interface SimDevice {
  id: number;
  name: string;
  type: "SWITCH" | "DIMMER" | "THERMOSTAT" | "SENSOR" | "BLIND";
  roomName: string;
  switchedOn: boolean;
  level: number;
}

interface SimEvent {
  hour: number;
  deviceId: number;
  switchedOn?: boolean;
  level?: number;
}

interface SimLogEntry {
  time: string;
  deviceName: string;
  roomName: string;
  description: string;
}

const SPEEDS = [
  { label: "1× – Echtzeit", ms: 1000 },
  { label: "10× – schnell", ms: 100 },
  { label: "60× – sehr schnell", ms: 17 },
  { label: "360× – maximal", ms: 3 },
];

function deviceIcon(type: SimDevice["type"]) {
  switch (type) {
    case "SWITCH": return Lightbulb;
    case "DIMMER": return SunDim;
    case "THERMOSTAT": return Thermometer;
    case "SENSOR": return Radio;
    case "BLIND": return Blinds;
  }
}

function formatSimTime(minutes: number) {
  const h = Math.floor(minutes / 60) % 24;
  const m = minutes % 60;
  return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
}

/** Detect device role by name keywords */
function detectRole(name: string): string {
  const n = name.toLowerCase();
  if (n.includes("küche") || n.includes("kitchen")) return "kitchen";
  if (n.includes("bad") || n.includes("bath") || n.includes("wc")) return "bathroom";
  if (n.includes("schlaf") || n.includes("bed")) return "bedroom";
  if (n.includes("outside") || n.includes("außen") || n.includes("garten")) return "outside";
  if (n.includes("ceiling") || n.includes("decke")) return "ceiling";
  if (n.includes("motion") || n.includes("bewegung")) return "motion";
  if (n.includes("thermo") || n.includes("heizung")) return "thermo";
  if (n.includes("window") || n.includes("blind") || n.includes("jalousie") || n.includes("rollo")) return "blind";
  return "general";
}

/** Build a realistic day scenario based on device type and name */
function buildScenario(devices: SimDevice[]): SimEvent[] {
  const events: SimEvent[] = [];
  devices.forEach((d) => {
    const role = detectRole(d.name);

    if (d.type === "THERMOSTAT") {
      // Heat up in morning, lower during day, warm in evening, low at night
      events.push({ hour: 6, deviceId: d.id, level: 21 });
      events.push({ hour: 9, deviceId: d.id, level: 18 });
      events.push({ hour: 17, deviceId: d.id, level: 22 });
      events.push({ hour: 23, deviceId: d.id, level: 17 });
      return;
    }

    if (d.type === "BLIND") {
      // Open at sunrise, close at sunset
      events.push({ hour: 7, deviceId: d.id, switchedOn: true, level: 100 });
      events.push({ hour: 20, deviceId: d.id, switchedOn: false, level: 0 });
      return;
    }

    if (d.type === "SENSOR") {
      // Motion sensor: active morning, lunch, evening
      events.push({ hour: 0, deviceId: d.id, level: 0 });
      events.push({ hour: 6, deviceId: d.id, level: 0.3 });
      events.push({ hour: 7, deviceId: d.id, level: 1.0 });
      events.push({ hour: 9, deviceId: d.id, level: 0.1 });
      events.push({ hour: 12, deviceId: d.id, level: 0.8 });
      events.push({ hour: 13, deviceId: d.id, level: 0.2 });
      events.push({ hour: 17, deviceId: d.id, level: 0.9 });
      events.push({ hour: 22, deviceId: d.id, level: 0.1 });
      return;
    }

    // SWITCH / DIMMER – realistic per room role
    if (role === "kitchen") {
      // Kitchen: breakfast, lunch, dinner – off rest of day
      events.push({ hour: 0,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 6,  deviceId: d.id, switchedOn: true, level: 90 });
      events.push({ hour: 9,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 11, deviceId: d.id, switchedOn: true, level: 80 });
      events.push({ hour: 13, deviceId: d.id, switchedOn: false });
      events.push({ hour: 18, deviceId: d.id, switchedOn: true, level: 100 });
      events.push({ hour: 21, deviceId: d.id, switchedOn: false });
    } else if (role === "bedroom") {
      // Bedroom: brief morning, long evening
      events.push({ hour: 0,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 6,  deviceId: d.id, switchedOn: true, level: 30 });
      events.push({ hour: 8,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 21, deviceId: d.id, switchedOn: true, level: 20 });
      events.push({ hour: 23, deviceId: d.id, switchedOn: false });
    } else if (role === "bathroom") {
      // Bathroom: short bursts morning and evening
      events.push({ hour: 0,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 6,  deviceId: d.id, switchedOn: true, level: 100 });
      events.push({ hour: 7,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 20, deviceId: d.id, switchedOn: true, level: 100 });
      events.push({ hour: 21, deviceId: d.id, switchedOn: false });
    } else if (role === "ceiling") {
      // Main ceiling light: morning and evening
      events.push({ hour: 0,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 6,  deviceId: d.id, switchedOn: true, level: 70 });
      events.push({ hour: 10, deviceId: d.id, switchedOn: false });
      events.push({ hour: 17, deviceId: d.id, switchedOn: true, level: 80 });
      events.push({ hour: 23, deviceId: d.id, switchedOn: false });
    } else {
      // General light: morning and evening use
      events.push({ hour: 0,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 7,  deviceId: d.id, switchedOn: true, level: 75 });
      events.push({ hour: 9,  deviceId: d.id, switchedOn: false });
      events.push({ hour: 18, deviceId: d.id, switchedOn: true, level: 70 });
      events.push({ hour: 23, deviceId: d.id, switchedOn: false });
    }
  });
  return events;
}

export default function SimulationPage() {
  const { user, isOwner } = useAuth();
  const [devices, setDevices] = useState<SimDevice[]>([]);
  const [simDevices, setSimDevices] = useState<SimDevice[]>([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [paused, setPaused] = useState(false);
  const [simMinutes, setSimMinutes] = useState(0); // 0–1439
  const [speedIdx, setSpeedIdx] = useState(1); // default 10×
  const [log, setLog] = useState<SimLogEntry[]>([]);
  const [startHour, setStartHour] = useState("0");
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const scenarioRef = useRef<SimEvent[]>([]);
  const firedRef = useRef<Set<string>>(new Set());

  useEffect(() => {
    loadDevices();
  }, [user]);

  async function loadDevices() {
    if (!user) return;
    setLoading(true);
    try {
      const rooms = await api.get<{ id: number; name: string }[]>(
        `/rooms${isOwner ? `?userId=${user.id}` : ""}`
      );
      const all: SimDevice[] = [];
      for (const room of rooms) {
        const devs = await api.get<SimDevice[]>(`/rooms/${room.id}/devices`);
        devs.forEach((d) => all.push({ ...d, roomName: room.name }));
      }
      setDevices(all);
      // Show initial state at hour 0 (midnight) – all off
      const scenario = buildScenario(all);
      const initDevices = all.map((d) => {
        const hour0Events = scenario
          .filter((e) => e.deviceId === d.id && e.hour === 0)
          .sort((a, b) => a.hour - b.hour);
        const state = { ...d };
        hour0Events.forEach((e) => {
          if (e.switchedOn !== undefined) state.switchedOn = e.switchedOn;
          if (e.level !== undefined) state.level = e.level;
        });
        // If no hour-0 event, default to off/baseline
        if (hour0Events.length === 0) {
          if (d.type === "SWITCH" || d.type === "DIMMER") state.switchedOn = false;
          if (d.type === "BLIND") { state.switchedOn = false; state.level = 0; }
          if (d.type === "THERMOSTAT") state.level = 17;
          if (d.type === "SENSOR") state.level = 0;
        }
        return state;
      });
      setSimDevices(initDevices);
    } catch {
      /* empty */
    } finally {
      setLoading(false);
    }
  }

  function startSimulation() {
    const startH = Math.max(0, Math.min(23, parseInt(startHour) || 0));
    const start = startH * 60;
    const scenario = buildScenario(devices);
    scenarioRef.current = scenario;

    // Apply all scenario events up to startHour to get correct initial state
    const initialDevices = devices.map((d) => {
      const deviceEvents = scenario
        .filter((e) => e.deviceId === d.id && e.hour <= startH)
        .sort((a, b) => a.hour - b.hour);
      let state = { ...d };
      deviceEvents.forEach((e) => {
        if (e.switchedOn !== undefined) state.switchedOn = e.switchedOn;
        if (e.level !== undefined) state.level = e.level;
      });
      return state;
    });

    setSimMinutes(start);
    setSimDevices(initialDevices);
    setLog([]);
    firedRef.current = new Set(
      scenario.filter((e) => e.hour <= startH).map((e) => `${e.hour}-${e.deviceId}`)
    );
    setPaused(false);
    setRunning(true);
  }

  function resumeSimulation() {
    setRunning(true);
    setPaused(false);
  }

  function stopSimulation() {
    setRunning(false);
    setPaused(true);
    if (intervalRef.current) clearInterval(intervalRef.current);
  }

  function resetSimulation() {
    setRunning(false);
    setPaused(false);
    if (intervalRef.current) clearInterval(intervalRef.current);
    setSimMinutes(0);
    setSimDevices(devices.map((d) => ({ ...d })));
    setLog([]);
    firedRef.current = new Set();
  }

  useEffect(() => {
    if (!running) {
      if (intervalRef.current) clearInterval(intervalRef.current);
      return;
    }

    const tickMs = SPEEDS[speedIdx].ms;

    intervalRef.current = setInterval(() => {
      setSimMinutes((prev) => {
        const next = prev + 1;
        if (next >= 1440) {
          setRunning(false);
          return 1439;
        }

        // Check scenario events for this minute
        const currentHour = Math.floor(next / 60);
        const currentMinute = next % 60;

        if (currentMinute === 0) {
          scenarioRef.current
            .filter((e) => e.hour === currentHour)
            .forEach((e) => {
              const key = `${e.hour}-${e.deviceId}`;
              if (firedRef.current.has(key)) return;
              firedRef.current.add(key);

              setSimDevices((prev) =>
                prev.map((d) => {
                  if (d.id !== e.deviceId) return d;
                  const updated = { ...d };
                  if (e.switchedOn !== undefined) updated.switchedOn = e.switchedOn;
                  if (e.level !== undefined) updated.level = e.level;

                  // Add log entry
                  const desc =
                    d.type === "THERMOSTAT"
                      ? `Temperatur auf ${e.level}°C gesetzt`
                      : d.type === "SENSOR"
                      ? `Sensorwert: ${e.level}`
                      : d.type === "BLIND"
                      ? e.switchedOn ? "Jalousie geöffnet" : "Jalousie geschlossen"
                      : e.switchedOn
                      ? "Eingeschaltet"
                      : "Ausgeschaltet";

                  setLog((l) => [
                    {
                      time: formatSimTime(next),
                      deviceName: d.name,
                      roomName: d.roomName,
                      description: desc,
                    },
                    ...l.slice(0, 49),
                  ]);
                  return updated;
                })
              );
            });
        }

        return next;
      });
    }, tickMs);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [running, speedIdx]);

  const progress = Math.round((simMinutes / 1439) * 100);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold flex items-center gap-2">
          <FastForward className="h-8 w-8" /> Zeitraffer-Simulation
        </h1>
        <p className="text-muted-foreground mt-1">
          Simuliert einen vollständigen Tag im Zeitraffer – ohne das Live-System zu beeinflussen
        </p>
      </div>

      {/* Controls */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Simulation steuern</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap items-end gap-4">
            <div className="space-y-2">
              <Label>Startzeit (Stunde)</Label>
              <Input
                type="number"
                min={0}
                max={23}
                value={startHour}
                onChange={(e) => setStartHour(e.target.value)}
                className="w-24"
                disabled={running || paused}
              />
            </div>
            <div className="space-y-2">
              <Label>Geschwindigkeit</Label>
              <Select
                value={SPEEDS[speedIdx].label}
                onValueChange={(v) => setSpeedIdx(SPEEDS.findIndex((s) => s.label === v))}
              >
                <SelectTrigger className="w-52">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {SPEEDS.map((s, i) => (
                    <SelectItem key={i} value={s.label}>
                      {s.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex gap-2">
              {running ? (
                <Button variant="destructive" onClick={stopSimulation}>
                  <Square className="h-4 w-4 mr-2" /> Pausieren
                </Button>
              ) : paused ? (
                <Button onClick={resumeSimulation}>
                  <Play className="h-4 w-4 mr-2" /> Weiter
                </Button>
              ) : (
                <Button onClick={startSimulation} disabled={devices.length === 0}>
                  <Play className="h-4 w-4 mr-2" /> Simulation starten
                </Button>
              )}
              <Button variant="outline" onClick={resetSimulation} disabled={running}>
                <RotateCcw className="h-4 w-4 mr-2" /> Zurücksetzen
              </Button>
            </div>
          </div>

          {/* Time + Progress */}
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="flex items-center gap-1 font-medium">
                <Clock className="h-4 w-4" />
                Simulierte Zeit: <span className="text-primary ml-1">{formatSimTime(simMinutes)}</span>
              </span>
              <span className="text-muted-foreground">{progress}%</span>
            </div>
            <div className="w-full bg-muted rounded-full h-3 overflow-hidden">
              <div
                className="h-3 bg-primary rounded-full transition-all duration-100"
                style={{ width: `${progress}%` }}
              />
            </div>
            {/* Hour markers */}
            <div className="flex justify-between text-xs text-muted-foreground px-0.5">
              {[0, 6, 12, 18, 23].map((h) => (
                <span key={h}>{String(h).padStart(2, "0")}:00</span>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Device States */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              Gerätezustände{" "}
              <Badge variant="outline" className="ml-2">
                {simDevices.length} Geräte
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent>
            {simDevices.length === 0 ? (
              <p className="text-muted-foreground text-sm">Keine Geräte gefunden.</p>
            ) : (
              <div className="space-y-2">
                {simDevices.map((d) => {
                  const Icon = deviceIcon(d.type);
                  const isOn = d.switchedOn;
                  return (
                    <div
                      key={d.id}
                      className={`flex items-center justify-between p-3 rounded-lg border transition-colors ${
                        isOn ? "border-primary/30 bg-primary/5" : "bg-muted/30"
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <Icon
                          className={`h-5 w-5 ${isOn ? "text-primary" : "text-muted-foreground"}`}
                        />
                        <div>
                          <p className="text-sm font-medium">{d.name}</p>
                          <p className="text-xs text-muted-foreground">{d.roomName}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        {d.type === "SWITCH" && (
                          <Badge variant={isOn ? "default" : "secondary"}>
                            {isOn ? "Ein" : "Aus"}
                          </Badge>
                        )}
                        {d.type === "DIMMER" && (
                          <div className="space-y-1">
                            <Badge variant={isOn ? "default" : "secondary"}>
                              {isOn ? `${d.level}%` : "Aus"}
                            </Badge>
                          </div>
                        )}
                        {d.type === "THERMOSTAT" && (
                          <Badge variant="outline">{d.level}°C</Badge>
                        )}
                        {d.type === "SENSOR" && (
                          <Badge variant="outline">{d.level}</Badge>
                        )}
                        {d.type === "BLIND" && (
                          <Badge variant={isOn ? "default" : "secondary"}>
                            {isOn ? "Offen" : "Geschlossen"}
                          </Badge>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Simulation Log */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              Simulationsprotokoll{" "}
              <Badge variant="outline" className="ml-2">
                {log.length} Ereignisse
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-80 overflow-y-auto space-y-2">
              {log.length === 0 ? (
                <p className="text-muted-foreground text-sm">
                  {running
                    ? "Warte auf Ereignisse..."
                    : "Starte die Simulation um Ereignisse zu sehen."}
                </p>
              ) : (
                log.map((entry, i) => (
                  <div
                    key={i}
                    className="flex items-start gap-3 p-2 rounded-md bg-muted/30 text-sm"
                  >
                    <span className="font-mono text-xs text-primary shrink-0 mt-0.5">
                      {entry.time}
                    </span>
                    <div>
                      <span className="font-medium">{entry.deviceName}</span>
                      <span className="text-muted-foreground"> · {entry.roomName}</span>
                      <p className="text-muted-foreground text-xs">{entry.description}</p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
