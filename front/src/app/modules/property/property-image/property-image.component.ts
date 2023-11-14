import { ChangeDetectorRef, Component, Input, NgZone, OnInit } from "@angular/core";
import { ImageServiceService } from "../../service/image-service.service";

@Component({
  selector: 'app-property-img',
  template: `
    <img [src]="imageSrc" alt="Binary Image">
  `,
  styleUrls: ['./property-image.component.css']
})
export class PropertyImageComponent implements OnInit {

  imageSrc: any;
  @Input() propertyId: string = '';

  constructor(private imageService: ImageServiceService,
    private cdRef: ChangeDetectorRef,
    private zone: NgZone) {}

  ngOnInit(): void {
  }

  loadImage(): void {
    if (this.imageService.isImageLoaded(this.propertyId)) {
      this.imageSrc = this.imageService.getCachedImage(this.propertyId);
    } else {
      this.imageService.getPropertyImage(this.propertyId).subscribe(
        (data: any) => {
          const reader = new FileReader();
          reader.onloadend = () => {
            this.zone.run(() => {
              this.imageSrc = reader.result;
              this.imageService.cacheImage(this.propertyId, this.imageSrc);
              this.cdRef.detectChanges();
            });
          };
          reader.readAsDataURL(data);
        },
        (error) => {
          console.error('Error loading image:', error);
        }
      );
    }
  }

}