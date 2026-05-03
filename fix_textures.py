from PIL import Image
import os

# 1. Fix GUI Image (Must be power of 2)
gui_path = "src/main/resources/assets/mykaisen/textures/gui/cursed_book.png"
if os.path.exists(gui_path):
    gui_img = Image.open(gui_path).convert("RGBA")
    gui_img = gui_img.resize((256, 256), Image.Resampling.LANCZOS)
    gui_img.save(gui_path)
    print("Resized GUI to 256x256")
else:
    print(f"GUI path not found: {gui_path}")

# 2. Fix Tattoo Transparency and UV Mapping
tattoo_path = "src/main/resources/assets/mykaisen/textures/entity/sukuna_tattoos.png"
if os.path.exists(tattoo_path):
    img = Image.open(tattoo_path).convert("RGBA")
    pixels = img.load()
    # Strip fake checkerboard/white background
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if r > 100 and g > 100 and b > 100:
                pixels[x, y] = (0, 0, 0, 0) # Make transparent
            else:
                pixels[x, y] = (0, 0, 0, 255) # Make pitch black
    
    # Map to 64x64 Curios/Skin layout
    mc_skin = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    face_crop = img.crop((0, 0, img.width, int(img.height * 0.40))).resize((8, 8), Image.Resampling.LANCZOS)
    chest_crop = img.crop((0, int(img.height * 0.40), img.width, img.height)).resize((8, 12), Image.Resampling.LANCZOS)
    mc_skin.paste(face_crop, (8, 8), face_crop)
    mc_skin.paste(chest_crop, (20, 20), chest_crop)
    mc_skin.save(tattoo_path)
    print("Tattoo fixed and mapped to 64x64")
else:
    print(f"Tattoo path not found: {tattoo_path}")
