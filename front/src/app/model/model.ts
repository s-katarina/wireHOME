export interface CityDTO {
    id: number;
    name: string;
    country: CountryDTO;
}
  
export interface CountryDTO {
    id: number;
    name: string;
}

export interface CityOverview {
    city: CityDTO
    propertyesNum: number
    energy: number
    electodistribution: number
}

export interface PropertyRequestDTO {
    propertyType: string;
    address: string;
    cityId: number | null;
    area: number | null;
    floorCount: number | null;
}

export interface DeviceRequestDTO {
    modelName: string;
    usesElectricity: boolean;
    consumptionAmount: number;
    propertyId: string;
    regimes: string[];
    minTemp: number;
    maxTemp: number;
    panelSize: number;
    efficiency: number;
    capacity: number;
    portNumber: number;
}

export interface DeviceDTO{
    id: string;
    state: boolean;
    modelName: string;
    usesElectricity: boolean;
    imagePath: string;
    deviceType: string;
    consumptionAmount: number;
    propertyId: number;
    on: boolean
}

export interface PropertyDTO {
    id: string;
    propertyType: string;
    address: string;
    city: CityDTO;
    propertyOwner: AppUserDTO
    imagePath: string;
    area: number | null;
    floorCount: number | null;
    propertyStatus: string;
    energy?: number
    electodistribution?: number

}

export interface ByTimeOfDay {
    dayElec: number
    nightElec: number
    dayDist: number
    nightDist: number
}

export interface LoginDTO {
    email: string;
    password: string;
}

export interface TokenResponseDTO {
    accessToken: string;
}

export interface AppUserDTO {
    id: number | null;
    name: string;
    lastName: string;
    email: string;
    password: string | null;
    active: boolean | null;
}

export interface Lamp extends DeviceDTO {
    bulbState: boolean,
    automatic: boolean
}

export interface Gate extends DeviceDTO {
    open: boolean,
    public: boolean
    licencePlates: string[]
}

export interface Sprinkler extends DeviceDTO {
    scheduleMode: boolean
    scheduleDTO: {
        startHour: number,
        endHour: number,
        weekdays: number[]
    }
}

export interface SolarPanel extends DeviceDTO {
    surfaceSize: number;
    efficiency: number;
}

export interface Battery extends DeviceDTO {
    capacity: number;
    currentFill: number;
}

export interface Charger extends DeviceDTO {
    chargingStrength: number
    portNumber: number
    availablePortNumber: number
    percentage: number
}

export interface AirConditionerDTO extends DeviceDTO {
    regimes: string[],
    currentAction: string,
    temp: number,
    minTemp: number,
    maxTemp: number
}

export interface WashingMachineDTO extends DeviceDTO {
    regimes: string[],
    currentAction: string
}

export interface GateEvent  {
    caller: string,
    eventType: string
    timestamp: string
    callerUsername?: string,
}

export interface SprinklerCommand  {
    caller: string,
    callerUsername: string,
    command: string
    timestamp: string
}

export interface ApiResponse {
    status: number,
    data: any
}


export interface ChartData {
    data: GraphDTO[]
} 

export interface GraphDTO {
    x: number
    y: number
}

export interface LightSensorDTO {
    value: number
    timestamp: number
}

export interface PyChartDTO {
    indexLabel: string
    y: number
}

export interface BarChartDTO {
    label: string
    y: number
}


export interface AmbientSensorDateValueDTO {
    dates: string[],
    values: number[]
}

export interface AmbientSensorTempHumDTO {
    temp: AmbientSensorDateValueDTO,
    hum: AmbientSensorDateValueDTO,
    length: number
}

export interface GraphPoint {
    x: string,
    y: number
}

export interface AirConditionerActionRequest {
    action: string,
    userEmail: string
}

export interface AirConditionActionDTO {
    action: string,
    email: string,
    date: string
}

export interface Car {
    plate: string,
    batterySize: number,
    percentage: number,
    energyConsumed: number
}

export interface StartEnd {
    start: number
    end: number
}

export interface LabeledGraphDTO {
    label: string
    graphDTOS: GraphDTO[]
}

export interface ACIntervalDTO {
    id: number,
    startTime: string,
    endTime: string,
    action: string
}

export interface WMTaskDTO {
    id: number,
    startTime: string,
    action: string
}

export interface SharedPropertyDTO {
    id: number,
    shareWith: AppUserDTO,
    property: PropertyDTO
}

export interface SharedDeviceDTO {
    id: number,
    shareWith: AppUserDTO,
    device: DeviceDTO
}

export interface ShareActionDTO {
    email: string,
    id: string
}
