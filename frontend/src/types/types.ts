export type UserRole = "OWNER" | "MEMBER";
export type DeviceType = "SWITCH" | "DIMMER" | "THERMOSTAT" | "SENSOR" | "BLIND";
export type TriggerType = "TIME_BASED" | "THRESHOLD" | "EVENT";

export interface User {
  id: number;
  email: string;
  role: UserRole;
  createdAt: string;
}

export interface Room {
  id: number;
  name: string;
  userId: number;
}

export interface Device {
  id: number;
  name: string;
  type: DeviceType;
  roomId: number;
  roomName: string;
  switchedOn: boolean | null;
  level: number | null;
  powerConsumptionWatt: number | null;
  updatedAt: string;
}

export interface Rule {
  id: number;
  name: string;
  triggerType: TriggerType;
  triggerCondition: string;
  triggerDeviceId: number | null;
  triggerDeviceName: string | null;
  triggerThresholdValue: number | null;
  actionDeviceId: number;
  actionDeviceName: string;
  actionValue: string;
  active: boolean;
  userId: number;
}

export interface Schedule {
  id: number;
  name: string;
  cronExpression: string;
  deviceId: number;
  deviceName: string;
  actionValue: string;
  active: boolean;
  userId: number;
}

export interface SceneDeviceState {
  id: number;
  deviceId: number;
  deviceName: string;
  targetSwitchedOn: boolean | null;
  targetLevel: number | null;
}

export interface Scene {
  id: number;
  name: string;
  userId: number;
  deviceStates: SceneDeviceState[];
}

export interface ActivityLog {
  id: number;
  deviceId: number;
  deviceName: string;
  roomName: string;
  actor: string;
  description: string;
  timestamp: string;
}

export interface EnergyDeviceSummary {
  deviceId: number;
  deviceName: string;
  roomName: string;
  todayWh: number;
  weekWh: number;
}

export interface EnergyRoomSummary {
  roomId: number;
  roomName: string;
  todayWh: number;
  weekWh: number;
}

export interface EnergyDashboard {
  byDevice: EnergyDeviceSummary[];
  byRoom: EnergyRoomSummary[];
  totalTodayWh: number;
  totalWeekWh: number;
}

export interface Notification {
  id: number;
  userId: number;
  message: string;
  createdAt: string;
  read: boolean;
}

export interface VacationMode {
  id: number;
  userId: number;
  startDate: string;
  endDate: string;
  scheduleId: number;
  scheduleName: string;
  active: boolean;
}
