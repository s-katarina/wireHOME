import { AfterViewInit, Component, OnInit } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements AfterViewInit {

  private map : any;
  public marker!: L.Marker;

  private initMap(): void {
    this.map = L.map('map', {
      center: [ 39.8282, -98.5795 ],
      zoom: 3
    });
    
    const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    });

    tiles.addTo(this.map);

    L.Marker.prototype.options.icon = L.icon({
      iconUrl: 'https://cdn-icons-png.flaticon.com/512/7606/7606169.png',
      iconSize: [40, 40],
      iconAnchor: [18, 36]
    })

    this.map.on('click', this.onMapClick.bind(this));

  }

  constructor() { }

  ngAfterViewInit(): void {
    setTimeout(() => {
      if (this.map == undefined) {
        this.initMap()
      }
    }, 1000)  }

  public getMap (): any { return this.map }

  private onMapClick(e: L.LeafletMouseEvent) {
    if (this.marker) {
      this.map.removeLayer(this.marker);
    }
    this.marker = L.marker(e.latlng).addTo(this.map);
  }

  public addMarkerAndAdjustView(latitude: number, longitude: number): void {
    this.map.setView([latitude, longitude], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(this.map);
    L.marker([latitude, longitude]).addTo(this.map);
  }


}
