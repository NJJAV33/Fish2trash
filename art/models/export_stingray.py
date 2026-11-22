"""Regenerates the 12-frame Stingray sprite sheet from art/models/Stingray.blend.

Usage (from the repository root):
    blender --background art/models/Stingray.blend --python art/models/export_stingray.py -- \\
        --output src/ui/render/stingray.png

On Linux with Flatpak Blender:
    flatpak run org.blender.Blender --background art/models/Stingray.blend \\
        --python art/models/export_stingray.py -- --output src/ui/render/stingray.png
"""

import argparse
import math
import os
import sys
import tempfile

import bpy
from bpy_extras.object_utils import world_to_camera_view
from mathutils import Vector

DEFAULT_FRAMES = 12
DEFAULT_FRAME_WIDTH = 264
DEFAULT_FRAME_HEIGHT = 168
DEFAULT_SAMPLES = 32
DEFAULT_OUTPUT = os.path.join("src", "ui", "render", "stingray.png")

# Matches ui.render.StingrayPainter's 3x sprite grid (88x56 logical, disc center at (56, 30)).
LOGICAL_WIDTH = 88
LOGICAL_HEIGHT = 56
DISC_X_RATIO = 56.0 / LOGICAL_WIDTH
DISC_Y_RATIO = 30.0 / LOGICAL_HEIGHT


def parse_args():
    argv = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    parser = argparse.ArgumentParser(description="Export a 12-frame Stingray swim sprite sheet.")
    parser.add_argument("--output", default=DEFAULT_OUTPUT, help="Output PNG sprite sheet path.")
    parser.add_argument("--frames", type=int, default=DEFAULT_FRAMES, help="Number of swim frames.")
    parser.add_argument("--frame-width", type=int, default=DEFAULT_FRAME_WIDTH, help="Frame width in pixels.")
    parser.add_argument("--frame-height", type=int, default=DEFAULT_FRAME_HEIGHT, help="Frame height in pixels.")
    parser.add_argument("--samples", type=int, default=DEFAULT_SAMPLES, help="Cycles samples per frame.")
    parser.add_argument("--save-blend", action="store_true", help="Save Sprite camera and swim keys into the .blend.")
    return parser.parse_args(argv)


def find_stingray_body():
    for obj in bpy.data.objects:
        if obj.type == "MESH" and "disc" in obj.name.lower():
            return obj
    raise RuntimeError("Could not find stingray body mesh in blend file.")


def wave_offset(co, phase):
    """Computes smooth wing undulation and trailing whip-tail motion for a rest vertex."""
    x, y, z = co.x, co.y, co.z
    # Wing tips (large |x|) undulate along the body axis with a traveling wave from nose (-y) to tail (+y).
    wing_factor = max(0.0, min(1.0, (abs(x) - 0.65) / 2.1)) ** 1.4
    # Behind y = 1.65 is the whip tail; it trails in a soft S-wave across both X and Z.
    tail_factor = max(0.0, min(1.0, (y - 1.65) / 2.85)) ** 1.35

    wave_phase = phase - 0.85 * y
    dz = 0.42 * wing_factor * math.sin(wave_phase) + 0.24 * tail_factor * math.sin(phase - 1.45 * y)
    dx = 0.28 * tail_factor * math.cos(phase - 1.35 * y)
    return Vector((dx, 0.0, dz))


def ensure_basis_shape_key(obj):
    if obj.data.shape_keys is None:
        obj.shape_key_add(name="Basis", from_mix=False)
    obj.data.shape_keys.use_relative = True
    return [v.co.copy() for v in obj.data.shape_keys.key_blocks[0].data]


def build_swim_keys(body, body_rest, tail_spine, spine_rest, frame_count):
    """Creates Swim_00..Swim_NN shape keys on the stingray meshes."""
    for frame in range(frame_count):
        key_name = f"Swim_{frame:02d}"
        kb = body.data.shape_keys.key_blocks.get(key_name) or body.shape_key_add(name=key_name, from_mix=False)
        phase = 2.0 * math.pi * frame / frame_count
        for i, base_co in enumerate(body_rest):
            kb.data[i].co = base_co + wave_offset(base_co, phase)
        if tail_spine is not None and spine_rest is not None:
            skb = tail_spine.data.shape_keys.key_blocks.get(key_name) or tail_spine.shape_key_add(
                name=key_name, from_mix=False
            )
            for i, base_co in enumerate(spine_rest):
                skb.data[i].co = base_co + wave_offset(base_co, phase)


