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