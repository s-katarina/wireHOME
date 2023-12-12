export interface CityDTO {
    id: number;
    name: string;
    country: CountryDTO;
}
  
export interface CountryDTO {
    id: number;
    name: string;
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