def activate_swim_frame(body, tail_spine, frame_index, frame_count):
    """Enables exactly one Swim_XX shape key (or rest Basis when frame_index < 0)."""
    for obj in (body, tail_spine):
        if obj is None or obj.data.shape_keys is None:
            continue
        for frame in range(frame_count):
            kb = obj.data.shape_keys.key_blocks.get(f"Swim_{frame:02d}")
            if kb is not None:
                kb.value = 1.0 if frame == frame_index else 0.0
        obj.update_tag()
    bpy.context.view_layer.update()


def keyframe_swim_action(body, tail_spine, frame_count):
    """Stores cyclic shape-key curves in the SwimCycle action for Blender playback."""
    action = bpy.data.actions.get("SwimCycle") or bpy.data.actions.new("SwimCycle")
    for obj in (body, tail_spine):
        if obj is None or obj.data.shape_keys is None:
            continue
        obj.data.shape_keys.animation_data_create()
        obj.data.shape_keys.animation_data.action = action
        for frame in range(frame_count):
            kb = obj.data.shape_keys.key_blocks.get(f"Swim_{frame:02d}")
            if kb is None:
                continue
            for key_frame in range(frame_count):
                kb.value = 1.0 if key_frame == frame else 0.0
                kb.keyframe_insert(data_path="value", frame=key_frame + 1)
            kb.value = 0.0


def ensure_sprite_camera(scene, body, frame_count):
    cam = bpy.data.objects.get("SpriteCamera")
    if cam is None or cam.type != "CAMERA":
        cam_data = bpy.data.cameras.new("SpriteCamera")
        cam = bpy.data.objects.new("SpriteCamera", cam_data)
        scene.collection.objects.link(cam)

    # Positioned on the -X side looking toward +X/-Y so the nose (-Y) faces right and the tail (+Y) trails left.
    cam_pos = Vector((-6.2, -2.1, 4.4))
    target = Vector((0.0, 0.55, 0.25))
    cam.location = cam_pos
    direction = (target - cam_pos).normalized()
    cam.rotation_euler = direction.to_track_quat("-Z", "Y").to_euler()

    cam.data.type = "ORTHO"
    cam.data.clip_start = 0.1
    cam.data.clip_end = 100.0
    cam.data.shift_x = 0.0
    cam.data.shift_y = 0.0

    # Fit bounding box across all vertices and animated swim poses with clean margin inside 264x168.
    bpy.context.view_layer.update()
    aspect = scene.render.resolution_x / float(scene.render.resolution_y)
    disc_world = body.matrix_world @ Vector((0.0, -0.35, 0.20))

    # Measure normalized extents at temporary scale 10.0 to solve exact ortho_scale and camera shift.
    test_scale = 10.0
    cam.data.ortho_scale = test_scale
    bpy.context.view_layer.update()

    disc_uv = world_to_camera_view(scene, cam, disc_world)
    uvs = [world_to_camera_view(scene, cam, body.matrix_world @ v.co) for v in body.data.vertices]
    for frame in range(frame_count):
        phase = 2.0 * math.pi * frame / frame_count
        uvs.extend(
            world_to_camera_view(scene, cam, body.matrix_world @ (v.co + wave_offset(v.co, phase)))
            for v in body.data.vertices
        )
    min_u = min(p.x for p in uvs)
    max_u = max(p.x for p in uvs)
    min_v = min(p.y for p in uvs)
    max_v = max(p.y for p in uvs)

    # Distance from disc anchor to frame edges in Blender camera units at ortho_scale = test_scale.
    left_margin = 0.04
    right_margin = 0.04
    top_margin = 0.08
    bottom_margin = 0.06
    req_scale_left = ((disc_uv.x - min_u) * test_scale) / max(0.05, DISC_X_RATIO - left_margin)
    req_scale_right = ((max_u - disc_uv.x) * test_scale) / max(0.05, (1.0 - right_margin) - DISC_X_RATIO)
    disc_v_target = 1.0 - DISC_Y_RATIO  # Blender camera Y is bottom-up, Java2D is top-down
    req_scale_top = ((max_v - disc_uv.y) * test_scale) / max(0.05, (1.0 - top_margin) - disc_v_target)
    req_scale_bottom = ((disc_uv.y - min_v) * test_scale) / max(0.05, disc_v_target - bottom_margin)

    ortho_scale = max(req_scale_left, req_scale_right, req_scale_top, req_scale_bottom)
    cam.data.ortho_scale = ortho_scale
    bpy.context.view_layer.update()

    # Align disc_world exactly onto (DISC_X_RATIO, disc_v_target).
    anchor_uv = world_to_camera_view(scene, cam, disc_world)
    cam.data.shift_x += (anchor_uv.x - DISC_X_RATIO)
    cam.data.shift_y += (anchor_uv.y - disc_v_target) / aspect
    bpy.context.view_layer.update()
    return cam


