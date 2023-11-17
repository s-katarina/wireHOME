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

export interface PropertyResponseDTO {
    id: string;
    propertyType: string;
    address: string;
    city: CityDTO;
    imagePath: string;
    area: number | null;
    floorCount: number | null;
    propertyStatus: string;
}

export interface OwnerDTO {
    id: string;
    username: string;
}

export interface PendingPropertyRequestDTO {
    id: string;
    propertyType: string;
    address: string;
    // owner: OwnerDTO;
    city: CityDTO;
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