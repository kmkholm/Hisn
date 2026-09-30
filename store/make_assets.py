# -*- coding: utf-8 -*-
"""توليد أصول متجر Google Play: الأيقونة، الرسم المميز، ولقطات الشاشة."""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import arabic_reshaper
from bidi.algorithm import get_display

FONT_B = "C:/Windows/Fonts/segoeuib.ttf"
FONT_R = "C:/Windows/Fonts/segoeui.ttf"
OUT = "store/final"
os.makedirs(OUT, exist_ok=True)

def ar(t):
    return get_display(arabic_reshaper.reshape(t))

def f(path, size):
    return ImageFont.truetype(path, size)

def center_text(d, xy, text, font, fill, anchor="mm"):
    d.text(xy, text, font=font, fill=fill, anchor=anchor)

# ─────────── مسار SVG مبسّط (M / L / C / Z) ───────────
def bez(p0, p1, p2, p3, n=24):
    pts = []
    for i in range(1, n + 1):
        t = i / n
        u = 1 - t
        x = u*u*u*p0[0] + 3*u*u*t*p1[0] + 3*u*t*t*p2[0] + t*t*t*p3[0]
        y = u*u*u*p0[1] + 3*u*u*t*p1[1] + 3*u*t*t*p2[1] + t*t*t*p3[1]
        pts.append((x, y))
    return pts

def path_points(cmds, scale, off=(0, 0)):
    pts, cur = [], (0, 0)
    for c in cmds:
        k = c[0]
        v = c[1:]
        if k == "M":
            cur = (v[0], v[1]); pts.append(cur)
        elif k == "L":
            cur = (v[0], v[1]); pts.append(cur)
        elif k == "C":
            p1, p2, p3 = (v[0], v[1]), (v[2], v[3]), (v[4], v[5])
            pts += bez(cur, p1, p2, p3); cur = p3
    return [(x*scale + off[0], y*scale + off[1]) for x, y in pts]

SHIELD_OUT = [("M",54,26),("L",76,34),("L",76,54),
              ("C",76,68,66,78,54,83),("C",42,78,32,68,32,54),("L",32,34)]
SHIELD_IN  = [("M",54,34),("L",68,39),("L",68,54),
              ("C",68,64,61,71,54,75),("C",47,71,40,64,40,54),("L",40,39)]
CHECK      = [("M",46,54),("L",52,60),("L",64,45),("L",67,49),("L",52,67),("L",43,58)]

BRAND_DARK = (11, 107, 88)
BRAND_MID  = (15, 163, 138)
BRAND_IND  = (76, 77, 255)

def vgrad(size, top, bottom, diagonal=False):
    w, h = size
    base = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(base)
    n = h if not diagonal else w + h
    for i in range(n):
        t = i / max(1, n - 1)
        c = tuple(int(top[j] + (bottom[j] - top[j]) * t) for j in range(3))
        if diagonal:
            d.line([(i, 0), (0, i)], fill=c)
        else:
            d.line([(0, i), (w, i)], fill=c)
    return base

# ─────────── 1) أيقونة 512×512 ───────────
def make_icon():
    U = 10                      # بكسل لكل وحدة من 108
    big = vgrad((108*U, 108*U), (16, 140, 116), (9, 92, 76))
    d = ImageDraw.Draw(big, "RGBA")
    d.polygon(path_points(SHIELD_OUT, U), fill=(255, 255, 255, 255))
    d.polygon(path_points(SHIELD_IN, U), fill=BRAND_DARK + (255,))
    d.polygon(path_points(CHECK, U), fill=(255, 255, 255, 255))
    # منطقة العرض في الأيقونة التكيّفية هي المربع الأوسط 72 من 108
    m = 18*U
    icon = big.crop((m, m, m + 72*U, m + 72*U)).resize((512, 512), Image.LANCZOS)
    icon.convert("RGB").save(f"{OUT}/icon_512.png")
    print("icon_512.png")

