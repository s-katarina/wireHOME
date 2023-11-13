import { Component, Input, OnInit } from '@angular/core';
import { ImageServiceService } from '../../service/image-service.service';

@Component({
  selector: 'app-property-img',
  template: `
    <img [src]="imageSrc">
  `,
  styleUrls: ['./property-img.component.css']
})
export class PropertyImgComponent implements OnInit {

  imageSrc: any;
  @Input() propertyId: string = '';

  constructor(private imageService: ImageServiceService) {}

  ngOnInit(): void {
    this.loadImage();
  }

  loadImage(): void {
    this.imageService.getPropertyImage(this.propertyId).subscribe(
      (data: any) => {
        const reader = new FileReader();
        reader.onloadend = () => {
          this.imageSrc = reader.result;
        };
        reader.readAsDataURL(data);
      },
      (error) => {
        console.error('Error loading image:', error);
      }
    );
  }

}
