# Photomosaic
Creating photomosaics in java and openCV

1. Crop source images to 96x96 rectangles.
2. Hash source images into buckets by their mean RGB-value.  
e.g.: (255, 10, 20) -> (1,0,0) -> 4;
        (10, 200, 200) -> (0,1,1) -> 3
3. Iterate over target image fields and find the source image with the highest similarity.  

I tried three ways to measure image similarity:
- The mean value of all pixels: Very fast, but misses structural information.
- The MSE (https://en.wikipedia.org/wiki/Mean_squared_error): Slower than the global mean, especially on RGB images, but produces way better results. Needs hashing of source images to cut computation time.
- SSIM (https://en.wikipedia.org/wiki/Structural_similarity): Very slow even with prehashed source images, not practical.





Example (MSE):  

<img src="side.jpg">