def stitch_strip(frame_paths, frame_w, frame_h, output_path):
    total_w = frame_w * len(frame_paths)
    sheet = bpy.data.images.new("StingraySheet", width=total_w, height=frame_h, alpha=True)
    sheet.colorspace_settings.name = "sRGB"
    out_pixels = [0.0] * (total_w * frame_h * 4)

    for idx, path in enumerate(frame_paths):
        img = bpy.data.images.load(path)
        src = list(img.pixels)
        x_off = idx * frame_w
        for row in range(frame_h):
            src_row = row * frame_w * 4
            dst_row = (row * total_w + x_off) * 4
            out_pixels[dst_row:dst_row + frame_w * 4] = src[src_row:src_row + frame_w * 4]
        bpy.data.images.remove(img)

    sheet.pixels.foreach_set(out_pixels)
    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    sheet.filepath_raw = os.path.abspath(output_path)
    sheet.file_format = "PNG"
    sheet.save()
    bpy.data.images.remove(sheet)


def main():
    args = parse_args()
    scene = bpy.context.scene

    body = find_stingray_body()
    tail_spine = bpy.data.objects.get("Tail spine")
    body_rest = ensure_basis_shape_key(body)
    spine_rest = ensure_basis_shape_key(tail_spine) if tail_spine is not None else None
    build_swim_keys(body, body_rest, tail_spine, spine_rest, args.frames)

    backdrop = bpy.data.objects.get("Backdrop")
    orig_backdrop_hide = backdrop.hide_render if backdrop else False
    if backdrop is not None:
        backdrop.hide_render = True

    orig_cam = scene.camera
    orig_res_x = scene.render.resolution_x
    orig_res_y = scene.render.resolution_y
    orig_pct = scene.render.resolution_percentage
    orig_transp = scene.render.film_transparent
    orig_samples = scene.cycles.samples
    orig_format = scene.render.image_settings.file_format
    orig_color = scene.render.image_settings.color_mode

    scene.render.resolution_x = args.frame_width
    scene.render.resolution_y = args.frame_height
    scene.render.resolution_percentage = 100
    scene.render.film_transparent = True
    scene.cycles.samples = args.samples
    scene.render.image_settings.file_format = "PNG"
    scene.render.image_settings.color_mode = "RGBA"

    sprite_cam = ensure_sprite_camera(scene, body, args.frames)
    scene.camera = sprite_cam

    frame_paths = []
    with tempfile.TemporaryDirectory(prefix="stingray_frames_") as tmpdir:
        for frame in range(args.frames):
            activate_swim_frame(body, tail_spine, frame, args.frames)
            out_frame = os.path.join(tmpdir, f"frame_{frame:02d}.png")
            scene.render.filepath = out_frame
            bpy.ops.render.render(write_still=True)
            frame_paths.append(out_frame)
        stitch_strip(frame_paths, args.frame_width, args.frame_height, args.output)

    # Restore rest pose and scene portrait settings.
    activate_swim_frame(body, tail_spine, -1, args.frames)
    if args.save_blend:
        keyframe_swim_action(body, tail_spine, args.frames)

    if backdrop is not None:
        backdrop.hide_render = orig_backdrop_hide
    scene.camera = orig_cam
    scene.render.resolution_x = orig_res_x
    scene.render.resolution_y = orig_res_y
    scene.render.resolution_percentage = orig_pct
    scene.render.film_transparent = orig_transp
    scene.cycles.samples = orig_samples
    scene.render.image_settings.file_format = orig_format
    scene.render.image_settings.color_mode = orig_color

    if args.save_blend:
        bpy.ops.wm.save_mainfile()

    print(f"Wrote {args.frames}-frame sprite sheet to {args.output}")


if __name__ == "__main__":
    main()
