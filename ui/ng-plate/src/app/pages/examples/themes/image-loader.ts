import { IMAGE_LOADER, type ImageLoaderConfig } from '@angular/common';
import { type Provider } from '@angular/core';

export interface ShowcaseImage {
  readonly src: string;
  readonly width: number;
  readonly height: number;
  readonly alt: string;
}

/** 图库 URL 已带 w/q 参数，直接返回原始地址，避免默认 loader 改写远程图片。 */
export const PASSTHROUGH_IMAGE_LOADER: Provider = {
  provide: IMAGE_LOADER,
  useValue: (config: ImageLoaderConfig): string => config.src,
};

export function showcaseImage(
  photoId: string,
  width: number,
  height: number,
  alt: string,
): ShowcaseImage {
  return {
    src: `https://images.unsplash.com/${photoId}?auto=format&fit=crop&w=${width}&h=${height}&q=80`,
    width,
    height,
    alt,
  };
}