# ─────────── 2) الرسم المميز 1024×500 ───────────
def make_feature():
    W, H = 1024, 500
    g = vgrad((W, H), (13, 120, 100), (70, 72, 220))
    # توهّج
    glow = Image.new("RGB", (W, H), (0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([-120, 120, 460, 700], fill=(20, 200, 165))
    glow = glow.filter(ImageFilter.GaussianBlur(120))
    g = Image.blend(g, Image.blend(g, glow, 0.0), 0)
    base = Image.new("RGB", (W, H))
    base.paste(g)
    d = ImageDraw.Draw(base, "RGBA")

    # الشعار على اليمين (اتجاه عربي)
    U = 3.1
    ox, oy = 700, 150
    off = (ox - 54*U, oy - 54*U + 10)
    d.polygon(path_points(SHIELD_OUT, U, off), fill=(255, 255, 255, 240))
    d.polygon(path_points(SHIELD_IN, U, off), fill=(11, 107, 88, 255))
    d.polygon(path_points(CHECK, U, off), fill=(255, 255, 255, 255))

    d.text((905, 150), ar("حِصن"), font=f(FONT_B, 96), fill="white", anchor="rm")
    d.text((905, 240), "HISN", font=f(FONT_B, 40), fill=(255, 255, 255, 210), anchor="rm")
    d.text((905, 330), ar("رفيقك في التعافي من أي إدمان"),
           font=f(FONT_B, 44), fill="white", anchor="rm")
    d.text((905, 392), ar("عدّاد لا ينقص · تمارين · مسار ٩٠ يومًا"),
           font=f(FONT_R, 30), fill=(255, 255, 255, 205), anchor="rm")
    d.text((905, 440), "Recovery companion — private, offline",
           font=f(FONT_R, 24), fill=(255, 255, 255, 170), anchor="rm")
    base.save(f"{OUT}/feature_1024x500.png")
    print("feature_1024x500.png")

# ─────────── 3) لقطات المتجر 1080×1920 ───────────
CAPS = [
    ("01_home.png",     "عدّاد لا ينقص أبدًا",          "Your clean-day count never resets"),
    ("02_home_mid.png", "أدوات اللحظة الصعبة",          "Tools for the hard moment"),
    ("03_program.png",  "مسار تعافٍ من ٩٠ يومًا",        "A 90-day recovery path"),
    ("04_tools.png",    "تمارين ذهنية مبنية على العلم",  "Evidence-based mental exercises"),
    ("05_games.png",    "ألعاب تشغل ذهنك وقت الرغبة",   "Brain games for the urge"),
    ("06_trophies.png", "كؤوس وشهادات لكل إنجاز",       "Trophies and certificates"),
    ("07_insights.png", "رؤى تحوّل سجلّك إلى معرفة",      "Insights from your own patterns"),
    ("08_breath.png",   "مدرّب تنفّس يهدّئك في دقيقة",    "A visual breathing coach"),
]

PALETTE = [
    ((13, 120, 100), (56, 60, 190)),
    ((190, 60, 90),  (100, 30, 140)),
    ((20, 130, 150), (30, 60, 160)),
    ((16, 140, 116), (20, 90, 150)),
    ((150, 40, 130), (70, 40, 180)),
    ((190, 130, 30), (150, 45, 60)),
    ((25, 110, 160), (20, 70, 120)),
    ((16, 150, 160), (40, 70, 190)),
]

def rounded(im, r):
    mask = Image.new("L", im.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, im.size[0]-1, im.size[1]-1], r, fill=255)
    out = im.convert("RGBA")
    out.putalpha(mask)
    return out

def make_shots():
    W, H = 1080, 1920
    for i, (src, cap, en) in enumerate(CAPS):
        top, bot = PALETTE[i]
        canvas = vgrad((W, H), top, bot)
        d = ImageDraw.Draw(canvas, "RGBA")
        d.text((W//2, 118), ar(cap), font=f(FONT_B, 62), fill="white", anchor="mm")
        d.text((W//2, 196), en, font=f(FONT_R, 33), fill=(255, 255, 255, 195), anchor="mm")

        shot = Image.open(f"store/raw/{src}").convert("RGB")
        shot = shot.crop((0, 96, shot.width, shot.height - 30))   # قصّ شريط الحالة
        target_h = 1520
        tw = int(shot.width * target_h / shot.height)
        shot = shot.resize((tw, target_h), Image.LANCZOS)
        shot = rounded(shot, 34)

        x = (W - tw)//2
        y = 268
        shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        ImageDraw.Draw(shadow).rounded_rectangle([x+8, y+14, x+tw+8, y+target_h+14], 34,
                                                 fill=(0, 0, 0, 110))
        shadow = shadow.filter(ImageFilter.GaussianBlur(22))
        canvas = Image.alpha_composite(canvas.convert("RGBA"), shadow)
        canvas.alpha_composite(shot, (x, y))
        canvas.convert("RGB").save(f"{OUT}/shot_{i+1:02d}.png")
    print("8 screenshots")

make_icon(); make_feature(); make_shots()
